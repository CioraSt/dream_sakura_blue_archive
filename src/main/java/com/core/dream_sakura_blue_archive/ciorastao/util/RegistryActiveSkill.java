package com.core.dream_sakura_blue_archive.ciorastao.util;

import com.core.dream_sakura.skill.SkillBinding;
import com.core.dream_sakura_blue_archive.ciorastao.dream_sakura_blue_archive;
import com.core.dream_sakura_blue_archive.ciorastao.events.DamageHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

import java.util.function.Supplier;

@Mod.EventBusSubscriber(modid = dream_sakura_blue_archive.MODID)
public class RegistryActiveSkill {

    // ============ 天童 爱丽丝 (TENDOUARIS) ============

    /** 平衡崩坏 - 冷却60s (60000ms) */
    private static final float[] TENDOUARIS_MULTIPLIERS = {
        3.11f,  // L1: 311%
        3.57f,  // L2: 357%
        4.51f,  // L3: 451%
        5.00f,  // L4: 500%
        6.00f   // MAX: 600%
    };

    public static final Supplier<SkillBinding> TENDOUARIS_HALO_Skill = () ->
        new SkillBinding(
            GLFW.GLFW_KEY_J,
            "TENDOUARIS Halo Skill",
            60000,
            "tendouaris_halo",
            (player, stack) -> {
                if (player.level().isClientSide) return;

                CompoundTag tag = stack.getOrCreateTag();
                int haloLevel = HaloLevelManager.getHaloLevel(stack);

                // 充能倍率
                int charge = tag.getInt("ChargeLevel");
                float chargeMultiplier = 1.0f;
                if (charge == 1) chargeMultiplier = 1.5f;
                else if (charge >= 2) chargeMultiplier = 2.0f;

                // 消耗充能并重置计时
                tag.putInt("ChargeLevel", 0);
                tag.putLong("NextChargeTick", player.level().getGameTime() + 500);

                // 开启爆发状态 (20s = 400 ticks)
                tag.putLong("BurstEndTime", player.level().getGameTime() + 400);

                // 基础倍率 (5档映射)
                float baseMultiplier = OtherHelper.getActiveValue(haloLevel, TENDOUARIS_MULTIPLIERS);

                DamageHandler.executeActiveSkill(player, baseMultiplier * chargeMultiplier);
            }
        );

    // ============ 小鸟游 星野 (HOSHINO) ============

    /** 战术镇压 - 冷却40s (40000ms), 5×5范围, 4次攻击间隔0.3s */
    private static final float[] HOSHINO_DAMAGE_MULTIPLIERS = {
        4.35f,  // L1: 435%
        5.01f,  // L2: 501%
        5.66f,  // L3: 566%
        6.32f,  // L4: 632%
        6.97f   // MAX: 697%
    };
    private static final float[] HOSHINO_STUN_DURATIONS = {
        0f,     // L1: 无
        0f,     // L2: 无
        1.0f,   // L3: 1s
        1.2f,   // L4: 1.2s
        1.4f    // MAX: 1.4s
    };

    public static final Supplier<SkillBinding> Hoshino_Halo_Skill = () ->
        new SkillBinding(
            GLFW.GLFW_KEY_J,
            "Hoshino Halo Skill",
            40000,
            "hoshino_halo",
            (player, stack) -> {
                if (player.level().isClientSide) return;

                int haloLevel = HaloLevelManager.getHaloLevel(stack);
                float damageMultiplier = OtherHelper.getActiveValue(haloLevel, HOSHINO_DAMAGE_MULTIPLIERS);
                float stunDuration = OtherHelper.getActiveValue(haloLevel, HOSHINO_STUN_DURATIONS);

                // 记录主动技能数据到NBT，由DamageHandler在tick中执行4次攻击
                CompoundTag playerData = player.getPersistentData();
                CompoundTag tacTag = new CompoundTag();
                tacTag.putInt("HitsRemaining", 4);
                tacTag.putLong("NextHitTick", player.level().getGameTime());
                tacTag.putFloat("DamageMultiplier", damageMultiplier);
                tacTag.putFloat("StunDuration", stunDuration);
                tacTag.putBoolean("Active", true);

                // 护盾被动3数据
                float shieldPercent = OtherHelper.getPassiveValue(haloLevel,
                    new float[]{1.60f, 2.00f, 2.60f, 3.20f, 4.80f, 5.50f, 6.40f, 7.20f, 8.80f, 10.00f});
                tacTag.putFloat("ShieldAmount", player.getMaxHealth() * shieldPercent);
                tacTag.putLong("ShieldEndTime", player.level().getGameTime() + 800); // 40s

                CompoundTag skillData = playerData.contains("SkillData")
                    ? playerData.getCompound("SkillData") : new CompoundTag();
                skillData.put("HoshinoHaloData", tacTag);
                playerData.put("SkillData", skillData);
            }
        );

    // ============ 空崎 日奈 (HINA) ============

    /** 终幕：伊施波设 - 冷却70s (70000ms), 前方扇形范围单次伤害 */
    private static final float[] HINA_SKILL_MULTIPLIERS = {
        6.36f,  // L1: 636%
        7.31f,  // L2: 731%
        9.22f,  // L3: 922%
        10.17f, // L4: 1017%
        12.08f  // MAX: 1208%
    };

    public static final Supplier<SkillBinding> Hina_Halo_Skill = () ->
        new SkillBinding(
            GLFW.GLFW_KEY_J,
            "Hina Halo Skill",
            70000,
            "hina_halo",
            (player, stack) -> {
                if (player.level().isClientSide) return;

                int haloLevel = HaloLevelManager.getHaloLevel(stack);
                float baseMultiplier = OtherHelper.getActiveValue(haloLevel, HINA_SKILL_MULTIPLIERS);
                double attackDamage = player.getAttributeValue(Attributes.ATTACK_DAMAGE);
                float finalDamage = (float)(attackDamage * baseMultiplier);

                // 前方扇形范围：60°, 半径8格
                Vec3 look = player.getLookAngle();
                Vec3 eyePos = player.getEyePosition();
                AABB searchBox = player.getBoundingBox().inflate(8);
                var targets = player.level().getEntitiesOfClass(
                    net.minecraft.world.entity.LivingEntity.class, searchBox,
                    e -> e != player && e.isAlive() && !e.isAlliedTo(player)
                );

                for (var target : targets) {
                    Vec3 toTarget = target.position().subtract(eyePos).normalize();
                    double dot = look.dot(toTarget);
                    if (dot > 0.5 && target.distanceToSqr(player) <= 64) { // cos(60°)=0.5
                        // 使用DamageHandler统一处理穿透
                        DamageHandler.applyHinaSkillDamage(player, target, finalDamage);
                    }
                }

                // 被动3激活标记
                CompoundTag playerData = player.getPersistentData();
                CompoundTag skillData = playerData.contains("SkillData")
                    ? playerData.getCompound("SkillData") : new CompoundTag();
                skillData.putBoolean("HinaSkillJustUsed", true);
                playerData.put("SkillData", skillData);
            }
        );

    // ============ 白子 (SHIROKO) ============

    public static final Supplier<SkillBinding> SHIROKO_Halo_Skill = () ->
        new SkillBinding(
            GLFW.GLFW_KEY_J,
            "SHIROKO Halo Skill",
            20000,
            "shiroko_halo",
            (player, stack) -> {
                CompoundTag playerData = player.getPersistentData();
                CompoundTag tag = new CompoundTag();
                CompoundTag skillData = playerData.contains("SkillData")
                    ? playerData.getCompound("SkillData") : new CompoundTag();
                skillData.put("HinaHaloData", tag);
                playerData.put("SkillData", skillData);
            }
        );
}
