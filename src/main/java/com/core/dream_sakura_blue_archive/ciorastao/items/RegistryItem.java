package com.core.dream_sakura_blue_archive.ciorastao.items;

import com.core.dream_sakura_blue_archive.ciorastao.dream_sakura_blue_archive;
import com.core.dream_sakura_blue_archive.ciorastao.util.RegistryActiveSkill;
import com.core.dream_sakura_blue_archive.ciorastao.util.RegistryPassiveSkill;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class RegistryItem {
    // 创建注册器
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, dream_sakura_blue_archive.MODID);

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
    private static final float[] ALS_GLOW = {0.8235f, 0.9961f, 0.9137f}; // 爱丽丝蓝
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
                    .withGlowIntensity(1f)//发光强度控制
                    .withTooltipTextureConfig(new com.core.dream_sakura.api.tooltip.DreamSakuraTooltipAPI.DreamSakuraTextureConfig(
                            192, 192, 4.0F, 1500.0F, 0xD00A0A1A, 0xD04169E1, 0xFF00FFFF, 0xFFFFFFFF,
                            0.0F, -25.0F, false, new String[]{"dream_sakura_blue_archive:textures/screens/tendouaris.png"},
                            new int[]{916}, new int[]{1035}, 130, 130, 0.0F, 0.0F, 40, 0.8F, 1.8F, 3, 1000L, true, false
                    ))
                    .withSkillBinding(RegistryActiveSkill.TENDOUARIS_HALO_Skill.get())//主动技能
                    .withCurioEquipCallback(//被动技能
                            (slotContext, stack) -> {
                                RegistryPassiveSkill.TENDOUARIS_Halo_Skill_2(slotContext, stack);
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

//                    .withSkillBinding(RegistryActiveSkill.AYANE_Halo_Skill.get())
//                    .withCurioEquipCallback(
//                            (slotContext, stack) -> {}
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
                                RegistryPassiveSkill.Hina_Halo_Skill_3_5(slotContext, stack);

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
                    .withGlowIntensity(1f)
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
                    .withGlowIntensity(1f)
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
                    .withHaloLevelSystem()
                    .withBAElementsParticles()
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
}
