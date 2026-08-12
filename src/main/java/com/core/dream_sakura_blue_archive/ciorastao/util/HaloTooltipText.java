package com.core.dream_sakura_blue_archive.ciorastao.util;

import net.minecraft.locale.Language;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import com.core.dream_sakura_blue_archive.ciorastao.halo.HaloCatalog;

import java.util.List;

public final class HaloTooltipText {
    private static final String PREFIX = "tooltip.dream_sakura.";
    public static final int MAX_LINE_COUNT = 20;

    private HaloTooltipText() {
    }

    public static void addCharacterTooltip(String itemId, List<Component> tooltip) {
        HaloCatalog.Entry entry = HaloCatalog.get(HaloVariantHelper.baseItemId(itemId));
        int firstAddedLine = tooltip.size();
        addTranslated(tooltip, PREFIX + itemId + ".base");
        addSequentialLines(tooltip, PREFIX + itemId + ".describe.line");
        if (entry != null && !entry.existing()) {
            Style style = Style.EMPTY.withColor(entry.primaryColor());
            for (int index = firstAddedLine; index < tooltip.size(); index++) {
                tooltip.set(index, tooltip.get(index).copy().withStyle(style));
            }
        }
    }

    public static void addSkillTooltip(String itemId, List<Component> tooltip) {
        addSequentialLines(tooltip, PREFIX + itemId + ".effect.line");
    }

    public static void addPrompt(String itemId, String translationKey, List<Component> tooltip) {
        String keyName = translationKey.endsWith("shift_prompt") ? "[SHIFT]" : "[CTRL]";
        tooltip.add(Component.translatable(
                translationKey,
                Component.literal(keyName).withStyle(ChatFormatting.GOLD)
        ).withStyle(getItemNameStyle(itemId)));
    }

    public static void addSkillKey(String itemId, String skillDescriptionId, Component keyName, List<Component> tooltip) {
        String key = PREFIX + skillDescriptionId + ".key";
        if (Language.getInstance().has(key)) {
            tooltip.add(Component.translatable(
                    key,
                    keyName.copy().withStyle(ChatFormatting.GOLD)
            ).withStyle(getItemNameStyle(itemId)));
        }
    }

    private static Style getItemNameStyle(String itemId) {
        HaloCatalog.Entry entry = HaloCatalog.get(HaloVariantHelper.baseItemId(itemId));
        if (entry != null && !entry.existing()) {
            return Style.EMPTY.withColor(entry.primaryColor());
        }
        String key = "item.dream_sakura_blue_archive." + itemId;
        String text = Language.getInstance().getOrDefault(key);
        int marker = text.indexOf('\u00A7');
        if (marker >= 0 && marker + 1 < text.length()) {
            ChatFormatting formatting = ChatFormatting.getByCode(text.charAt(marker + 1));
            if (formatting != null && formatting.isColor()) {
                return Style.EMPTY.applyFormat(formatting);
            }
        }
        return Style.EMPTY.applyFormat(ChatFormatting.GRAY);
    }

    private static void addSequentialLines(List<Component> tooltip, String keyPrefix) {
        for (int i = 1; i <= MAX_LINE_COUNT; i++) {
            addTranslated(tooltip, keyPrefix + i);
        }
    }

    private static void addTranslated(List<Component> tooltip, String key) {
        Language language = Language.getInstance();
        if (language.has(key)) {
            String text = language.getOrDefault(key);
            if (!text.equals(key) && !isPlaceholder(text)) {
                tooltip.add(Component.translatable(key));
            }
        }
    }

    private static boolean isPlaceholder(String text) {
        return text.contains("请输入文本") || text.contains("Enter text") || text.contains("テキストを入力");
    }
}
