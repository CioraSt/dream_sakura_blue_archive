package com.core.dream_sakura_blue_archive.ciorastao.items.client;

import com.core.dream_sakura_blue_archive.ciorastao.dream_sakura_blue_archive;
import com.core.dream_sakura_blue_archive.ciorastao.items.RegistryItem;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;

@Mod.EventBusSubscriber(modid = dream_sakura_blue_archive.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {

        DecorationRenderer decorationRenderer = new DecorationRenderer();

//饰品渲染调用
        CuriosRendererRegistry.register(
                RegistryItem.TENDOUARIS_HALO.get(),
                () -> new GeckoCurioRendererHoxie<>(decorationRenderer)
        );
        CuriosRendererRegistry.register(
                RegistryItem.HOSHINO_HALO.get(),
                () -> new GeckoCurioRendererHoxie<>(decorationRenderer)
        );
        CuriosRendererRegistry.register(
                RegistryItem.HINA_HALO.get(),
                () -> new GeckoCurioRendererY<>(decorationRenderer)
        );
        CuriosRendererRegistry.register(
                RegistryItem.SHIROKO_HALO.get(),
                () -> new GeckoCurioRendererHoxie<>(decorationRenderer)
        );
        CuriosRendererRegistry.register(
                RegistryItem.KLUONUOYA_HALO.get(),
                () -> new GeckoCurioRendererHoxie<>(decorationRenderer)
        );
        CuriosRendererRegistry.register(
                RegistryItem.SAKULUNA_HALO.get(),
                () -> new GeckoCurioRendererHoxie<>(decorationRenderer)
        );
        CuriosRendererRegistry.register(
                RegistryItem.NONOMI_HALO.get(),
                () -> new GeckoCurioRendererHoxie<>(decorationRenderer)
        );
        CuriosRendererRegistry.register(
                RegistryItem.SERLKA_HALO.get(),
                () -> new GeckoCurioRendererHoxie<>(decorationRenderer)
        );
        CuriosRendererRegistry.register(
                RegistryItem.AYANE_HALO.get(),
                () -> new GeckoCurioRendererHoxie<>(decorationRenderer)
        );
        CuriosRendererRegistry.register(
                RegistryItem.SHIRASUAZUSA_HALO.get(),
                () -> new GeckoCurioRendererHoxie<>(decorationRenderer)
        );
    }


}
