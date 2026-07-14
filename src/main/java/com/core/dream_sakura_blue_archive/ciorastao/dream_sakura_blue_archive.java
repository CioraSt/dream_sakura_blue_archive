package com.core.dream_sakura_blue_archive.ciorastao;

import com.core.dream_sakura_blue_archive.ciorastao.effect.RegistryEffect;
import com.core.dream_sakura_blue_archive.ciorastao.entity.RegistryEntity;
import com.core.dream_sakura_blue_archive.ciorastao.items.RegistryItem;
import com.core.dream_sakura_blue_archive.ciorastao.menu.RegistryMenu;
import com.core.dream_sakura_blue_archive.ciorastao.network.NetworkHandler;
import com.core.dream_sakura_blue_archive.ciorastao.util.HaloSchoolHelper;
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
                output.accept(RegistryItem.PYROXENE.get());
                output.accept(RegistryItem.ARONA_SPAWN_EGG.get());
                acceptHalo(output, RegistryItem.SHIROKO_HALO, "shiroko_cycling_halo", "shiroko_swimsuit_halo");
                acceptHalo(output, RegistryItem.HOSHINO_HALO, "hoshino_swimsuit_halo");
                acceptHalo(output, RegistryItem.SERLKA_HALO);
                acceptHalo(output, RegistryItem.NONOMI_HALO);
                acceptHalo(output, RegistryItem.AYANE_HALO, "ayane_swimsuit_halo");
                acceptHalo(output, RegistryItem.YUME_HALO);
                acceptHalo(output, RegistryItem.HINA_HALO, "hina_dress_halo", "hina_swimsuit_halo");
                acceptHalo(output, RegistryItem.KAYOKO_HALO, "kayoko_newyear_halo");
                acceptHalo(output, RegistryItem.TENDOUARIS_HALO, "tendouaris_battle_halo", "tendouaris_maid_halo");
                output.accept(RegistryItem.TENDOUARIS_SWORD_OF_LIGHT.get());
                acceptHalo(output, RegistryItem.YUZU_HALO, "yuzu_battle_halo", "yuzu_maid_halo");
                acceptHalo(output, RegistryItem.MOMOI_HALO, "momoi_maid_halo");
                acceptHalo(output, RegistryItem.MIDORI_HALO, "midori_maid_halo");
                acceptHalo(output, RegistryItem.KAIYI_HALO);
                acceptHalo(output, RegistryItem.KLUONUOYA_HALO);
                acceptHalo(output, RegistryItem.SHIRASUAZUSA_HALO, "shirasuazusa_swimsuit_halo");
                acceptHalo(output, RegistryItem.MARI_HALO, "mari_idol_halo", "mari_gym_halo");
                acceptHalo(output, RegistryItem.SEIA_HALO, "seia_swimsuit_halo");
                output.accept(RegistryItem.MIKA_HALO.get());
                acceptHalo(output, RegistryItem.NATSU_HALO, "natsu_band_halo");
                acceptHalo(output, RegistryItem.SHUN_HALO);
                acceptHalo(output, RegistryItem.KARENA_HALO);
                acceptHalo(output, RegistryItem.SAKULUNA_HALO);
                acceptHalo(output, RegistryItem.JUGUANG_HALO);
                acceptHalo(output, RegistryItem.JUWANG_HALO);
                acceptHalo(output, RegistryItem.KUROKO_HALO);
                acceptHalo(output, RegistryItem.REN_HALO);
                output.accept(RegistryItem.PRIMARY_EXP.get());
                output.accept(RegistryItem.INTERMEDIATE_EXP.get());
                output.accept(RegistryItem.SENIOR_EXP.get());
                output.accept(RegistryItem.SUPERIOR_EXP.get());
                output.accept(RegistryItem.HOSHINO_TACTICAL_SHIELD.get());
            }).build()
    );

    private static void acceptHalo(CreativeModeTab.Output output, RegistryObject<net.minecraft.world.item.Item> item, String... variants) {
        output.accept(item.get());
        for (String variant : variants) {
            output.accept(HaloVariantHelper.createVariantStack(item.get(), variant));
        }
    }

    public dream_sakura_blue_archive(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();

        CREATIVE_MODE_TABS.register(modEventBus);
        RegistryItem.ITEMS.register(modEventBus);
        RegistryEffect.EFFECTS.register(modEventBus);
        RegistryEntity.ENTITY_TYPES.register(modEventBus);
        RegistryMenu.MENUS.register(modEventBus);
        MinecraftForge.EVENT_BUS.register(this);
        GeckoLib.initialize();
        NetworkHandler.register();

        // 注册梦樱API分类栏（需在注册完成后执行）
        modEventBus.addListener(this::onClientSetup);
    }

    private void onClientSetup(net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent event) {
        HaloSchoolHelper.registerCategories();
    }
}
