package com.core.dream_sakura_blue_archive.ciorastao.util;

import com.core.dream_sakura.api.tooltip.DreamSakuraTooltipAPI;
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
                if (sameBase(fallbackItemId, requested)) {
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

    public static java.util.Collection<String> allVariantIds() {
        return BASE_BY_VARIANT.keySet();
    }

    public static void registerVariantTooltips(String baseItemId, DreamSakuraTooltipAPI.DreamSakuraTextureConfig baseConfig) {
        for (Map.Entry<String, String> entry : BASE_BY_VARIANT.entrySet()) {
            if (!baseItemId.equals(entry.getValue())) {
                continue;
            }
            String variantId = entry.getKey();
            int[] size = portraitSize(variantId);
            DreamSakuraTooltipAPI.registerHaloTooltip(variantId, copyWithPortrait(baseConfig, variantId, size[0], size[1]));
        }
    }

    private static int[] portraitSize(String variantId) {
        return switch (variantId) {
            case "shiroko_cycling_halo" -> new int[]{764, 1019};
            case "shiroko_swimsuit_halo" -> new int[]{547, 1525};
            case "hoshino_swimsuit_halo" -> new int[]{866, 1092};
            case "ayane_swimsuit_halo" -> new int[]{1094, 1319};
            case "hina_dress_halo" -> new int[]{973, 1298};
            case "hina_swimsuit_halo" -> new int[]{692, 1111};
            case "tendouaris_battle_halo" -> new int[]{1105, 1300};
            case "tendouaris_maid_halo" -> new int[]{770, 1190};
            case "shirasuazusa_swimsuit_halo" -> new int[]{978, 1196};
            case "kayoko_newyear_halo" -> new int[]{331, 1291};
            case "yuzu_battle_halo" -> new int[]{591, 1300};
            case "yuzu_maid_halo" -> new int[]{648, 1080};
            case "natsu_band_halo" -> new int[]{432, 1239};
            case "mari_idol_halo" -> new int[]{514, 1310};
            case "mari_gym_halo" -> new int[]{842, 1303};
            case "seia_swimsuit_halo" -> new int[]{656, 1400};
            case "midori_maid_halo" -> new int[]{704, 1058};
            case "momoi_maid_halo" -> new int[]{877, 1236};
            default -> new int[]{64, 64};
        };
    }

    private static DreamSakuraTooltipAPI.DreamSakuraTextureConfig copyWithPortrait(
            DreamSakuraTooltipAPI.DreamSakuraTextureConfig config, String variantId, int width, int height) {
        String portraitId = variantId.substring(0, variantId.length() - "_halo".length());
        return new DreamSakuraTooltipAPI.DreamSakuraTextureConfig(
                config.maxWidth, config.maxHeight, config.floatAmplitude, config.floatPeriod,
                config.backgroundStart, config.backgroundEnd, config.borderStart, config.borderEnd,
                config.mainTextureOffsetX, config.mainTextureOffsetY, config.enableFoxBladeEffect,
                new String[]{"dream_sakura_blue_archive:textures/screens/" + portraitId + ".png"},
                new int[]{width}, new int[]{height}, config.foxBladeMaxWidth, config.foxBladeMaxHeight,
                config.foxBladeCenterOffsetX, config.foxBladeCenterOffsetY, config.foxBladeOrbitRadius,
                config.foxBladeAlpha, config.foxBladeRotationSpeed, config.foxBladeLayerCount,
                config.textureSwapInterval, config.swapWithMainTexture, config.foxBladeUseIndependentTextures
        );
    }

    private HaloVariantHelper() {
    }
}
