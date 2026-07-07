package com.core.dream_sakura_blue_archive.ciorastao.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Map;

public final class HaloVariantHelper {
    public static final String HALO_ID_TAG = "DBAHaloId";

    private static final Map<String, String> BASE_BY_VARIANT = Map.ofEntries(
            Map.entry("shiroko_cycling_halo", "shiroko_halo"),
            Map.entry("shiroko_swimsuit_halo", "shiroko_halo"),
            Map.entry("hoshino_swimsuit_halo", "hoshino_halo"),
            Map.entry("ayane_swimsuit_halo", "ayaneko_halo"),
            Map.entry("hina_dress_halo", "hina_halo"),
            Map.entry("hina_swimsuit_halo", "hina_halo"),
            Map.entry("tendouaris_battle_halo", "tendouaris_halo"),
            Map.entry("tendouaris_maid_halo", "tendouaris_halo"),
            Map.entry("shirasuazusa_swimsuit_halo", "shirasuazusa_halo"),
            Map.entry("kayoko_newyear_halo", "kayoko_halo"),
            Map.entry("yuzu_battle_halo", "yuzu_halo"),
            Map.entry("yuzu_maid_halo", "yuzu_halo"),
            Map.entry("natsu_band_halo", "natsu_halo"),
            Map.entry("mari_idol_halo", "mari_halo"),
            Map.entry("mari_gym_halo", "mari_halo"),
            Map.entry("seia_swimsuit_halo", "seia_halo"),
            Map.entry("midori_maid_halo", "midori_halo"),
            Map.entry("momoi_maid_halo", "momoi_halo")
    );

    public static ItemStack createVariantStack(Item item, String effectiveItemId) {
        ItemStack stack = new ItemStack(item);
        String baseItemId = baseItemId(effectiveItemId);
        if (!baseItemId.equals(effectiveItemId)) {
            stack.getOrCreateTag().putString(HALO_ID_TAG, effectiveItemId);
        }
        return stack;
    }

    public static String effectiveItemId(ItemStack stack, String fallbackItemId) {
        if (stack.hasTag()) {
            CompoundTag tag = stack.getTag();
            if (tag != null && tag.contains(HALO_ID_TAG, 8)) {
                String requested = tag.getString(HALO_ID_TAG);
                if (HaloSkillDefinitions.contains(requested) && sameBase(fallbackItemId, requested)) {
                    return requested;
                }
            }
        }
        return fallbackItemId;
    }

    public static String baseItemId(String itemId) {
        return BASE_BY_VARIANT.getOrDefault(itemId, itemId);
    }

    public static boolean isVariant(String itemId) {
        return BASE_BY_VARIANT.containsKey(itemId);
    }

    private static boolean sameBase(String fallbackItemId, String requestedItemId) {
        return baseItemId(fallbackItemId).equals(baseItemId(requestedItemId));
    }

    private HaloVariantHelper() {
    }
}
