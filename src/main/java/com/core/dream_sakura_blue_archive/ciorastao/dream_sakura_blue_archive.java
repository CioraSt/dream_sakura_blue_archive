package com.core.dream_sakura_blue_archive.ciorastao;

import com.core.dream_sakura_blue_archive.ciorastao.effect.RegistryEffect;
import com.core.dream_sakura_blue_archive.ciorastao.items.RegistryItem;
import com.core.dream_sakura_blue_archive.ciorastao.util.HaloVariantHelper;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.bernie.geckolib.GeckoLib;

@Mod(dream_sakura_blue_archive.MODID)
public class dream_sakura_blue_archive {
    public static final String MODID = "dream_sakura_blue_archive";
    public static final Logger LOGGER = LogManager.getLogger(dream_sakura_blue_archive.MODID);
    private static final ResourceKey<Registry<CreativeModeTab>> CREATIVE_MODE_TAB_REGISTRY =
            ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath("minecraft", "creative_mode_tab"));

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(CREATIVE_MODE_TAB_REGISTRY, MODID);
    public static final RegistryObject<CreativeModeTab> DREAM_SAKURA_BA_TAB = CREATIVE_MODE_TABS.register("dream_sakura_ba_tab", () -> CreativeModeTab.builder()
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .title(Component.translatable("itemGroup.dream_sakura_blue_archive_ba_tab"))
            .icon(() -> RegistryItem.TENDOUARIS_HALO.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(RegistryItem.SHIROKO_HALO.get());
                output.accept(HaloVariantHelper.createVariantStack(RegistryItem.SHIROKO_HALO.get(), "shiroko_cycling_halo"));
                output.accept(HaloVariantHelper.createVariantStack(RegistryItem.SHIROKO_HALO.get(), "shiroko_swimsuit_halo"));
                output.accept(RegistryItem.HOSHINO_HALO.get());
                output.accept(HaloVariantHelper.createVariantStack(RegistryItem.HOSHINO_HALO.get(), "hoshino_swimsuit_halo"));
                output.accept(RegistryItem.SERLKA_HALO.get());
                output.accept(RegistryItem.NONOMI_HALO.get());
                output.accept(RegistryItem.AYANE_HALO.get());
                output.accept(HaloVariantHelper.createVariantStack(RegistryItem.AYANE_HALO.get(), "ayane_swimsuit_halo"));
                output.accept(RegistryItem.YUME_HALO.get());

                output.accept(RegistryItem.HINA_HALO.get());
                output.accept(HaloVariantHelper.createVariantStack(RegistryItem.HINA_HALO.get(), "hina_dress_halo"));
                output.accept(HaloVariantHelper.createVariantStack(RegistryItem.HINA_HALO.get(), "hina_swimsuit_halo"));
                output.accept(RegistryItem.KAYOKO_HALO.get());
                output.accept(HaloVariantHelper.createVariantStack(RegistryItem.KAYOKO_HALO.get(), "kayoko_newyear_halo"));

                output.accept(RegistryItem.TENDOUARIS_HALO.get());
                output.accept(HaloVariantHelper.createVariantStack(RegistryItem.TENDOUARIS_HALO.get(), "tendouaris_battle_halo"));
                output.accept(HaloVariantHelper.createVariantStack(RegistryItem.TENDOUARIS_HALO.get(), "tendouaris_maid_halo"));
                output.accept(RegistryItem.YUZU_HALO.get());
                output.accept(HaloVariantHelper.createVariantStack(RegistryItem.YUZU_HALO.get(), "yuzu_battle_halo"));
                output.accept(HaloVariantHelper.createVariantStack(RegistryItem.YUZU_HALO.get(), "yuzu_maid_halo"));
                output.accept(RegistryItem.MOMOI_HALO.get());
                output.accept(HaloVariantHelper.createVariantStack(RegistryItem.MOMOI_HALO.get(), "momoi_maid_halo"));
                output.accept(RegistryItem.MIDORI_HALO.get());
                output.accept(HaloVariantHelper.createVariantStack(RegistryItem.MIDORI_HALO.get(), "midori_maid_halo"));
                output.accept(RegistryItem.KAIYI_HALO.get());
                output.accept(RegistryItem.KLUONUOYA_HALO.get());

                output.accept(RegistryItem.SHIRASUAZUSA_HALO.get());
                output.accept(HaloVariantHelper.createVariantStack(RegistryItem.SHIRASUAZUSA_HALO.get(), "shirasuazusa_swimsuit_halo"));
                output.accept(RegistryItem.MARI_HALO.get());
                output.accept(HaloVariantHelper.createVariantStack(RegistryItem.MARI_HALO.get(), "mari_idol_halo"));
                output.accept(HaloVariantHelper.createVariantStack(RegistryItem.MARI_HALO.get(), "mari_gym_halo"));
                output.accept(RegistryItem.SEIA_HALO.get());
                output.accept(HaloVariantHelper.createVariantStack(RegistryItem.SEIA_HALO.get(), "seia_swimsuit_halo"));
                output.accept(RegistryItem.MIKA_HALO.get());
                output.accept(RegistryItem.NATSU_HALO.get());
                output.accept(HaloVariantHelper.createVariantStack(RegistryItem.NATSU_HALO.get(), "natsu_band_halo"));

                output.accept(RegistryItem.SHUN_HALO.get());
                output.accept(RegistryItem.KARENA_HALO.get());
                output.accept(RegistryItem.SAKULUNA_HALO.get());
                output.accept(RegistryItem.JUGUANG_HALO.get());
                output.accept(RegistryItem.JUWANG_HALO.get());
                output.accept(RegistryItem.KUROKO_HALO.get());
                output.accept(RegistryItem.REN_HALO.get());
            }).build()
    );

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS1 = DeferredRegister.create(CREATIVE_MODE_TAB_REGISTRY, MODID);
    public static final RegistryObject<CreativeModeTab> DREAM_SAKURA_ZAX_TAB = CREATIVE_MODE_TABS1.register("dream_sakura_zax_tab", () -> CreativeModeTab.builder()
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .title(Component.translatable("itemGroup.dream_sakura_blue_archive_zax_tab"))
            .icon(() -> RegistryItem.SUPERIOR_EXP.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(RegistryItem.PRIMARY_EXP.get());
                output.accept(RegistryItem.INTERMEDIATE_EXP.get());
                output.accept(RegistryItem.SENIOR_EXP.get());
                output.accept(RegistryItem.SUPERIOR_EXP.get());
            }).build()
    );

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS3 = DeferredRegister.create(CREATIVE_MODE_TAB_REGISTRY, MODID);
    public static final RegistryObject<CreativeModeTab> DREAM_SAKURA_ZB_TAB = CREATIVE_MODE_TABS3.register("dream_sakura_zb_tab", () -> CreativeModeTab.builder()
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .title(Component.translatable("itemGroup.dream_sakura_blue_archive_zb_tab"))
            .icon(() -> RegistryItem.HOSHINO_TACTICAL_SHIELD.get().getDefaultInstance())
            .displayItems((parameters, output) -> output.accept(RegistryItem.HOSHINO_TACTICAL_SHIELD.get()))
            .build()
    );

    public dream_sakura_blue_archive(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();

        CREATIVE_MODE_TABS.register(modEventBus);
        CREATIVE_MODE_TABS1.register(modEventBus);
        CREATIVE_MODE_TABS3.register(modEventBus);
        RegistryItem.ITEMS.register(modEventBus);
        RegistryEffect.EFFECTS.register(modEventBus);
        MinecraftForge.EVENT_BUS.register(this);
        GeckoLib.initialize();
    }
}
