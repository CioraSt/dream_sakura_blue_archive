package com.core.dream_sakura_blue_archive.ciorastao.items;

import com.core.dream_sakura_blue_archive.ciorastao.dream_sakura_blue_archive;
import com.core.dream_sakura_blue_archive.ciorastao.util.HaloLevelManager;
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
                if (HaloLevelManager.isMaxed(halo)) {
                    player.sendSystemMessage(Component.translatable("message.dream_sakura_blue_archive.halo_max_level"));
                    return InteractionResultHolder.fail(itemStack);
                }

                int beforeLevel = HaloLevelManager.getHaloLevel(halo);
                int beforeXp = HaloLevelManager.getHaloXP(halo);
                HaloLevelManager.addHaloXP(halo, getXp());
                if (HaloLevelManager.getHaloLevel(halo) > beforeLevel || HaloLevelManager.getHaloXP(halo) > beforeXp) {
                    itemStack.shrink(1);
                } else {
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
