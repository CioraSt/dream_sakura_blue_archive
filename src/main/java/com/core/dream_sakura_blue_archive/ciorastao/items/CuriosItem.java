package com.core.dream_sakura_blue_archive.ciorastao.items;

import com.core.dream_sakura_blue_archive.ciorastao.dream_sakura_blue_archive;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.apache.logging.log4j.util.TriConsumer;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.function.BiConsumer;

public class CuriosItem extends Item implements ICurioItem {
    private final BiConsumer<SlotContext, ItemStack> curioEquipCallback;
    private final TriConsumer<SlotContext, ItemStack, ItemStack> curioUnequipCallback;
    private final String ItemId;

    public CuriosItem(String id, Properties properties) {
        this(id, properties, null);
    }

    public CuriosItem(String id, Properties properties, BiConsumer<SlotContext, ItemStack> curioTickCallback) {
        this(id, properties, curioTickCallback, null);
    }

    public CuriosItem(String id, Properties properties, BiConsumer<SlotContext, ItemStack> curioTickCallback, TriConsumer<SlotContext, ItemStack, ItemStack> curioUnequipCallback) {
        super(properties);
        this.ItemId = id;
        this.curioEquipCallback = curioTickCallback;
        this.curioUnequipCallback = curioUnequipCallback;
    }


    @Override
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        return "ring".equals(slotContext.identifier());
    }

    @Override
    public void onEquip(SlotContext slotContext, ItemStack prevStack, ItemStack stack) {
        ICurioItem.super.onEquip(slotContext, prevStack, stack);
    }

    @Override
    public void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack) {
        ICurioItem.super.onUnequip(slotContext, newStack, stack);
        if (curioUnequipCallback != null) {
            curioUnequipCallback.accept(slotContext, newStack, stack);
        }
    }

    @Override
    public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
        return true;
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        // 执行callback
        if (curioEquipCallback != null) {
            curioEquipCallback.accept(slotContext, stack);
        }
    }

    @Override
    public void appendHoverText(
            @Nonnull ItemStack stack,
            @Nullable Level level,
            @Nonnull List<Component> tooltip,
            @Nonnull TooltipFlag flag
    ) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.translatable("tooltip." + dream_sakura_blue_archive.MODID + "." + this.ItemId + ".base")
                .withStyle(ChatFormatting.GRAY));  // 添加基础描述
        if (this.ItemId == "farewell") {
            CompoundTag tag = stack.getOrCreateTag();
            double damageBonus = tag.getDouble("DamageBonus");
            tooltip.add(
                    Component.literal(
                            String.format("伤害加成: +%.2f%%", damageBonus * 100)
                    ).withStyle(ChatFormatting.GREEN)
            );
        }
    }

}