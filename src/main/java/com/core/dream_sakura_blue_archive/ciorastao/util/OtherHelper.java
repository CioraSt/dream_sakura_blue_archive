package com.core.dream_sakura_blue_archive.ciorastao.util;

import com.core.dream_sakura_blue_archive.ciorastao.items.DecorationItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

public class OtherHelper {
    private static final String TOUHOU_MAID_CLASS = "com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid";

    // 计算数值(线性)
    public static float calculate(float minValue, float maxValue, int maxLevel, int level) {
        float basicValue = minValue;
        float coefficient = (maxValue - minValue) / (maxLevel - 1);
        return calculate(basicValue, coefficient, level);
    }

    public static float calculate(float basicValue, float coefficient, int level) {
        return coefficient * (level - 1) + basicValue;
    }

    public static float expCalculate(int level) {
        float yOffset = 924.0f;
        float base = 0.95f;
        float molecule = 100.0f;

        float denominator = (float) Math.pow(base, level - 1);
        return molecule / denominator + yOffset;
    }

    /**
     * 将光环等级(1~100)映射到TXT设定的10档被动技能等级(1~10)
     * 映射规则：1-10→1, 11-20→2, ..., 91-100→10
     */
    public static int getPassiveSkillLevel(int haloLevel) {
        int skillLevel = (haloLevel - 1) / 10 + 1;
        return Math.min(skillLevel, 10);
    }

    /**
     * 从10档数值数组中获取当前光环等级对应的值
     * @param haloLevel 光环等级(1~100)
     * @param values 10档数值数组(索引0=1级, 9=MAX)
     */
    public static float getPassiveValue(int haloLevel, float[] values) {
        int skillLevel = getPassiveSkillLevel(haloLevel);
        return values[skillLevel - 1];
    }

    /**
     * 将光环等级(1~100)映射到TXT设定的5档主动技能等级(1~5/MAX)
     * 映射规则：1-15→1, 16-30→2, 31-45→3, 46-60→4, 61+→MAX
     */
    public static int getActiveSkillLevel(int haloLevel) {
        if (haloLevel >= 61) return 5;
        if (haloLevel >= 46) return 4;
        if (haloLevel >= 31) return 3;
        if (haloLevel >= 16) return 2;
        return 1;
    }

    /**
     * 从5档数值数组中获取当前光环等级对应的值
     * @param haloLevel 光环等级(1~100)
     * @param values 5档数值数组(索引0=1级, 4=MAX)
     */
    public static float getActiveValue(int haloLevel, float[] values) {
        int skillLevel = getActiveSkillLevel(haloLevel);
        return values[skillLevel - 1];
    }

    // 添加修饰符
    public static void addModifier(
            ListTag modifiersList,
            String attributeName,
            double amount,
            int operation,
            UUID uuid,
            String slot
    ) {
        CompoundTag modifierTag = new CompoundTag();
        modifierTag.putString("AttributeName", attributeName);
        modifierTag.putDouble("Amount", amount);
        modifierTag.putInt("Operation", operation);
        modifierTag.putIntArray("UUID", OtherHelper.uuidToIntArray(uuid));
        modifierTag.putString("Slot", slot);
        modifiersList.add(modifierTag);
    }

    // 移除已存在的修饰符
    public static void removeExistingModifier(ListTag modifiersList, UUID uuidToRemove) {
        int[] uuidArray = uuidToIntArray(uuidToRemove);

        for (int i = 0; i < modifiersList.size(); i++) {
            CompoundTag modifierTag = modifiersList.getCompound(i);
            if (modifierTag.contains("UUID") &&
                    arraysEqual(modifierTag.getIntArray("UUID"), uuidArray)) {
                modifiersList.remove(i);
                break;
            }
        }
    }

    // 将UUID转换为int数组
    public static int[] uuidToIntArray(UUID uuid) {
        long mostSignificant = uuid.getMostSignificantBits();
        long leastSignificant = uuid.getLeastSignificantBits();
        return new int[]{
                (int) (mostSignificant >> 32),
                (int) mostSignificant,
                (int) (leastSignificant >> 32),
                (int) leastSignificant
        };
    }

    // 比较两个int数组是否相等
    public static boolean arraysEqual(int[] a, int[] b) {
        if (a.length != b.length) return false;
        for (int i = 0; i < a.length; i++) {
            if (a[i] != b[i]) return false;
        }
        return true;
    }

    // 随机效果
    public static void applyRandomDebuff(Player player) {
        Collection<MobEffect> allEffects = ForgeRegistries.MOB_EFFECTS.getValues(); // 获取所有效果
        // List<MobEffect> effects = new ArrayList<>(allEffects);
        List<MobEffect> effects = allEffects.stream()
                .filter(effect -> effect.getCategory() == MobEffectCategory.HARMFUL)
                .collect(Collectors.toList());

        if (!effects.isEmpty()) {
            MobEffect randomEffect = effects.get(player.getRandom().nextInt(effects.size()));
            int duration = (30 + player.getRandom().nextInt(31)) * 20; // 30-60秒
            int level = 1 + player.getRandom().nextInt(255); // 1-255级
            player.addEffect(new MobEffectInstance(randomEffect, duration, level));
        }
    }

    // 判断是否拥有指定Curios物品
    public static boolean getCuriosItem(LivingEntity entity, String curiosSlot, String itemId) {
        Optional<ICuriosItemHandler> curiosHandlerOptional = CuriosApi.getCuriosInventory(entity).resolve();
        if (curiosHandlerOptional.isPresent()) {
            ICuriosItemHandler curiosHandler = curiosHandlerOptional.get();
            Map<String, ICurioStacksHandler> curios = curiosHandler.getCurios();
            ICurioStacksHandler stackHandler = curios.get(curiosSlot);

            if (stackHandler != null) {
                IDynamicStackHandler stacks = stackHandler.getStacks();
                int slots = stacks.getSlots();
                for (int i = 0; i < slots; i++) {
                    ItemStack stack = stacks.getStackInSlot(i);

                    if (!stack.isEmpty()) {
                        if (itemId.equals(getEffectiveRegistryId(stack))) {
                            return true;
                        }
                    }
                }
            }
        }
        return hasTouhouMaidBaubleItem(entity, itemId);
    }

    private static String getEffectiveRegistryId(ItemStack stack) {
        ResourceLocation registryId = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (registryId == null) {
            return "";
        }
        if (stack.getItem() instanceof DecorationItem decorationItem) {
            return registryId.getNamespace() + ":" + decorationItem.getEffectiveItemId(stack);
        }
        return registryId.toString();
    }

    public static boolean hasTouhouMaidBaubleItem(LivingEntity entity, String itemId) {
        if (entity == null || itemId == null || itemId.isEmpty()) {
            return false;
        }
        if (!ModList.get().isLoaded("touhou_little_maid")) {
            return false;
        }
        if (!isTouhouMaidEntity(entity)) {
            return false;
        }

        try {
            Method getMaidBauble = entity.getClass().getMethod("getMaidBauble");
            Object baubleHandler = getMaidBauble.invoke(entity);
            if (baubleHandler == null) {
                return false;
            }

            Method getSlots = baubleHandler.getClass().getMethod("getSlots");
            Method getStackInSlot = baubleHandler.getClass().getMethod("getStackInSlot", int.class);
            int slots = (int) getSlots.invoke(baubleHandler);
            for (int i = 0; i < slots; i++) {
                Object value = getStackInSlot.invoke(baubleHandler, i);
                if (!(value instanceof ItemStack stack) || stack.isEmpty()) {
                    continue;
                }
                if (itemId.equals(getEffectiveRegistryId(stack))) {
                    return true;
                }
            }
        } catch (Exception ignored) {
            return false;
        }

        return false;
    }

    public static boolean isTouhouMaidEntity(LivingEntity entity) {
        return entity != null && entity.getClass().getName().equals(TOUHOU_MAID_CLASS);
    }

    // 获取指定槽位饰品
    public static ItemStack getCurio(Player player, String curiosSlot, int index) {
        Optional<ICuriosItemHandler> curiosHandler = CuriosApi.getCuriosInventory(player).resolve();
        if (curiosHandler.isPresent()) {
            Map<String, ICurioStacksHandler> stacksHandlers = curiosHandler.get().getCurios();
            ICurioStacksHandler haloHandler = stacksHandlers.get(curiosSlot);
            if (haloHandler != null) {
                IDynamicStackHandler stacks = haloHandler.getStacks();
                if (index < stacks.getSlots()) {
                    return stacks.getStackInSlot(index);
                }
            }
        }
        return ItemStack.EMPTY;
    }

}
