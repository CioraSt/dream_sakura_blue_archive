package com.core.dream_sakura_blue_archive.ciorastao.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
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
 */
public class RegistryPassiveSkill {
    public static void tickHalo(SlotContext slotContext, ItemStack stack, String itemId) {
        HaloSkillRuntime.onCurioTick(slotContext, stack, itemId);
    }

    // 通用属性修饰符UUID
    private static final UUID DAMAGE_MODIFIER_UUID      = UUID.fromString("389b669a-4576-4bb2-9e87-ede652fbc7c9");
    private static final UUID CRIT_CHANCE_MODIFIER_UUID  = UUID.fromString("88d03014-7dda-45b7-a7aa-a5add5902374");
    private static final UUID CRIT_DAMAGE_MODIFIER_UUID  = UUID.fromString("3fed0d50-b6b4-4b9f-80b3-c2976e4c735b");
    private static final UUID LUCK_MODIFIER_UUID         = UUID.fromString("389b669a-4576-4bb2-9e08-ede652fbc7c9");
    private static final UUID ATTACK_MODIFIER_UUID       = UUID.fromString("6e2cb19d-471e-4adc-b39a-ed3c3dc29b67");
    private static final UUID ARMOR_MODIFIER_UUID        = UUID.fromString("22ca548e-fb29-4d54-8901-77bd84445b9a");

    // 爱丽丝专用UUID
    private static final UUID ALS_PASSIVE1_CRIT_DMG_UUID = UUID.fromString("a1b2c3d4-1001-4001-8001-000000000101");
    private static final UUID ALS_PASSIVE2_DAMAGE_UUID   = UUID.fromString("a1b2c3d4-1002-4002-8002-000000000102");

    // 星野专用UUID
    private static final UUID HOSHINO_ARMOR_FLAT_UUID    = UUID.fromString("6031e4b6-a956-4b42-9368-743761b1431a");
    private static final UUID HOSHINO_ARMOR_PCT_UUID     = UUID.fromString("554a983b-8ba9-4b4d-8db1-ebdbdbdbf391");
    private static final UUID HOSHINO_SHIELD_UUID        = UUID.fromString("b1c2d3e4-2001-4001-8001-000000000201");

    // 日奈专用UUID
    private static final UUID HINA_ATTACK_SPEED_UUID     = UUID.fromString("f6708c06-583f-4545-867d-48fc676e7326");

    // ============================================================
    // 天童 爱丽丝 (TENDOUARIS)
    // ============================================================

    /** TXT 10档: 25/50/75/100/125/150/175/200/225/250% */
    private static final float[] ALS_PASSIVE1_CRIT_DMG = {
        0.25f, 0.50f, 0.75f, 1.00f, 1.25f, 1.50f, 1.75f, 2.00f, 2.25f, 2.50f
    };
    /** TXT 10档: 20/35/50/65/80/100/125/150/180/200% */
    private static final float[] ALS_PASSIVE2_DAMAGE = {
        0.20f, 0.35f, 0.50f, 0.65f, 0.80f, 1.00f, 1.25f, 1.50f, 1.80f, 2.00f
    };
    /** TXT 10档: 25/50/75/100/150/200/300/400/500/600% */
    private static final float[] ALS_PASSIVE3_CRIT_DMG = {
        0.25f, 0.50f, 0.75f, 1.00f, 1.50f, 2.00f, 3.00f, 4.00f, 5.00f, 6.00f
    };

    /**
     * ALS 被动1：光啊！- 永久+暴击伤害 + 充能系统
     */
    public static void TENDOUARIS_Halo_Skill_1(SlotContext slotContext, ItemStack stack) {
        if (!(slotContext.entity() instanceof Player player) || player.level().isClientSide) return;

        CompoundTag itemData = stack.getOrCreateTag();
        int level = itemData.getInt("level");
        int lastLevel = itemData.getInt("lastPassive1Level");
        int haloLevel = HaloLevelManager.getHaloLevel(stack);

        // 充能逻辑 (每25s=500ticks, 上限2)
        long currentTime = player.level().getGameTime();
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

        // 暴击伤害属性维护
        if (level != lastLevel) {
            itemData.putInt("lastPassive1Level", level);
            ListTag modifiersList = getModifiersList(itemData);
            OtherHelper.removeExistingModifier(modifiersList, ALS_PASSIVE1_CRIT_DMG_UUID);
            float val = OtherHelper.getPassiveValue(haloLevel, ALS_PASSIVE1_CRIT_DMG);
            OtherHelper.addModifier(modifiersList, "attributeslib:crit_damage", val, 2, ALS_PASSIVE1_CRIT_DMG_UUID, "halo");
            itemData.put("CurioAttributeModifiers", modifiersList);
        }
    }

    /**
     * ALS 被动2：强化魔法 - 永久+攻击伤害
     */
    public static void TENDOUARIS_Halo_Skill_2(SlotContext slotContext, ItemStack stack) {
        if (!(slotContext.entity() instanceof Player player) || player.level().isClientSide) return;

        CompoundTag itemData = stack.getOrCreateTag();
        int level = itemData.getInt("level");
        int lastLevel = itemData.getInt("lastPassive2Level");
        int haloLevel = HaloLevelManager.getHaloLevel(stack);

        if (level != lastLevel) {
            itemData.putInt("lastPassive2Level", level);
            ListTag modifiersList = getModifiersList(itemData);
            OtherHelper.removeExistingModifier(modifiersList, ALS_PASSIVE2_DAMAGE_UUID);
            float val = OtherHelper.getPassiveValue(haloLevel, ALS_PASSIVE2_DAMAGE);
            OtherHelper.addModifier(modifiersList, "minecraft:generic.attack_damage", val, 2, ALS_PASSIVE2_DAMAGE_UUID, "halo");
            itemData.put("CurioAttributeModifiers", modifiersList);
        }
    }

    /**
     * ALS 被动3：觉醒吧超新星 - 爆发状态时+暴击伤害
     */
    public static void TENDOUARIS_Halo_Skill_3(SlotContext slotContext, ItemStack stack) {
        if (!(slotContext.entity() instanceof Player player) || player.level().isClientSide) return;

        CompoundTag itemData = stack.getOrCreateTag();
        long currentTime = player.level().getGameTime();
        long burstEndTime = itemData.getLong("BurstEndTime");
        boolean isInBurst = currentTime < burstEndTime;
        boolean lastState = itemData.getBoolean("lastBurstState");
        int haloLevel = HaloLevelManager.getHaloLevel(stack);

        if (isInBurst != lastState) {
            itemData.putBoolean("lastBurstState", isInBurst);
            ListTag modifiersList = getModifiersList(itemData);
            OtherHelper.removeExistingModifier(modifiersList, CRIT_DAMAGE_MODIFIER_UUID);

            if (isInBurst) {
                float val = OtherHelper.getPassiveValue(haloLevel, ALS_PASSIVE3_CRIT_DMG);
                OtherHelper.addModifier(modifiersList, "attributeslib:crit_damage", val, 2, CRIT_DAMAGE_MODIFIER_UUID, "halo_burst");
            }
            itemData.put("CurioAttributeModifiers", modifiersList);
        }
    }

    // ============================================================
    // 小鸟游 星野 (HOSHINO)
    // ============================================================

    /** DBA balanced passive2: armor +5~+50 */
    private static final float[] HOSHINO_P2_ARMOR_FLAT = {
        5f, 8f, 12f, 16f, 21f, 27f, 34f, 40f, 45f, 50f
    };
    /** DBA balanced passive2: armor toughness +1~+10 */
    private static final float[] HOSHINO_P2_ARMOR_TOUGHNESS = {
        1f, 1f, 2f, 2f, 3f, 4f, 5f, 6f, 8f, 10f
    };

    /**
     * 星野被动1：急救 - HP<20% 触发生命恢复, 120s冷却
     */
    public static void Hoshino_Halo_Skill_0(SlotContext slotContext, ItemStack stack) {
        if (!(slotContext.entity() instanceof Player player) || player.level().isClientSide) return;

        CompoundTag itemData = stack.getOrCreateTag();
        if (!itemData.contains("level")) {
            itemData.putInt("level", 1);
            itemData.putInt("lastExtraLevels", 0);
            itemData.putLong("FirstAidCooldownEnd", 0);
        }

        long currentTime = player.level().getGameTime();
        float healthPct = player.getHealth() / player.getMaxHealth();

        // HP<20% 且 冷却已过
        if (healthPct < 0.20f && currentTime >= itemData.getLong("FirstAidCooldownEnd")) {
            int haloLevel = HaloLevelManager.getHaloLevel(stack);
            // 被动等级I~X (0~9)
            int passiveLevel = OtherHelper.getPassiveSkillLevel(haloLevel) - 1;
            // 120s 冷却
            itemData.putLong("FirstAidCooldownEnd", currentTime + 2400);

            // 生命恢复 20s (原版生命恢复效果)
            MobEffectInstance regen = new MobEffectInstance(
                MobEffects.REGENERATION,
                400, // 20s
                passiveLevel,
                false, true, true
            );
            player.addEffect(regen);
        }
    }

    /**
     * 星野被动2：对策委员长 - 永久+护甲值+护甲%
     */
    public static void Hoshino_Halo_Skill_1(SlotContext slotContext, ItemStack stack) {
        if (!(slotContext.entity() instanceof Player player) || player.level().isClientSide) return;

        CompoundTag itemData = stack.getOrCreateTag();
        if (!itemData.contains("level")) {
            itemData.putInt("level", 1);
            itemData.putInt("lastExtraLevels", 0);
        }

        int level = itemData.getInt("level");
        int lastLevel = itemData.getInt("lastP2Level");
        int haloLevel = HaloLevelManager.getHaloLevel(stack);

        if (level != lastLevel) {
            itemData.putInt("lastP2Level", level);
            ListTag modifiersList = getModifiersList(itemData);
            OtherHelper.removeExistingModifier(modifiersList, HOSHINO_ARMOR_FLAT_UUID);
            OtherHelper.removeExistingModifier(modifiersList, HOSHINO_ARMOR_PCT_UUID);

            float armorFlat = OtherHelper.getPassiveValue(haloLevel, HOSHINO_P2_ARMOR_FLAT);
            float armorToughness = OtherHelper.getPassiveValue(haloLevel, HOSHINO_P2_ARMOR_TOUGHNESS);
            OtherHelper.addModifier(modifiersList, "minecraft:generic.armor", armorFlat, 0, HOSHINO_ARMOR_FLAT_UUID, "halo");
            OtherHelper.addModifier(modifiersList, "minecraft:generic.armor_toughness", armorToughness, 0, HOSHINO_ARMOR_PCT_UUID, "halo");
            itemData.put("CurioAttributeModifiers", modifiersList);
        }
    }

    /**
     * 星野被动3：熟练镇压 - 护盾吸收逻辑（由DamageHandler处理）
     * 此方法仅维护NBT结构
     */
    public static void Hoshino_Halo_Skill_2(SlotContext slotContext, ItemStack stack) {
        if (!(slotContext.entity() instanceof Player player) || player.level().isClientSide) return;

        CompoundTag itemData = stack.getOrCreateTag();
        if (!itemData.contains("level")) {
            itemData.putInt("level", 1);
            itemData.putInt("lastExtraLevels", 0);
        }
        // 护盾数据由主动技能写入，此处仅做NBT初始化保障
    }

    // ============================================================
    // 空崎 日奈 (HINA)
    // ============================================================

    /** DBA balanced passive2: attack speed +10%~+35% */
    private static final float[] HINA_P2_ATTACK_SPEED = {
        0.10f, 0.13f, 0.16f, 0.19f, 0.22f, 0.24f, 0.27f, 0.30f, 0.33f, 0.35f
    };

    /**
     * 日奈被动1：重装与毁灭 - 连续攻击+攻击力, 受击取消 (DamageHandler处理)
     */
    public static void Hina_Halo_Skill_0(SlotContext slotContext, ItemStack stack) {
        // 由DamageHandler.HinaHaloHurt和HinaHaloPassive处理
    }

    /**
     * 日奈被动2：冷静点的风纪委员会 - 永久+攻击速度
     */
    public static void Hina_Halo_Skill_2(SlotContext slotContext, ItemStack stack) {
        if (!(slotContext.entity() instanceof Player player) || player.level().isClientSide) return;

        CompoundTag itemData = stack.getOrCreateTag();
        int level = itemData.getInt("level");
        int lastLevel = itemData.getInt("lastP2Level");
        int haloLevel = HaloLevelManager.getHaloLevel(stack);

        if (level != lastLevel) {
            itemData.putInt("lastP2Level", level);
            ListTag modifiersList = getModifiersList(itemData);
            OtherHelper.removeExistingModifier(modifiersList, HINA_ATTACK_SPEED_UUID);
            float val = OtherHelper.getPassiveValue(haloLevel, HINA_P2_ATTACK_SPEED);
            OtherHelper.addModifier(modifiersList, "minecraft:generic.attack_speed", val, 2, HINA_ATTACK_SPEED_UUID, "halo");
            itemData.put("CurioAttributeModifiers", modifiersList);
        }
    }

    /**
     * 日奈被动3：彻头彻尾 - 对无护甲目标额外伤害 (DamageHandler处理)
     */
    public static void Hina_Halo_Skill_3(SlotContext slotContext, ItemStack stack) {
        // 由DamageHandler在LivingHurtEvent中检测目标护甲并附加伤害
    }

    // ============================================================
    // Kluonuoya 辅助瞄准
    // ============================================================

    public static void KLUONUOYA_Halo_Aim_Assist(SlotContext slotContext, ItemStack stack) {
        if (slotContext.entity() instanceof Player player) {
            CompoundTag itemData = stack.getOrCreateTag();
            if (!itemData.contains("aimAssistEnabled")) {
                itemData.putBoolean("aimAssistEnabled", true);
            }
        }
    }

    // ============================================================
    // Blessed Encounter
    // ============================================================

    public static void Blessed_Encounter_Skill(SlotContext slotContext, ItemStack stack) {
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
                    ListTag modifiersList = getModifiersList(tag);
                    OtherHelper.removeExistingModifier(modifiersList, LUCK_MODIFIER_UUID);
                    OtherHelper.addModifier(modifiersList, "minecraft:generic.luck", extraLevels, 0, LUCK_MODIFIER_UUID, "ring");
                    tag.put("CurioAttributeModifiers", modifiersList);
                    tag.putInt("LastExtraLevels", extraLevels);
                    tag.putInt("LastLevel", experienceLevel);
                }
            }
        }
    }

    // ============================================================
    // Farewell
    // ============================================================

    public static void Farewell_Skill(SlotContext slotContext, ItemStack stack) {
        LivingEntity entity = slotContext.entity();
        if (!entity.level().isClientSide() && entity instanceof Player player) {
            CompoundTag tag = stack.getOrCreateTag();
            MinecraftServer server = player.getServer();
            int playerCount = server != null ? server.getPlayerCount() : 1;
            boolean isTrue = false;
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
                    updateItemAttributeModifier(stack, damageBonus);
                    if (player.getRandom().nextFloat() < 0.025f) {
                        OtherHelper.applyRandomDebuff(player);
                    }
                }
            } else {
                ListTag modifiersList = getModifiersList(tag);
                OtherHelper.removeExistingModifier(modifiersList, ATTACK_MODIFIER_UUID);
            }
        }
    }

    private static void updateItemAttributeModifier(ItemStack stack, double damageBonus) {
        CompoundTag tag = stack.getOrCreateTag();
        ListTag modifiersList = getModifiersList(tag);
        OtherHelper.removeExistingModifier(modifiersList, ATTACK_MODIFIER_UUID);
        if (damageBonus > 0) {
            CompoundTag modifierTag = new CompoundTag();
            modifierTag.putString("AttributeName", "minecraft:generic.attack_damage");
            modifierTag.putDouble("Amount", damageBonus);
            modifierTag.putInt("Operation", 2);
            modifierTag.putIntArray("UUID", OtherHelper.uuidToIntArray(ATTACK_MODIFIER_UUID));
            modifierTag.putString("Slot", "ring");
            modifiersList.add(modifierTag);
        }
        tag.put("CurioAttributeModifiers", modifiersList);
        tag.putInt("HideFlags", tag.getInt("HideFlags") | 2);
    }

    // ============================================================
    // 工具方法
    // ============================================================

    private static ListTag getModifiersList(CompoundTag tag) {
        if (tag.contains("CurioAttributeModifiers", 9)) {
            return tag.getList("CurioAttributeModifiers", 10);
        }
        return new ListTag();
    }
}
