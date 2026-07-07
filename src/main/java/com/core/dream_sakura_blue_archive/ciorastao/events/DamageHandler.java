package com.core.dream_sakura_blue_archive.ciorastao.events;

import com.core.dream_sakura.enums.DamageType;
import com.core.dream_sakura_blue_archive.ciorastao.dream_sakura_blue_archive;
import com.core.dream_sakura_blue_archive.ciorastao.effect.RegistryEffect;
import com.core.dream_sakura_blue_archive.ciorastao.util.HaloLevelManager;
import com.core.dream_sakura_blue_archive.ciorastao.util.HaloSkillRuntime;
import com.core.dream_sakura_blue_archive.ciorastao.util.OtherHelper;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.*;

/**
 * 伤害处理器 —— 处理三个角色的主动/被动技能伤害、护盾和穿透逻辑
 */
@Mod.EventBusSubscriber(modid = dream_sakura_blue_archive.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class DamageHandler {

    private static final String HOSHINO_ID = "dream_sakura_blue_archive:hoshino_halo";
    private static final String HINA_ID    = "dream_sakura_blue_archive:hina_halo";
    private static final String ALICE_ID   = "dream_sakura_blue_archive:tendouaris_halo";

    // ============================================================
    // 爱丽丝 (TENDOUARIS) - 主动技能：平衡崩坏（直线扫描）
    // ============================================================

    public static void executeActiveSkill(Player player, float multiplier) {
        double attackDamage = player.getAttributeValue(Attributes.ATTACK_DAMAGE);
        float finalDamage = (float)(attackDamage * multiplier);

        double range = 32.0D;
        Vec3 start = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        Vec3 end = start.add(look.scale(range));

        AABB searchBox = player.getBoundingBox().expandTowards(look.scale(range)).inflate(1.5D);
        List<LivingEntity> targets = player.level().getEntitiesOfClass(LivingEntity.class, searchBox,
                e -> e != player && e.isAlive() && !e.isAlliedTo(player));

        for (LivingEntity target : targets) {
            if (target.getBoundingBox().inflate(0.5D).clip(start, end).isPresent()) {
                // 50%魔法穿透
                applyMagicPenetration(player, target, finalDamage, 0.5f);
                if (player.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.FLASH,
                        target.getX(), target.getY() + 1, target.getZ(), 1, 0, 0, 0, 0);
                }
            }
        }

        if (player.level() instanceof ServerLevel serverLevel) {
            for (int i = 0; i < range; i += 2) {
                Vec3 point = start.add(look.scale(i));
                serverLevel.sendParticles(ParticleTypes.SONIC_BOOM,
                    point.x, point.y, point.z, 1, 0, 0, 0, 0);
            }
        }
    }

    // ============================================================
    // 日奈 (HINA) - 主动技能：终幕（扇形单次伤害，由 RegistryActiveSkill 调用）
    // ============================================================

    public static void applyHinaSkillDamage(Player player, LivingEntity target, float damage) {
        applyPhysicalPenetration(player, target, damage, 0.5f);
    }

    // ============================================================
    // 星野 (HOSHINO) - 主动技能多段攻击（ServerTick 驱动）
    // ============================================================

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        HaloSkillRuntime.onServerTick(event.getServer());

        for (var serverLevel : event.getServer().getAllLevels()) {
            for (var player : serverLevel.players()) {
                processHoshinoTactical(player);
            }
        }
    }

    private static void processHoshinoTactical(Player player) {
        if (!OtherHelper.getCuriosItem(player, "halo", HOSHINO_ID)) return;

        CompoundTag playerData = player.getPersistentData();
        if (!playerData.contains("SkillData")) return;
        CompoundTag skillData = playerData.getCompound("SkillData");
        if (!skillData.contains("HoshinoHaloData")) return;
        CompoundTag tacTag = skillData.getCompound("HoshinoHaloData");
        if (!tacTag.getBoolean("Active")) return;

        int hitsRemaining = tacTag.getInt("HitsRemaining");
        if (hitsRemaining <= 0) {
            tacTag.putBoolean("Active", false);
            return;
        }

        long currentTick = player.level().getGameTime();
        long nextHit = tacTag.getLong("NextHitTick");
        if (currentTick < nextHit) return;

        float damageMultiplier = tacTag.getFloat("DamageMultiplier");
        float stunDuration = tacTag.getFloat("StunDuration");
        double attackDamage = player.getAttributeValue(Attributes.ATTACK_DAMAGE);
        float finalDamage = (float)(attackDamage * damageMultiplier);

        // 5×5 范围扫描
        AABB area = player.getBoundingBox().inflate(2.5, 0, 2.5)
            .move(player.getLookAngle().scale(2));
        var targets = player.level().getEntitiesOfClass(LivingEntity.class, area,
            e -> e != player && e.isAlive() && !e.isAlliedTo(player));

        for (var target : targets) {
            // 无视无敌帧 + 爆炸穿透50%
            target.invulnerableTime = 0;
            // 50%真实伤害绕过爆炸保护
            float pen = finalDamage * 0.5f;
            if (pen > 0 && target.isAlive()) {
                float penHealth = target.getHealth() - pen;
                if (penHealth <= 0) {
                    target.die(player.damageSources().explosion(player, player));
                } else {
                    target.setHealth(penHealth);
                }
            }
            // 50%正常伤害
            float normal = finalDamage * 0.5f;
            if (normal > 0 && target.isAlive()) {
                target.hurt(player.damageSources().explosion(player, player), normal);
            }
            // 眩晕
            if (stunDuration > 0 && RegistryEffect.STUN_EFFECT != null) {
                target.addEffect(new MobEffectInstance(
                    RegistryEffect.STUN_EFFECT.get(),
                    (int)(stunDuration * 20),
                    0, false, true, true
                ));
            }
            // 粒子
            if (player.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.EXPLOSION,
                    target.getX(), target.getY() + 1, target.getZ(), 1, 0, 0, 0, 0);
            }
        }

        // 下一击 0.3s 后
        tacTag.putLong("NextHitTick", currentTick + 6);
        tacTag.putInt("HitsRemaining", hitsRemaining - 1);
    }

    // ============================================================
    // LivingHurtEvent - 伤害修改（日奈被动1/3, 三种穿透）
    // ============================================================

    /**
     * DBA balanced passive1: 14%~45%.
     */
    private static final float[] HINA_P1_DAMAGE_BONUS = {
        0.14f, 0.17f, 0.21f, 0.24f, 0.28f, 0.31f, 0.35f, 0.39f, 0.42f, 0.45f
    };
    /**
     * DBA balanced passive3: 24%~80%.
     */
    private static final float[] HINA_P3_UNARMORED_BONUS = {
        0.24f, 0.30f, 0.37f, 0.43f, 0.50f, 0.56f, 0.62f, 0.69f, 0.74f, 0.80f
    };

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        HaloSkillRuntime.onLivingHurt(event);

        LivingEntity target = event.getEntity();
        DamageSource source = event.getSource();

        if (!(source.getEntity() instanceof Player player)) return;

        // ---- 日奈被动2：爆炸免疫 ----
        if (target instanceof Player) {
            handleHinaExplosionImmunity((Player) target, source, event);
        }

        // ---- 按光环分类处理 ----
        boolean hasHoshino = OtherHelper.getCuriosItem(player, "halo", HOSHINO_ID);
        boolean hasHina    = OtherHelper.getCuriosItem(player, "halo", HINA_ID);
        boolean hasAlice   = OtherHelper.getCuriosItem(player, "halo", ALICE_ID);

        if (hasHina) {
            handleHinaPassive1(player, event);
            handleHinaPassive3(player, target, event);
        }

        // 穿透处理
        if (hasAlice) {
            applyPenetrationInEvent(player, target, event, 0.5f, "magic");
        }
        if (hasHina) {
            applyPenetrationInEvent(player, target, event, 0.5f, "physical");
        }
        if (hasHoshino) {
            applyPenetrationInEvent(player, target, event, 0.5f, "explosion");
        }
    }

    // ---- 日奈被动2：爆炸免疫 ----
    private static void handleHinaExplosionImmunity(Player player, DamageSource source, LivingHurtEvent event) {
        if (!OtherHelper.getCuriosItem(player, "halo", HINA_ID)) return;
        CompoundTag playerData = player.getPersistentData();
        if (!playerData.contains("SkillData")) return;
        CompoundTag skillData = playerData.getCompound("SkillData");
        if (!skillData.contains("HinaHaloData")) return;
        CompoundTag hinaData = skillData.getCompound("HinaHaloData");
        // 爆炸免疫 (被动2的额外效果)
        if (hinaData.getBoolean("Passive2ExplosionImmune") &&
            DamageType.EXPLOSION.shouldImmune(source)) {
            event.setCanceled(true);
        }
    }

    // ---- 日奈被动1：重装与毁灭 ----
    private static void handleHinaPassive1(Player player, LivingHurtEvent event) {
        DamageSource source = event.getSource();
        LivingEntity attackedEntity = event.getEntity();
        if (attackedEntity == null) return;

        CompoundTag playerData = player.getPersistentData();
        if (!playerData.contains("SkillData")) return;
        CompoundTag skillData = playerData.getCompound("SkillData");
        if (!skillData.contains("HinaHaloData")) return;
        CompoundTag hinaData = skillData.getCompound("HinaHaloData");

        String lastTarget = hinaData.getString("LastAttackedEntity");
        long lastAttackTime = hinaData.getLong("LastAttackTime");
        long currentTime = player.level().getGameTime();
        String currentTarget = attackedEntity.getStringUUID();

        // 同一目标连续攻击（3秒内）
        if (currentTarget.equals(lastTarget) && currentTime - lastAttackTime < 60) {
            int comboCount = hinaData.getInt("ComboCount") + 1;
            hinaData.putInt("ComboCount", comboCount);
            // 连续3次后激活加成
            if (comboCount >= 3) {
                hinaData.putBoolean("Passive1Active", true);
                hinaData.putLong("Passive1StartTime", currentTime);
                ItemStack stack = OtherHelper.getCurio(player, "halo", 0);
                int haloLevel = HaloLevelManager.getHaloLevel(stack);
                float bonus = OtherHelper.getPassiveValue(haloLevel, HINA_P1_DAMAGE_BONUS);
                hinaData.putFloat("Passive1DamageBonus", bonus);
                event.setAmount(event.getAmount() * (1 + bonus));
            }
        } else {
            hinaData.putInt("ComboCount", 1);
        }

        // 持续20秒，继续攻击刷新
        if (hinaData.getBoolean("Passive1Active")) {
            long startTime = hinaData.getLong("Passive1StartTime");
            if (currentTime - startTime < 400) { // 20s
                float bonus = hinaData.getFloat("Passive1DamageBonus");
                event.setAmount(event.getAmount() * (1 + bonus));
                hinaData.putLong("Passive1StartTime", currentTime); // 刷新
            } else {
                hinaData.putBoolean("Passive1Active", false);
            }
        }

        hinaData.putString("LastAttackedEntity", currentTarget);
        hinaData.putLong("LastAttackTime", currentTime);
    }

    // ---- 日奈被动3：彻头彻尾 ----
    private static void handleHinaPassive3(Player player, LivingEntity target, LivingHurtEvent event) {
        // 检测目标无护甲
        double targetArmor = target.getAttributeValue(Attributes.ARMOR);
        if (targetArmor > 0) return;

        ItemStack stack = OtherHelper.getCurio(player, "halo", 0);
        int haloLevel = HaloLevelManager.getHaloLevel(stack);
        float bonus = OtherHelper.getPassiveValue(haloLevel, HINA_P3_UNARMORED_BONUS);
        event.setAmount(event.getAmount() * (1 + bonus));
    }

    // ---- 通用穿透处理 ----
    private static void applyPenetrationInEvent(Player player, LivingEntity target,
                                                 LivingHurtEvent event, float penPct, String type) {
        // 穿透：绕过护甲和免疫
        // 简单实现：增加穿透倍率伤害
        // 实际游戏中可通过多种方式实现，此处作为预留扩展点
        // 穿透效果结合在具体伤害方法中
    }

    // ---- 直接伤害方法（带穿透）----
    private static void applyMagicPenetration(Player player, LivingEntity target,
                                               float damage, float penPct) {
        // 魔法伤害 + 50%穿透 → 直接伤害绕过50%魔抗
        float newHealth = target.getHealth();
        // 检查目标是否有魔法免疫
        // 简单穿透实现：50%伤害为真实伤害
        float trueDmg = damage * penPct;
        float normalDmg = damage * (1 - penPct);
        if (normalDmg > 0) {
            target.hurt(player.damageSources().indirectMagic(player, player), normalDmg);
        }
        if (trueDmg > 0 && target.isAlive()) {
            newHealth = target.getHealth() - trueDmg;
            if (newHealth <= 0) {
                target.die(player.damageSources().magic());
            } else {
                target.setHealth(newHealth);
            }
        }
    }

    private static void applyPhysicalPenetration(Player player, LivingEntity target,
                                                  float damage, float penPct) {
        float trueDmg = damage * penPct;
        float normalDmg = damage * (1 - penPct);
        if (normalDmg > 0) {
            target.hurt(player.damageSources().mobAttack(player), normalDmg);
        }
        if (trueDmg > 0 && target.isAlive()) {
            float newHealth = target.getHealth() - trueDmg;
            if (newHealth <= 0) {
                target.die(player.damageSources().generic());
            } else {
                target.setHealth(newHealth);
            }
        }
    }

    // ============================================================
    // LivingDamageEvent - 护盾吸收（星野被动3）
    // ============================================================

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        HaloSkillRuntime.onLivingDamage(event);

        if (!(event.getEntity() instanceof Player player)) return;

        boolean isWearingHoshino = OtherHelper.getCuriosItem(player, "halo", HOSHINO_ID);
        boolean isWearingHina    = OtherHelper.getCuriosItem(player, "halo", HINA_ID);

        if (isWearingHoshino) {
            handleHoshinoShield(player, event);
        }
        if (isWearingHina) {
            handleHinaHitCancel(player, event);
        }
    }

    // ---- 星野被动3：护盾吸收 ----
    private static void handleHoshinoShield(Player player, LivingDamageEvent event) {
        CompoundTag playerData = player.getPersistentData();
        if (!playerData.contains("SkillData")) return;
        CompoundTag skillData = playerData.getCompound("SkillData");
        if (!skillData.contains("HoshinoHaloData")) return;
        CompoundTag tacTag = skillData.getCompound("HoshinoHaloData");

        long shieldEndTime = tacTag.getLong("ShieldEndTime");
        if (player.level().getGameTime() > shieldEndTime) return;

        float shieldAmount = tacTag.getFloat("ShieldAmount");
        float damage = event.getAmount();

        if (shieldAmount >= damage) {
            tacTag.putFloat("ShieldAmount", shieldAmount - damage);
            event.setAmount(0);
        } else {
            tacTag.putFloat("ShieldAmount", 0);
            event.setAmount(damage - shieldAmount);
        }
    }

    // ---- 日奈被动1：受击取消加成 ----
    private static void handleHinaHitCancel(Player player, LivingDamageEvent event) {
        CompoundTag playerData = player.getPersistentData();
        if (!playerData.contains("SkillData")) return;
        CompoundTag skillData = playerData.getCompound("SkillData");
        if (!skillData.contains("HinaHaloData")) return;
        CompoundTag hinaData = skillData.getCompound("HinaHaloData");

        // 被攻击时取消被动1加成
        if (hinaData.getBoolean("Passive1Active")) {
            hinaData.putBoolean("Passive1Active", false);
            hinaData.putInt("ComboCount", 0);
        }
    }
}
