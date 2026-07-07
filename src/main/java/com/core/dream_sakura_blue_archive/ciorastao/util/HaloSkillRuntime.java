package com.core.dream_sakura_blue_archive.ciorastao.util;

import com.core.dream_sakura.skill.SkillBinding;
import com.core.dream_sakura_blue_archive.ciorastao.effect.RegistryEffect;
import com.core.dream_sakura_blue_archive.ciorastao.items.DecorationItem;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.lwjgl.glfw.GLFW;
import top.theillusivec4.curios.api.SlotContext;

import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class HaloSkillRuntime {
    private static final String GLOBAL = "global";
    private static final String MARK_VULNERABILITY = "MarkVulnerability";
    private static final int DEFAULT_BUFF_TICKS = 600;
    private static final int DEFAULT_MARK_TICKS = 400;
    private static final int DRONE_INTERVAL_TICKS = 20;

    public static SkillBinding createBinding(String itemId) {
        HaloSkillDefinitions.Definition definition = HaloSkillDefinitions.get(itemId);
        if (definition == null) {
            return null;
        }
        return new SkillBinding(
                GLFW.GLFW_KEY_J,
                skillDescriptionId(itemId),
                definition.cooldownMs,
                itemId,
                (player, stack) -> {
                    String effectiveItemId = HaloVariantHelper.effectiveItemId(stack, itemId);
                    HaloSkillDefinitions.Definition effectiveDefinition = HaloSkillDefinitions.get(effectiveItemId);
                    if (effectiveDefinition != null) {
                        useActive(player, stack, effectiveDefinition);
                    }
                }
        );
    }

    public static String skillDescriptionId(String itemId) {
        return itemId + "_skill";
    }

    public static void onCurioTick(SlotContext slotContext, ItemStack stack, String itemId) {
        if (!(slotContext.entity() instanceof Player player) || player.level().isClientSide) {
            return;
        }
        HaloSkillDefinitions.Definition definition = HaloSkillDefinitions.get(itemId);
        if (definition == null) {
            return;
        }

        HaloLevelManager.getHaloLevel(stack);
        HaloLevelManager.getHaloXP(stack);
        HaloLevelManager.getMaxHaloXP(stack);

        if (!definition.usesLegacyPassiveHooks()) {
            applyStaticModifiers(stack, definition);
            processLowHealthPassives(player, stack, definition);
            processPeriodicPassives(player, stack, definition);
        }
    }

    public static void onServerTick(MinecraftServer server) {
        for (ServerLevel level : server.getAllLevels()) {
            for (ServerPlayer player : level.players()) {
                processDrone(player);
                processDelayedBomb(player);
                processBeacon(player);
            }
        }
    }

    public static void onLivingHurt(LivingHurtEvent event) {
        LivingEntity target = event.getEntity();
        applyTargetMarkVulnerability(target, event);

        if (!(event.getSource().getEntity() instanceof Player attacker)) {
            return;
        }

        applyGlobalDamageBonus(attacker, event);

        EquippedHalo equipped = getEquippedHalo(attacker);
        if (equipped == null) {
            return;
        }
        HaloSkillDefinitions.Definition definition = HaloSkillDefinitions.get(equipped.itemId);
        if (definition == null || definition.usesLegacyPassiveHooks()) {
            return;
        }

        int haloLevel = HaloLevelManager.getHaloLevel(equipped.stack);
        float attack = (float) attacker.getAttributeValue(Attributes.ATTACK_DAMAGE);
        CompoundTag data = haloData(attacker, definition.itemId);

        for (HaloSkillDefinitions.PassiveSpec passive : definition.passives) {
            if (passive.kind != HaloSkillDefinitions.PassiveKind.ON_HIT_EXTRA) {
                continue;
            }
            if (!canTriggerOnHit(attacker, data, passive)) {
                continue;
            }
            float extra = OtherHelper.getPassiveValue(haloLevel, passive.values);
            event.setAmount(event.getAmount() + attack * extra);
            markOnHitTriggered(attacker, data, passive);
            spawnBurstParticles(attacker, target);
        }
    }

    public static void onLivingDamage(LivingDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        CompoundTag global = haloData(player, GLOBAL);
        long now = player.level().getGameTime();

        float amount = event.getAmount();
        if (global.getLong(HaloRuntimeKeys.DAMAGE_REDUCTION_END) > now) {
            amount *= Math.max(0.0f, 1.0f - global.getFloat(HaloRuntimeKeys.DAMAGE_REDUCTION));
        }

        if (global.getLong(HaloRuntimeKeys.SHIELD_END) > now) {
            float shield = global.getFloat(HaloRuntimeKeys.SHIELD_AMOUNT);
            if (shield > 0.0f) {
                float absorbed = Math.min(shield, amount);
                shield -= absorbed;
                amount -= absorbed;
                global.putFloat(HaloRuntimeKeys.SHIELD_AMOUNT, shield);
                if (player.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.END_ROD, player.getX(), player.getY() + 1.0D, player.getZ(), 12, 0.6D, 0.6D, 0.6D, 0.02D);
                }
            }
        }
        event.setAmount(amount);
    }

    public static void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getSource().getEntity() instanceof Player player)) {
            return;
        }
        EquippedHalo equipped = getEquippedHalo(player);
        if (equipped == null) {
            return;
        }
        HaloSkillDefinitions.Definition definition = HaloSkillDefinitions.get(equipped.itemId);
        if (definition == null || definition.usesLegacyPassiveHooks()) {
            return;
        }

        int haloLevel = HaloLevelManager.getHaloLevel(equipped.stack);
        float killBonus = definition.rarity >= 3 ? 0.12f : 0.08f;
        addGlobalDamageBonus(player, killBonus + 0.001f * haloLevel, 200);
        healIfNeeded(player, player.getMaxHealth() * (definition.rarity >= 3 ? 0.05f : 0.035f));
    }

    private static void useActive(Player player, ItemStack stack, HaloSkillDefinitions.Definition definition) {
        if (player.level().isClientSide || !(player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        int haloLevel = HaloLevelManager.getHaloLevel(stack);
        int index = OtherHelper.getActiveSkillLevel(haloLevel) - 1;
        float main = valueAt(definition.activeValues, index);
        float alt = valueAt(definition.activeAltValues, index);
        float attack = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE);

        switch (definition.activeKind) {
            case LINE -> damageLine(player, definition, attack * main, 32.0D, 1.5D);
            case SINGLE -> damageSingle(player, definition, attack * main, 24.0D);
            case CONE -> damageCone(player, definition, attack * main, 8.0D, 0.5D, false);
            case CONE_MULTI -> damageCone(player, definition, attack * main, 6.0D, 0.45D, true);
            case MULTI_TARGET -> damageNearest(player, definition, attack * main, 20.0D, 5);
            case TRIPLE_LINE -> {
                damageLine(player, definition, attack * main, 28.0D, 1.0D);
                damageLine(player, definition, attack * main, 28.0D, 1.0D);
                damageLine(player, definition, attack * Math.max(main, alt), 32.0D, 1.5D);
            }
            case AREA -> damageArea(player, definition, attack * main, 7.0D);
            case MARK_DAMAGE -> {
                LivingEntity target = findNearestEnemy(player, 24.0D);
                if (target != null) {
                    markTarget(player, target, Math.max(0.2f, main * 0.04f), DEFAULT_MARK_TICKS);
                    applyHaloDamage(player, target, definition, attack * main);
                }
            }
            case MARK, ALLY_MARK -> {
                LivingEntity target = findNearestEnemy(player, 24.0D);
                if (target != null) {
                    markTarget(player, target, Math.max(0.12f, main), DEFAULT_MARK_TICKS);
                    target.addEffect(new MobEffectInstance(MobEffects.GLOWING, DEFAULT_MARK_TICKS, 0, false, true, true));
                    spawnBurstParticles(player, target);
                }
            }
            case DRONE -> startDrone(player, definition, attack, main, alt);
            case DELAYED_BOMB -> placeBomb(player, definition, attack * main);
            case FEAR -> {
                damageArea(player, definition, attack * main, 7.0D);
                for (LivingEntity target : enemiesAround(player, 7.0D)) {
                    target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 120, 2, false, true, true));
                    target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 120, 1, false, true, true));
                }
            }
            case ALLY_BUFF, ALLY_CHARM -> buffAllies(serverPlayer, main, DEFAULT_BUFF_TICKS);
            case ALLY_HEAL -> healAllies(serverPlayer, healPercent(definition, main), durationFromValue(main, 160));
            case AREA_HEAL_DAMAGE -> {
                damageArea(player, definition, attack * main, 7.0D);
                healAllies(serverPlayer, alt > 0 ? alt : 0.18f, 160);
            }
            case ALLY_SHIELD -> shieldAllies(serverPlayer, Math.max(main, 0.2f), 600);
            case ALLY_SHIELD_BEACON -> {
                shieldAllies(serverPlayer, Math.max(main, 0.2f), 600);
                createBeacon(player, durationFromValue(alt, 200));
            }
            case SELF_BUFF, OVERDRIVE -> {
                addGlobalDamageBonus(player, Math.max(main, 0.2f), DEFAULT_BUFF_TICKS);
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, DEFAULT_BUFF_TICKS, 1, false, true, true));
            }
            case SELF_SHIELD, FUTURE_SHIELD -> addShield(player, player.getMaxHealth() * Math.max(main, 0.3f), DEFAULT_BUFF_TICKS);
            case EXTEND_BUFF -> extendBuffs(player, durationFromValue(main, 120));
            case REVEAL_ZONE -> revealZone(player, main, alt);
            case LINE_SPEED -> {
                damageLine(player, definition, attack * main, 24.0D, 1.0D);
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, durationFromValue(alt, 160), 1, false, true, true));
            }
            case ANCHOR_STEALTH -> {
                createBeacon(player, durationFromValue(main, 160));
                player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, durationFromValue(main, 160), 0, false, true, true));
                addDamageReduction(player, 0.35f, durationFromValue(main, 160));
            }
        }

        for (HaloSkillDefinitions.PassiveSpec passive : definition.passives) {
            if (passive.kind == HaloSkillDefinitions.PassiveKind.ACTIVE_AFTER_BUFF) {
                float bonus = OtherHelper.getPassiveValue(haloLevel, passive.values);
                addGlobalDamageBonus(player, bonus, passive.cooldownTicks > 0 ? passive.cooldownTicks : DEFAULT_BUFF_TICKS);
            }
        }
    }

    private static void applyStaticModifiers(ItemStack stack, HaloSkillDefinitions.Definition definition) {
        CompoundTag tag = stack.getOrCreateTag();
        int level = HaloLevelManager.getHaloLevel(stack);
        String signature = definition.itemId + ":" + level;
        if (signature.equals(tag.getString(HaloRuntimeKeys.ITEM_MODIFIER_SIGNATURE))) {
            return;
        }

        ListTag modifiersList = getModifiersList(tag);
        for (int i = 0; i < definition.passives.size(); i++) {
            OtherHelper.removeExistingModifier(modifiersList, modifierUuid(definition.itemId, "passive_" + i));
        }

        for (int i = 0; i < definition.passives.size(); i++) {
            HaloSkillDefinitions.PassiveSpec passive = definition.passives.get(i);
            if (passive.kind != HaloSkillDefinitions.PassiveKind.STATIC_ATTRIBUTE) {
                continue;
            }
            float value = OtherHelper.getPassiveValue(level, passive.values);
            OtherHelper.addModifier(modifiersList, passive.attributeName, value, passive.operation,
                    modifierUuid(definition.itemId, "passive_" + i), "halo");
        }

        tag.put("CurioAttributeModifiers", modifiersList);
        tag.putString(HaloRuntimeKeys.ITEM_MODIFIER_SIGNATURE, signature);
        tag.putInt(HaloRuntimeKeys.ITEM_MODIFIER_LEVEL, level);
        tag.putInt("HideFlags", tag.getInt("HideFlags") | 2);
    }

    private static void processLowHealthPassives(Player player, ItemStack stack, HaloSkillDefinitions.Definition definition) {
        int level = HaloLevelManager.getHaloLevel(stack);
        long now = player.level().getGameTime();
        CompoundTag data = haloData(player, definition.itemId);
        if (player.getHealth() / player.getMaxHealth() > 0.35f || data.getLong(HaloRuntimeKeys.LOW_HP_COOLDOWN) > now) {
            return;
        }

        for (HaloSkillDefinitions.PassiveSpec passive : definition.passives) {
            if (passive.kind != HaloSkillDefinitions.PassiveKind.LOW_HEALTH_BUFF) {
                continue;
            }
            float amount = OtherHelper.getPassiveValue(level, passive.values);
            float reduction = OtherHelper.getPassiveValue(level, passive.altValues);
            addGlobalDamageBonus(player, amount, 220);
            addDamageReduction(player, reduction, 220);
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 160, 1, false, true, true));
            data.putLong(HaloRuntimeKeys.LOW_HP_COOLDOWN, now + Math.max(400, passive.cooldownTicks));
        }
    }

    private static void processPeriodicPassives(Player player, ItemStack stack, HaloSkillDefinitions.Definition definition) {
        long now = player.level().getGameTime();
        CompoundTag data = haloData(player, definition.itemId);
        if (data.getLong(HaloRuntimeKeys.LAST_PASSIVE_TICK) + 100 > now) {
            return;
        }
        data.putLong(HaloRuntimeKeys.LAST_PASSIVE_TICK, now);

        int level = HaloLevelManager.getHaloLevel(stack);
        for (HaloSkillDefinitions.PassiveSpec passive : definition.passives) {
            float amount = OtherHelper.getPassiveValue(level, passive.values);
            if (passive.kind == HaloSkillDefinitions.PassiveKind.PERIODIC_AURA) {
                for (ServerPlayer ally : allies(player, 8.0D)) {
                    healIfNeeded(ally, ally.getMaxHealth() * Math.min(0.08f, amount * 0.4f));
                }
            } else if (passive.kind == HaloSkillDefinitions.PassiveKind.DAMAGE_REDUCTION) {
                addDamageReduction(player, amount, 120);
            } else if (passive.kind == HaloSkillDefinitions.PassiveKind.SHIELD_BOOST) {
                addShield(player, player.getMaxHealth() * amount * 0.2f, 140);
            }
        }
    }

    private static void processDrone(Player player) {
        CompoundTag root = runtimeRoot(player);
        long now = player.level().getGameTime();
        for (String itemId : HaloSkillDefinitions.all().stream().map(d -> d.itemId).toList()) {
            CompoundTag data = root.getCompound(itemId);
            if (data.getLong(HaloRuntimeKeys.DRONE_END) <= now || data.getLong(HaloRuntimeKeys.DRONE_NEXT) > now) {
                continue;
            }
            HaloSkillDefinitions.Definition definition = HaloSkillDefinitions.get(itemId);
            if (definition == null) {
                continue;
            }
            LivingEntity target = findNearestEnemy(player, 24.0D);
            if (target != null) {
                applyHaloDamage(player, target, definition, data.getFloat(HaloRuntimeKeys.DRONE_DAMAGE));
                markTarget(player, target, 0.15f, 80);
            }
            data.putLong(HaloRuntimeKeys.DRONE_NEXT, now + DRONE_INTERVAL_TICKS);
        }
    }

    private static void processDelayedBomb(Player player) {
        CompoundTag root = runtimeRoot(player);
        long now = player.level().getGameTime();
        for (String itemId : HaloSkillDefinitions.all().stream().map(d -> d.itemId).toList()) {
            CompoundTag data = root.getCompound(itemId);
            long bombTick = data.getLong(HaloRuntimeKeys.BOMB_TICK);
            if (bombTick <= 0 || bombTick > now) {
                continue;
            }
            HaloSkillDefinitions.Definition definition = HaloSkillDefinitions.get(itemId);
            if (definition != null) {
                Vec3 center = new Vec3(data.getDouble(HaloRuntimeKeys.BOMB_X), data.getDouble(HaloRuntimeKeys.BOMB_Y), data.getDouble(HaloRuntimeKeys.BOMB_Z));
                for (LivingEntity target : enemiesAround(player, center, 5.0D)) {
                    applyHaloDamage(player, target, definition, data.getFloat(HaloRuntimeKeys.BOMB_DAMAGE));
                }
                if (player.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.EXPLOSION, center.x, center.y + 0.5D, center.z, 2, 0.4D, 0.2D, 0.4D, 0.02D);
                }
            }
            data.putLong(HaloRuntimeKeys.BOMB_TICK, 0);
        }
    }

    private static void processBeacon(Player player) {
        CompoundTag data = haloData(player, GLOBAL);
        long now = player.level().getGameTime();
        if (data.getLong(HaloRuntimeKeys.BEACON_END) <= now || !player.isShiftKeyDown()) {
            return;
        }
        player.teleportTo(data.getDouble(HaloRuntimeKeys.BEACON_X), data.getDouble(HaloRuntimeKeys.BEACON_Y), data.getDouble(HaloRuntimeKeys.BEACON_Z));
        data.putLong(HaloRuntimeKeys.BEACON_END, 0);
    }

    private static void damageLine(Player player, HaloSkillDefinitions.Definition definition, float damage, double range, double width) {
        Vec3 start = player.getEyePosition();
        Vec3 look = player.getLookAngle().normalize();
        for (LivingEntity target : enemiesAround(player, range + 2.0D)) {
            Vec3 toTarget = target.getEyePosition().subtract(start);
            double forward = toTarget.dot(look);
            if (forward < 0.0D || forward > range) {
                continue;
            }
            Vec3 closest = start.add(look.scale(forward));
            if (target.getEyePosition().distanceToSqr(closest) <= width * width) {
                applyHaloDamage(player, target, definition, damage);
            }
        }
        if (player.level() instanceof ServerLevel serverLevel) {
            for (int i = 2; i <= range; i += 2) {
                Vec3 point = start.add(look.scale(i));
                serverLevel.sendParticles(ParticleTypes.SONIC_BOOM, point.x, point.y, point.z, 1, 0, 0, 0, 0);
            }
        }
    }

    private static void damageSingle(Player player, HaloSkillDefinitions.Definition definition, float damage, double range) {
        LivingEntity target = findNearestEnemy(player, range);
        if (target != null) {
            applyHaloDamage(player, target, definition, damage);
            spawnBurstParticles(player, target);
        }
    }

    private static void damageCone(Player player, HaloSkillDefinitions.Definition definition, float damage, double range, double dot, boolean stun) {
        Vec3 look = player.getLookAngle().normalize();
        Vec3 eyePos = player.getEyePosition();
        for (LivingEntity target : enemiesAround(player, range)) {
            Vec3 toTarget = target.getEyePosition().subtract(eyePos).normalize();
            if (look.dot(toTarget) >= dot) {
                applyHaloDamage(player, target, definition, damage);
                if (stun && RegistryEffect.STUN_EFFECT != null) {
                    target.addEffect(new MobEffectInstance(RegistryEffect.STUN_EFFECT.get(), 28, 0, false, true, true));
                }
            }
        }
    }

    private static void damageNearest(Player player, HaloSkillDefinitions.Definition definition, float damage, double range, int count) {
        enemiesAround(player, range).stream()
                .sorted(Comparator.comparingDouble(e -> e.distanceToSqr(player)))
                .limit(count)
                .forEach(target -> applyHaloDamage(player, target, definition, damage));
    }

    private static void damageArea(Player player, HaloSkillDefinitions.Definition definition, float damage, double range) {
        for (LivingEntity target : enemiesAround(player, range)) {
            applyHaloDamage(player, target, definition, damage);
        }
    }

    private static void applyHaloDamage(Player player, LivingEntity target, HaloSkillDefinitions.Definition definition, float damage) {
        if (damage <= 0.0f || !target.isAlive()) {
            return;
        }
        DamageSource source = switch (definition.damageKind) {
            case MYSTIC -> player.damageSources().indirectMagic(player, player);
            case EXPLOSIVE -> player.damageSources().explosion(player, player);
            case PIERCING -> player.damageSources().playerAttack(player);
        };

        float trueDamage = damage * 0.5f;
        float normalDamage = damage - trueDamage;
        target.invulnerableTime = 0;
        if (normalDamage > 0.0f) {
            target.hurt(source, normalDamage);
        }
        if (trueDamage > 0.0f && target.isAlive()) {
            float health = target.getHealth() - trueDamage;
            if (health <= 0.0f) {
                target.die(source);
            } else {
                target.setHealth(health);
            }
        }
    }

    private static void startDrone(Player player, HaloSkillDefinitions.Definition definition, float attack, float main, float alt) {
        int duration = definition.itemId.equals("kuroko_halo") ? durationFromValue(main, 320) : 160;
        float totalMultiplier = alt > 0.0f ? alt : main;
        CompoundTag data = haloData(player, definition.itemId);
        long now = player.level().getGameTime();
        data.putLong(HaloRuntimeKeys.DRONE_END, now + duration);
        data.putLong(HaloRuntimeKeys.DRONE_NEXT, now);
        data.putFloat(HaloRuntimeKeys.DRONE_DAMAGE, attack * totalMultiplier / Math.max(1, duration / DRONE_INTERVAL_TICKS));
        data.putString(HaloRuntimeKeys.DRONE_DAMAGE_KIND, definition.damageKind.name());
    }

    private static void placeBomb(Player player, HaloSkillDefinitions.Definition definition, float damage) {
        Vec3 pos = player.position().add(player.getLookAngle().normalize().scale(4.0D));
        CompoundTag data = haloData(player, definition.itemId);
        data.putDouble(HaloRuntimeKeys.BOMB_X, pos.x);
        data.putDouble(HaloRuntimeKeys.BOMB_Y, pos.y);
        data.putDouble(HaloRuntimeKeys.BOMB_Z, pos.z);
        data.putLong(HaloRuntimeKeys.BOMB_TICK, player.level().getGameTime() + 40);
        data.putFloat(HaloRuntimeKeys.BOMB_DAMAGE, damage);
        player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 60, 0, false, true, true));
    }

    private static void buffAllies(ServerPlayer player, float amount, int durationTicks) {
        for (ServerPlayer ally : allies(player, 8.0D)) {
            addGlobalDamageBonus(ally, amount, durationTicks);
            ally.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, durationTicks, Math.max(0, Math.min(2, (int) (amount * 5))), false, true, true));
        }
    }

    private static void healAllies(ServerPlayer player, float amount, int durationTicks) {
        for (ServerPlayer ally : allies(player, 8.0D)) {
            healIfNeeded(ally, ally.getMaxHealth() * amount);
            ally.addEffect(new MobEffectInstance(MobEffects.REGENERATION, durationTicks, 1, false, true, true));
        }
    }

    private static void shieldAllies(ServerPlayer player, float amount, int durationTicks) {
        for (ServerPlayer ally : allies(player, 8.0D)) {
            addShield(ally, ally.getMaxHealth() * amount, durationTicks);
        }
    }

    private static void createBeacon(Player player, int durationTicks) {
        CompoundTag data = haloData(player, GLOBAL);
        data.putDouble(HaloRuntimeKeys.BEACON_X, player.getX());
        data.putDouble(HaloRuntimeKeys.BEACON_Y, player.getY());
        data.putDouble(HaloRuntimeKeys.BEACON_Z, player.getZ());
        data.putLong(HaloRuntimeKeys.BEACON_END, player.level().getGameTime() + durationTicks);
    }

    private static void revealZone(Player player, float radiusValue, float bonus) {
        double radius = Math.max(6.0D, radiusValue);
        for (LivingEntity target : enemiesAround(player, radius)) {
            target.addEffect(new MobEffectInstance(MobEffects.GLOWING, DEFAULT_BUFF_TICKS, 0, false, true, true));
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 120, 1, false, true, true));
            if (bonus > 0.0f) {
                markTarget(player, target, bonus, DEFAULT_BUFF_TICKS);
            }
        }
        player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, DEFAULT_BUFF_TICKS, 0, false, true, true));
        player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, DEFAULT_BUFF_TICKS, 0, false, true, true));
    }

    private static void extendBuffs(Player player, int durationTicks) {
        CompoundTag global = haloData(player, GLOBAL);
        long now = player.level().getGameTime();
        global.putLong(HaloRuntimeKeys.DAMAGE_BONUS_END, Math.max(global.getLong(HaloRuntimeKeys.DAMAGE_BONUS_END), now) + durationTicks);
        global.putLong(HaloRuntimeKeys.DAMAGE_REDUCTION_END, Math.max(global.getLong(HaloRuntimeKeys.DAMAGE_REDUCTION_END), now) + durationTicks);
        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, durationTicks, 1, false, true, true));
    }

    private static void markTarget(Player player, LivingEntity target, float amount, int durationTicks) {
        CompoundTag casterData = haloData(player, GLOBAL);
        CompoundTag targetData = globalData(target);
        long end = player.level().getGameTime() + durationTicks;
        casterData.putUUID(HaloRuntimeKeys.MARKED_TARGET, target.getUUID());
        casterData.putLong(HaloRuntimeKeys.MARK_END, end);
        targetData.putFloat(MARK_VULNERABILITY, Math.max(targetData.getFloat(MARK_VULNERABILITY), amount));
        targetData.putLong(HaloRuntimeKeys.MARK_END, end);
    }

    private static void applyTargetMarkVulnerability(LivingEntity victim, LivingHurtEvent event) {
        CompoundTag data = globalData(victim);
        if (data.getLong(HaloRuntimeKeys.MARK_END) <= victim.level().getGameTime()) {
            return;
        }
        event.setAmount(event.getAmount() * (1.0f + data.getFloat(MARK_VULNERABILITY)));
    }

    private static void applyGlobalDamageBonus(Player attacker, LivingHurtEvent event) {
        CompoundTag data = haloData(attacker, GLOBAL);
        if (data.getLong(HaloRuntimeKeys.DAMAGE_BONUS_END) <= attacker.level().getGameTime()) {
            return;
        }
        event.setAmount(event.getAmount() * (1.0f + data.getFloat(HaloRuntimeKeys.DAMAGE_BONUS)));
    }

    private static void addGlobalDamageBonus(Player player, float amount, int durationTicks) {
        CompoundTag data = haloData(player, GLOBAL);
        data.putFloat(HaloRuntimeKeys.DAMAGE_BONUS, Math.max(data.getFloat(HaloRuntimeKeys.DAMAGE_BONUS), amount));
        data.putLong(HaloRuntimeKeys.DAMAGE_BONUS_END, player.level().getGameTime() + durationTicks);
    }

    private static void addDamageReduction(Player player, float amount, int durationTicks) {
        CompoundTag data = haloData(player, GLOBAL);
        data.putFloat(HaloRuntimeKeys.DAMAGE_REDUCTION, Math.max(data.getFloat(HaloRuntimeKeys.DAMAGE_REDUCTION), amount));
        data.putLong(HaloRuntimeKeys.DAMAGE_REDUCTION_END, player.level().getGameTime() + durationTicks);
    }

    private static void addShield(Player player, float amount, int durationTicks) {
        CompoundTag data = haloData(player, GLOBAL);
        float cap = player.getMaxHealth() * 10.0f;
        data.putFloat(HaloRuntimeKeys.SHIELD_AMOUNT, Math.min(cap, data.getFloat(HaloRuntimeKeys.SHIELD_AMOUNT) + amount));
        data.putLong(HaloRuntimeKeys.SHIELD_END, player.level().getGameTime() + durationTicks);
    }

    private static boolean canTriggerOnHit(Player player, CompoundTag data, HaloSkillDefinitions.PassiveSpec passive) {
        long now = player.level().getGameTime();
        if (passive.everyHits > 0) {
            int count = data.getInt(HaloRuntimeKeys.HIT_COUNT) + 1;
            data.putInt(HaloRuntimeKeys.HIT_COUNT, count);
            return count % passive.everyHits == 0;
        }

        long cooldownEnd = data.getLong("OnHitCooldown");
        return cooldownEnd <= now;
    }

    private static void markOnHitTriggered(Player player, CompoundTag data, HaloSkillDefinitions.PassiveSpec passive) {
        if (passive.everyHits <= 0) {
            data.putLong("OnHitCooldown", player.level().getGameTime() + Math.max(60, passive.cooldownTicks));
        }
    }

    private static List<LivingEntity> enemiesAround(Player player, double radius) {
        return enemiesAround(player, player.position(), radius);
    }

    private static List<LivingEntity> enemiesAround(Player player, Vec3 center, double radius) {
        AABB box = new AABB(center, center).inflate(radius);
        return player.level().getEntitiesOfClass(LivingEntity.class, box,
                entity -> entity != player && entity.isAlive() && !isFriendly(player, entity));
    }

    private static LivingEntity findNearestEnemy(Player player, double radius) {
        return enemiesAround(player, radius).stream()
                .min(Comparator.comparingDouble(entity -> entity.distanceToSqr(player)))
                .orElse(null);
    }

    private static List<ServerPlayer> allies(Player player, double radius) {
        if (!(player.level() instanceof ServerLevel serverLevel)) {
            return List.of();
        }
        return serverLevel.players().stream()
                .filter(other -> !other.isSpectator())
                .filter(other -> other.distanceToSqr(player) <= radius * radius)
                .filter(other -> isFriendly(player, other))
                .toList();
    }

    private static boolean isFriendly(Player player, LivingEntity entity) {
        if (entity == player) {
            return true;
        }
        if (entity instanceof Player other) {
            if (player.getTeam() != null) {
                return player.isAlliedTo(other);
            }
            return !other.isSpectator();
        }
        return player.isAlliedTo(entity);
    }

    private static EquippedHalo getEquippedHalo(Player player) {
        ItemStack stack = OtherHelper.getCurio(player, "halo", 0);
        if (stack.isEmpty()) {
            return null;
        }
        String itemId = Optional.ofNullable(ForgeRegistries.ITEMS.getKey(stack.getItem()))
                .map(location -> location.getPath())
                .orElse("");
        if (stack.getItem() instanceof DecorationItem decorationItem) {
            itemId = decorationItem.getEffectiveItemId(stack);
        }
        if (!HaloSkillDefinitions.contains(itemId)) {
            return null;
        }
        return new EquippedHalo(itemId, stack);
    }

    private static CompoundTag haloData(Player player, String itemId) {
        CompoundTag root = runtimeRoot(player);
        if (!root.contains(itemId)) {
            root.put(itemId, new CompoundTag());
        }
        return root.getCompound(itemId);
    }

    private static CompoundTag globalData(LivingEntity entity) {
        CompoundTag persistent = entity.getPersistentData();
        if (!persistent.contains(HaloRuntimeKeys.SKILL_DATA)) {
            persistent.put(HaloRuntimeKeys.SKILL_DATA, new CompoundTag());
        }
        CompoundTag skillData = persistent.getCompound(HaloRuntimeKeys.SKILL_DATA);
        if (!skillData.contains(HaloRuntimeKeys.ROOT)) {
            skillData.put(HaloRuntimeKeys.ROOT, new CompoundTag());
        }
        CompoundTag root = skillData.getCompound(HaloRuntimeKeys.ROOT);
        if (!root.contains(GLOBAL)) {
            root.put(GLOBAL, new CompoundTag());
        }
        return root.getCompound(GLOBAL);
    }

    private static CompoundTag runtimeRoot(Player player) {
        CompoundTag persistent = player.getPersistentData();
        if (!persistent.contains(HaloRuntimeKeys.SKILL_DATA)) {
            persistent.put(HaloRuntimeKeys.SKILL_DATA, new CompoundTag());
        }
        CompoundTag skillData = persistent.getCompound(HaloRuntimeKeys.SKILL_DATA);
        if (!skillData.contains(HaloRuntimeKeys.ROOT)) {
            skillData.put(HaloRuntimeKeys.ROOT, new CompoundTag());
        }
        return skillData.getCompound(HaloRuntimeKeys.ROOT);
    }

    private static ListTag getModifiersList(CompoundTag tag) {
        if (tag.contains("CurioAttributeModifiers", 9)) {
            return tag.getList("CurioAttributeModifiers", 10);
        }
        return new ListTag();
    }

    private static UUID modifierUuid(String itemId, String key) {
        return UUID.nameUUIDFromBytes(("dba:" + itemId + ":" + key).getBytes(StandardCharsets.UTF_8));
    }

    private static void healIfNeeded(Player player, float amount) {
        if (amount > 0.0f && player.getHealth() < player.getMaxHealth()) {
            player.heal(amount);
        }
    }

    private static float healPercent(HaloSkillDefinitions.Definition definition, float main) {
        if (main > 1.0f || definition.activeKind == HaloSkillDefinitions.ActiveKind.ALLY_HEAL) {
            return definition.rarity >= 3 ? 0.18f : 0.12f;
        }
        return Math.max(0.08f, main);
    }

    private static int durationFromValue(float value, int fallbackTicks) {
        if (value <= 0.0f) {
            return fallbackTicks;
        }
        return value > 1.0f ? (int) (value * 20.0f) : fallbackTicks;
    }

    private static float valueAt(float[] values, int index) {
        if (values.length == 0) {
            return 0.0f;
        }
        return values[Math.max(0, Math.min(values.length - 1, index))];
    }

    private static String trim(float value) {
        if (Math.abs(value - Math.round(value)) < 0.001f) {
            return Integer.toString(Math.round(value));
        }
        return String.format(java.util.Locale.ROOT, "%.2f", value);
    }

    private static void spawnBurstParticles(Player player, LivingEntity target) {
        if (player.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.FLASH, target.getX(), target.getY() + 1.0D, target.getZ(), 1, 0, 0, 0, 0);
        }
    }

    private record EquippedHalo(String itemId, ItemStack stack) {
    }

    private HaloSkillRuntime() {
    }
}
