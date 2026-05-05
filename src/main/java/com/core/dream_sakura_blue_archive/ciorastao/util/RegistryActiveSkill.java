package com.core.dream_sakura_blue_archive.ciorastao.util;

import com.core.dream_sakura.skill.SkillBinding;
import com.core.dream_sakura_blue_archive.ciorastao.dream_sakura_blue_archive;
import com.core.dream_sakura_blue_archive.ciorastao.events.DamageHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

import java.util.function.Supplier;

@Mod.EventBusSubscriber(modid = dream_sakura_blue_archive.MODID)
public class RegistryActiveSkill {

    /**
     * 创建一个名为"TENDOUARIS Halo Skill"的技能绑定
     * 该技能使用J键触发，冷却时间为32秒
     *
     * @return 返回一个Supplier<SkillBinding>对象，用于创建技能绑定实例
     */
    public static final Supplier<SkillBinding> TENDOUARIS_HALO_Skill = () -> {
        return new SkillBinding(
                GLFW.GLFW_KEY_J,
                "TENDOUARIS Halo Skill",
                2400,
                "tendouaris_halo",
                (player, stack) -> {
                    if (player.level().isClientSide) return;

                    CompoundTag tag = stack.getOrCreateTag();
                    int haloLevel = HaloLevelManager.getHaloLevel(stack);

                    // --- 核心：在这里处理充能倍率并清空 ---
                    int charge = tag.getInt("ChargeLevel");
                    float chargeMultiplier = 1.0f;
                    if (charge == 1) chargeMultiplier = 1.5f;
                    else if (charge >= 2) chargeMultiplier = 2.0f;

                    // 消耗充能并重置计时
                    tag.putInt("ChargeLevel", 0);
                    tag.putLong("NextChargeTick", player.level().getGameTime() + 500);

                    // --- 核心：开启爆发状态 (被动注册会检测这个) ---
                    tag.putLong("BurstEndTime", player.level().getGameTime() + 400);

                    // 计算基础等级倍率
                    float baseMultiplier = 3.11f;
                    if (haloLevel >= 61) baseMultiplier = 5.91f;
                    else if (haloLevel >= 46) baseMultiplier = 4.97f;
                    else if (haloLevel >= 31) baseMultiplier = 4.51f;
                    else if (haloLevel >= 16) baseMultiplier = 3.57f;

                    // 将【总倍率】传给实现方法
                    DamageHandler.executeActiveSkill(player, baseMultiplier * chargeMultiplier);
                }
        );
    };

    public static final Supplier<SkillBinding> Hoshino_Halo_Skill = () -> {
        return new SkillBinding(
                GLFW.GLFW_KEY_J,
                "Hoshino Halo Skill",
                28000,
                "hoshino_halo",
                (player, stack) -> {
                    CompoundTag playerData = player.getPersistentData();
                    CompoundTag itemData = stack.getOrCreateTag();
                    CompoundTag tag = new CompoundTag();
                    if (!itemData.contains("level")) {
                        itemData.putInt("level", 1);
                        itemData.putInt("lastExtraLevels", 0);
                    }
                    int level = itemData.getInt("level");
                    float ExtraDamageMultiplier = OtherHelper.calculate(4.8f, 17, 90, level);
                    float ExtraShieldMultiplier = OtherHelper.calculate(0.25f, 9.5f, 90, level);
                    tag.putFloat("ExtraDamageMultiplier", ExtraDamageMultiplier);
                    tag.putInt("ExtraDamage", 4);
                    tag.putLong("StartShieldTime", player.level().getGameTime());
                    tag.putFloat("ExtraShieldMultiplier", 268 * (1 + ExtraShieldMultiplier));
                    tag.putBoolean("ExtraDamageIsTrue", true);

                    if (playerData.contains("SkillData")) {
                        playerData.getCompound("SkillData").put("HoshinoHaloData", tag);
                    } else {
                        CompoundTag HoshinoHaloData = new CompoundTag();
                        HoshinoHaloData.put("HoshinoHaloData", tag);
                        playerData.put("SkillData", HoshinoHaloData);
                    }
                }
        );
    };

    public static final Supplier<SkillBinding> Hina_Halo_Skill = () -> {
        return new SkillBinding(
                GLFW.GLFW_KEY_J,
                "Hina Halo Skill",
                52000,
                "hina_halo",
                (player, stack) -> {
                    // 获取数据
                    CompoundTag playerData = player.getPersistentData();
                    CompoundTag itemData = stack.getOrCreateTag();
                    CompoundTag tag = new CompoundTag();

                    // 初始化等级
                    if (!itemData.contains("level")) {
                        itemData.putInt("level", 1);
                        itemData.putInt("lastExtraLevels", 0);
                    }
                    int level = itemData.getInt("level");

                    // 记录基础速度
                    AttributeInstance movementAttribute = player.getAttribute(Attributes.MOVEMENT_SPEED);
                    if (movementAttribute != null) {
                        tag.putDouble("BaseMovementSpeed", movementAttribute.getValue());
                        movementAttribute.setBaseValue(0);
                        player.onUpdateAbilities();
                        if (!player.level().isClientSide() && player instanceof ServerPlayer serverPlayer)
                            serverPlayer.onUpdateAbilities();
                    }

                    // 设置攻击标记
                    float ExtraDamageMultiplier1 = OtherHelper.calculate(6.18f, 18, 90, level);
                    float ExtraDamageMultiplier2 = OtherHelper.calculate(12.88f, 44.8f, 90, level);
                    tag.putInt("DamageCount", 0);
                    tag.putFloat("ExtraDamageMultiplier1", ExtraDamageMultiplier1);
                    tag.putFloat("ExtraDamageMultiplier2", ExtraDamageMultiplier2);
                    tag.putBoolean("ExtraDamageIsTrue", true);
                    tag.putBoolean("SkillCompleted", false);

                    // 被动2激活
                    tag.putBoolean("Passive2Active", true);
                    tag.putBoolean("Passive2ActiveCC", false);

                    // 技能初始化
                    CompoundTag skillData = playerData.contains("SkillData") ? playerData.getCompound("SkillData") : new CompoundTag();
                    skillData.put("HinaHaloData", tag);
                    playerData.put("SkillData", skillData);
                }
        );
    };

    public static final Supplier<SkillBinding> SHIROKO_Halo_Skill = () -> {// 技能
        return new SkillBinding(// 技能按键
                GLFW.GLFW_KEY_J,// 技能按键
                "SHIROKO Halo Skill",// 技能名称
                20000,// 技能冷却时间
                "shiroko_halo",// 技能物品ID
                (player, stack) -> {// 技能逻辑
                    // 获取数据
                    CompoundTag playerData = player.getPersistentData();// 玩家数据
                    CompoundTag tag = new CompoundTag();// 技能数据

                    // 获取光环等级
                    int level = HaloLevelManager.getHaloLevel(stack);// 获取等级

                    // 技能初始化
                    CompoundTag skillData = playerData.contains("SkillData") ? playerData.getCompound("SkillData") : new CompoundTag();// 获取技能数据
                    skillData.put("HinaHaloData", tag);// 将技能数据添加到技能数据中
                    playerData.put("SkillData", skillData);// 将技能数据添加到玩家数据中
                }
        );
    };
}
