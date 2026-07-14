package com.core.dream_sakura_blue_archive.ciorastao.items;

import com.core.dream_sakura_blue_archive.ciorastao.dream_sakura_blue_archive;
import com.core.dream_sakura_blue_archive.ciorastao.entity.RegistryEntity;
import com.core.dream_sakura_blue_archive.ciorastao.util.RegistryActiveSkill;
import com.core.dream_sakura_blue_archive.ciorastao.util.RegistryPassiveSkill;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class RegistryItem {
    // 创建注册器
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, dream_sakura_blue_archive.MODID);

    public static final RegistryObject<Item> PYROXENE = ITEMS.register(
            "pyroxene",
            () -> new Item(new Item.Properties()
                    .rarity(Rarity.RARE)
                    .stacksTo(64)
                    .fireResistant())
    );

    public static final RegistryObject<Item> ARONA_SPAWN_EGG = ITEMS.register(
            "arona_spawn_egg",
            () -> new ForgeSpawnEggItem(RegistryEntity.ARONA, 0x4A90E2, 0xFFFFFF, new Item.Properties())
    );

    // #region 经验材料
    public static final RegistryObject<Item> PRIMARY_EXP = ITEMS.register(
            "primary_exp",
            () -> new ExpToolItem(
                    new Item.Properties()
                            .rarity(Rarity.COMMON),
                    50
            )
    );
    // 定义发光颜色
    private static final float[] NULL_GLOW = {}; // 无色
    private static final float[] PINK_GLOW = {1.000f, 0.773f, 1.000f}; // 粉色
    //黑见芹香光环注册SERLKA_HALO
    public static final RegistryObject<Item> SERLKA_HALO = ITEMS.register(
            "serlka_halo",
            () -> new DecorationItem.Builder(
                    "serlka_halo",
                    new Item.Properties()
                            .stacksTo(1).
                            rarity(Rarity.EPIC)
            )
                    .withHaloLevelSystem()
                    .withBAElementsParticles()
                    .withGlowIntensity(1f)
                    .withGlowColor(PINK_GLOW)
                    .withGlowColor(PINK_GLOW)
                    .withTooltipTextureConfig(new com.core.dream_sakura.api.tooltip.DreamSakuraTooltipAPI.DreamSakuraTextureConfig(
                            192, 192, 2.5F, 3000.0F, 0xD01A1A2E, 0xD0CC0033, 0xFFFF6699, 0xFFFF0033,
                            0.0F, -20.0F, false, new String[]{"dream_sakura_blue_archive:textures/screens/serlka.png"},
                            new int[]{532}, new int[]{1024}, 128, 128, 0.0F, 0.0F, 40, 0.85F, 1.2F, 3, 2000L, true, false
                    ))
//                    .withSkillBinding(RegistryActiveSkill.SERLKA_Halo_Skill.get())
//                    .withCurioEquipCallback(
//                            (slotContext, stack) -> {}
                    .build()
    );
    //星野光环注册
    public static final RegistryObject<Item> HOSHINO_HALO = ITEMS.register(
            "hoshino_halo",
            () -> new DecorationItem.Builder(
                    "hoshino_halo",
                    new Item.Properties()
                            .stacksTo(1)
                            .rarity(Rarity.EPIC)
            )
                    .withHaloLevelSystem()
                    .withBAElementsParticles()
                    .withGlowColor(PINK_GLOW)
                    .withGlowIntensity(1f)
                    .withSkillBinding(RegistryActiveSkill.Hoshino_Halo_Skill.get())
                    .withTooltipTextureConfig(new com.core.dream_sakura.api.tooltip.DreamSakuraTooltipAPI.DreamSakuraTextureConfig(
                            192, 192, 1.5F, 4000.0F, 0xD0FFB6C1, 0xD087CEEB, 0xFFFF69B4, 0xFF1E90FF,
                            0.0F, -20.0F, false, new String[]{"dream_sakura_blue_archive:textures/screens/hoshino.png"},
                            new int[]{673}, new int[]{1024}, 132, 132, 0.0F, 0.0F, 38, 0.7F, 0.7F, 3, 3000L, true, false
                    ))
                    .withCurioEquipCallback(
                            (slotContext, stack) -> {
                                RegistryPassiveSkill.Hoshino_Halo_Skill_0(slotContext, stack);
                                RegistryPassiveSkill.Hoshino_Halo_Skill_1(slotContext, stack);
                                RegistryPassiveSkill.Hoshino_Halo_Skill_2(slotContext, stack);
                            }
                    ).build()
    );
    private static final float[] ALS_GLOW = {0.45f, 0.78f, 0.95f}; // 爱丽丝蓝偏蓝
    //爱丽丝光环注册
    public static final RegistryObject<Item> TENDOUARIS_HALO = ITEMS.register(
            "tendouaris_halo",//名称
            () -> new DecorationItem.Builder(//属性
                    "tendouaris_halo",//名称
                    new Item.Properties()//属性
                            .stacksTo(1).//堆叠数
                            rarity(Rarity.EPIC)//稀有度
            )
                    .withHaloLevelSystem()
                    .withBAElementsParticles()
                    .withGlowColor(ALS_GLOW)//发光颜色控制
                    .withGlowIntensity(0.8f)//发光强度控制(降低20%)
                    .withTooltipTextureConfig(new com.core.dream_sakura.api.tooltip.DreamSakuraTooltipAPI.DreamSakuraTextureConfig(
                            192, 192, 4.0F, 1500.0F, 0xD00A0A1A, 0xD04169E1, 0xFF00FFFF, 0xFFFFFFFF,
                            0.0F, -25.0F, false, new String[]{"dream_sakura_blue_archive:textures/screens/tendouaris.png"},
                            new int[]{916}, new int[]{1035}, 130, 130, 0.0F, 0.0F, 40, 0.8F, 1.8F, 3, 1000L, true, false
                    ))
                    .withSkillBinding(RegistryActiveSkill.TENDOUARIS_HALO_Skill.get())//主动技能
                    .withCurioEquipCallback(//被动技能
                            (slotContext, stack) -> {
                                RegistryPassiveSkill.TENDOUARIS_Halo_Skill_1(slotContext, stack);
                                RegistryPassiveSkill.TENDOUARIS_Halo_Skill_2(slotContext, stack);
                                RegistryPassiveSkill.TENDOUARIS_Halo_Skill_3(slotContext, stack);
                            }
                    )
                    .build()
    );
    public static final RegistryObject<Item> INTERMEDIATE_EXP = ITEMS.register(
            "intermediate_exp",
            () -> new ExpToolItem(
                    new Item.Properties()
                            .rarity(Rarity.COMMON),
                    500
            )
    );
    public static final RegistryObject<Item> SENIOR_EXP = ITEMS.register(
            "senior_exp",
            () -> new ExpToolItem(
                    new Item.Properties()
                            .rarity(Rarity.COMMON),
                    2000
            )
    );
    public static final RegistryObject<Item> SUPERIOR_EXP = ITEMS.register(
            "superior_exp",
            () -> new ExpToolItem(
                    new Item.Properties()
                            .rarity(Rarity.COMMON),
                    10000
            )
    );
    //星野战术盾牌  注册 HOSHINO_TACTICAL_SHIELD
    public static final RegistryObject<Item> HOSHINO_TACTICAL_SHIELD = ITEMS.register(
            "hoshino_tactical_shield",
            () -> new DecorationItem.Builder(
                    "hoshino_tactical_shield",
                    new Item.Properties()
                            .stacksTo(1)
                            .rarity(Rarity.EPIC)
            )
                    .withBAElementsParticles()
                    .withGlowIntensity(1.0f)
                    .withGlowColor(PINK_GLOW)
                    .build()
    );
    private static final float[] GREY_GLOW = {0.35f, 0.38f, 0.5f}; // 佳代子灰蓝
    private static final float[] AMBER_GLOW = {1.0f, 0.78f, 0.3f}; // 梦琥珀金
    private static final float[] VIOLET_GLOW = {0.56f, 0.28f, 0.84f}; // 黑子暗紫(亮度+40%)
    private static final float[] PINK_LIGHT_GLOW = {1.0f, 0.6f, 0.7f}; // 心奈浅粉
    private static final float[] JUGUANG_GLOW = {0.3f, 0.85f, 0.4f}; // 橘光绿
    private static final float[] JUWANG_GLOW = {0.35f, 0.9f, 0.35f}; // 橘望草绿
    private static final float[] KAIYI_GLOW = {1.0f, 0.55f, 0.7f}; // 凯伊粉
    private static final float[] MARI_GLOW = {1.0f, 0.9f, 0.3f}; // 玛丽黄
    private static final float[] SEIA_GLOW = {1.0f, 0.85f, 0.2f}; // 圣娅金
    private static final float[] SHUN_GLOW = {0.45f, 0.5f, 0.85f}; // 瞬蓝紫
    private static final float[] MIDORI_GLOW = {0.25f, 0.8f, 0.35f}; // 小绿浅绿
    private static final float[] MOMOI_GLOW = {1.0f, 0.45f, 0.6f}; // 小桃桃红
    private static final float[] NATSU_GLOW = {1.0f, 0.6f, 0.75f}; // 小夏粉红
    private static final float[] YUZU_GLOW = {1.0f, 0.9f, 0.4f}; // 柚子暖黄
    private static final float[] REN_GLOW = {0.6f, 0.85f, 1.0f}; // 联邦学生会长冰蓝
    //奥空绫音光环 注册AYANE_HALO
    public static final RegistryObject<Item> AYANE_HALO = ITEMS.register(
            "ayaneko_halo",
            () -> new DecorationItem.Builder(
                    "ayaneko_halo",
                    new Item.Properties()
                            .stacksTo(1).
                            rarity(Rarity.EPIC)
            )
                    .withHaloLevelSystem()
                    .withBAElementsParticles()
                    .withGlowIntensity(1f)
                    .withGlowColor(PINK_GLOW)
                    .withTooltipTextureConfig(new com.core.dream_sakura.api.tooltip.DreamSakuraTooltipAPI.DreamSakuraTextureConfig(
                            192, 192, 1.8F, 3000.0F, 0xD0FFE4E1, 0xD0FFB6C1, 0xFFFF69B4, 0xFFFF1493,
                            0.0F, -20.0F, false, new String[]{"dream_sakura_blue_archive:textures/screens/ayaneko.png"},
                            new int[]{336}, new int[]{1024}, 132, 132, 0.0F, 0.0F, 40, 0.8F, 1.2F, 3, 2000L, true, false
                    ))
                    .build()
    );
    //鬼方佳代子光环 注册KAYOKO_HALO
    public static final RegistryObject<Item> KAYOKO_HALO = ITEMS.register(
            "kayoko_halo",
            () -> new DecorationItem.Builder(
                    "kayoko_halo",
                    new Item.Properties()
                            .stacksTo(1)
                            .rarity(Rarity.EPIC)
            )
                    .withHaloLevelSystem()
                    .withBAElementsParticles()
                    .withGlowColor(GREY_GLOW)
                    .withGlowIntensity(1f)
                    .withTooltipTextureConfig(new com.core.dream_sakura.api.tooltip.DreamSakuraTooltipAPI.DreamSakuraTextureConfig(
                            192, 192, 2.0F, 3500.0F, 0xD01A1A40, 0xD0B0C4DE, 0xFF4169E1, 0xFF87CEEB,
                            0.0F, -20.0F, false, new String[]{"dream_sakura_blue_archive:textures/screens/kayoko.png"},
                            new int[]{403}, new int[]{1022}, 132, 132, 0.0F, 0.0F, 40, 0.75F, 1.2F, 3, 2000L, true, false
                    ))
                    .build()
    );
    //梦光环 注册YUME_HALO
    public static final RegistryObject<Item> YUME_HALO = ITEMS.register(
            "yume_halo",
            () -> new DecorationItem.Builder(
                    "yume_halo",
                    new Item.Properties()
                            .stacksTo(1)
                            .rarity(Rarity.EPIC)
            )
                    .withHaloLevelSystem()
                    .withBAElementsParticles()
                    .withGlowColor(AMBER_GLOW)
                    .withGlowIntensity(1f)
                    .withTooltipTextureConfig(new com.core.dream_sakura.api.tooltip.DreamSakuraTooltipAPI.DreamSakuraTextureConfig(
                            192, 192, 3.0F, 2500.0F, 0xD0F3E5F5, 0xD0E1BEE7, 0xFFDDA0DD, 0xFFBA55D3,
                            0.0F, -20.0F, false, new String[]{"dream_sakura_blue_archive:textures/screens/yume.png"},
                            new int[]{609}, new int[]{1229}, 132, 132, 0.0F, 0.0F, 40, 0.8F, 1.4F, 3, 2000L, true, false
                    ))
                    .build()
    );
    //砂狼黑子光环 注册KUROKO_HALO
    public static final RegistryObject<Item> KUROKO_HALO = ITEMS.register(
            "kuroko_halo",
            () -> new DecorationItem.Builder(
                    "kuroko_halo",
                    new Item.Properties()
                            .stacksTo(1)
                            .rarity(Rarity.EPIC)
            )
                    .withHaloLevelSystem()
                    .withBAElementsParticles()
                    .withGlowColor(VIOLET_GLOW)
                    .withGlowIntensity(1f)
                    .withTooltipTextureConfig(new com.core.dream_sakura.api.tooltip.DreamSakuraTooltipAPI.DreamSakuraTextureConfig(
                            192, 192, 3.5F, 2000.0F, 0xD01A0020, 0xD04A0080, 0xFF8B00FF, 0xFFD8BFD8,
                            0.0F, -20.0F, false, new String[]{"dream_sakura_blue_archive:textures/screens/kuroko.png"},
                            new int[]{1125}, new int[]{1517}, 132, 132, 0.0F, 0.0F, 40, 0.75F, 1.5F, 3, 2000L, true, false
                    ))
                    .build()
    );
    //春原心奈光环 注册KARENA_HALO
    public static final RegistryObject<Item> KARENA_HALO = ITEMS.register(
            "karena_halo",
            () -> new DecorationItem.Builder(
                    "karena_halo",
                    new Item.Properties()
                            .stacksTo(1)
                            .rarity(Rarity.EPIC)
            )
                    .withHaloLevelSystem()
                    .withBAElementsParticles()
                    .withGlowColor(PINK_LIGHT_GLOW)
                    .withGlowIntensity(1f)
                    .withTooltipTextureConfig(new com.core.dream_sakura.api.tooltip.DreamSakuraTooltipAPI.DreamSakuraTextureConfig(
                            192, 192, 3.0F, 2500.0F, 0xD0FFE4E1, 0xD0FFB6C1, 0xFFFF69B4, 0xFFFF1493,
                            0.0F, -20.0F, false, new String[]{"dream_sakura_blue_archive:textures/screens/karena.png"},
                            new int[]{706}, new int[]{1028}, 132, 132, 0.0F, 0.0F, 40, 0.8F, 1.3F, 3, 2000L, true, false
                    ))
                    .build()
    );
    private static final float[] PURPLE_GLOW = {0.6f, 0.2f, 0.8f}; // 紫色
    //日奈光环注册
    public static final RegistryObject<Item> HINA_HALO = ITEMS.register(
            "hina_halo",
            () -> new DecorationItem.Builder(
                    "hina_halo",
                    new Item.Properties()
                            .stacksTo(1)
                            .rarity(Rarity.EPIC)
            )
                    .withBAElementsParticles()
                    .withGlowColor(PURPLE_GLOW)
                    .withGlowIntensity(1f)
                    .withHaloLevelSystem()
                    .withSkillBinding(RegistryActiveSkill.Hina_Halo_Skill.get())
                    .withTooltipTextureConfig(new com.core.dream_sakura.api.tooltip.DreamSakuraTooltipAPI.DreamSakuraTextureConfig(
                            320, 192, 1.2F, 5000.0F, 0xD52B1B3D, 0xD5800040, 0xFFE6E6FA, 0xFFC8A2C8,
                            0.0F, -35.0F, false, new String[]{"dream_sakura_blue_archive:textures/screens/hina.png"},
                            new int[]{1400}, new int[]{927}, 175, 175, 0.0F, 0.0F, 52, 0.8F, 0.6F, 4, 2500L, true, false
                    ))
                    .withCurioEquipCallback(
                            (slotContext, stack) -> {
                                RegistryPassiveSkill.Hina_Halo_Skill_0(slotContext, stack);
                                RegistryPassiveSkill.Hina_Halo_Skill_2(slotContext, stack);
                                RegistryPassiveSkill.Hina_Halo_Skill_3(slotContext, stack);

                            }
                    ).build()
    );
    private static final float[] WHITE_GLOW = {1.000f, 1.000f, 1.000f}; // 白色
    //白洲梓光环 注册SHIRASUAZUSA_HALO
    public static final RegistryObject<Item> SHIRASUAZUSA_HALO = ITEMS.register(
            "shirasuazusa_halo",
            () -> new DecorationItem.Builder(
                    "shirasuazusa_halo",
                    new Item.Properties()
                            .stacksTo(1).
                            rarity(Rarity.EPIC)
            )
                    .withHaloLevelSystem()
                    .withBAElementsParticles()
                    .withGlowIntensity(0.8f)
                    .withGlowColor(WHITE_GLOW)
                    .withTooltipTextureConfig(new com.core.dream_sakura.api.tooltip.DreamSakuraTooltipAPI.DreamSakuraTextureConfig(
                            192, 192, 3.0F, 2500.0F, 0xD0F0F8FF, 0xD0DDA0DD, 0xFFFFB6C1, 0xFFE6E6FA,
                            0.0F, -20.0F, false, new String[]{"dream_sakura_blue_archive:textures/screens/shirasuazusa.png"},
                            new int[]{849}, new int[]{1128}, 132, 132, 0.0F, 0.0F, 40, 0.75F, 1.3F, 3, 2000L, true, false
                    ))
                    //.withSkillBinding(RegistryActiveSkill.SHIRASUAZUSA_Halo_Skill.get())
                    //.withCurioEquipCallback()
                    .build()
    );
    //白子光环注册
    public static final RegistryObject<Item> SHIROKO_HALO = ITEMS.register(
            "shiroko_halo",
            () -> new DecorationItem.Builder(
                    "shiroko_halo",
                    new Item.Properties()
                            .stacksTo(1).
                            rarity(Rarity.EPIC)
            )
                    .withHaloLevelSystem()
                    .withBAElementsParticles()
                    .withGlowIntensity(0.8f)
                    .withGlowColor(WHITE_GLOW)
                    .withSkillBinding(RegistryActiveSkill.SHIROKO_Halo_Skill.get())
                    .withTooltipTextureConfig(new com.core.dream_sakura.api.tooltip.DreamSakuraTooltipAPI.DreamSakuraTextureConfig(
                            192, 192, 3.5F, 2000.0F, 0xD0A2C2E1, 0xD0B0C4DE, 0xFF00BFFF, 0xFFF0F8FF,
                            0.0F, -20.0F, false, new String[]{"dream_sakura_blue_archive:textures/screens/shiroko.png"},
                            new int[]{540}, new int[]{1024}, 135, 135, 0.0F, 0.0F, 40, 0.8F, 1.6F, 3, 2000L, true, false
                    ))
                    //.withCurioEquipCallback()
                    .build()
    );
    //K螺诺亚光环注册kluonuoya_halo
    public static final RegistryObject<Item> KLUONUOYA_HALO = ITEMS.register(
            "kluonuoya_halo",
            () -> new DecorationItem.Builder(
                    "kluonuoya_halo",
                    new Item.Properties()
                            .stacksTo(1).
                            rarity(Rarity.EPIC)
            )
                    .withGlowIntensity(1f)
                    .withGlowColor(WHITE_GLOW)
                    .withTooltipTextureConfig(new com.core.dream_sakura.api.tooltip.DreamSakuraTooltipAPI.DreamSakuraTextureConfig(
                            192, 192, 2.0F, 3500.0F, 0xD0F8F8FF, 0xD0E6E6FA, 0xFF87CEEB, 0xFFFFFFFF,
                            0.0F, -20.0F, false, new String[]{"dream_sakura_blue_archive:textures/screens/kluonuoya.png"},
                            new int[]{3000}, new int[]{4000}, 132, 132, 0.0F, 0.0F, 40, 0.75F, 1.1F, 3, 2500L, true, false
                    ))
                    //.withSkillBinding(RegistryActiveSkill.KLUONUOYA_Halo_Skill.get())
                    .withCurioEquipCallback(
                            (slotContext, stack) -> {
                                // 启用辅助瞄准被动技能（第一个被动技能）
                                RegistryPassiveSkill.KLUONUOYA_Halo_Aim_Assist(slotContext, stack);
                            }
                    )
                    .build()
    );
    //七叶渡羽光环注册Sakuluna_Halo
    public static final RegistryObject<Item> SAKULUNA_HALO = ITEMS.register(
            "sakuluna_halo",
            () -> new DecorationItem.Builder(
                    "sakuluna_halo",
                    new Item.Properties()
                            .stacksTo(1).
                            rarity(Rarity.EPIC)
            )
                    .withHaloLevelSystem()
                    .withGlowIntensity(1f)
                    .withGlowColor(WHITE_GLOW)
                    .withTooltipTextureConfig(new com.core.dream_sakura.api.tooltip.DreamSakuraTooltipAPI.DreamSakuraTextureConfig(
                            192, 192, 4.0F, 1200.0F, 0xD0FFFFFF, 0xD0FFC0CB, 0xFFFF69B4, 0xFFE6E6FA,
                            0.0F, -20.0F, false, new String[]{"dream_sakura_blue_archive:textures/screens/sakuluna.png"},
                            new int[]{3672}, new int[]{1964}, 135, 135, 0.0F, 0.0F, 42, 0.85F, 2.2F, 3, 1500L, true, false
                    ))
                    //.withSkillBinding(RegistryActiveSkill.SAKULUNA_Halo_Skill.get())
                    //.withCurioEquipCallback(
                    //        (slotContext, stack) -> {
                    // 技能绑定逻辑
                    //            RegistryActiveSkill.SAKULUNA_Halo_Skill.get().onEquip(slotContext, stack);
                    //        }
                    //)
                    .build()
    );
    //橘光光环注册 JUGUANG_HALO
    public static final RegistryObject<Item> JUGUANG_HALO = ITEMS.register(
            "juguang_halo",
            () -> new DecorationItem.Builder(
                    "juguang_halo",
                    new Item.Properties()
                            .stacksTo(1)
                            .rarity(Rarity.EPIC)
            )
                    .withHaloLevelSystem()
                    .withBAElementsParticles()
                    .withGlowColor(JUGUANG_GLOW)
                    .withGlowIntensity(1f)
                    .withTooltipTextureConfig(new com.core.dream_sakura.api.tooltip.DreamSakuraTooltipAPI.DreamSakuraTextureConfig(
                            192, 192, 2.5F, 3000.0F, 0xD01A3A1A, 0xD0008800, 0xFF00FF00, 0xFF98FB98,
                            0.0F, -20.0F, false, new String[]{"dream_sakura_blue_archive:textures/screens/juguang.png"},
                            new int[]{466}, new int[]{1027}, 132, 132, 0.0F, 0.0F, 40, 0.8F, 1.2F, 3, 2000L, true, false
                    ))
                    .build()
    );
    //橘望光环注册 JUWANG_HALO
    public static final RegistryObject<Item> JUWANG_HALO = ITEMS.register(
            "juwang_halo",
            () -> new DecorationItem.Builder(
                    "juwang_halo",
                    new Item.Properties()
                            .stacksTo(1)
                            .rarity(Rarity.EPIC)
            )
                    .withHaloLevelSystem()
                    .withBAElementsParticles()
                    .withGlowColor(JUWANG_GLOW)
                    .withGlowIntensity(1f)
                    .withTooltipTextureConfig(new com.core.dream_sakura.api.tooltip.DreamSakuraTooltipAPI.DreamSakuraTextureConfig(
                            192, 192, 2.5F, 3000.0F, 0xD01A2E1A, 0xD0228B22, 0xFF32CD32, 0xFF90EE90,
                            0.0F, -20.0F, false, new String[]{"dream_sakura_blue_archive:textures/screens/juwang.png"},
                            new int[]{536}, new int[]{1030}, 132, 132, 0.0F, 0.0F, 40, 0.8F, 1.2F, 3, 2000L, true, false
                    ))
                    .build()
    );
    //凯伊光环注册 KAIYI_HALO
    public static final RegistryObject<Item> KAIYI_HALO = ITEMS.register(
            "kaiyi_halo",
            () -> new DecorationItem.Builder(
                    "kaiyi_halo",
                    new Item.Properties()
                            .stacksTo(1)
                            .rarity(Rarity.EPIC)
            )
                    .withHaloLevelSystem()
                    .withBAElementsParticles()
                    .withGlowColor(KAIYI_GLOW)
                    .withGlowIntensity(1f)
                    .withTooltipTextureConfig(new com.core.dream_sakura.api.tooltip.DreamSakuraTooltipAPI.DreamSakuraTextureConfig(
                            192, 192, 2.0F, 3500.0F, 0xD02A1A2A, 0xD0C71585, 0xFFFF69B4, 0xFFFFB6C1,
                            0.0F, -20.0F, false, new String[]{"dream_sakura_blue_archive:textures/screens/kaiyi.png"},
                            new int[]{1105}, new int[]{1300}, 132, 132, 0.0F, 0.0F, 40, 0.8F, 1.2F, 3, 2000L, true, false
                    ))
                    .build()
    );
    //玛丽光环注册 MARI_HALO
    public static final RegistryObject<Item> MARI_HALO = ITEMS.register(
            "mari_halo",
            () -> new DecorationItem.Builder(
                    "mari_halo",
                    new Item.Properties()
                            .stacksTo(1)
                            .rarity(Rarity.EPIC)
            )
                    .withHaloLevelSystem()
                    .withBAElementsParticles()
                    .withGlowColor(MARI_GLOW)
                    .withGlowIntensity(1f)
                    .withTooltipTextureConfig(new com.core.dream_sakura.api.tooltip.DreamSakuraTooltipAPI.DreamSakuraTextureConfig(
                            192, 192, 2.5F, 3000.0F, 0xD0332200, 0xD0B8860B, 0xFFFFD700, 0xFFFFFACD,
                            0.0F, -20.0F, false, new String[]{"dream_sakura_blue_archive:textures/screens/mari.png"},
                            new int[]{577}, new int[]{1280}, 132, 132, 0.0F, 0.0F, 40, 0.8F, 1.2F, 3, 2000L, true, false
                    ))
                    .build()
    );
    //圣娅光环注册 SEIA_HALO
    public static final RegistryObject<Item> SEIA_HALO = ITEMS.register(
            "seia_halo",
            () -> new DecorationItem.Builder(
                    "seia_halo",
                    new Item.Properties()
                            .stacksTo(1)
                            .rarity(Rarity.EPIC)
            )
                    .withHaloLevelSystem()
                    .withBAElementsParticles()
                    .withGlowColor(SEIA_GLOW)
                    .withGlowIntensity(1f)
                    .withTooltipTextureConfig(new com.core.dream_sakura.api.tooltip.DreamSakuraTooltipAPI.DreamSakuraTextureConfig(
                            192, 192, 2.5F, 3500.0F, 0xD0332A1A, 0xD0B8860B, 0xFFFFD700, 0xFFFFE4B5,
                            0.0F, -20.0F, false, new String[]{"dream_sakura_blue_archive:textures/screens/seia.png"},
                            new int[]{824}, new int[]{1400}, 132, 132, 0.0F, 0.0F, 40, 0.8F, 1.2F, 3, 2000L, true, false
                    ))
                    .build()
    );
    //瞬光环注册 SHUN_HALO
    public static final RegistryObject<Item> SHUN_HALO = ITEMS.register(
            "shun_halo",
            () -> new DecorationItem.Builder(
                    "shun_halo",
                    new Item.Properties()
                            .stacksTo(1)
                            .rarity(Rarity.EPIC)
            )
                    .withHaloLevelSystem()
                    .withBAElementsParticles()
                    .withGlowColor(SHUN_GLOW)
                    .withGlowIntensity(1f)
                    .withTooltipTextureConfig(new com.core.dream_sakura.api.tooltip.DreamSakuraTooltipAPI.DreamSakuraTextureConfig(
                            192, 192, 1.8F, 3500.0F, 0xD00A2E1A, 0xD0006B3F, 0xFF00FA9A, 0xFF98FB98,
                            0.0F, -20.0F, false, new String[]{"dream_sakura_blue_archive:textures/screens/shun.png"},
                            new int[]{1369}, new int[]{2013}, 132, 132, 0.0F, 0.0F, 40, 0.8F, 1.2F, 3, 2000L, true, false
                    ))
                    .build()
    );
    //小绿光环注册 MIDORI_HALO
    public static final RegistryObject<Item> MIDORI_HALO = ITEMS.register(
            "midori_halo",
            () -> new DecorationItem.Builder(
                    "midori_halo",
                    new Item.Properties()
                            .stacksTo(1)
                            .rarity(Rarity.EPIC)
            )
                    .withHaloLevelSystem()
                    .withBAElementsParticles()
                    .withGlowColor(MIDORI_GLOW)
                    .withGlowIntensity(1f)
                    .withTooltipTextureConfig(new com.core.dream_sakura.api.tooltip.DreamSakuraTooltipAPI.DreamSakuraTextureConfig(
                            192, 192, 2.5F, 3000.0F, 0xD01A3020, 0xD0228B22, 0xFF7CFC00, 0xFFADFF2F,
                            0.0F, -20.0F, false, new String[]{"dream_sakura_blue_archive:textures/screens/midori.png"},
                            new int[]{905}, new int[]{1200}, 132, 132, 0.0F, 0.0F, 40, 0.8F, 1.2F, 3, 2000L, true, false
                    ))
                    .build()
    );
    //小桃光环注册 MOMOI_HALO
    public static final RegistryObject<Item> MOMOI_HALO = ITEMS.register(
            "momoi_halo",
            () -> new DecorationItem.Builder(
                    "momoi_halo",
                    new Item.Properties()
                            .stacksTo(1)
                            .rarity(Rarity.EPIC)
            )
                    .withHaloLevelSystem()
                    .withBAElementsParticles()
                    .withGlowColor(MOMOI_GLOW)
                    .withGlowIntensity(1f)
                    .withTooltipTextureConfig(new com.core.dream_sakura.api.tooltip.DreamSakuraTooltipAPI.DreamSakuraTextureConfig(
                            192, 192, 2.5F, 3000.0F, 0xD02A1515, 0xD0FF69B4, 0xFFFF69B4, 0xFFFFC0CB,
                            0.0F, -20.0F, false, new String[]{"dream_sakura_blue_archive:textures/screens/momoi.png"},
                            new int[]{887}, new int[]{1054}, 132, 132, 0.0F, 0.0F, 40, 0.8F, 1.2F, 3, 2000L, true, false
                    ))
                    .build()
    );
    //小夏光环注册 NATSU_HALO
    public static final RegistryObject<Item> NATSU_HALO = ITEMS.register(
            "natsu_halo",
            () -> new DecorationItem.Builder(
                    "natsu_halo",
                    new Item.Properties()
                            .stacksTo(1)
                            .rarity(Rarity.EPIC)
            )
                    .withHaloLevelSystem()
                    .withBAElementsParticles()
                    .withGlowColor(NATSU_GLOW)
                    .withGlowIntensity(1f)
                    .withTooltipTextureConfig(new com.core.dream_sakura.api.tooltip.DreamSakuraTooltipAPI.DreamSakuraTextureConfig(
                            192, 192, 2.5F, 3000.0F, 0xD02A1A20, 0xD0DB7093, 0xFFFF69B4, 0xFFFFB6C1,
                            0.0F, -20.0F, false, new String[]{"dream_sakura_blue_archive:textures/screens/natsu.png"},
                            new int[]{403}, new int[]{1280}, 132, 132, 0.0F, 0.0F, 40, 0.8F, 1.2F, 3, 2000L, true, false
                    ))
                    .build()
    );
    //柚子光环注册 YUZU_HALO
    public static final RegistryObject<Item> YUZU_HALO = ITEMS.register(
            "yuzu_halo",
            () -> new DecorationItem.Builder(
                    "yuzu_halo",
                    new Item.Properties()
                            .stacksTo(1)
                            .rarity(Rarity.EPIC)
            )
                    .withHaloLevelSystem()
                    .withBAElementsParticles()
                    .withGlowColor(YUZU_GLOW)
                    .withGlowIntensity(1f)
                    .withTooltipTextureConfig(new com.core.dream_sakura.api.tooltip.DreamSakuraTooltipAPI.DreamSakuraTextureConfig(
                            192, 192, 2.5F, 3000.0F, 0xD0333000, 0xD0FFA500, 0xFFFFD700, 0xFFFFF0B0,
                            0.0F, -20.0F, false, new String[]{"dream_sakura_blue_archive:textures/screens/yuzu.png"},
                            new int[]{601}, new int[]{1102}, 132, 132, 0.0F, 0.0F, 40, 0.8F, 1.2F, 3, 2000L, true, false
                    ))
                    .build()
    );
    //联邦学生会长光环注册 REN_HALO
    public static final RegistryObject<Item> REN_HALO = ITEMS.register(
            "ren_halo",
            () -> new DecorationItem.Builder(
                    "ren_halo",
                    new Item.Properties()
                            .stacksTo(1)
                            .rarity(Rarity.EPIC)
            )
                    .withHaloLevelSystem()
                    .withBAElementsParticles()
                    .withGlowColor(REN_GLOW)
                    .withGlowIntensity(1f)
                    .withTooltipTextureConfig(new com.core.dream_sakura.api.tooltip.DreamSakuraTooltipAPI.DreamSakuraTextureConfig(
                            192, 192, 2.5F, 3500.0F, 0xD00A1A33, 0xD04169E1, 0xFF87CEEB, 0xFFE0FFFF,
                            0.0F, -20.0F, false, new String[]{"dream_sakura_blue_archive:textures/screens/ren.png"},
                            new int[]{500}, new int[]{1474}, 132, 132, 0.0F, 0.0F, 40, 0.8F, 1.2F, 3, 2000L, true, false
                    ))
                    .build()
    );
    private static final float[] GREEN_GLOW = {0.9059f, 0.9922f, 0.0f}; // 野宫绿
    //十六夜野宫注册NONOMI_HALO
    public static final RegistryObject<Item> NONOMI_HALO = ITEMS.register(
            "nonomi_halo",
            () -> new DecorationItem.Builder(
                    "nonomi_halo",
                    new Item.Properties()
                            .stacksTo(1).
                            rarity(Rarity.EPIC)
            )
                    .withHaloLevelSystem()
                    .withBAElementsParticles()
                    .withGlowIntensity(1f)
                    .withGlowColor(GREEN_GLOW)
                    .withTooltipTextureConfig(new com.core.dream_sakura.api.tooltip.DreamSakuraTooltipAPI.DreamSakuraTextureConfig(
                            192, 192, 2.0F, 3000.0F, 0xD0FFFACD, 0xD03CB371, 0xFF32CD32, 0xFFFFD700,
                            0.0F, -20.0F, false, new String[]{"dream_sakura_blue_archive:textures/screens/nonomi.png"},
                            new int[]{614}, new int[]{1024}, 135, 135, 0.0F, 0.0F, 40, 0.8F, 1.2F, 3, 2500L, true, false
                    ))
//                    .withSkillBinding(RegistryActiveSkill.NONOMI_Halo_Skill.get())
//                    .withCurioEquipCallback(
//                            (slotContext, stack) -> {
//                                // 技能绑定逻辑
//                            }

                    .build()
    );

    // 圣园未花光环（OBJ模型）
    public static final RegistryObject<Item> MIKA_HALO = ITEMS.register(
            "mika_halo",
            () -> new MikaObjHaloItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC))
    );

}
