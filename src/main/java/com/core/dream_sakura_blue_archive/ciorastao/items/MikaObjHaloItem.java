package com.core.dream_sakura_blue_archive.ciorastao.items;

import com.core.dream_sakura.api.tooltip.DreamSakuraTooltipAPI;
import com.core.dream_sakura_blue_archive.ciorastao.util.HaloLevelManager;
import com.core.dream_sakura_blue_archive.ciorastao.util.HaloTooltipText;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class MikaObjHaloItem extends Item implements ICurioItem {
    public static final String ITEM_ID = "mika_halo";

    public MikaObjHaloItem(Properties properties) {
        super(properties.stacksTo(1));
        DreamSakuraTooltipAPI.registerHaloTooltip(ITEM_ID,
                new DreamSakuraTooltipAPI.DreamSakuraTextureConfig(
                        192, 192, 2.5F, 3000.0F, 0xD0FFF5F8, 0xD0FFD6E7, 0xFFFF9FD1, 0xFFFFF5A8,
                        0.0F, -20.0F, false, new String[]{"dream_sakura_blue_archive:textures/screens/mika.png"},
                        new int[]{816}, new int[]{1367}, 132, 132, 0.0F, 0.0F, 40, 0.8F, 1.2F, 3, 2000L, true, false
                ));
    }

    @Override
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        return "halo".equals(slotContext.identifier());
    }

    @Override
    public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
        return true;
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        HaloLevelManager.getHaloLevel(stack);
        HaloLevelManager.getHaloXP(stack);
        HaloLevelManager.getMaxHaloXP(stack);
    }

    @Override
    public void appendHoverText(@Nonnull ItemStack stack, @Nullable Level level, @Nonnull List<Component> tooltip, @Nonnull TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        int itemLevel = HaloLevelManager.getHaloLevel(stack);
        int itemXp = HaloLevelManager.getHaloXP(stack);
        int itemMaxXp = HaloLevelManager.getMaxHaloXP(stack);

        tooltip.add(Component.translatable("tooltip.dream_sakura_blue_archive.level", itemLevel)
                .withStyle(Style.EMPTY.withColor(0xFF00FF)));
        if (itemLevel >= 90 && itemXp >= itemMaxXp) {
            tooltip.add(Component.translatable("tooltip.dream_sakura_blue_archive.xp.max")
                    .withStyle(Style.EMPTY.withColor(0xADD8E6)));
        } else {
            tooltip.add(Component.translatable("tooltip.dream_sakura_blue_archive.xp", itemXp, itemMaxXp)
                    .withStyle(Style.EMPTY.withColor(0xADD8E6)));
        }
        if (level != null && level.isClientSide()) {
            HaloTooltipText.addHaloTooltip(ITEM_ID, tooltip);
        }
    }
}
