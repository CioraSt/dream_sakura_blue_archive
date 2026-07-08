package com.core.dream_sakura_blue_archive.ciorastao.events;

import com.core.dream_sakura.api.tooltip.DreamSakuraTooltipAPI;
import com.core.dream_sakura_blue_archive.ciorastao.dream_sakura_blue_archive;
import com.core.dream_sakura_blue_archive.ciorastao.items.DecorationItem;
import com.core.dream_sakura_blue_archive.ciorastao.items.MikaObjHaloItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = dream_sakura_blue_archive.MODID, value = Dist.CLIENT)
public class TooltipRenderHandler {

    @SubscribeEvent
    public static void onTooltipPre(RenderTooltipEvent.Pre event) {
        ItemStack stack = event.getItemStack();
        if (stack.getItem() instanceof DecorationItem decorationItem) {
            String itemId = decorationItem.getItemId();
            DreamSakuraTooltipAPI.DreamSakuraTextureConfig config = DreamSakuraTooltipAPI.getConfig(itemId);
            if (config != null) {
                DreamSakuraTooltipAPI.renderHaloTooltipBackground(event, config);
            }
        } else if (stack.getItem() instanceof MikaObjHaloItem) {
            DreamSakuraTooltipAPI.DreamSakuraTextureConfig config = DreamSakuraTooltipAPI.getConfig(MikaObjHaloItem.ITEM_ID);
            if (config != null) {
                DreamSakuraTooltipAPI.renderHaloTooltipBackground(event, config);
            }
        }
    }

    @SubscribeEvent
    public static void onTooltipColor(RenderTooltipEvent.Color event) {
        ItemStack stack = event.getItemStack();
        if (stack.getItem() instanceof DecorationItem decorationItem) {
            String itemId = decorationItem.getItemId();
            DreamSakuraTooltipAPI.DreamSakuraTextureConfig config = DreamSakuraTooltipAPI.getConfig(itemId);
            if (config != null) {
                DreamSakuraTooltipAPI.setHaloTooltipColor(event, config);
            }
        } else if (stack.getItem() instanceof MikaObjHaloItem) {
            DreamSakuraTooltipAPI.DreamSakuraTextureConfig config = DreamSakuraTooltipAPI.getConfig(MikaObjHaloItem.ITEM_ID);
            if (config != null) {
                DreamSakuraTooltipAPI.setHaloTooltipColor(event, config);
            }
        }
    }
}
