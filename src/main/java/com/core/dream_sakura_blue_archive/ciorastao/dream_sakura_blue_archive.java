package com.core.dream_sakura_blue_archive.ciorastao;

import com.core.dream_sakura_blue_archive.ciorastao.effect.RegistryEffect;
import com.core.dream_sakura_blue_archive.ciorastao.items.RegistryItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.bernie.geckolib.GeckoLib;

// 这里的值应与 META-INF/mods.toml 文件中的条目相匹配
@Mod(dream_sakura_blue_archive.MODID)
// 保留MOD事件订阅，但明确指定只在客户端注册
@Mod.EventBusSubscriber(modid = dream_sakura_blue_archive.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class dream_sakura_blue_archive {
    public static final String MODID = "dream_sakura_blue_archive";
    public static final Logger LOGGER = LogManager.getLogger(dream_sakura_blue_archive.MODID); // 日志记录器

    // 注册创造物品栏
    //光环 tab
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
    public static final RegistryObject<CreativeModeTab> DREAM_SAKURA_BA_TAB = CREATIVE_MODE_TABS.register("dream_sakura_ba_tab", () -> CreativeModeTab.builder()
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .title(Component.translatable("itemGroup.dream_sakura_blue_archive_ba_tab"))
            .icon(() -> RegistryItem.TENDOUARIS_HALO.get().getDefaultInstance())
            .displayItems((parameters, output) -> {

                // === 阿比多斯 ===
                output.accept(RegistryItem.SHIROKO_HALO.get());      // 砂狼白子
                output.accept(RegistryItem.HOSHINO_HALO.get());      // 小鸟游星野
                output.accept(RegistryItem.SERLKA_HALO.get());       // 黑见芹香
                output.accept(RegistryItem.NONOMI_HALO.get());       // 十六夜野宫
                output.accept(RegistryItem.AYANE_HALO.get());        // 奥空绫音
                output.accept(RegistryItem.YUME_HALO.get());         // 梦
                // === 格黑娜 ===
                output.accept(RegistryItem.HINA_HALO.get());         // 空崎日奈
                output.accept(RegistryItem.KAYOKO_HALO.get());       // 鬼方佳代子
                // === 千年 ===
                output.accept(RegistryItem.TENDOUARIS_HALO.get());   // 天童爱丽丝
                output.accept(RegistryItem.YUZU_HALO.get());         // 柚子
                output.accept(RegistryItem.MOMOI_HALO.get());        // 才羽小桃
                output.accept(RegistryItem.MIDORI_HALO.get());       // 才羽小绿
                output.accept(RegistryItem.KAIYI_HALO.get());        // 凯
                output.accept(RegistryItem.KLUONUOYA_HALO.get());    // K螺诺亚
                // === 三一 ===
                output.accept(RegistryItem.SHIRASUAZUSA_HALO.get()); // 白洲梓
                output.accept(RegistryItem.MARI_HALO.get());         // 伊落玛丽
                output.accept(RegistryItem.SEIA_HALO.get());         // 圣园圣娅
                // === 山海经 ===
                output.accept(RegistryItem.SHUN_HALO.get());         // 春原瞬
                output.accept(RegistryItem.KARENA_HALO.get());       // 春原心奈
                // === 其他 ===
                output.accept(RegistryItem.SAKULUNA_HALO.get());     // 七叶渡羽
                output.accept(RegistryItem.JUGUANG_HALO.get());      // 橘光
                output.accept(RegistryItem.JUWANG_HALO.get());       // 橘望
                output.accept(RegistryItem.NATSU_HALO.get());        // 小夏
                output.accept(RegistryItem.KUROKO_HALO.get());       // 砂狼黑子
                output.accept(RegistryItem.REN_HALO.get());          // 联邦学生会长

            }).build()
    );

    //杂项tab
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS1 = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
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

    //装备 tab
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS3 = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
    public static final RegistryObject<CreativeModeTab> DREAM_SAKURA_ZB_TAB = CREATIVE_MODE_TABS3.register("dream_sakura_zb_tab", () -> CreativeModeTab.builder()
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .title(Component.translatable("itemGroup.dream_sakura_blue_archive_zb_tab"))
            .icon(() -> RegistryItem.HOSHINO_TACTICAL_SHIELD.get().getDefaultInstance())
            .displayItems((parameters, output) -> {

                output.accept(RegistryItem.HOSHINO_TACTICAL_SHIELD.get());

            }).build()
    );

    public dream_sakura_blue_archive(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();

        CREATIVE_MODE_TABS.register(modEventBus);  // 注册创造模式物品栏
        CREATIVE_MODE_TABS1.register(modEventBus);  // 注册创造模式物品栏
        CREATIVE_MODE_TABS3.register(modEventBus);
        RegistryItem.ITEMS.register(modEventBus);  // 注册物品
        RegistryEffect.EFFECTS.register(modEventBus);//注册药水效果
        MinecraftForge.EVENT_BUS.register(this);
        GeckoLib.initialize();
    }
}
