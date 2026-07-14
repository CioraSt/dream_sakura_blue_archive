package com.core.dream_sakura_blue_archive.ciorastao.client;

import com.core.dream_sakura_blue_archive.ciorastao.client.renderer.AronaRenderer;
import com.core.dream_sakura_blue_archive.ciorastao.client.screen.AronaGachaScreen;
import com.core.dream_sakura_blue_archive.ciorastao.dream_sakura_blue_archive;
import com.core.dream_sakura_blue_archive.ciorastao.entity.RegistryEntity;
import com.core.dream_sakura_blue_archive.ciorastao.menu.RegistryMenu;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = dream_sakura_blue_archive.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientModEvents {
    private ClientModEvents() {
    }

    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(RegistryEntity.ARONA.get(), AronaRenderer::new);
        event.registerEntityRenderer(RegistryEntity.ARONA_PROJECTILE.get(), ThrownItemRenderer::new);
    }

    @SubscribeEvent
    public static void registerScreens(FMLClientSetupEvent event) {
        event.enqueueWork(() -> MenuScreens.register(RegistryMenu.ARONA_GACHA.get(), AronaGachaScreen::new));
    }
}
