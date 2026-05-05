package com.core.dream_sakura_blue_archive.ciorastao.events;

import com.core.dream_sakura.enums.DamageType;
import com.core.dream_sakura_blue_archive.ciorastao.dream_sakura_blue_archive;
import com.core.dream_sakura_blue_archive.ciorastao.effect.RegistryEffect;
import com.core.dream_sakura_blue_archive.ciorastao.util.OtherHelper;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = dream_sakura_blue_archive.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class DamageHandler {

    // 爱丽丝光环主动技能伤害逻辑
    public static void executeActiveSkill(Player player, float multiplier) {
        // 1. 获取玩家基础攻击力并计算最终伤害
        // 此时的 multiplier 已经是【等级倍率 * 充能加成】后的结果了
        double attackDamage = player.getAttributeValue(Attributes.ATTACK_DAMAGE);
        float finalDamage = (float) (attackDamage * multiplier);

        // 2. 执行直线扫描逻辑
        double range = 32.0D;
        Vec3 start = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        Vec3 end = start.add(look.scale(range));

        AABB searchBox = player.getBoundingBox().expandTowards(look.scale(range)).inflate(1.5D);
        List<LivingEntity> targets = player.level().getEntitiesOfClass(LivingEntity.class, searchBox,
                e -> e != player && e.isAlive() && !e.isAlliedTo(player));

        for (LivingEntity target : targets) {
            if (target.getBoundingBox().inflate(0.5D).clip(start, end).isPresent()) {
                // 造成伤害
                target.hurt(player.damageSources().indirectMagic(player, player), finalDamage);

                if (player.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.FLASH, target.getX(), target.getY() + 1, target.getZ(), 1, 0, 0, 0, 0);
                }
            }
        }

        // 3. 视觉效果
        if (player.level() instanceof ServerLevel serverLevel) {
            for (int i = 0; i < range; i += 2) {
                Vec3 point = start.add(look.scale(i));
                serverLevel.sendParticles(ParticleTypes.SONIC_BOOM, point.x, point.y, point.z, 1, 0, 0, 0, 0);
            }
        }
    }



    // 星野光环主动技能伤害逻辑
    @SubscribeEvent
    public static void HoshinoHaloAHurt(LivingHurtEvent event) {

        DamageSource damageSource = event.getSource();
        // 检查伤害源是否为玩家
        if (!(damageSource.getEntity() instanceof Player player)) return;

        boolean isWearingHalo = OtherHelper.getCuriosItem(player, "halo", "dream_sakura_blue_archive:hoshino_halo");
        if (!isWearingHalo) return;

        // 获取玩家的持久化数据
        CompoundTag playerData = player.getPersistentData();
        if (!playerData.contains("SkillData")) return;
        // 获取技能数据
        CompoundTag skillData = playerData.getCompound("SkillData");
        if (!skillData.contains("HoshinoHaloData")) return;


        CompoundTag HoshinoHaloData = skillData.getCompound("HoshinoHaloData");
        int extraDamage = HoshinoHaloData.getInt("ExtraDamage");
        if (extraDamage > 0) {
            event.setAmount(event.getAmount() * (1 + extraDamage));
            HoshinoHaloData.putInt("ExtraDamage", extraDamage - 1);
        }
    }

    // 星野光环主动技能护盾逻辑
    @SubscribeEvent
    public static void HoshinoShieldAHurt(LivingDamageEvent event) {

        if (!(event.getEntity() instanceof Player player)) return;

        boolean isWearingHalo = OtherHelper.getCuriosItem(player, "halo", "dream_sakura_blue_archive:hoshino_halo");
        if (!isWearingHalo) return;

        // 获取玩家的持久化数据
        CompoundTag playerData = player.getPersistentData();
        if (!playerData.contains("SkillData")) return;
        // 获取技能数据
        CompoundTag skillData = playerData.getCompound("SkillData");
        if (!skillData.contains("HoshinoHaloData")) return;


        CompoundTag HoshinoHaloData = skillData.getCompound("HoshinoHaloData");

        float shieldAmount = HoshinoHaloData.getFloat("ExtraShieldMultiplier") - event.getAmount();

        if (HoshinoHaloData.getBoolean("ExtraDamageIsTrue")) {
            if (shieldAmount >= 0) {
                event.setAmount(0);
                HoshinoHaloData.putFloat("ExtraShieldMultiplier", shieldAmount);
            } else {
                event.setAmount(-shieldAmount);
                HoshinoHaloData.putFloat("ExtraShieldMultiplier", 0);
            }
        }
    }

    //#region 日奈光环技能逻辑
    // 攻击检测
    @SubscribeEvent
    public static void HinaHaloHurt(LivingHurtEvent event) {
        DamageSource damageSource = event.getSource();

        if (!(damageSource.getEntity() instanceof Player player)) return;

        boolean isWearingHalo = OtherHelper.getCuriosItem(player, "halo", "dream_sakura_blue_archive:hina_halo");
        if (!isWearingHalo) return;

        // 获取数据
        CompoundTag playerData = player.getPersistentData();
        if (!playerData.contains("SkillData")) return;
        CompoundTag skillData = playerData.getCompound("SkillData");
        if (!skillData.contains("HinaHaloData")) return;
        CompoundTag HinaHaloData = skillData.getCompound("HinaHaloData");

        // 检查是否开启技能
        if (!HinaHaloData.getBoolean("ExtraDamageIsTrue")) return;

        // 检查是否已经完成
        if (HinaHaloData.getBoolean("SkillCompleted")) return;


        // #region 主动技能逻辑
        // 伤害计算
        int damageCount = HinaHaloData.getInt("DamageCount");
        float originalDamage = event.getAmount();
        float finalDamage = 0;

        switch (damageCount) {
            case 0:
            case 1:
                float multiplier1 = HinaHaloData.getFloat("ExtraDamageMultiplier1");
                finalDamage = originalDamage * multiplier1;
                break;
            case 2:
                float multiplier2 = HinaHaloData.getFloat("ExtraDamageMultiplier2");
                finalDamage = originalDamage * multiplier2;
                break;
            default:
                finalDamage = originalDamage;
                break;
        }

        // 真伤(设置生命值)
        LivingEntity target = event.getEntity();
        float newHealth = target.getHealth() - finalDamage;
        if (newHealth <= 0) {
            target.die(damageSource);
        } else {
            target.setHealth(newHealth);
            event.setAmount(0);
            event.setCanceled(true);
        }
        HinaHaloData.putInt("DamageCount", damageCount + 1);

        if (damageCount >= 2) {
            HinaHaloData.putBoolean("ExtraDamageIsTrue", false);
            HinaHaloData.putBoolean("SkillCompleted", true);
        }
        //#endregion

        //#region 被动1技能逻辑
        // 记录最后攻击实体和时间
        LivingEntity attackedEntity = event.getEntity();
        if (attackedEntity != null) {
            HinaHaloData.putString("LastAttackedEntity", attackedEntity.getStringUUID());
            HinaHaloData.putLong("LastAttackTime", player.level().getGameTime());
            HinaHaloData.putDouble("LastDamageAmount", originalDamage);

            // 范围伤害
            triggerAreaDamage(player, attackedEntity, originalDamage, HinaHaloData);

        }
        //#endregion

        // 被动3检测
        if (!HinaHaloData.contains("Passive3Active") && OtherHelper.getCuriosItem(player, "halo", "dream_sakura_blue_archive:hina_halo")) {
            HinaHaloData.putBoolean("Passive3Active", true);
        } else {
            HinaHaloData.putBoolean("Passive3Active", false);
        }
    }

    private static void triggerAreaDamage(Player player, LivingEntity attackedEntity, float originalDamage, CompoundTag HinaHaloData) {
        CompoundTag itemData = OtherHelper.getCurio(player, "halo", 0).getOrCreateTag();
        int level = itemData.getInt("level");

        float areaDamageMultiplier = OtherHelper.calculate(2.32f, 4.41f, 95, level);

        // 获取实体周围实体(半径8)
        List<LivingEntity> nearbyEntities = player.level().getEntitiesOfClass(
                LivingEntity.class,
                attackedEntity.getBoundingBox().inflate(8),
                entity -> {
                    // 确保entity不为null，并且不是玩家自己
                    return entity != null && !entity.equals(player);
                }
        );


        Random random = new Random();
        List<LivingEntity> selectedTargets = new ArrayList<>();

        if (nearbyEntities.size() <= 5) {
            selectedTargets.addAll(nearbyEntities);
        } else {
            List<LivingEntity> shuffled = new ArrayList<>(nearbyEntities);
            Collections.shuffle(shuffled, random);
            selectedTargets = shuffled.subList(0, 5);
        }

        // 应用范围伤害
        for (LivingEntity target : selectedTargets) {
            if (!target.isAlive()) continue;

            float finalDamage = originalDamage * areaDamageMultiplier;
            int stunDuration = 20 + random.nextInt(80);

            float newHealth = target.getHealth() - finalDamage;
            target.setHealth(newHealth);

            if (RegistryEffect.STUN_EFFECT != null) {
                MobEffectInstance stunEffect = new MobEffectInstance(
                        RegistryEffect.STUN_EFFECT.get(),
                        stunDuration,
                        0,
                        true,
                        true,
                        true
                );
                target.addEffect(stunEffect);
            }
        }
    }

    // 受击检测
    @SubscribeEvent
    public static void HinaHaloPassive(LivingHurtEvent event) {
        LivingEntity entity = event.getEntity();
        if (!(entity instanceof Player player)) return;

        boolean isWearingHalo = OtherHelper.getCuriosItem(player, "halo", "dream_sakura_blue_archive:hina_halo");
        if (!isWearingHalo) return;

        // 获取数据
        CompoundTag playerData = player.getPersistentData();
        if (!playerData.contains("SkillData")) return;
        CompoundTag skillData = playerData.getCompound("SkillData");
        if (!skillData.contains("HinaHaloData")) return;
        CompoundTag HinaHaloData = skillData.getCompound("HinaHaloData");

        // 被动2检测
        if (HinaHaloData.getBoolean("Passive2Active") && OtherHelper.getCuriosItem(player, "halo", "dream_sakura_blue_archive:hina_halo")) {
            DamageSource source = event.getSource();
            if (DamageType.EXPLOSION.shouldImmune(source)) event.setCanceled(true);
        }
        ;

        // 被动3检测
        if (!HinaHaloData.contains("Passive3Active") && OtherHelper.getCuriosItem(player, "halo", "dream_sakura_blue_archive:hina_halo")) {
            HinaHaloData.putBoolean("Passive3Active", true);
        } else {
            HinaHaloData.putBoolean("Passive3Active", false);
        }
    }



    /**
     * 强制添加效果（无视抗性）
     * 尝试多种可能的混淆字段名
     */
    private static void forceAddEffect(LivingEntity entity, MobEffectInstance instance) {
        // 尝试的字段名列表（根据Minecraft版本变化）
        String[] fieldNames = {"f_20945_", "activeEffects", "effects"};

        for (String fieldName : fieldNames) {
            try {
                Field effectsField = LivingEntity.class.getDeclaredField(fieldName);
                effectsField.setAccessible(true);

                @SuppressWarnings("unchecked")
                Map<MobEffect, MobEffectInstance> effects = (Map<MobEffect, MobEffectInstance>) effectsField.get(entity);
                effects.put(instance.getEffect(), instance);
                return; // 成功则返回
            } catch (Exception ex) {
                // 继续尝试下一个字段名
                continue;
            }
        }

        // 所有字段名都失败，回退到正常方式
        entity.addEffect(instance);
    }

    /**
     * 应用所有负面buff（强制添加，无视抗性）
     */
    private static void applyAllNegativeEffects(LivingEntity entity, int level, int duration) {
        // 从注册表获取所有效果
        for (MobEffect effect : ForgeRegistries.MOB_EFFECTS.getValues()) {
            // 只添加负面效果
            if (effect.getCategory() == MobEffectCategory.HARMFUL) {
                // 创建效果实例
                MobEffectInstance effectInstance = new MobEffectInstance(
                        effect,
                        duration, // 持续时间（刻）
                        level - 1, // 等级（0-based）
                        false, // 是否环境粒子
                        false, // 是否显示粒子
                        true // 是否显示图标
                );

                // 强制添加效果（使用用户提供的代码）
                forceAddEffect(entity, effectInstance);
            }
        }
    }


    /**
     * 存储生物被evilPearl攻击的数据
     */
    private static class EvilPearlData {
        int hitCount = 0; // 被攻击次数
        long lastHitTime = System.currentTimeMillis(); // 上次被攻击时间

        // 可以添加清理机制，比如一段时间后重置计数
        public boolean shouldReset() {
            return System.currentTimeMillis() - lastHitTime > 30 * 1000; // 30秒后重置
        }
    }
    //#endregion

}