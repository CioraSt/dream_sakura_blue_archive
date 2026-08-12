package com.core.dream_sakura_blue_archive.ciorastao.halo;

import com.core.dream_sakura_blue_archive.ciorastao.util.HaloLevelManager;
import com.core.dream_sakura_blue_archive.ciorastao.util.OtherHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

/** 根据有效外观一次性重建该物品负责的 Curios 属性。 */
final class HaloAttributes {
    private static final UUID ATTACK_PERCENT = UUID.fromString("4cd3d7dd-87a2-4db0-b916-3212f21a1001");
    private static final UUID ATTACK_FLAT = UUID.fromString("4cd3d7dd-87a2-4db0-b916-3212f21a1002");
    private static final UUID ARMOR_FLAT = UUID.fromString("4cd3d7dd-87a2-4db0-b916-3212f21a1003");
    private static final UUID ARMOR_PERCENT = UUID.fromString("4cd3d7dd-87a2-4db0-b916-3212f21a1004");
    private static final UUID TOUGHNESS = UUID.fromString("4cd3d7dd-87a2-4db0-b916-3212f21a1005");
    private static final UUID ATTACK_SPEED = UUID.fromString("4cd3d7dd-87a2-4db0-b916-3212f21a1006");
    private static final UUID CRIT_DAMAGE = UUID.fromString("4cd3d7dd-87a2-4db0-b916-3212f21a1007");

    static void refresh(Player player, ItemStack stack, String id, boolean force) {
        CompoundTag tag = stack.getOrCreateTag();
        int level = HaloLevelManager.getHaloLevel(stack);
        CompoundTag state = HaloRuntime.state(player);
        int dynamic = dynamicSignature(player, state, tag, id);
        if (!force && level == tag.getInt("DBAAttributeLevel")
                && id.equals(tag.getString("DBAAttributeProfile"))
                && dynamic == tag.getInt("DBAAttributeDynamic")) return;

        tag.putInt("DBAAttributeLevel", level);
        tag.putString("DBAAttributeProfile", id);
        tag.putInt("DBAAttributeDynamic", dynamic);
        ListTag modifiers = tag.contains("CurioAttributeModifiers", 9)
                ? tag.getList("CurioAttributeModifiers", 10) : new ListTag();
        removeManaged(modifiers);

        switch (id) {
            case "tendouaris_halo" -> {
                add(modifiers, "attributeslib:crit_damage", passive(level,
                        .25f,.50f,.75f,1f,1.25f,1.5f,1.75f,2f,2.25f,2.5f), 2, CRIT_DAMAGE);
                add(modifiers, "minecraft:generic.attack_damage", passive(level,
                        .20f,.35f,.50f,.65f,.80f,1f,1.25f,1.5f,1.8f,2f), 2, ATTACK_PERCENT);
                if (player.level().getGameTime() < tag.getLong("DBAAliceBurstEnd")) {
                    add(modifiers, "attributeslib:crit_damage", passive(level,
                            .25f,.50f,.75f,1f,1.5f,2f,3f,4f,5f,6f), 2,
                            UUID.fromString("4cd3d7dd-87a2-4db0-b916-3212f21a1017"));
                }
            }
            case "tendouaris_maid_halo" -> add(modifiers, "attributeslib:crit_damage", passive(level,
                    1f,2f,3f,3.6f,4.2f,5.36f,6f,7.14f,9.12f,10.24f), 2, CRIT_DAMAGE);
            case "hoshino_halo" -> {
                float armor = passive(level,10,20,30,40,50,60,70,80,90,100);
                add(modifiers, "minecraft:generic.armor", armor, 0, ARMOR_FLAT);
                add(modifiers, "minecraft:generic.armor", armor / 100f, 2, ARMOR_PERCENT);
            }
            case "hoshino_swimsuit_halo" -> {
                int tier = OtherHelper.getPassiveSkillLevel(level);
                add(modifiers, "minecraft:generic.armor", new float[]{5,10,15,20,40,50,50,50,50,50}[tier-1], 0, ARMOR_FLAT);
                add(modifiers, "minecraft:generic.attack_damage", new float[]{.10f,.20f,.30f,.40f,.50f,.50f,.50f,.50f,.50f,.50f}[tier-1], 2, ATTACK_PERCENT);
                add(modifiers, "minecraft:generic.armor_toughness", new float[]{0,0,0,0,0,10,20,30,40,50}[tier-1], 0, TOUGHNESS);
            }
            case "hina_halo" -> {
                add(modifiers, "minecraft:generic.attack_speed", passive(level,
                        .15f,.30f,.45f,.60f,.75f,.90f,1.15f,1.30f,1.50f,2f), 2, ATTACK_SPEED);
                if (state.getLong("HinaComboEnd") > player.level().getGameTime()) {
                    add(modifiers, "minecraft:generic.attack_damage", passive(level,
                            .20f,.60f,1.20f,1.80f,2.60f,4f,5f,5.61f,7.20f,8.88f), 2, ATTACK_PERCENT);
                }
            }
            case "hina_swimsuit_halo" -> {
                add(modifiers, "attributeslib:crit_damage", passive(level,
                        .26f,.44f,.66f,.88f,1.10f,1.39f,1.57f,1.79f,1.88f,1.99f), 2, CRIT_DAMAGE);
                int stacks = Math.min(5, state.getInt("HinaSwimsuitStacks"));
                add(modifiers, "minecraft:generic.attack_damage", stacks * passive(level,
                        .19f,.23f,.30f,.35f,.40f,.60f,.83f,.99f,1.20f,1.60f), 2, ATTACK_PERCENT);
            }
            case "hina_dress_halo" -> {
                add(modifiers, "minecraft:generic.attack_damage", passive(level,
                        .69f,.99f,1.20f,1.56f,1.88f,2f,2.80f,3.60f,5f,6.66f), 2, ATTACK_PERCENT);
                add(modifiers, "minecraft:generic.attack_damage", passive(level,
                        3,9,15,24,30,37,43,50,55,70), 0, ATTACK_FLAT);
                add(modifiers, "attributeslib:crit_damage", passive(level,
                        .38f,.54f,.66f,.90f,1.10f,1.35f,1.90f,2.70f,3.25f,4f), 2, CRIT_DAMAGE);
            }
        }
        tag.put("CurioAttributeModifiers", modifiers);
    }

    private static int dynamicSignature(Player player, CompoundTag state, CompoundTag itemTag, String id) {
        return switch (id) {
            case "tendouaris_halo" -> itemTag.getLong("DBAAliceBurstEnd") > player.level().getGameTime() ? 1 : 0;
            case "hina_halo" -> state.getLong("HinaComboEnd") > player.level().getGameTime() ? 1 : 0;
            case "hina_swimsuit_halo" -> state.getInt("HinaSwimsuitStacks");
            default -> 0;
        };
    }

    private static void removeManaged(ListTag list) {
        for (int i = list.size() - 1; i >= 0; i--) {
            CompoundTag entry = list.getCompound(i);
            String slot = entry.getString("Slot");
            if ("halo".equals(slot) || "halo_burst".equals(slot) || "dba_halo".equals(slot)) list.remove(i);
        }
    }

    private static void add(ListTag list, String attribute, double amount, int operation, UUID id) {
        if (amount == 0) return;
        // Curios 的 NBT 修饰符会按槽位标识过滤；这里必须使用真实的 halo 槽名。
        OtherHelper.addModifier(list, attribute, amount, operation, id, "halo");
    }

    private static float passive(int haloLevel, float... values) {
        return OtherHelper.getPassiveValue(haloLevel, values);
    }

    private HaloAttributes() {}
}
