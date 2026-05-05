package com.core.dream_sakura_blue_archive.ciorastao.events;

import com.core.dream_sakura_blue_archive.ciorastao.dream_sakura_blue_archive;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootTableReference;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = dream_sakura_blue_archive.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class LootTableEventHandler {
    private static final ResourceLocation ABANDONED_MINESHAFT = ResourceLocation.fromNamespaceAndPath("minecraft", "chests/abandoned_mineshaft");
    private static final ResourceLocation ANCIENT_CITY = ResourceLocation.fromNamespaceAndPath("minecraft", "chests/ancient_city");
    private static final ResourceLocation ANCIENT_CITY_ICE_BOX = ResourceLocation.fromNamespaceAndPath("minecraft", "chests/ancient_city_ice_box");
    private static final ResourceLocation BURIED_TREASURE = ResourceLocation.fromNamespaceAndPath("minecraft", "chests/buried_treasure");
    private static final ResourceLocation DESERT_PYRAMID = ResourceLocation.fromNamespaceAndPath("minecraft", "chests/desert_pyramid");
    private static final ResourceLocation IGLOO_CHEST = ResourceLocation.fromNamespaceAndPath("minecraft", "chests/igloo_chest");
    private static final ResourceLocation JUNGLE_TEMPLE = ResourceLocation.fromNamespaceAndPath("minecraft", "chests/jungle_temple");
    private static final ResourceLocation PILLAGER_OUTPOST = ResourceLocation.fromNamespaceAndPath("minecraft", "chests/pillager_outpost");
    private static final ResourceLocation RUINED_PORTAL = ResourceLocation.fromNamespaceAndPath("minecraft", "chests/ruined_portal");
    private static final ResourceLocation SHIPWRECK_MAP = ResourceLocation.fromNamespaceAndPath("minecraft", "chests/shipwreck_map");
    private static final ResourceLocation SHIPWRECK_SUPPLY = ResourceLocation.fromNamespaceAndPath("minecraft", "chests/shipwreck_supply");
    private static final ResourceLocation SHIPWRECK_TREASURE = ResourceLocation.fromNamespaceAndPath("minecraft", "chests/shipwreck_treasure");
    private static final ResourceLocation SIMPLE_DUNGEON = ResourceLocation.fromNamespaceAndPath("minecraft", "chests/simple_dungeon");
    private static final ResourceLocation SPAWN_BONUS_CHEST = ResourceLocation.fromNamespaceAndPath("minecraft", "chests/spawn_bonus_chest");
    private static final ResourceLocation STRONGHOLD_CORRIDOR = ResourceLocation.fromNamespaceAndPath("minecraft", "chests/stronghold_corridor");
    private static final ResourceLocation STRONGHOLD_CROSSING = ResourceLocation.fromNamespaceAndPath("minecraft", "chests/stronghold_crossing");
    private static final ResourceLocation STRONGHOLD_LIBRARY = ResourceLocation.fromNamespaceAndPath("minecraft", "chests/stronghold_library");
    private static final ResourceLocation UNDERWATER_RUIN_BIG = ResourceLocation.fromNamespaceAndPath("minecraft", "chests/underwater_ruin_big");
    private static final ResourceLocation UNDERWATER_RUIN_SMALL = ResourceLocation.fromNamespaceAndPath("minecraft", "chests/underwater_ruin_small");
    private static final ResourceLocation WOODLAND_MANSION = ResourceLocation.fromNamespaceAndPath("minecraft", "chests/woodland_mansion");


    private static final ResourceLocation BASTION_BRIDGE = ResourceLocation.fromNamespaceAndPath("minecraft", "chests/bastion_bridge");
    private static final ResourceLocation BASTION_HOGLIN_STABLE = ResourceLocation.fromNamespaceAndPath("minecraft", "chests/bastion_hoglin_stable");
    private static final ResourceLocation BASTION_OTHER = ResourceLocation.fromNamespaceAndPath("minecraft", "chests/bastion_other");
    private static final ResourceLocation BASTION_TREASURE = ResourceLocation.fromNamespaceAndPath("minecraft", "chests/bastion_treasure");
    private static final ResourceLocation NETHER_BRIDGE = ResourceLocation.fromNamespaceAndPath("minecraft", "chests/nether_bridge");

    private static final ResourceLocation END_CITY_TREASURE = ResourceLocation.fromNamespaceAndPath("minecraft", "chests/end_city_treasure");

    private static final ResourceLocation OVERWORLD_CHEST = ResourceLocation.fromNamespaceAndPath(dream_sakura_blue_archive.MODID, "chests/overworld_chest");
    private static final ResourceLocation NETHER_CHEST = ResourceLocation.fromNamespaceAndPath(dream_sakura_blue_archive.MODID, "chests/nether_chest");
    private static final ResourceLocation END_CHEST = ResourceLocation.fromNamespaceAndPath(dream_sakura_blue_archive.MODID, "chests/end_chest");

    @SubscribeEvent
    public static void onLootTableLoad(LootTableLoadEvent event) {
        ResourceLocation lootTableName = event.getName();
        if (
                lootTableName.equals(ABANDONED_MINESHAFT) ||
                        lootTableName.equals(ANCIENT_CITY) ||
                        lootTableName.equals(ANCIENT_CITY_ICE_BOX) ||
                        lootTableName.equals(BURIED_TREASURE) ||
                        lootTableName.equals(DESERT_PYRAMID) ||
                        lootTableName.equals(IGLOO_CHEST) ||
                        lootTableName.equals(JUNGLE_TEMPLE) ||
                        lootTableName.equals(PILLAGER_OUTPOST) ||
                        lootTableName.equals(RUINED_PORTAL) ||
                        lootTableName.equals(SHIPWRECK_MAP) ||
                        lootTableName.equals(SHIPWRECK_SUPPLY) ||
                        lootTableName.equals(SHIPWRECK_TREASURE) ||
                        lootTableName.equals(SIMPLE_DUNGEON) ||
                        lootTableName.equals(SPAWN_BONUS_CHEST) ||
                        lootTableName.equals(STRONGHOLD_CORRIDOR) ||
                        lootTableName.equals(STRONGHOLD_CROSSING) ||
                        lootTableName.equals(STRONGHOLD_LIBRARY) ||
                        lootTableName.equals(UNDERWATER_RUIN_BIG) ||
                        lootTableName.equals(UNDERWATER_RUIN_SMALL) ||
                        lootTableName.equals(WOODLAND_MANSION)
        ) {
            event.getTable().addPool(
                    LootPool.lootPool()
                            .add(LootTableReference.lootTableReference(OVERWORLD_CHEST))
                            .build()
            );
        }

        if (
                lootTableName.equals(BASTION_BRIDGE) ||
                        lootTableName.equals(BASTION_HOGLIN_STABLE) ||
                        lootTableName.equals(BASTION_OTHER) ||
                        lootTableName.equals(BASTION_TREASURE) ||
                        lootTableName.equals(NETHER_BRIDGE)
        ) {
            event.getTable().addPool(
                    LootPool.lootPool()
                            .add(LootTableReference.lootTableReference(NETHER_CHEST))
                            .build()
            );
        }

        if (
                lootTableName.equals(END_CITY_TREASURE)
        ) {
            event.getTable().addPool(
                    LootPool.lootPool()
                            .add(LootTableReference.lootTableReference(END_CHEST))
                            .build()
            );
        }
    }
}
