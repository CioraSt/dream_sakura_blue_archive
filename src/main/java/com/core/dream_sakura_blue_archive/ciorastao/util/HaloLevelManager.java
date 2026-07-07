package com.core.dream_sakura_blue_archive.ciorastao.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

/**
 * 光环等级管理器
 * 用于统一管理光环物品的等级系统
 */
public class HaloLevelManager {
    public static final int MAX_LEVEL = 100;
    public static final int DEFAULT_MAX_XP = 1024;

    /**
     * 获取光环等级
     *
     * @param stack 光环物品堆栈
     * @return 光环等级，默认为1
     */
    public static int getHaloLevel(ItemStack stack) {
        if (stack.isEmpty()) {
            return 1;
        }

        CompoundTag tag = stack.getOrCreateTag();
        if (!tag.contains("level")) {
            // 初始化等级为1
            setHaloLevel(stack, 1);
            return 1;
        }

        return tag.getInt("level");
    }

    /**
     * 设置光环等级
     *
     * @param stack 光环物品堆栈
     * @param level 要设置的等级
     */
    public static void setHaloLevel(ItemStack stack, int level) {
        if (stack.isEmpty()) {
            return;
        }

        CompoundTag tag = stack.getOrCreateTag();
        tag.putInt("level", Math.max(1, Math.min(MAX_LEVEL, level)));
        stack.setTag(tag);
    }

    /**
     * 提升光环等级
     *
     * @param stack     光环物品堆栈
     * @param increment 提升的等级数
     * @return 提升后的等级
     */
    public static int increaseHaloLevel(ItemStack stack, int increment) {
        int currentLevel = getHaloLevel(stack);
        int newLevel = currentLevel + increment;
        setHaloLevel(stack, newLevel);
        return newLevel;
    }

    /**
     * 获取光环经验值
     *
     * @param stack 光环物品堆栈
     * @return 光环经验值，默认为0
     */
    public static int getHaloXP(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }

        CompoundTag tag = stack.getOrCreateTag();
        if (!tag.contains("xp")) {
            // 初始化经验值为0
            setHaloXP(stack, 0);
            return 0;
        }

        return tag.getInt("xp");
    }

    /**
     * 设置光环经验值
     *
     * @param stack 光环物品堆栈
     * @param xp    要设置的经验值
     */
    public static void setHaloXP(ItemStack stack, int xp) {
        if (stack.isEmpty()) {
            return;
        }

        CompoundTag tag = stack.getOrCreateTag();
        tag.putInt("xp", xp);
        stack.setTag(tag);
    }

    /**
     * 获取光环所需升级经验
     *
     * @param stack 光环物品堆栈
     * @return 升级所需的经验值
     */
    public static int getMaxHaloXP(ItemStack stack) {
        if (stack.isEmpty()) {
            return DEFAULT_MAX_XP;
        }

        CompoundTag tag = stack.getOrCreateTag();
        if (!tag.contains("maxXp")) {
            // 初始化最大经验值为1024
            setMaxHaloXP(stack, DEFAULT_MAX_XP);
            return DEFAULT_MAX_XP;
        }

        return tag.getInt("maxXp");
    }

    /**
     * 设置光环所需升级经验
     *
     * @param stack 光环物品堆栈
     * @param maxXp 要设置的最大经验值
     */
    public static void setMaxHaloXP(ItemStack stack, int maxXp) {
        if (stack.isEmpty()) {
            return;
        }

        CompoundTag tag = stack.getOrCreateTag();
        tag.putInt("maxXp", maxXp);
        stack.setTag(tag);
    }

    /**
     * 添加光环经验值
     *
     * @param stack 光环物品堆栈
     * @param xp    要添加的经验值
     * @return 是否成功升级
     */
    public static boolean addHaloXP(ItemStack stack, int xp) {
        if (stack.isEmpty() || xp <= 0 || isMaxed(stack)) {
            return false;
        }

        int itemLevel = getHaloLevel(stack);
        int itemXp = getHaloXP(stack);
        int itemMaxXp = getMaxHaloXP(stack);
        int newXp = itemXp + xp;
        boolean leveled = false;

        while (newXp >= itemMaxXp && itemLevel < MAX_LEVEL) {
            newXp -= itemMaxXp;
            itemLevel++;
            itemMaxXp = (int) OtherHelper.expCalculate(itemLevel);
            leveled = true;
        }

        if (itemLevel >= MAX_LEVEL) {
            itemLevel = MAX_LEVEL;
            newXp = Math.min(newXp, itemMaxXp);
        } else if (newXp > itemMaxXp) {
            newXp = itemMaxXp;
        }

        setHaloLevel(stack, itemLevel);
        setHaloXP(stack, newXp);
        setMaxHaloXP(stack, itemMaxXp);
        return leveled;
    }

    public static boolean isMaxed(ItemStack stack) {
        return getHaloLevel(stack) >= MAX_LEVEL && getHaloXP(stack) >= getMaxHaloXP(stack);
    }

    /**
     * 获取上次记录的等级（用于比较是否需要更新属性）
     *
     * @param stack 光环物品堆栈
     * @return 上次记录的等级，默认为0
     */
    public static int getLastRecordedLevel(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }

        CompoundTag tag = stack.getOrCreateTag();
        if (!tag.contains("lastExtraLevels")) {
            setLastRecordedLevel(stack, 0);
            return 0;
        }

        return tag.getInt("lastExtraLevels");
    }

    /**
     * 设置上次记录的等级
     *
     * @param stack 光环物品堆栈
     * @param level 要设置的等级
     */
    public static void setLastRecordedLevel(ItemStack stack, int level) {
        if (stack.isEmpty()) {
            return;
        }

        CompoundTag tag = stack.getOrCreateTag();
        tag.putInt("lastExtraLevels", level);
        stack.setTag(tag);
    }

    /**
     * 检查光环等级是否已更改，并更新记录的等级
     *
     * @param stack 光环物品堆栈
     * @return 如果等级已更改则返回true，否则返回false
     */
    public static boolean checkAndUpdateLevelChange(ItemStack stack) {
        int currentLevel = getHaloLevel(stack);
        int lastRecordedLevel = getLastRecordedLevel(stack);

        if (currentLevel != lastRecordedLevel) {
            setLastRecordedLevel(stack, currentLevel);
            return true;
        }

        return false;
    }
}
