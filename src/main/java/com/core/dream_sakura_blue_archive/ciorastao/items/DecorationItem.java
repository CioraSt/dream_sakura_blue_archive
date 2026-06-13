package com.core.dream_sakura_blue_archive.ciorastao.items;

import com.core.dream_sakura.api.tooltip.DreamSakuraTooltipAPI;
import com.core.dream_sakura.api.tooltip.tooltipparticle.ITooltipParticleItem;
import com.core.dream_sakura.api.tooltip.tooltipparticle.PTID;
import com.core.dream_sakura.api.tooltip.tooltipparticle.TooltipParticleSystem;
import com.core.dream_sakura.enums.DamageType;
import com.core.dream_sakura.listener.IDamageImmunity;
import com.core.dream_sakura.skill.SkillBinding;
import com.core.dream_sakura.skill.SkillRegistry;
import com.core.dream_sakura.tooltip.TooltipHelper;
import com.core.dream_sakura_blue_archive.ciorastao.items.client.DecorationRenderer;
import com.core.dream_sakura_blue_archive.ciorastao.items.client.IGlowingItem;
import com.core.dream_sakura_blue_archive.ciorastao.util.HaloLevelManager;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;
import software.bernie.geckolib.util.RenderUtils;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

// 将事件处理器移到单独的类中以避免加载器冲突
public class DecorationItem extends Item implements ICurioItem, GeoItem, IDamageImmunity, IGlowingItem, ITooltipParticleItem {
    // 创建一个RawAnimation对象，用于定义物品的闲置动画
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation");
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final String itemId;
    private final BiConsumer<SlotContext, ItemStack> curioEquipCallback; // 装备时的tick回调函数
    private final float[] glowColor; // 发光颜色
    private final Float glowIntensity; // 发光强度
    private final List<Integer> tooltipColor; // 工具提示颜色列表
    private final String tooltipText; // 工具提示文本
    private final String tooltipTranslationKey; // 工具提示翻译键
    private final SkillBinding skillBinding; // 技能绑定
    private final ResourceLocation musicResource; // 背景音乐资源
    private final Builder.OnCraftedCallback onCraftedCallback; // 合成时回调函数
    private final TooltipParticleSystem.ParticleConfig particleConfig; // 粒子配置
    private final java.util.List<TooltipParticleSystem.ParticleConfig> multipleParticleConfigs; // 多粒子配置
    private final boolean enableParticles; // 是否启用粒子效果
    private final boolean hasHaloLevelSystem; // 是否启用了光环等级系统
    private final boolean enableShiftPrompt; // 是否启用Shift提示
    private final boolean enableCtrlPrompt; // 是否启用Ctrl提示
    private final java.util.List<Integer> gradientColors; // 渐变颜色列表
    private final java.util.Map<net.minecraft.world.entity.EquipmentSlot, java.util.List<java.util.Map.Entry<net.minecraft.world.entity.ai.attributes.Attribute, net.minecraft.world.entity.ai.attributes.AttributeModifier>>> attributeModifiersMap;
    private final DreamSakuraTooltipAPI.DreamSakuraTextureConfig tooltipTextureConfig; // Tooltip纹理配置
    private Function<ItemStack, Set<DamageType>> immunityProvider; // 伤害免疫提供者

    // 私有构造函数，使用Builder模式
    private DecorationItem(Builder builder) {
        super(builder.properties.stacksTo(1));
        this.itemId = builder.itemId;
        this.curioEquipCallback = builder.curioEquipCallback;
        this.immunityProvider = builder.immunityProvider;
        this.glowColor = builder.glowColor != null ? builder.glowColor : new float[]{1.0f, 0.84f, 0.0f};
        this.glowIntensity = builder.glowIntensity != null ? builder.glowIntensity : 1.0f;
        this.tooltipColor = builder.tooltipColor != null ? builder.tooltipColor : Collections.emptyList();
        this.tooltipText = builder.tooltipText;
        this.tooltipTranslationKey = builder.tooltipTranslationKey;
        this.skillBinding = builder.skillBinding;
        this.musicResource = builder.musicResource;
        this.onCraftedCallback = builder.onCraftedCallback;
        this.particleConfig = builder.particleConfig;
        this.multipleParticleConfigs = builder.multipleParticleConfigs;
        this.enableParticles = builder.enableParticles;
        this.hasHaloLevelSystem = builder.hasHaloLevelSystem;
        this.enableShiftPrompt = builder.enableShiftPrompt;
        this.enableCtrlPrompt = builder.enableCtrlPrompt;
        this.gradientColors = builder.gradientColors;
        this.attributeModifiersMap = builder.attributeModifiersMap != null ? builder.attributeModifiersMap : new java.util.HashMap<>();
        this.tooltipTextureConfig = builder.tooltipTextureConfig;

        if (this.skillBinding != null) {
            SkillRegistry.registerBinding(this.skillBinding);
        }

        // 注册Tooltip纹理配置
        if (this.tooltipTextureConfig != null && this.itemId != null) {
            DreamSakuraTooltipAPI.registerHaloTooltip(this.itemId, this.tooltipTextureConfig);
        }
    }
    //#endregion

    // 实现IGlowingItem接口
    @Override
    public float[] getGlowColor() {
        return glowColor;
    }

    @Override
    public float getGlowIntensity() {
        return glowIntensity;
    }


    //#region geckolib

    /**
     * 核心动画逻辑：定义动画状态机
     * - 来自于geckolib
     *
     * @param controllers - 动画控制器注册器
     */
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, this::handleAnimations));
    }

    /**
     * 处理动画状态
     * - 来自于geckolib
     *
     * @param state - 动画状态
     * @return - 播放状态
     */
    private <T extends GeoItem> PlayState handleAnimations(AnimationState<T> state) {
        if (state.getController().getAnimationState() == AnimationController.State.STOPPED) {
            state.getController().setAnimation(IDLE);
        }
        return PlayState.CONTINUE;
    }

    @Override
    /**
     * 获取动画实例缓存
     * - 来自于geckolib
     * @return - 动画实例缓存
     */
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    /**
     * 获取当前tick
     * - 来自于geckolib
     *
     * @param ItemStack - 物品栈
     * @return - 当前tick
     */
    @Override
    public double getTick(Object ItemStack) {
        return RenderUtils.getCurrentTick();
    }

    /**
     * 初始化客户端扩展
     * - 来自于geckolib
     *
     * @param consumer - 客户端扩展消费者
     * @return - 无返回值
     */
    @Override
    public void initializeClient(@Nonnull Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private DecorationRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.renderer == null) {
                    this.renderer = new DecorationRenderer();
                }
                return this.renderer;
            }

        });
    }

    /**
     * 判断物品是否可以装备
     * - 来自于Curios API
     *
     * @param slotContext - 槽位上下文
     * @param stack       - 物品栈
     * @return - 是否可以装备
     */
    @Override
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        return "halo".equals(slotContext.identifier());
    }
    //#endregion


//#region Curios API

    /**
     * 禁用能力
     * - 用于onUnequip函数
     *
     * @param itemID      - 禁用物品ID
     * @param slotContext - 槽位上下文
     */
    public void disableMayfly(String itemID, SlotContext slotContext) {
        if (itemID.equals(this.itemId)) {
            LivingEntity entity = slotContext.entity();
            if (entity instanceof Player player && !player.level().isClientSide()) {
                Abilities abilities = player.getAbilities();
                if (!player.isCreative()) {
                    abilities.mayfly = false;
                    abilities.flying = false;
                    abilities.invulnerable = false;

                    if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                        serverPlayer.onUpdateAbilities();
                    }
                }
            }
        }
    }

    /**
     * 是否允许右键直接装备
     * - 来自于Curios API
     *
     * @param slotContext - 物品上下文
     * @param stack       - 物品栈
     * @return - 返回true表示允许右键直接装备
     */
    @Override
    public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
        return true;
    }

    /**
     * tick事件
     * - 来自Curios API
     *
     * @param slotContext - 物品上下文
     * @param stack       - 物品栈
     */
    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        // 执行callback
        if (curioEquipCallback != null) {
            curioEquipCallback.accept(slotContext, stack);
        }
    }

    /**
     * 添加文本提示
     * - 来自minecraft
     *
     * @param stack   - 物品栈
     * @param level   - 游戏世界
     * @param tooltip - 提示文本列表
     * @param flag    - 提示标志
     */
    @Override
    public void appendHoverText(@Nonnull ItemStack stack, @Nullable Level level, @Nonnull List<Component> tooltip, @Nonnull TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);

        // 添加自定义工具提示文本
        if (this.tooltipTranslationKey != null && !this.tooltipTranslationKey.isEmpty()) {
            tooltip.add(Component.translatable(this.tooltipTranslationKey).withStyle(ChatFormatting.GRAY));
        }

        // 只有在启用了光环等级系统时才显示等级信息
        if (this.hasHaloLevelSystem) {
            int itemLevel = HaloLevelManager.getHaloLevel(stack);
            int itemXp = HaloLevelManager.getHaloXP(stack);
            int itemMaxXp = HaloLevelManager.getMaxHaloXP(stack);

            tooltip.add(
                    Component.translatable(
                            "tooltip.dream_sakura_blue_archive.level",
                            itemLevel
                    ).withStyle(Style.EMPTY.withColor(0xFF00FF))
            );

            // 只有在达到 90 级且经验值已满时才显示 MAX，否则显示正常经验值
            if (itemLevel >= 90 && itemXp >= itemMaxXp) {
                tooltip.add(
                        Component.translatable(
                                "tooltip.dream_sakura_blue_archive.xp.max"
                        ).withStyle(Style.EMPTY.withColor(0xADD8E6))
                );
            } else {
                tooltip.add(
                        Component.translatable(
                                "tooltip.dream_sakura_blue_archive.xp",
                                itemXp,
                                itemMaxXp
                        ).withStyle(Style.EMPTY.withColor(0xADD8E6))
                );
            }
        }
        // 添加自定义工具提示文本
        if (this.tooltipText != null && !this.tooltipText.isEmpty()) {
            tooltip.add(Component.literal(this.tooltipText));
        }

        // 添加Shift/Ctrl详细描述（使用前置模组的TooltipHelper）
        if (level != null && level.isClientSide() && this.itemId != null) {
            TooltipHelper.addTooltip(
                    this.itemId,
                    stack,
                    level,
                    tooltip,
                    flag,
                    this.tooltipColor,
                    this.enableShiftPrompt,
                    this.enableCtrlPrompt
            );
        }

        if (this.skillBinding != null) {
            TooltipHelper.addSkillsDescription(
                    tooltip,
                    this.skillBinding.getDescription(),
                    this.skillBinding.getkeyMapping().getKey().getName()
            );
        }


        // 如果设置了自定义渐变颜色，则应用自定义渐变效果
        if (this.gradientColors != null && !this.gradientColors.isEmpty() && level != null && level.isClientSide()) {
            applyCustomGradientTooltip(tooltip, this.gradientColors);
        }
    }
    //#endregion


    //#region 重载mc函数

    // 允许重命名
    @Override
    public int getEnchantmentValue() {
        return 1;
    }

    @Override
    public void onCraftedBy(@Nonnull ItemStack stack, @Nonnull Level level, @Nonnull Player player) {
        super.onCraftedBy(stack, level, player);
        if (this.onCraftedCallback != null) this.onCraftedCallback.onCrafted(stack, level, player);
    }

    public Multimap<net.minecraft.world.entity.ai.attributes.Attribute, AttributeModifier> getAttributeModifiers(ItemStack stack, EquipmentSlot slot) {
        ImmutableMultimap.Builder<net.minecraft.world.entity.ai.attributes.Attribute, AttributeModifier> builder =
                ImmutableMultimap.builder();

        if (this.attributeModifiersMap != null && slot != null) {
            java.util.List<java.util.Map.Entry<net.minecraft.world.entity.ai.attributes.Attribute, AttributeModifier>> slotModifiers =
                    this.attributeModifiersMap.get(slot);
            if (slotModifiers != null) {
                for (java.util.Map.Entry<net.minecraft.world.entity.ai.attributes.Attribute, AttributeModifier> entry : slotModifiers) {
                    builder.put(entry.getKey(), entry.getValue());
                }
            }
        }

        return builder.build();
    }

    /**
     * 获取物品ID
     * - 自定义
     *
     * @return - 物品ID字符串
     */
    public String getItemId() {
        return itemId;
    }
    //#endregion


    //#region 自定义函数

    /**
     * 检查物品是否支持光环等级系统
     *
     * @return - 如果物品支持光环等级系统则返回true，否则返回false
     */
    public boolean hasHaloLevelSystem() {
        return this.hasHaloLevelSystem;
    }

    /**
     * 获取工具提示文本
     * - 自定义
     *
     * @return - 工具提示文本字符串
     */
    public String getTooltipText() {
        return tooltipText;
    }

    /**
     * 获取工具提示翻译键
     * - 自定义
     *
     * @return - 工具提示翻译键字符串
     */
    public String getTooltipTranslationKey() {
        return tooltipTranslationKey;
    }

    /**
     * 是否存在播放资源
     * - 自定义
     *
     * @return - 布尔值
     */
    public boolean isPlayingMusic() {
        return this.musicResource != null;
    }

    /**
     * 获取物品播放的音乐资源
     * - 自定义
     *
     * @return - 音乐资源
     */
    public ResourceLocation getMusicResource() {
        return this.musicResource;
    }

    /**
     * 为物品应用自定义颜色的渐变效果
     *
     * @param tooltip - 工具提示列表
     * @param colors  - 自定义颜色列表
     */
    private void applyCustomGradientTooltip(List<Component> tooltip, java.util.List<Integer> colors) {
        for (int i = 0; i < tooltip.size(); i++) {
            Component original = tooltip.get(i);
            if (original != null && !isEmptyComponent(original)) {
                Component gradientComponent = createDynamicGradientWithCustomColors(original.getString(), i, colors);
                tooltip.set(i, gradientComponent);
            }
        }
    }

    /**
     * 检查组件是否为空
     */
    private boolean isEmptyComponent(Component component) {
        String text = component.getString();
        return text == null || text.trim().isEmpty();
    }

    /**
     * 使用自定义颜色创建动态渐变组件
     *
     * @param text         - 要应用渐变的文本
     * @param lineIndex    - 行索引，用于动画偏移
     * @param customColors - 自定义颜色列表
     * @return - 应用渐变效果的组件
     */
    private Component createDynamicGradientWithCustomColors(String text, int lineIndex, java.util.List<Integer> customColors) {
        if (text == null || text.isEmpty()) {
            return Component.empty();
        }

        Component result = Component.empty();

        // 获取当前游戏时间用于动态动画
        final long currentTime = System.currentTimeMillis();
        final double timeOffset = currentTime * 0.001; // 减慢动画速度，3秒1次波浪
        final double lineOffset = lineIndex * 0.2; // 减少相位偏移

        // 将List<Integer>转换为int[]数组
        int[] colors = customColors.stream().mapToInt(Integer::intValue).toArray();
        if (colors.length == 0) {
            // 如果没有自定义颜色，返回原始文本
            return Component.literal(text);
        }

        final int textLength = text.replace(" ", "").length();
        if (textLength == 0) {
            return Component.literal(text);
        }

        int charIndex = 0;

        for (int i = 0; i < text.length(); i++) {
            final char c = text.charAt(i);
            if (c == ' ') {
                result = result.copy().append(Component.literal(" "));
                continue;
            }

            final double wavePhase = timeOffset + lineOffset + charIndex * 0.2;
            final double mainWave = Math.sin(wavePhase);
            final double dreamWave = Math.sin(wavePhase * 1.3) * 0.3;
            final double sparkleWave = Math.cos(wavePhase * 0.5) * 0.15;
            final double whiteWave = Math.sin(wavePhase * 1.0) * 0.9; // 1秒1次的白色波浪

            final double basePosition = (double) charIndex / Math.max(1, textLength - 1);
            final double waveInfluence = (mainWave + dreamWave + sparkleWave) * 0.15; // 减少波浪影响
            final double animatedPosition = Math.max(0, Math.min(1, basePosition + waveInfluence));

            final boolean isWhiteWave = whiteWave > 0.8; // 更严格的白色波浪阈值

            final int colorIndex;
            if (isWhiteWave && colors.length > 0) {
                colorIndex = colors.length - 1; // 最后一个颜色作为白色波浪
            } else {
                colorIndex = Math.min((int) (animatedPosition * (colors.length - 1)), colors.length - 1);
            }

            final double brightnessWave = Math.sin(wavePhase * 1.2);
            final double dreamBrightness = 0.9 + 0.2 * brightnessWave; // 更柔和的亮度变化（0.9-1.1）
            final int baseColor = colors[colorIndex];
            final int dreamColor = adjustBrightness(baseColor, dreamBrightness);

            Component charComponent = Component.literal(String.valueOf(c))
                    .withStyle(style -> style.withColor(dreamColor)
                            .withBold(true)
                            .withItalic(false));

            result = result.copy().append(charComponent);
            charIndex++;
        }

        return result;
    }

    /**
     * 调整颜色亮度
     * 该方法通过调整RGB各分量的值来改变颜色的亮度
     *
     * @param color      原始颜色值，使用RGB格式表示
     * @param brightness 亮度调整因子，大于1变亮，小于1变暗
     * @return 调整亮度后的新颜色值
     */
    private int adjustBrightness(int color, double brightness) {
        // 使用位运算提取颜色的红色分量（RGB中的R）
        final int r = (color >> 16) & 0xFF;
        // 使用位运算提取颜色的绿色分量（RGB中的G）
        final int g = (color >> 8) & 0xFF;
        // 使用位运算提取颜色的蓝色分量（RGB中的B）
        final int b = color & 0xFF;

        // 计算新的红色分量，确保值在0-255范围内
        final int newR = Math.min(255, Math.max(0, (int) (r * brightness)));
        // 计算新的绿色分量，确保值在0-255范围内
        final int newG = Math.min(255, Math.max(0, (int) (g * brightness)));
        // 计算新的蓝色分量，确保值在0-255范围内
        final int newB = Math.min(255, Math.max(0, (int) (b * brightness)));

        // 将调整后的RGB分量重新组合成新的颜色值并返回
        return (newR << 16) | (newG << 8) | newB;
    }

    // 修复属性修饰符方法，正确创建新的不可变multimap
    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
        Multimap<Attribute, AttributeModifier> attributes = super.getDefaultAttributeModifiers(slot);

        // 仅在主手时添加属性修饰符
        if (slot == EquipmentSlot.MAINHAND) {
            if (this.attributeModifiersMap != null) {
                java.util.List<java.util.Map.Entry<net.minecraft.world.entity.ai.attributes.Attribute, AttributeModifier>> slotModifiers =
                        this.attributeModifiersMap.get(slot);
                if (slotModifiers != null) {
                    // 使用HashMultimap临时收集属性，然后构建不可变Multimap
                    HashMultimap<Attribute, AttributeModifier> tempMap = HashMultimap.create(attributes);

                    for (java.util.Map.Entry<net.minecraft.world.entity.ai.attributes.Attribute, AttributeModifier> entry : slotModifiers) {
                        tempMap.put(entry.getKey(), entry.getValue());
                    }

                    // 返回新的不可变multimap
                    return ImmutableMultimap.copyOf(tempMap);
                }
            }
        }

        return ImmutableMultimap.copyOf(attributes);
    }

    // 实现ITooltipParticleItem接口的默认方法
    @Override
    public TooltipParticleSystem.ParticleConfig getParticleConfig() {
        return this.particleConfig;
    }

    @Override
    public List<TooltipParticleSystem.ParticleConfig> getMultipleParticleConfigs() {
        return this.multipleParticleConfigs;
    }

    @Override
    public boolean shouldSpawnParticles(ItemStack stack) {
        return this.enableParticles;
    }

    @Override
    public int getParticleSpawnRate() {
        return 5; // 默认每5tick生成一次
    }

    /**
     * 获取物品的伤害免疫类型
     * - 来自自定义IDamageImmunity接口
     *
     * @param stack - 物品栈
     * @return - 伤害类型集合
     */
    @Override
    public Set<DamageType> getImmunities(ItemStack stack) {
        return immunityProvider.apply(stack);
    }

    /**
     * 设置免疫提供者函数
     * 该函数用于根据物品栈(ItemStack)获取对应的免疫伤害类型集合(Set<DamageType>)
     *
     * @param immunityProvider 一个函数式接口，接收ItemStack参数，返回Set<DamageType>类型的免疫伤害集合
     *                         用于确定特定物品能够免疫哪些类型的伤害
     */
    public void setImmunityProvider(Function<ItemStack, Set<DamageType>> immunityProvider) {
        // 将传入的免疫提供者函数赋值给当前对象的immunityProvider属性
        this.immunityProvider = immunityProvider;
    }

    //#region 构造器
    public static class Builder {
        // 必需参数
        private final String itemId;
        private final Properties properties;

        // 可选参数
        private BiConsumer<SlotContext, ItemStack> curioEquipCallback = (slotContext, stack) -> {
        };
        private Function<ItemStack, Set<DamageType>> immunityProvider = stack -> Collections.emptySet();
        private float[] glowColor = null;
        private Float glowIntensity = 1.0f;
        private List<Integer> tooltipColor = null;
        private String tooltipText = null;
        private String tooltipTranslationKey = null;
        private SkillBinding skillBinding = null;
        private ResourceLocation musicResource = null;
        private OnCraftedCallback onCraftedCallback = null;
        private TooltipParticleSystem.ParticleConfig particleConfig = null;
        private java.util.List<TooltipParticleSystem.ParticleConfig> multipleParticleConfigs = null;
        private boolean enableParticles = false;
        private boolean hasHaloLevelSystem = false;
        private boolean enableShiftPrompt = true; // 默认启用Shift提示
        private boolean enableCtrlPrompt = true; // 默认启用Ctrl提示
        private java.util.List<Integer> gradientColors = null; // 自定义渐变颜色列表
        private java.util.Map<net.minecraft.world.entity.EquipmentSlot, java.util.List<java.util.Map.Entry<net.minecraft.world.entity.ai.attributes.Attribute, net.minecraft.world.entity.ai.attributes.AttributeModifier>>> attributeModifiersMap = new java.util.HashMap<>();
        private DreamSakuraTooltipAPI.DreamSakuraTextureConfig tooltipTextureConfig = null; // Tooltip纹理配置

        public Builder(String itemId, Properties properties) {
            this.itemId = itemId;
            this.properties = properties;
        }

        /**
         * 设置饰品装备时的回调函数
         *
         * @param callback - 回调函数
         * @return - Builder实例
         */
        public Builder withCurioEquipCallback(BiConsumer<SlotContext, ItemStack> callback) {
            this.curioEquipCallback = callback;
            return this;
        }

        /**
         * 设置饰品的免疫类型
         *
         * @param provider - 免疫类型提供者
         * @return - Builder实例
         */
        public Builder withImmunityProvider(Function<ItemStack, Set<DamageType>> provider) {
            this.immunityProvider = provider;
            return this;
        }

        /**
         * 添加饰物发光颜色
         *
         * @param glowColor - 光照颜色
         * @return - Builder实例
         */
        public Builder withGlowColor(float[] glowColor) {
            this.glowColor = glowColor;
            return this;
        }

        public Builder withGlowIntensity(float glowIntensity) {
            this.glowIntensity = glowIntensity;
            return this;
        }

        /**
         * 添加饰物提示颜色
         *
         * @param tooltipColor - 提示颜色
         * @return - Builder实例
         */
        public Builder withTooltipColor(List<Integer> tooltipColor) {
            this.tooltipColor = tooltipColor;
            return this;
        }

        /**
         * 添加工具提示文本
         *
         * @param tooltipText - 工具提示文本
         * @return - Builder实例
         */
        public Builder withTooltip(String tooltipText) {
            this.tooltipText = tooltipText;
            return this;
        }

        /**
         * 添加工具提示翻译键
         *
         * @param translationKey - 工具提示翻译键
         * @return - Builder实例
         */
        public Builder withTooltipTranslation(String translationKey) {
            this.tooltipTranslationKey = translationKey;
            return this;
        }

        /**
         * 添加技能绑定
         *
         * @param skillBinding - 技能绑定
         * @return - Builder实例
         */
        public Builder withSkillBinding(SkillBinding skillBinding) {
            this.skillBinding = skillBinding;
            return this;
        }

        /**
         * 启用光环等级系统
         * 为光环物品启用等级和经验值管理功能
         *
         * @return - Builder实例
         */
        public Builder withHaloLevelSystem() {
            // 设置默认的等级、经验和最大经验值
            withCurioEquipCallback((slotContext, stack) -> {
                // 确保光环物品有等级系统
                HaloLevelManager.getHaloLevel(stack);  // 初始化等级
                HaloLevelManager.getHaloXP(stack);     // 初始化经验值
                HaloLevelManager.getMaxHaloXP(stack);  // 初始化最大经验值
            });
            this.hasHaloLevelSystem = true;
            return this;
        }

        /**
         * 禁用Shift提示功能
         * 阻止物品显示Shift提示
         *
         * @return - Builder实例
         */
        public Builder withoutShiftPrompt() {
            this.enableShiftPrompt = false;
            return this;
        }

        /**
         * 禁用Ctrl提示功能
         * 阻止物品显示Ctrl提示
         *
         * @return - Builder实例
         */
        public Builder withoutCtrlPrompt() {
            this.enableCtrlPrompt = false;
            return this;
        }

        /**
         * 添加属性修饰符
         *
         * @param attribute 属性
         * @param modifier  修饰符
         * @return Builder实例
         */
        public Builder withAttributeModifier(net.minecraft.world.entity.ai.attributes.Attribute attribute, AttributeModifier modifier) {
            return withAttributeModifier(net.minecraft.world.entity.EquipmentSlot.MAINHAND, attribute, modifier);
        }

        /**
         * 添加属性修饰符到指定槽位
         *
         * @param slot      装备槽位
         * @param attribute 属性
         * @param modifier  修饰符
         * @return Builder实例
         */
        public Builder withAttributeModifier(net.minecraft.world.entity.EquipmentSlot slot, net.minecraft.world.entity.ai.attributes.Attribute attribute, AttributeModifier modifier) {
            java.util.List<java.util.Map.Entry<net.minecraft.world.entity.ai.attributes.Attribute, AttributeModifier>> modifiers =
                    attributeModifiersMap.computeIfAbsent(slot, k -> new java.util.ArrayList<>());
            modifiers.add(java.util.Map.entry(attribute, modifier));
            return this;
        }

        /**
         * 添加背景音乐
         *
         * @param musicResource - 音乐资源
         * @return - Builder实例
         */
        public Builder withMusicResource(ResourceLocation musicResource) {
            this.musicResource = musicResource;
            return this;
        }

        /**
         * 设置合成时的回调函数
         *
         * @param callback - 合成回调函数
         * @return - Builder实例
         */
        public Builder withOnCraftedCallback(OnCraftedCallback callback) {
            this.onCraftedCallback = callback;
            return this;
        }

        /**
         * 设置粒子配置
         *
         * @param config - 粒子配置
         * @return - Builder实例
         */
        public Builder withParticleConfig(TooltipParticleSystem.ParticleConfig config) {
            this.particleConfig = config;
            this.enableParticles = true;
            return this;
        }

        /**
         * 启用BA元素粒子效果
         *
         * @return - Builder实例
         */
        public Builder withBAElementsParticles() {
            this.particleConfig = getBAElementsParticleConfig();
            this.enableParticles = true;
            return this;
        }

        /**
         * 设置多个粒子配置
         *
         * @param configs - 粒子配置列表
         * @return - Builder实例
         */
        public Builder withMultipleParticleConfigs(java.util.List<TooltipParticleSystem.ParticleConfig> configs) {
            this.multipleParticleConfigs = configs;
            this.enableParticles = true;
            return this;
        }

        /**
         * 创建BA光环专属粒子框
         */
        private TooltipParticleSystem.ParticleConfig getBAElementsParticleConfig() {
            return new TooltipParticleSystem.ParticleConfig().setTextures(PTID.FE_0)
                    .setParticleCount(5, 7)
                    .setMaxTotalParticles(120)
                    .setLife(5.5f, 10.5f)
                    .setSpeed(50.0f, 60.0f)
                    .setTextures(PTID.FE_2)
                    .setSize(4.0f, 8.0f) // 增大粒子大小
                    .setRandomSize(true)
                    .setColors(0x87CEEB, 0xDFFFFF, 0xF8F8FF, 0xF2D3DB, 0xFFA7F7) // 天空蓝、蔚蓝、淡白、浅粉
                    .setMotionType(TooltipParticleSystem.MotionType.RANDOM_WALK)
                    .setRadius(50.0f)
                    .setFadeIn(true, 1.0f)
                    .setFadeOut(true, 1.0f)// 设置淡入淡出
                    .setPulsing(true, 1.5f, 0.5f)// 设置闪烁
                    .setGlow(true, 2.0f);// 设置高光
        }

        /**
         * 获取紧凑型粒子系统的配置
         * 返回一个配置了较少粒子数和较小尺寸的粒子系统参数，适合用于紧凑的UI提示
         *
         * @return TooltipParticleSystem.ParticleConfig 配置好的粒子系统参数对象
         */
        private TooltipParticleSystem.ParticleConfig getCompactParticleConfig() {
            return new TooltipParticleSystem.ParticleConfig()
                    .setTextures(PTID.Sakura_2)
                    .setParticleCount(4, 6)
                    .setMaxTotalParticles(80)
                    .setSize(3.5f, 6.0f) // 增大粒子大小
                    .setRandomSize(true)
                    .setLife(3.0f, 4.5f)
                    .setSpeed(9.0f, 16.0f)
                    .setColors(0xFFFFC0CB, 0xFFFF69B4, 0xFFFF1493, 0xFFFFB6C1) // 樱花粉色系
                    .setMotionType(TooltipParticleSystem.MotionType.MAGIC_SWIRL)
                    .setRadius(30.0f)
                    .setFadeIn(true, 0.5f)
                    .setFadeOut(true, 0.5f)
                    .setTwinkling(true, 3.0f, 0.4f)
                    .setGlow(true, 1.5f);
        }

        /**
         * 构建DecorationItem实例
         *
         * @return - DecorationItem实例
         */
        public DecorationItem build() {
            return new DecorationItem(this);
        }

        /**
         * 设置自定义渐变颜色
         *
         * @param colors 渐变颜色列表
         * @return Builder实例
         */
        public Builder withGradientColors(java.util.List<Integer> colors) {
            this.gradientColors = colors;
            return this;
        }

        /**
         * 设置Tooltip纹理配置
         *
         * @param config Tooltip纹理配置
         * @return Builder实例
         */
        public Builder withTooltipTextureConfig(DreamSakuraTooltipAPI.DreamSakuraTextureConfig config) {
            this.tooltipTextureConfig = config;
            return this;
        }


        public Builder withCurioTickCallback(Object o) {
            return null;
        }

        /**
         * 合成回调接口
         */
        @FunctionalInterface
        public interface OnCraftedCallback {
            void onCrafted(ItemStack stack, Level level, Player player);
        }
    }
    //#endregion
}
