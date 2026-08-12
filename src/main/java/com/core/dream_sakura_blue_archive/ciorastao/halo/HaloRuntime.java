package com.core.dream_sakura_blue_archive.ciorastao.halo;

import com.core.dream_sakura.api.combat.DamageBypass;
import com.core.dream_sakura_blue_archive.ciorastao.dream_sakura_blue_archive;
import com.core.dream_sakura_blue_archive.ciorastao.items.DecorationItem;
import com.core.dream_sakura_blue_archive.ciorastao.util.HaloLevelManager;
import com.core.dream_sakura_blue_archive.ciorastao.util.OtherHelper;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.SlotContext;

import java.util.UUID;

/**
 * 光环运行时：状态推进、普通攻击转型、被动触发、防御和濒死拦截集中在这里。
 */
@Mod.EventBusSubscriber(modid = dream_sakura_blue_archive.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class HaloRuntime {
    private static final String ROOT = "DBAHaloRuntime";

    public static CompoundTag state(Player player) {
        CompoundTag persistent = player.getPersistentData();
        if (!persistent.contains(ROOT, 10)) persistent.put(ROOT, new CompoundTag());
        return persistent.getCompound(ROOT);
    }

    /** 由 DecorationItem 的 Curio tick 调用。 */
    public static void tickEquipped(SlotContext context, ItemStack stack) {
        if (!(context.entity() instanceof ServerPlayer player)
                || !(stack.getItem() instanceof DecorationItem item)) return;
        String id = item.getEffectiveItemId(stack);
        if (HaloProfile.byId(id).isEmpty()) return;
        long now = player.level().getGameTime();
        int level = HaloLevelManager.getHaloLevel(stack);
        CompoundTag itemTag = stack.getOrCreateTag();

        if ("tendouaris_halo".equals(id)) {
            int charge = Math.min(2, itemTag.getInt("DBAAliceCharge"));
            if (charge < 2) {
                long next = itemTag.getLong("DBAAliceNextCharge");
                if (next <= 0) itemTag.putLong("DBAAliceNextCharge", now + 500);
                else if (now >= next) {
                    itemTag.putInt("DBAAliceCharge", charge + 1);
                    itemTag.putLong("DBAAliceNextCharge", now + 500);
                }
            }
        } else if ("hoshino_halo".equals(id)) {
            if (player.getHealth() < player.getMaxHealth() * .20f
                    && now >= itemTag.getLong("DBAHoshinoFirstAid")) {
                itemTag.putLong("DBAHoshinoFirstAid", now + 2400);
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 400,
                        OtherHelper.getPassiveSkillLevel(level) - 1, false, true, true));
            }
        } else if ("hoshino_swimsuit_halo".equals(id)) {
            applyHoshinoSwimsuitEffects(player, level, now);
        } else if ("sakuluna_halo".equals(id)) {
            if (now >= itemTag.getLong("DBASakulunaNextHeal")) {
                itemTag.putLong("DBASakulunaNextHeal", now + 100);
                float heal = HaloSkills.passive(level,.03f,.07f,.10f,.17f,.20f,.24f,.29f,.35f,.42f,.50f);
                player.heal(player.getMaxHealth() * heal);
            }
            clearAggro(player);
        }
        HaloAttributes.refresh(player, stack, id, false);
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide
                || !(event.player instanceof ServerPlayer player)) return;
        CompoundTag state = state(player);
        long now = player.level().getGameTime();

        long aliceFire = state.getLong("AliceFireTick");
        if (aliceFire > 0 && now >= aliceFire) {
            state.remove("AliceFireTick");
            HaloSkills.fireAlice(player, state.getFloat("AliceMultiplier"));
        }

        int hits = state.getInt("HoshinoHits");
        if (hits > 0 && now >= state.getLong("HoshinoNextHit")) {
            HaloSkills.hoshinoHit(player, state);
            state.putInt("HoshinoHits", hits - 1);
            state.putLong("HoshinoNextHit", now + 6);
        }

        if (state.getInt("HinaDressShots") > 0 && now > state.getLong("HinaDressSessionEnd")) {
            state.remove("HinaDressShots");
            state.remove("HinaDressSessionEnd");
            HaloProfile.equipped(player).filter(e -> "hina_dress_halo".equals(e.profile().id()))
                    .ifPresent(e -> e.stack().getOrCreateTag().putLong("DBACooldownHinaDress", now + 1200));
        }

        long vanishEnd = state.getLong("SakulunaVanishEnd");
        if (vanishEnd > 0) {
            if (now >= vanishEnd) endVanish(player, state);
            else clearAggro(player);
        }

        long flightEnd = state.getLong("SakulunaFlightEnd");
        if (flightEnd > 0 && now >= flightEnd) {
            state.remove("SakulunaFlightEnd");
            if (!player.isCreative() && !player.isSpectator()) {
                player.getAbilities().mayfly = false;
                player.getAbilities().flying = false;
                player.onUpdateAbilities();
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingAttack(LivingAttackEvent event) {
        if (HaloDamage.isInternal()) return;
        LivingEntity victim = event.getEntity();
        if (!(event.getSource().getEntity() instanceof ServerPlayer attacker)) return;
        HaloProfile.Equipped equipped = HaloProfile.equipped(attacker).orElse(null);
        if (equipped == null) return;
        CompoundTag attackerState = state(attacker);
        float amount = event.getAmount();
        if (attackerState.getLong("HoshinoSupportEnd") > attacker.level().getGameTime())
            amount *= 1 + attackerState.getFloat("HoshinoSupportBonus");
        amount = applyAttackPassives(attacker, victim, equipped, amount);
        event.setCanceled(true);
        victim.invulnerableTime = 0;
        HaloDamage.deal(attacker, victim, equipped.profile().attackType(), amount,
                equipped.profile().penetration());
    }

    /** 在梦樱的 IDamageImmunity 处理完后，仅恢复标记为 bypassAll 的穿透半段。 */
    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public static void onInternalBypassAttack(LivingAttackEvent event) {
        if (HaloDamage.isInternal() && isBypassAll(event.getSource())) event.setCanceled(false);
    }

    /** 阻止免疫监听把穿透半段的数值降为 0；普通半段不会进入这里。 */
    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    public static void prepareInternalBypassHurt(LivingHurtEvent event) {
        if (HaloDamage.isInternal() && isBypassAll(event.getSource())) {
            DamageBypass.fullOverride(event.getSource(), event);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingHurt(LivingHurtEvent event) {
        if (HaloDamage.isInternal()) {
            if (isBypassAll(event.getSource())) event.setCanceled(false);
            return;
        }
        LivingEntity victim = event.getEntity();
        if (victim instanceof ServerPlayer targetPlayer) {
            if (blocksIncoming(targetPlayer)) {
                event.setAmount(0);
                event.setCanceled(true);
                return;
            }
            event.setAmount(event.getAmount() * incomingMultiplier(targetPlayer));
            recordIncoming(targetPlayer);
        }

        // 水上支援也能强化没有装备本模组光环的友方玩家。
        if (event.getSource().getEntity() instanceof ServerPlayer attacker
                && HaloProfile.equipped(attacker).isEmpty()) {
            CompoundTag attackerState = state(attacker);
            if (attackerState.getLong("HoshinoSupportEnd") > attacker.level().getGameTime())
                event.setAmount(event.getAmount() * (1 + attackerState.getFloat("HoshinoSupportBonus")));
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingDamage(LivingDamageEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        CompoundTag state = state(player);
        long now = player.level().getGameTime();
        if (state.getLong("HoshinoShieldEnd") < now) return;
        float shield = state.getFloat("HoshinoShield");
        if (shield <= 0) return;
        float absorbed = Math.min(shield, event.getAmount());
        state.putFloat("HoshinoShield", shield - absorbed);
        event.setAmount(event.getAmount() - absorbed);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer victim)) return;
        LivingEntity killer = event.getSource().getEntity() instanceof LivingEntity living ? living : null;
        if (preventDeath(victim, killer)) {
            event.setCanceled(true);
        }
    }

    /** DamageBypass 会令死亡事件不可取消，因此穿透段在真正扣血前也调用这里。 */
    public static boolean preventLethalBypass(ServerPlayer victim, LivingEntity killer, float damage) {
        if (damage < victim.getHealth() + victim.getAbsorptionAmount()) return false;
        return preventDeath(victim, killer);
    }

    private static boolean preventDeath(ServerPlayer victim, LivingEntity killer) {
        CompoundTag victimState = state(victim);
        if (victimState.contains("SakulunaAnchor", 10)) {
            restoreAnchor(victim, victimState);
            return true;
        }
        for (ServerPlayer rescuer : victim.serverLevel().players()) {
            if (rescuer == victim || rescuer == killer || rescuer.distanceToSqr(victim) > 256) continue;
            HaloProfile.Equipped equipped = HaloProfile.equipped(rescuer).orElse(null);
            if (equipped == null || !"sakuluna_halo".equals(equipped.profile().id())) continue;
            int level = HaloLevelManager.getHaloLevel(equipped.stack());
            float cost = HaloSkills.passive(level,.70f,.66f,.56f,.49f,.33f,.24f,.17f,.10f,.05f,.01f);
            float healthCost = rescuer.getMaxHealth() * cost;
            if (rescuer.getHealth() <= healthCost) continue;
            rescuer.setHealth(rescuer.getHealth() - healthCost);
            victim.setHealth(Math.max(1, victim.getMaxHealth() * .10f));
            rescuer.displayClientMessage(net.minecraft.network.chat.Component.literal("残樱代偿已发动"), true);
            return true;
        }
        return false;
    }

    private static float applyAttackPassives(ServerPlayer attacker, LivingEntity victim,
                                              HaloProfile.Equipped equipped, float amount) {
        String id = equipped.profile().id();
        ItemStack stack = equipped.stack();
        int level = HaloLevelManager.getHaloLevel(stack);
        CompoundTag state = state(attacker);
        long now = attacker.level().getGameTime();
        float attack = (float) attacker.getAttributeValue(Attributes.ATTACK_DAMAGE);

        switch (id) {
            case "tendouaris_maid_halo" -> {
                int count = state.getInt("AliceMaidAttackCount") + 1;
                state.putInt("AliceMaidAttackCount", count % 16);
                if (count % 16 == 0) HaloDamage.deal(attacker, victim, HaloAttackType.MAGIC,
                        attack * HaloSkills.passive(level,.20f,.40f,.60f,.80f,1f,1.5f,2f,3f,4f,5f), .5f);
                String current = victim.getStringUUID();
                if (current.equals(state.getString("AliceMaidLastTarget"))) {
                    victim.invulnerableTime = 0;
                    HaloDamage.deal(attacker, victim, HaloAttackType.MAGIC,
                            attack * HaloSkills.passive(level,.15f,.20f,.30f,.40f,.50f,.60f,.70f,.80f,.90f,1f), .5f);
                }
                state.putString("AliceMaidLastTarget", current);
            }
            case "hoshino_swimsuit_halo" -> {
                if (now >= state.getLong("HoshinoSwimsuitP1Next")) {
                    state.putLong("HoshinoSwimsuitP1Next", now + 800);
                    HaloDamage.deal(attacker, victim, HaloAttackType.PHYSICAL,
                            attack * HaloSkills.passive(level,1.83f,2.10f,2.40f,2.70f,3.10f,3.40f,3.60f,3.90f,4.50f,5f), .5f);
                    attacker.heal(attacker.getMaxHealth() * HaloSkills.passive(level,
                            .50f,.55f,.60f,.70f,.75f,.80f,.90f,.95f,.97f,.999f));
                }
            }
            case "hina_halo" -> {
                boolean alreadyActive = state.getLong("HinaComboEnd") > now;
                state.putLong("HinaComboEnd", now + 400);
                if (!alreadyActive) {
                    float bonus = HaloSkills.passive(level,.20f,.60f,1.20f,1.80f,2.60f,4f,5f,5.61f,7.20f,8.88f);
                    amount *= 1 + bonus;
                    HaloAttributes.refresh(attacker, stack, id, true);
                }
                if (victim.getAttributeValue(Attributes.ARMOR) <= 0) {
                    HaloDamage.deal(attacker, victim, HaloAttackType.PHYSICAL,
                            attack * HaloSkills.passive(level,.08f,.16f,.24f,.32f,.40f,.50f,.75f,.90f,1.20f,1.50f), .5f);
                }
            }
            case "hina_swimsuit_halo" -> {
                if (now >= state.getLong("HinaSwimsuitP1Next")) {
                    state.putLong("HinaSwimsuitP1Next", now + 500);
                    HaloDamage.deal(attacker, victim, HaloAttackType.PHYSICAL,
                            attack * HaloSkills.passive(level,1.41f,1.67f,2.03f,2.56f,2.99f,3.60f,4f,4.55f,5f,5.55f), .5f);
                    HaloSkills.stun(victim, HaloSkills.passive(level,1.6f,1.8f,2.1f,2.5f,2.8f,3.2f,3.6f,4f,4.3f,5f));
                }
            }
            case "sakuluna_halo" -> {
                if (state.getLong("SakulunaVanishEnd") > now) {
                    captureAnchor(attacker, state);
                    endVanish(attacker, state);
                }
            }
        }
        return amount;
    }

    public static boolean blocksIncoming(ServerPlayer player) {
        return state(player).getLong("SakulunaVanishEnd") > player.level().getGameTime();
    }

    public static float incomingMultiplier(ServerPlayer player) {
        HaloProfile.Equipped equipped = HaloProfile.equipped(player).orElse(null);
        if (equipped == null) return 1;
        String id = equipped.profile().id();
        int level = HaloLevelManager.getHaloLevel(equipped.stack());
        long now = player.level().getGameTime();
        float resistance = 0;
        if ("hina_dress_halo".equals(id) && state(player).getInt("HinaDressShots") > 0) {
            resistance = HaloSkills.passive(level,.09f,.17f,.26f,.33f,.42f,.50f,.66f,.78f,.90f,.99f);
        } else if ("sakuluna_halo".equals(id)) {
            resistance = HaloSkills.passive(level,.07f,.15f,.24f,.35f,.49f,.60f,.71f,.88f,.99f,1.20f);
            if (state(player).getLong("SakulunaFlightEnd") > now && player.getAbilities().flying) {
                resistance = 1 - (1 - Math.min(.999f, resistance)) * (1 - HaloSkills.passive(level,
                        .10f,.20f,.30f,.40f,.50f,.60f,.70f,.80f,.90f,.999f));
            }
        }
        return 1 - Math.min(.999f, resistance);
    }

    public static void recordIncoming(ServerPlayer player) {
        CompoundTag state = state(player);
        if (state.getLong("HinaComboEnd") <= 0) return;
        state.remove("HinaComboEnd");
        HaloProfile.equipped(player).ifPresent(e -> HaloAttributes.refresh(player, e.stack(), e.profile().id(), true));
    }

    private static void applyHoshinoSwimsuitEffects(ServerPlayer player, int level, long now) {
        if (state(player).getLong("HoshinoSwimsuitActiveEnd") <= now) return;
        int tier = OtherHelper.getPassiveSkillLevel(level);
        int resistance = new int[]{0,0,0,1,1,1,2,2,4,4}[tier-1];
        int speed = new int[]{-1,0,0,0,1,1,1,1,1,1}[tier-1];
        int regen = new int[]{-1,-1,0,0,0,1,1,2,3,3}[tier-1];
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 30, resistance, false, false, true));
        if (speed >= 0) player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 30, speed, false, false, true));
        if (regen >= 0) player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 30, regen, false, false, true));
    }

    private static void clearAggro(ServerPlayer player) {
        for (Mob mob : player.level().getEntitiesOfClass(Mob.class,
                player.getBoundingBox().inflate(32), entity -> entity.getTarget() == player)) {
            mob.setTarget(null);
        }
    }

    private static void endVanish(ServerPlayer player, CompoundTag state) {
        state.remove("SakulunaVanishEnd");
        player.setInvisible(state.getBoolean("SakulunaWasInvisible"));
        player.noPhysics = state.getBoolean("SakulunaWasNoPhysics");
        state.remove("SakulunaWasInvisible");
        state.remove("SakulunaWasNoPhysics");
    }

    private static void captureAnchor(ServerPlayer player, CompoundTag state) {
        CompoundTag anchor = new CompoundTag();
        anchor.putString("Dimension", player.level().dimension().location().toString());
        anchor.putDouble("X", player.getX());
        anchor.putDouble("Y", player.getY());
        anchor.putDouble("Z", player.getZ());
        anchor.putFloat("Yaw", player.getYRot());
        anchor.putFloat("Pitch", player.getXRot());
        anchor.putFloat("Health", player.getHealth());
        anchor.putFloat("Absorption", player.getAbsorptionAmount());
        anchor.putInt("Food", player.getFoodData().getFoodLevel());
        anchor.putFloat("Saturation", player.getFoodData().getSaturationLevel());
        anchor.putInt("Air", player.getAirSupply());
        anchor.putInt("Fire", player.getRemainingFireTicks());
        anchor.putFloat("ExperienceProgress", player.experienceProgress);
        anchor.putInt("TotalExperience", player.totalExperience);
        anchor.putInt("ExperienceLevel", player.experienceLevel);
        anchor.putDouble("MotionX", player.getDeltaMovement().x);
        anchor.putDouble("MotionY", player.getDeltaMovement().y);
        anchor.putDouble("MotionZ", player.getDeltaMovement().z);
        anchor.putFloat("FallDistance", player.fallDistance);
        ListTag effects = new ListTag();
        for (MobEffectInstance effect : player.getActiveEffects()) effects.add(effect.save(new CompoundTag()));
        anchor.put("Effects", effects);
        state.put("SakulunaAnchor", anchor);
    }

    private static void restoreAnchor(ServerPlayer player, CompoundTag state) {
        CompoundTag anchor = state.getCompound("SakulunaAnchor");
        state.remove("SakulunaAnchor");
        endVanish(player, state);
        ResourceLocation location = ResourceLocation.tryParse(anchor.getString("Dimension"));
        ServerLevel destination = location == null ? player.serverLevel()
                : player.getServer().getLevel(ResourceKey.create(Registries.DIMENSION, location));
        if (destination == null) destination = player.serverLevel();
        player.teleportTo(destination, anchor.getDouble("X"), anchor.getDouble("Y"), anchor.getDouble("Z"),
                anchor.getFloat("Yaw"), anchor.getFloat("Pitch"));
        player.setHealth(Math.max(1, Math.min(player.getMaxHealth(), anchor.getFloat("Health"))));
        player.setAbsorptionAmount(anchor.getFloat("Absorption"));
        FoodData food = player.getFoodData();
        food.setFoodLevel(anchor.getInt("Food"));
        food.setSaturation(anchor.getFloat("Saturation"));
        player.setAirSupply(anchor.getInt("Air"));
        player.setRemainingFireTicks(anchor.getInt("Fire"));
        player.experienceProgress = anchor.getFloat("ExperienceProgress");
        player.totalExperience = anchor.getInt("TotalExperience");
        player.experienceLevel = anchor.getInt("ExperienceLevel");
        player.setDeltaMovement(anchor.getDouble("MotionX"), anchor.getDouble("MotionY"), anchor.getDouble("MotionZ"));
        player.fallDistance = anchor.getFloat("FallDistance");
        player.removeAllEffects();
        ListTag effects = anchor.getList("Effects", 10);
        for (int i = 0; i < effects.size(); i++) {
            MobEffectInstance effect = MobEffectInstance.load(effects.getCompound(i));
            if (effect != null) player.addEffect(effect);
        }
        player.invulnerableTime = 40;
    }

    private static boolean isBypassAll(net.minecraft.world.damagesource.DamageSource source) {
        return source instanceof DamageBypass.IDamageBypass bypass && bypass.dreamsakura$isBypassAll();
    }

    private HaloRuntime() {}
}
