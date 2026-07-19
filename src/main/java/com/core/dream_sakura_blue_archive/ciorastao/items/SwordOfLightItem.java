package com.core.dream_sakura_blue_archive.ciorastao.items;

import com.core.dream_sakura_blue_archive.ciorastao.items.client.SwordOfLightRenderer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

/**
 * 天童爱丽丝的光之剑。
 *
 * <p>客户端使用分层 OBJ 渲染器，使盖板可以活动，并为核心区域追加呼吸发光层。</p>
 */
public class SwordOfLightItem extends net.minecraft.world.item.Item {
    public static final int CHARGE_TICKS = 60;

    public SwordOfLightItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.NONE;
    }

    @Override
    public void initializeClient(@Nonnull Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private SwordOfLightRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null) {
                    renderer = new SwordOfLightRenderer();
                }
                return renderer;
            }
        });
    }
}