package com.core.dream_sakura_blue_archive.ciorastao.events;

import com.core.dream_sakura_blue_archive.ciorastao.ai.TargetHinaHaloPlayerGoal;
import com.core.dream_sakura_blue_archive.ciorastao.dream_sakura_blue_archive;
import net.minecraft.world.entity.monster.Monster;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = dream_sakura_blue_archive.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EntityAIHandler {
    @SubscribeEvent
    public static void onEntityJoinWorld(EntityJoinLevelEvent event) {
        if (!event.getLevel().isClientSide() && event.getEntity() instanceof Monster monster) {
            monster.targetSelector.addGoal(1, new TargetHinaHaloPlayerGoal(monster));
        }
    }
}
