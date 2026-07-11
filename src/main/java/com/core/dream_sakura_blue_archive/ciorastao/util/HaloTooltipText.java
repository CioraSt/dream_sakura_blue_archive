package com.core.dream_sakura_blue_archive.ciorastao.util;

import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;

import java.util.List;

public final class HaloTooltipText {
    private static final String PREFIX = "tooltip.dream_sakura.";
    public static final int MAX_LINE_COUNT = 20;

    private HaloTooltipText() {
    }

    public static void addHaloTooltip(String itemId, List<Component> tooltip) {
        addTranslated(tooltip, PREFIX + itemId + ".base");
        addSequentialLines(tooltip, PREFIX + itemId + ".describe.line");
        addSequentialLines(tooltip, PREFIX + itemId + ".effect.line");
    }

    public static void addSkillKey(String skillDescriptionId, String keyName, List<Component> tooltip) {
        String key = PREFIX + skillDescriptionId + ".key";
        if (Language.getInstance().has(key)) {
            tooltip.add(Component.translatable(key, keyName));
        }
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
            if (!text.equals(key) && !text.contains("请输入文本")) {
                tooltip.add(Component.translatable(key));
            }
        }
    }
}
