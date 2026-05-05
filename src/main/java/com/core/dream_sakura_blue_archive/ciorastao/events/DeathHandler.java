package com.core.dream_sakura_blue_archive.ciorastao.events;

import com.core.dream_sakura_blue_archive.ciorastao.dream_sakura_blue_archive;
import com.core.dream_sakura_blue_archive.ciorastao.items.DecorationItem;
import com.core.dream_sakura_blue_archive.ciorastao.util.OtherHelper;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Random;

@Mod.EventBusSubscriber(modid = dream_sakura_blue_archive.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class DeathHandler {
    private static Random RANDOM = new Random();

    // 击杀生物获取经验逻辑
    @SubscribeEvent
    public static void GainExpEvent(LivingDeathEvent event) {
        if (event.getSource().getEntity() instanceof Player player && event.getEntity() instanceof Monster monster) {
            ItemStack haloStack = OtherHelper.getCurio(player, "halo", 0);
            if (haloStack.getItem() != Items.AIR) {
                // 检查光环物品是否启用了等级系统
                boolean supportsLevelSystem = false;

                // 如果物品是 DecorationItem 类型，检查它是否启用了光环等级系统
                if (haloStack.getItem() instanceof DecorationItem) {
                    DecorationItem decorationItem = (DecorationItem) haloStack.getItem();
                    supportsLevelSystem = decorationItem.hasHaloLevelSystem();
                } else {
                    // 对于其他类型的物品，检查它是否已经初始化了等级系统
                    CompoundTag haloTag = haloStack.getOrCreateTag();
                    supportsLevelSystem = haloTag.contains("level") && haloTag.contains("xp") && haloTag.contains("maxXp");
                }

                // 只有支持等级系统的光环才给予经验
                if (!supportsLevelSystem) {
                    return;
                }

                CompoundTag haloTag = haloStack.getOrCreateTag();
                int itemLevel = haloTag.getInt("level");
                int itemXp = haloTag.getInt("xp");
                int itemMaxXp = haloTag.getInt("maxXp");

                // 检查是否已达到满级 90 级且经验值已满，满级后不再获取经验
                if (itemLevel >= 90 && itemXp >= itemMaxXp) {
                    return;
                }

                int randomExp = RANDOM.nextInt(2, 17);

                if (monster.getType().is(TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath("forge", "bosses")))) {
                    randomExp = RANDOM.nextInt(50, 201);
                }

                // 添加经验值，但不能超过当前等级的最大经验值
                int newXp = itemXp + randomExp;

                // 循环处理升级，直到经验不足或达到满级
                while (newXp >= itemMaxXp && itemLevel < 90) {
                    itemLevel++;
                    itemMaxXp = (int) OtherHelper.expCalculate(itemLevel);
                    newXp = newXp - itemMaxXp;

                    // 确保经验值不为负数
                    if (newXp < 0) {
                        newXp = 0;
                    }

                    // 达到满级后，清空多余经验
                    if (itemLevel >= 90) {
                        newXp = 0;
                        break;
                    }
                }

                // 如果未达到 90 级但经验值超过了当前等级上限，则截断到上限
                if (itemLevel < 90 && newXp > itemMaxXp) {
                    newXp = itemMaxXp;
                }

                haloTag.putInt("level", itemLevel);
                haloTag.putInt("xp", newXp);
                haloTag.putInt("maxXp", itemMaxXp);
            }
        }
    }

    // 星野光环被动1吸血逻辑
    @SubscribeEvent
    public static void HoshinoHaloBEvent(LivingDeathEvent event) {
        if (event.getSource().getEntity() instanceof Player player) {
            if (OtherHelper.getCuriosItem(player, "halo", "dream_sakura_blue_archive:hoshino_halo")) {
                float healAmount = player.getMaxHealth() * 0.05f;
                player.heal(healAmount);
            }
        }
    }

    // 日奈光环被动死亡逻辑
    @SubscribeEvent
    public static void HinaHaloBEvent(LivingDeathEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (OtherHelper.getCuriosItem(player, "halo", "dream_sakura_blue_archive:hina_halo")) {
                CompoundTag playerData = player.getPersistentData();
                CompoundTag skillData = playerData.contains("SkillData") ? playerData.getCompound("SkillData") : new CompoundTag();
                if (skillData.contains("HinaHaloData")) {
                    CompoundTag hinaHaloData = skillData.getCompound("HinaHaloData");
                    // 移除被动2效果
                    hinaHaloData.remove("Passive2Active");
                    // 移除被动3效果
                    hinaHaloData.remove("Passive3Active");
                }
                ;
            }
        }
    }
}
