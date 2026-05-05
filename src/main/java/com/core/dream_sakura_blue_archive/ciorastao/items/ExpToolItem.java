package com.core.dream_sakura_blue_archive.ciorastao.items;

import com.core.dream_sakura_blue_archive.ciorastao.dream_sakura_blue_archive;
import com.core.dream_sakura_blue_archive.ciorastao.util.OtherHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * ExpToolItem类，继承自Item类，用于创建具有经验值的工具物品
 */
public class ExpToolItem extends Item {
    // 经验值属性，用于存储该物品提供的经验值
    private final int xp;

    /**
     * ExpToolItem的构造函数
     *
     * @param properties 物品的属性配置
     * @param xp         该物品提供的经验值
     */
    public ExpToolItem(Properties properties, int xp) {
        super(properties);
        this.xp = xp;
    }

    /**
     * 获取该物品提供的经验值
     *
     * @return 物品提供的经验值
     */
    public int getXp() {
        return xp;
    }

    /**
     * 重写物品的使用方法
     *
     * @param level  当前游戏世界
     * @param player 使用物品的玩家
     * @param hand   交互的手（主手或副手）
     * @return 交互结果
     */
    @Override
    public InteractionResultHolder<ItemStack> use(@Nonnull Level level, @Nonnull Player player, @Nonnull InteractionHand hand) {
        // 获取玩家手中的物品堆
        ItemStack itemStack = player.getItemInHand(hand);

        // 获取玩家身上的光环饰品
        ItemStack halo = OtherHelper.getCurio(player, "halo", 0);
        // 判断玩家是否拥有光环饰品
        boolean isHalo = !halo.isEmpty();

        // 如果是在服务器端且玩家拥有光环饰品
        if (!level.isClientSide && isHalo) {
            // 检查该光环物品是否支持光环等级系统
            boolean supportsLevelSystem = false;

            // 如果物品是DecorationItem类型，检查它是否启用了光环等级系统
            if (halo.getItem() instanceof DecorationItem) {
                DecorationItem decorationItem = (DecorationItem) halo.getItem();
                supportsLevelSystem = decorationItem.hasHaloLevelSystem();
            } else {
                // 对于其他类型的物品，我们可以检查它是否已经初始化了等级系统
                CompoundTag haloTag = halo.getOrCreateTag();
                supportsLevelSystem = haloTag.contains("level") && haloTag.contains("xp") && haloTag.contains("maxXp");
            }

            if (supportsLevelSystem) {
                // 获取光环的当前等级、经验值和最大经验值
                CompoundTag haloTag = halo.getOrCreateTag();
                int itemLevel = haloTag.getInt("level");
                int itemXp = haloTag.getInt("xp");
                int itemMaxXp = haloTag.getInt("maxXp");

                // 如果光环等级已达到 90 级且经验值已满，发送提示信息并返回失败（不消耗物品）
                if (itemLevel >= 90 && itemXp >= itemMaxXp) {
                    player.sendSystemMessage(Component.translatable("message.dream_sakura_blue_archive.halo_max_level"));
                    return InteractionResultHolder.fail(itemStack);
                }

                // 计算新的经验值
                int newXp = itemXp + getXp();

                // 处理升级逻辑
                while (newXp >= itemMaxXp && itemLevel < 90) {
                    // 先升级到下一级
                    itemLevel++;
                    // 计算下一级所需经验值
                    itemMaxXp = (int) OtherHelper.expCalculate(itemLevel);
                    // 减去升级消耗的经验
                    newXp = newXp - itemMaxXp;

                    // 确保经验值不为负数
                    if (newXp < 0) {
                        newXp = 0;
                    }

                    // 如果达到 90 级，限制经验值不超过最大值
                    if (itemLevel >= 90) {
                        if (newXp > itemMaxXp) {
                            newXp = itemMaxXp;
                        }
                        break;
                    }
                }

                // 如果未达到 90 级但经验值超过了当前等级上限，则截断到上限（不升级）
                if (itemLevel < 90 && newXp > itemMaxXp) {
                    newXp = itemMaxXp;
                }

                // 只有当经验值确实增加时才消耗物品并更新数据
                if (newXp > itemXp || itemLevel > haloTag.getInt("level")) {
                    // 消耗一个物品
                    itemStack.shrink(1);

                    // 更新光环标签中的等级、经验值和最大经验值
                    haloTag.putInt("level", itemLevel);
                    haloTag.putInt("xp", newXp);
                    haloTag.putInt("maxXp", itemMaxXp);
                } else {
                    // 经验值没有变化（已满），不消耗物品
                    return InteractionResultHolder.fail(itemStack);
                }
            }
        }
        // 根据客户端或服务器端返回相应的交互结果
        return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide());
    }

    /**
     * 重写物品的悬停提示信息
     *
     * @param stack   当前物品堆
     * @param level   当前游戏世界
     * @param tooltip 提示信息列表
     * @param flag    提示标志
     */
    @Override
    public void appendHoverText(@Nonnull ItemStack stack, @Nullable Level level, @Nonnull List<Component> tooltip, @Nonnull TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        // 添加基础经验值的提示信息
        tooltip.add(Component.translatable("tooltip." + dream_sakura_blue_archive.MODID + ".baseXp", getXp()));
    }
}