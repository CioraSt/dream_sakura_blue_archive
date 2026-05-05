package com.core.dream_sakura_blue_archive.ciorastao.util;

import com.core.dream_sakura_blue_archive.ciorastao.effect.RegistryEffect;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * 被动技能注册类
 * 包含多个角色的被动技能实现
 */
public class RegistryPassiveSkill {

    // 通用属性修饰符UUID
    private static final UUID DAMAGE_MODIFIER_UUID = UUID.fromString("389b669a-4576-4bb2-9e87-ede652fbc7c9"); // 伤害修饰符UUID
    private static final UUID CRIT_CHANCE_MODIFIER_UUID = UUID.fromString("88d03014-7dda-45b7-a7aa-a5add5902374"); // 暴击率修饰符UUID
    private static final UUID CRIT_DAMAGE_MODIFIER_UUID = UUID.fromString("3fed0d50-b6b4-4b9f-80b3-c2976e4c735b"); // 暴击伤害修饰符UUID
    private static final UUID LUCK_MODIFIER_UUID = UUID.fromString("389b669a-4576-4bb2-9e08-ede652fbc7c9"); // 幸运值修饰符UUID
    private static final UUID ATTACK_MODIFIER_UUID = UUID.fromString("6e2cb19d-471e-4adc-b39a-ed3c3dc29b67"); // 攻击力修饰符UUID
    private static final UUID ARMOR_MODIFIER_UUID = UUID.fromString("22ca548e-fb29-4d54-8901-77bd84445b9a"); // 护甲修饰符UUID


    // 星野角色专用UUID
    private static final UUID HOSHINO_ARMOR_MODIFIER_UUID = UUID.fromString("6031e4b6-a956-4b42-9368-743761b1431a"); // 星野护甲修饰符UUID
    private static final UUID HOSHINO_DAMAGE_MODIFIER_UUID = UUID.fromString("554a983b-8ba9-4b4d-8db1-ebdbdbdbf391"); // 星野伤害修饰符UUID


    // 日奈角色专用UUID
    private static final UUID HINA_CRIT_CHANCE_MODIFIER_UUID_1 = UUID.fromString("f6708c06-583f-4545-867d-48fc676e7326"); // 日奈暴击率修饰符UUID1
    private static final UUID HINA_DAMAGE_MODIFIER_UUID_1 = UUID.fromString("c8d9b411-e6b6-45dd-8855-8c04f0507539"); // 日奈伤害修饰符UUID1
    private static final UUID HINA_DAMAGE_MODIFIER_UUID_2 = UUID.fromString("af38a193-eb97-49c2-94fa-aa9ee656c872"); // 日奈伤害修饰符UUID2
    private static final UUID HINA_CRIT_DAMAGE_MODIFIER_UUID_1 = UUID.fromString("f6b427ba-24f7-4c7f-a0cc-17dcb300d019"); // 日奈暴击伤害修饰符UUID1
    private static final UUID HINA_CRIT_CHANCE_MODIFIER_UUID_2 = UUID.fromString("7d16fe92-88e3-4517-a58d-ec7d854ee37a"); // 日奈暴击率修饰符UUID2
    private static final UUID HINA_CRIT_DAMAGE_MODIFIER_UUID_2 = UUID.fromString("96d55313-c76b-4ad4-86f0-c6d95445d4b2"); // 日奈暴击伤害修饰符UUID2
    private static final UUID HINA_DAMAGE_MODIFIER_UUID_3 = UUID.fromString("ef03a27e-6a0e-416e-a9fd-a8814980b849"); // 日奈伤害修饰符UUID3
    private static final UUID HINA_DAMAGE_MODIFIER_UUID_4 = UUID.fromString("3078605d-12ee-46c8-acc9-1afaec56c2ca"); // 日奈伤害修饰符UUID4
    private static final UUID HINA_ARMOR_MODIFIER_UUID = UUID.fromString("2e570ecd-5013-4df0-bec8-9c73c5ec07f0"); // 日奈护甲修饰符UUID
    private static final UUID MAX_HEALTH_UUID = UUID.fromString("2e891ecd-5013-4df0-bec8-9c73c5ec07f0"); // 最大生命值UUID

    /**
     * ALS 1 Halo Skill 2实现
     *
     * @param slotContext 装备槽位上下文
     * @param stack       物品堆栈
     */
    public static final void TENDOUARIS_Halo_Skill_2(SlotContext slotContext, ItemStack stack) {
        if (!(slotContext.entity() instanceof Player player) || player.level().isClientSide) return;

        CompoundTag itemData = stack.getOrCreateTag();
        long currentTime = player.level().getGameTime();

        // 1. 自动充能计时逻辑 (25s = 500 ticks)
        int charge = itemData.getInt("ChargeLevel");
        if (charge < 2) {
            if (!itemData.contains("NextChargeTick") || itemData.getLong("NextChargeTick") == 0) {
                itemData.putLong("NextChargeTick", currentTime + 500);
            }
            if (currentTime >= itemData.getLong("NextChargeTick")) {
                itemData.putInt("ChargeLevel", charge + 1);
                itemData.putLong("NextChargeTick", currentTime + 500);
            }
        }

        // 2. 20s 爆发属性状态维护 (NBT 列表操作)
        long burstEndTime = itemData.getLong("BurstEndTime");
        boolean isInBurst = currentTime < burstEndTime;
        boolean lastState = itemData.getBoolean("lastBurstState");

        // 仅在状态切换瞬间更新 CurioAttributeModifiers，节省性能
        if (isInBurst != lastState) {
            itemData.putBoolean("lastBurstState", isInBurst);

            ListTag modifiersList = itemData.contains("CurioAttributeModifiers", 9)
                    ? itemData.getList("CurioAttributeModifiers", 10)
                    : new ListTag();

            // 使用你工具类中的方法移除/添加
            OtherHelper.removeExistingModifier(modifiersList, CRIT_DAMAGE_MODIFIER_UUID);

            if (isInBurst) {
                int level = itemData.getInt("level");
                // 基础 22% + (等级/10)*4%，上限 90 级
                double totalBonus = 0.22 + (Math.min(level, 90) / 10) * 0.04;
                OtherHelper.addModifier(modifiersList, "attributeslib:crit_damage", totalBonus, 0, CRIT_DAMAGE_MODIFIER_UUID, "halo_burst");
            }
            itemData.put("CurioAttributeModifiers", modifiersList);
        }
    }

    /**
     * 星野光环技能0实现
     *
     * @param slotContext 装备槽位上下文
     * @param stack       物品堆栈
     */
    public static final void Hoshino_Halo_Skill_0(SlotContext slotContext, ItemStack stack) {
        if (slotContext.entity() instanceof Player player) {
            CompoundTag playerData = player.getPersistentData();
            if (!playerData.contains("SkillData")) return;

            CompoundTag skillData = playerData.getCompound("SkillData");
            if (!skillData.contains("HoshinoHaloData")) return;

            CompoundTag hoshinoHaloData = skillData.getCompound("HoshinoHaloData");

            if (
                    player.level().getGameTime() - hoshinoHaloData.getLong("StartShieldTime") >= 20 * 12 ||
                            hoshinoHaloData.getInt("ExtraDamage") == 0 ||
                            hoshinoHaloData.getFloat("ExtraShieldMultiplier") <= 0
            ) {
                hoshinoHaloData.putBoolean("ExtraDamageIsTrue", false);
            }
        }
    }

    /**
     * 星野光环技能1实现
     *
     * @param slotContext 装备槽位上下文
     * @param stack       物品堆栈
     */
    public static final void Hoshino_Halo_Skill_1(SlotContext slotContext, ItemStack stack) {
        if (slotContext.entity() instanceof Player player) {
            CompoundTag itemData = stack.getOrCreateTag();
            if (!itemData.contains("level")) {
                itemData.putInt("level", 1);
                itemData.putInt("lastExtraLevels", 0);
                itemData.putLong("passiveTime", 0);
                itemData.putLong("passiveEffectTime", 0);
            }

            ListTag modifiersList;
            if (itemData.contains("CurioAttributeModifiers", 9)) { // 9表示LIST TAG
                modifiersList = itemData.getList("CurioAttributeModifiers", 10); // 10表示COMPOUND TAG
            } else {
                modifiersList = new ListTag();
            }

            long passiveTime = itemData.getLong("passiveTime");
            long passiveEffectTime = itemData.getLong("passiveEffectTime");

            if (player.level().getGameTime() - passiveEffectTime >= 20 * 10) {
                OtherHelper.removeExistingModifier(modifiersList, HOSHINO_DAMAGE_MODIFIER_UUID);
                OtherHelper.removeExistingModifier(modifiersList, HOSHINO_ARMOR_MODIFIER_UUID);
            }

            if (player.level().getGameTime() - passiveTime < 20 * 20) {
                return;
            }

            int level = itemData.getInt("level");
            float maxHealth = player.getMaxHealth();
            float health = player.getHealth();
            float healthPercentage = health / maxHealth; // 获取当前生命值百分比
            OtherHelper.removeExistingModifier(modifiersList, HOSHINO_DAMAGE_MODIFIER_UUID);
            OtherHelper.removeExistingModifier(modifiersList, HOSHINO_ARMOR_MODIFIER_UUID);
            float attackDamageValue = OtherHelper.calculate(1.28f, 36.8f, 95, level);
            float armorValue = OtherHelper.calculate(2, 12.8f, 95, level);
            if (healthPercentage < 0.5f) {
                OtherHelper.addModifier(modifiersList, "minecraft:generic.attack_damage", attackDamageValue, 2, HOSHINO_DAMAGE_MODIFIER_UUID, "halo");
                OtherHelper.addModifier(modifiersList, "minecraft:generic.armor", armorValue, 2, HOSHINO_ARMOR_MODIFIER_UUID, "halo");
                long GameTime = player.level().getGameTime();
                itemData.putLong("passiveTime", GameTime);
                itemData.putLong("passiveEffectTime", GameTime);

                MobEffectInstance regenerationEffect = new MobEffectInstance(
                        RegistryEffect.PERCENT_REGENERATION_EFFECT.get(),
                        200,
                        level - 1,
                        false,
                        true,
                        true
                );

                player.addEffect(regenerationEffect);
            }
        }
    }

    /**
     * 星野光环技能2实现
     *
     * @param slotContext 装备槽位上下文
     * @param stack       物品堆栈
     */
    public static final void Hoshino_Halo_Skill_2(SlotContext slotContext, ItemStack stack) {
        CompoundTag itemData = stack.getOrCreateTag();
        if (!itemData.contains("level")) {
            itemData.putInt("level", 1);
            itemData.putInt("lastExtraLevels", 0);
        }
        int level = itemData.getInt("level");
        int lastExtraLevels = itemData.getInt("lastExtraLevels");
        if (level != lastExtraLevels) {
            ListTag modifiersList;
            if (itemData.contains("CurioAttributeModifiers", 9)) { // 9表示LIST TAG
                modifiersList = itemData.getList("CurioAttributeModifiers", 10); // 10表示COMPOUND TAG
            } else {
                modifiersList = new ListTag();
            }

            itemData.putInt("lastExtraLevels", level);
            OtherHelper.removeExistingModifier(modifiersList, DAMAGE_MODIFIER_UUID);
            OtherHelper.removeExistingModifier(modifiersList, ARMOR_MODIFIER_UUID);
            OtherHelper.removeExistingModifier(modifiersList, MAX_HEALTH_UUID);
            OtherHelper.addModifier(modifiersList, "minecraft:generic.attack_damage", level * 0.05, 2, DAMAGE_MODIFIER_UUID, "halo");
            OtherHelper.addModifier(modifiersList, "minecraft:generic.armor", level * 0.1, 2, ARMOR_MODIFIER_UUID, "halo");
            OtherHelper.addModifier(modifiersList, "minecraft:generic.max_health", level * 0.15, 2, MAX_HEALTH_UUID, "halo");
            itemData.put("CurioAttributeModifiers", modifiersList);
        }
    }

    /**
     * Kluonuoya光环辅助瞄准技能实现（第一个被动技能）
     *
     * @param slotContext 装备槽位上下文
     * @param stack       物品堆栈
     */
    public static final void KLUONUOYA_Halo_Aim_Assist(SlotContext slotContext, ItemStack stack) {
        // 这个方法只是注册被动技能，实际的瞄准辅助逻辑由客户端Mixin实现
        // 用于标识玩家佩戴了kluonuoya_halo光环，激活辅助瞄准功能
        if (slotContext.entity() instanceof Player player) {
            CompoundTag itemData = stack.getOrCreateTag();
            if (!itemData.contains("aimAssistEnabled")) {
                itemData.putBoolean("aimAssistEnabled", true);
            }
        }
    }
    //#region 日奈光环被动

    /**
     * 日奈光环技能0实现
     *
     * @param slotContext 装备槽位上下文
     * @param stack       物品堆栈
     */
    public static final void Hina_Halo_Skill_0(SlotContext slotContext, ItemStack stack) {
        if (slotContext.entity() instanceof Player player) {
            // 数据获取
            CompoundTag playerData = player.getPersistentData();
            if (!playerData.contains("SkillData")) return;
            CompoundTag skillData = playerData.getCompound("SkillData");
            if (!skillData.contains("HinaHaloData")) return;
            CompoundTag HinaHaloData = skillData.getCompound("HinaHaloData");

            boolean skillCompleted = HinaHaloData.getBoolean("SkillCompleted");

            if (skillCompleted) {
                // 恢复移动速度
                AttributeInstance movementAttribute = player.getAttribute(Attributes.MOVEMENT_SPEED);
                if (movementAttribute != null && HinaHaloData.contains("BaseMovementSpeed")) {
                    movementAttribute.setBaseValue(HinaHaloData.getDouble("BaseMovementSpeed"));
                    player.onUpdateAbilities();
                    if (!player.level().isClientSide() && player instanceof ServerPlayer serverPlayer)
                        serverPlayer.onUpdateAbilities();

                    // 调试信息
                    player.sendSystemMessage(Component.literal("§e日奈光环§r: §a已恢复移动能力！")
                            .withStyle(ChatFormatting.GREEN));
                }
                // 清理技能数据
                HinaHaloData.remove("ExtraDamageIsTrue");
                HinaHaloData.remove("DamageCount");
                HinaHaloData.remove("SkillCompleted");
                HinaHaloData.remove("BaseMovementSpeed");
            }
        }
    }

    /**
     * 日奈光环技能2实现
     *
     * @param slotContext 装备槽位上下文
     * @param stack       物品堆栈
     */
    public static final void Hina_Halo_Skill_2(SlotContext slotContext, ItemStack stack) {
        if (!(slotContext.entity() instanceof Player player)) return;

        CompoundTag itemData = stack.getOrCreateTag();
        // 初始化
        ListTag modifiersList;
        if (itemData.contains("CurioAttributeModifiers", 9)) { // 9表示LIST TAG
            modifiersList = itemData.getList("CurioAttributeModifiers", 10); // 10表示COMPOUND TAG
        } else {
            modifiersList = new ListTag();
        }

        CompoundTag playerData = player.getPersistentData();
        if (!playerData.contains("SkillData")) {
            OtherHelper.removeExistingModifier(modifiersList, HINA_CRIT_CHANCE_MODIFIER_UUID_1);
            return;
        }
        ;
        CompoundTag skillData = playerData.getCompound("SkillData");
        if (!skillData.contains("HinaHaloData")) {
            OtherHelper.removeExistingModifier(modifiersList, HINA_CRIT_CHANCE_MODIFIER_UUID_1);
            return;
        }
        ;
        CompoundTag hinaHaloData = skillData.getCompound("HinaHaloData");
        Boolean passive2Active = hinaHaloData.getBoolean("Passive2Active");
        Boolean passive2ActiveCC = hinaHaloData.getBoolean("Passive2ActiveCC");

        if (passive2Active) {
            if (passive2ActiveCC) return;
            OtherHelper.addModifier(modifiersList, "attributeslib:crit_chance", 4.41, 2, HINA_CRIT_CHANCE_MODIFIER_UUID_1, "halo");
            itemData.put("CurioAttributeModifiers", modifiersList);
            hinaHaloData.putBoolean("Passive2ActiveCC", true);
        } else {
            OtherHelper.removeExistingModifier(modifiersList, HINA_CRIT_CHANCE_MODIFIER_UUID_1);
        }
        ;

    }

    /**
     * 日奈光环技能3.5实现
     *
     * @param slotContext 装备槽位上下文
     * @param stack       物品堆栈
     */
    public static final void Hina_Halo_Skill_3_5(SlotContext slotContext, ItemStack stack) {
        if (!(slotContext.entity() instanceof Player player)) return;

        CompoundTag itemData = stack.getOrCreateTag();
        // 初始化
        if (!itemData.contains("level")) {
            itemData.putInt("level", 1);
            itemData.putInt("lastExtraLevels", 0);
        }
        int level = itemData.getInt("level");
        int lastExtraLevels = itemData.getInt("lastExtraLevels");

        ListTag modifiersList;
        if (itemData.contains("CurioAttributeModifiers", 9)) { // 9表示LIST TAG
            modifiersList = itemData.getList("CurioAttributeModifiers", 10); // 10表示COMPOUND TAG
        } else {
            modifiersList = new ListTag();
        }

        CompoundTag playerData = player.getPersistentData();
        if (!playerData.contains("SkillData")) {
            OtherHelper.removeExistingModifier(modifiersList, HINA_DAMAGE_MODIFIER_UUID_1);
            OtherHelper.removeExistingModifier(modifiersList, HINA_DAMAGE_MODIFIER_UUID_2);
            OtherHelper.removeExistingModifier(modifiersList, HINA_CRIT_DAMAGE_MODIFIER_UUID_1);
            return;
        }
        ;
        CompoundTag skillData = playerData.getCompound("SkillData");
        if (!skillData.contains("HinaHaloData")) {
            OtherHelper.removeExistingModifier(modifiersList, HINA_DAMAGE_MODIFIER_UUID_1);
            OtherHelper.removeExistingModifier(modifiersList, HINA_DAMAGE_MODIFIER_UUID_2);
            OtherHelper.removeExistingModifier(modifiersList, HINA_CRIT_DAMAGE_MODIFIER_UUID_1);
            return;
        }
        ;
        CompoundTag hinaHaloData = skillData.getCompound("HinaHaloData");
        Boolean passive3Active = hinaHaloData.getBoolean("Passive3Active");

        // 被动3逻辑
        float damageAdder = OtherHelper.calculate(50.6f, 1288, 95, level);
        float damageMultiplier = OtherHelper.calculate(0.042f, 0.38f, 95, level);
        float critDamageMultiplier = OtherHelper.calculate(2, 8, 95, level);
        if (passive3Active) {
            if (level != lastExtraLevels) {
                OtherHelper.removeExistingModifier(modifiersList, HINA_DAMAGE_MODIFIER_UUID_1);
                OtherHelper.removeExistingModifier(modifiersList, HINA_DAMAGE_MODIFIER_UUID_2);
                OtherHelper.removeExistingModifier(modifiersList, HINA_CRIT_DAMAGE_MODIFIER_UUID_1);
                OtherHelper.addModifier(modifiersList, "minecraft:generic.attack_damage", damageAdder, 0, HINA_DAMAGE_MODIFIER_UUID_1, "halo");
                OtherHelper.addModifier(modifiersList, "minecraft:generic.attack_damage", damageMultiplier, 2, HINA_DAMAGE_MODIFIER_UUID_2, "halo");
                OtherHelper.addModifier(modifiersList, "attributeslib:crit_damage", critDamageMultiplier, 2, HINA_CRIT_DAMAGE_MODIFIER_UUID_1, "halo");
                itemData.put("CurioAttributeModifiers", modifiersList);
            }
        } else {
            OtherHelper.removeExistingModifier(modifiersList, HINA_DAMAGE_MODIFIER_UUID_1);
            OtherHelper.removeExistingModifier(modifiersList, HINA_DAMAGE_MODIFIER_UUID_2);
            OtherHelper.removeExistingModifier(modifiersList, HINA_CRIT_DAMAGE_MODIFIER_UUID_1);
        }
        ;

        // 被动5逻辑
        if (HaloLevelManager.checkAndUpdateLevelChange(stack)) {

            OtherHelper.removeExistingModifier(modifiersList, HINA_CRIT_CHANCE_MODIFIER_UUID_2);
            OtherHelper.removeExistingModifier(modifiersList, HINA_CRIT_DAMAGE_MODIFIER_UUID_2);
            OtherHelper.removeExistingModifier(modifiersList, HINA_DAMAGE_MODIFIER_UUID_3);
            OtherHelper.removeExistingModifier(modifiersList, HINA_DAMAGE_MODIFIER_UUID_4);
            OtherHelper.removeExistingModifier(modifiersList, HINA_ARMOR_MODIFIER_UUID);
            OtherHelper.addModifier(modifiersList, "attributeslib:crit_chance", level * 0.1, 2, HINA_CRIT_CHANCE_MODIFIER_UUID_2, "halo");
            OtherHelper.addModifier(modifiersList, "attributeslib:crit_damage", level * 0.08, 2, HINA_CRIT_DAMAGE_MODIFIER_UUID_2, "halo");
            OtherHelper.addModifier(modifiersList, "minecraft:generic.attack_damage", level * 0.04, 2, HINA_DAMAGE_MODIFIER_UUID_3, "halo");
            OtherHelper.addModifier(modifiersList, "minecraft:generic.attack_damage", level * 10, 0, HINA_DAMAGE_MODIFIER_UUID_4, "halo");
            OtherHelper.addModifier(modifiersList, "minecraft:generic.armor", level, 0, HINA_ARMOR_MODIFIER_UUID, "halo");
            itemData.put("CurioAttributeModifiers", modifiersList);
        }
    }
    //#endregion


    //#region Blessed Encounter

    /**
     * Blessed Encounter技能实现
     *
     * @param slotContext 装备槽位上下文
     * @param stack       物品堆栈
     */
    public static final void Blessed_Encounter_Skill(SlotContext slotContext, ItemStack stack) {
        LivingEntity entity = slotContext.entity();
        if (!entity.level().isClientSide() && entity instanceof Player player) {
            CompoundTag tag = stack.getOrCreateTag();
            int lastExtraLevels = tag.getInt("LastExtraLevels");
            int lastLevel = tag.getInt("LastLevel");

            int experienceLevel = player.experienceLevel;
            int extraLevels = Math.max(0, experienceLevel - 520);
            if (experienceLevel != lastLevel || extraLevels != lastExtraLevels) {
                var attributes = player.getAttributes();
                AttributeInstance luckAttribute = attributes.getInstance(Attributes.LUCK);

                if (luckAttribute != null) {
                    ListTag modifiersList;
                    if (tag.contains("CurioAttributeModifiers", 9)) { // 9表示LIST TAG
                        modifiersList = tag.getList("CurioAttributeModifiers", 10); // 10表示COMPOUND TAG
                    } else {
                        modifiersList = new ListTag();
                    }

                    OtherHelper.removeExistingModifier(modifiersList, LUCK_MODIFIER_UUID); // 移除旧的幸运值修改器
                    OtherHelper.addModifier(modifiersList, "minecraft:generic.luck", extraLevels, 0, LUCK_MODIFIER_UUID, "ring");

                    tag.put("CurioAttributeModifiers", modifiersList);

                    // 更新NBT中的存储值
                    tag.putInt("LastExtraLevels", extraLevels);
                    tag.putInt("LastLevel", experienceLevel);
                }
            }
        }
    }
    //#endregion

    //#region Farewell技能相关

    /**
     * Farewell技能实现
     *
     * @param slotContext 装备槽位上下文
     * @param stack       物品堆栈
     */
    public static final void Farewell_Skill(SlotContext slotContext, ItemStack stack) {
        LivingEntity entity = slotContext.entity();
        if (!entity.level().isClientSide() && entity instanceof Player player) {
            CompoundTag tag = stack.getOrCreateTag();
            MinecraftServer server = player.getServer();
            int playerCount = server != null ? server.getPlayerCount() : 1;
            Boolean isTrue = false;
            try {
                Path configPath = Paths.get("usercache.json");
                File file = configPath.toFile();
                JsonArray currentData = JsonParser.parseString(Files.readString(file.toPath())).getAsJsonArray();
                isTrue = currentData.size() > 1;
            } catch (Exception e) {
                isTrue = false;
            }
            if (playerCount == 1 && isTrue) {
                double damageBonus = tag.getDouble("DamageBonus");
                if (player.level().getGameTime() % 20 == 0) {
                    damageBonus += 0.00001;
                    tag.putDouble("DamageBonus", damageBonus);
                    stack.getAttributeModifiers(EquipmentSlot.MAINHAND);

                    // applyDamageBonus(player, damageBonus);
                    updateItemAttributeModifier(stack, damageBonus);
                    if (player.getRandom().nextFloat() < 0.025f) {
                        OtherHelper.applyRandomDebuff(player);
                    }
                }
            } else {
                ListTag modifiersList;
                if (tag.contains("CurioAttributeModifiers", 9)) { // 9表示LIST TAG
                    modifiersList = tag.getList("CurioAttributeModifiers", 10); // 10表示COMPOUND TAG
                } else {
                    modifiersList = new ListTag();
                }
                OtherHelper.removeExistingModifier(modifiersList, ATTACK_MODIFIER_UUID);
            }
        }
    }

    /**
     * 更新物品属性修饰符
     *
     * @param stack       物品堆栈
     * @param damageBonus 伤害加成值
     */
    private static void updateItemAttributeModifier(ItemStack stack, double damageBonus) {
        CompoundTag tag = stack.getOrCreateTag();

        // 创建或获取AttributeModifiers列表
        ListTag modifiersList;
        if (tag.contains("CurioAttributeModifiers", 9)) { // 9表示LIST TAG
            modifiersList = tag.getList("CurioAttributeModifiers", 10); // 10表示COMPOUND TAG
        } else {
            modifiersList = new ListTag();
        }

        // 移除旧的修饰符（如果存在）
        OtherHelper.removeExistingModifier(modifiersList, ATTACK_MODIFIER_UUID);

        // 创建新的属性修饰符
        if (damageBonus > 0) {
            CompoundTag modifierTag = new CompoundTag();
            modifierTag.putString("AttributeName", "minecraft:generic.attack_damage");
            modifierTag.putDouble("Amount", damageBonus);
            modifierTag.putInt("Operation", 2);
            modifierTag.putIntArray("UUID", OtherHelper.uuidToIntArray(ATTACK_MODIFIER_UUID));
            modifierTag.putString("Slot", "ring"); // 使用curio槽位

            modifiersList.add(modifierTag);
        }

        // 将更新后的列表放回NBT
        tag.put("CurioAttributeModifiers", modifiersList);

        // 设置HideFlags以隐藏属性修饰符信息（可选）
        tag.putInt("HideFlags", tag.getInt("HideFlags") | 2); // 2表示隐藏属性修饰符信息
    }
    //#endregion
}