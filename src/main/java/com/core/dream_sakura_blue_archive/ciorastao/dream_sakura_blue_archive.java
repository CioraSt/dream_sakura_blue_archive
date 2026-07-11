package com.core.dream_sakura_blue_archive.ciorastao;

import com.core.dream_sakura_blue_archive.ciorastao.effect.RegistryEffect;
import com.core.dream_sakura_blue_archive.ciorastao.items.RegistryItem;
import com.core.dream_sakura_blue_archive.ciorastao.util.HaloSchoolHelper;
import com.core.dream_sakura_blue_archive.ciorastao.util.HaloVariantHelper;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
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
                for (var entry : ForgeRegistries.ITEMS.getEntries()) {
                    if (!MODID.equals(entry.getKey().location().getNamespace())) continue;
                    output.accept(entry.getValue());
                }
                // NBT 变体（紧跟在对应基础物品后面）
                for (String variantId : HaloVariantHelper.allVariantIds()) {
                    String baseId = HaloVariantHelper.baseItemId(variantId);
                    Item baseItem = ForgeRegistries.ITEMS.getValue(
                            ResourceLocation.fromNamespaceAndPath(MODID, baseId));
                    if (baseItem != null) {
                        output.accept(HaloVariantHelper.createVariantStack(baseItem, variantId));
                    }
                }
            }).build()
    );

    public dream_sakura_blue_archive(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();

        CREATIVE_MODE_TABS.register(modEventBus);
        RegistryItem.ITEMS.register(modEventBus);
        RegistryEffect.EFFECTS.register(modEventBus);
        MinecraftForge.EVENT_BUS.register(this);
        GeckoLib.initialize();

        // 注册梦樱API分类栏（需在注册完成后执行）
        modEventBus.addListener(this::onClientSetup);
    }

    private void onClientSetup(net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent event) {
        HaloSchoolHelper.registerCategories();
    }
}
