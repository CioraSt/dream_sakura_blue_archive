package com.core.dream_sakura_blue_archive.ciorastao.entity;

import net.eca.api.EcaAPI;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.network.NetworkHooks;
import com.core.dream_sakura_blue_archive.ciorastao.menu.AronaGachaMenu;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.ArrayDeque;
import java.util.Deque;
import javax.annotation.Nullable;

public class AronaEntity extends PathfinderMob implements GeoEntity {
    private static final int ANGER_DAMAGE_THRESHOLD = 4;
    private static final long ANGER_DAMAGE_WINDOW_TICKS = 20L * 10L;

    private static final RawAnimation IDLE_ANIMATION = RawAnimation.begin().thenLoop("idle");
    private final AnimatableInstanceCache animationCache = GeckoLibUtil.createInstanceCache(this);
    /** 服务端记录最近的有效受伤 tick；第 4 次发生在 10 秒窗口内时进入愤怒状态。 */
    private final Deque<Long> recentDamageTicks = new ArrayDeque<>();
    // 仅用于内部暂存；刻意不提供单数值参数的 setter，避免暴露给通用写血探针。
    private float pendingHealth;
    // ==================== 构造与注册 ====================

    public AronaEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 200.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.FOLLOW_RANGE, 16.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new AronaRangedAttackGoal(this));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(ARONA_VALUE, "");
        this.entityData.define(ARONA_VALUE_KEY, String.valueOf(DEFAULT_KEY));
        this.entityData.define(ARONA_VALUE_CHECK, "");
        this.entityData.define(ANGRY, false);
    }

// ==================== 加密生命值系统 ====================

    /** 加密后的生命值密文，公式：ARONA_VALUE = 密钥 - (int)真实血量 */
    private static final EntityDataAccessor<String> ARONA_VALUE =
            SynchedEntityData.defineId(AronaEntity.class, EntityDataSerializers.STRING);

    /** 每 tick 轮换的加密密钥，范围 0000-9999 */
    private static final EntityDataAccessor<String> ARONA_VALUE_KEY =
            SynchedEntityData.defineId(AronaEntity.class, EntityDataSerializers.STRING);

    /** 完整性校验码，通过 MethodHandle 计算，防止密文被外部篡改 */
    private static final EntityDataAccessor<String> ARONA_VALUE_CHECK =
            SynchedEntityData.defineId(AronaEntity.class, EntityDataSerializers.STRING);

    private static final EntityDataAccessor<Boolean> ANGRY =
            SynchedEntityData.defineId(AronaEntity.class, EntityDataSerializers.BOOLEAN);

    /** 首次轮换前的默认密钥 */
    private static final int DEFAULT_KEY = 1234;

    public boolean isAngry() {
        return this.entityData.get(ANGRY);
    }

    public void setAngry(boolean angry) {
        this.entityData.set(ANGRY, angry);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (isAngry()) return super.mobInteract(player, hand);
        if (this.level().isClientSide) return InteractionResult.CONSUME;
        if (player instanceof ServerPlayer serverPlayer) {
            NetworkHooks.openScreen(serverPlayer, new MenuProvider() {
                @Override public Component getDisplayName() { return Component.literal("阿罗娜的招募"); }
                @Override public net.minecraft.world.inventory.AbstractContainerMenu createMenu(int id, net.minecraft.world.entity.player.Inventory inventory, Player ignored) {
                    return new AronaGachaMenu(id, inventory, AronaEntity.this);
                }
            }, buffer -> buffer.writeInt(this.getId()));
        }
        return InteractionResult.CONSUME;
    }

    /**
     * 间接解密的 MethodHandle。
     * 通过 invokeExact 调用 {@link #decryptCore}，多态签名使调用链不被字节码分析追踪。
     */
    private static final MethodHandle DECRYPT_MH;

    /**
     * 完整性校验码计算的 MethodHandle。
     * 通过 invokeExact 调用 {@link #computeCheck}，与解密链路分离。
     */
    private static final MethodHandle CHECK_MH;

    /**
     * 加密写入的 MethodHandle。
     * 真正的加密逻辑通过此 MH 调用，实例方法 encryptHealth 为诱饵。
     */
    private static final MethodHandle WRITE_MH;

    static {
        try {
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            DECRYPT_MH = lookup.findStatic(
                    AronaEntity.class,
                    "decryptCore",
                    MethodType.methodType(int.class, int.class, int.class)
            );
            CHECK_MH = lookup.findStatic(
                    AronaEntity.class,
                    "computeCheck",
                    MethodType.methodType(int.class, int.class, int.class)
            );
            WRITE_MH = lookup.findStatic(
                    AronaEntity.class,
                    "writeHealthCore",
                    MethodType.methodType(void.class, AronaEntity.class, float.class)
            );
        } catch (Exception e) {
            throw new RuntimeException("阿罗娜：MethodHandle 初始化失败", e);
        }
    }


    /**
     * 核心解密：真实血量 = 密钥 - 密文。
     * 通过 {@link #DECRYPT_MH} MethodHandle 间接调用。
     */
    @SuppressWarnings("unused") // 通过 MethodHandle 调用
    private static int decryptCore(int encrypted, int key) {
        return key - encrypted;
    }

    /**
     * 核心校验：校验码 = 密钥 + 密文。
     * 通过 {@link #CHECK_MH} MethodHandle 间接调用，与解密链路分离。
     */
    @SuppressWarnings("unused") // 通过 MethodHandle 调用
    private static int computeCheck(int encrypted, int key) {
        return key + encrypted;
    }

    /** 安全整数解析，空值/非法输入返回 0 */
    private static int parseIntSafe(String s) {
        if (s == null || s.isEmpty()) return 0;
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * 核心加密写入。通过 {@link #WRITE_MH} MethodHandle 间接调用。
     * 密文 = 密钥 - (int)血量，同步计算校验码。
     */
    @SuppressWarnings("unused") // 通过 MethodHandle 调用
    private static void writeHealthCore(AronaEntity entity, float health) {
        int key = parseIntSafe(entity.entityData.get(ARONA_VALUE_KEY));
        int rawHealth = Math.max(0, (int) health);
        int encrypted = key - rawHealth;
        entity.entityData.set(ARONA_VALUE, String.valueOf(encrypted));

        try {
            int check = (int) CHECK_MH.invokeExact(encrypted, key);
            entity.entityData.set(ARONA_VALUE_CHECK, String.valueOf(check));
        } catch (Throwable e) {
            entity.entityData.set(ARONA_VALUE_CHECK, String.valueOf(key + encrypted));
        }
    }

    /** 内部写入：通过 {@link #WRITE_MH} 间接调用核心加密逻辑 */
    private void commitPendingHealth() {
        try {
            WRITE_MH.invokeExact(this, pendingHealth);
        } catch (Throwable ignored) {
        }
    }

    /**
     * 完整性校验：通过 {@link #CHECK_MH} 重算校验码并与存储值比对。
     * 密文或密钥被外部篡改时校验失败，触发自动恢复。
     *
     * @return true 表示数据完整，false 表示已被篡改
     */
    private boolean validateIntegrity() {
        String encStr = this.entityData.get(ARONA_VALUE);
        String keyStr = this.entityData.get(ARONA_VALUE_KEY);
        String checkStr = this.entityData.get(ARONA_VALUE_CHECK);

        if (encStr.isEmpty() || keyStr.isEmpty() || checkStr.isEmpty()) {
            return false;
        }

        try {
            int encrypted = parseIntSafe(encStr);
            int key = parseIntSafe(keyStr);
            int storedCheck = parseIntSafe(checkStr);
            int expectedCheck = (int) CHECK_MH.invokeExact(encrypted, key);
            return storedCheck == expectedCheck;
        } catch (Throwable e) {
            // 校验码计算异常视为篡改
            return false;
        }
    }

    /**
     * 从 {@link #ARONA_VALUE} 解密并返回真实血量。
     * 解密前先完整性校验：首次初始化用最大血量做种子，不信任原版 super.getHealth()；
     * 校验失败自动恢复满血。
     *
     * @return 真实（解密后）的生命值
     */
    private float decryptHealth() {
        String encStr = this.entityData.get(ARONA_VALUE);

        if (encStr.isEmpty()) {
            // 首次调用：以最大生命值为种子，不信任原版血量
            float maxHealth = getMaxHealth();
            pendingHealth = maxHealth;
            commitPendingHealth();
            return maxHealth;
        }

        // 完整性校验：密文被篡改则恢复满血
        if (!validateIntegrity()) {
            float maxHealth = getMaxHealth();
            pendingHealth = maxHealth;
            commitPendingHealth();
            return maxHealth;
        }

        try {
            int encrypted = parseIntSafe(encStr);
            int key = parseIntSafe(this.entityData.get(ARONA_VALUE_KEY));
            int result = (int) DECRYPT_MH.invokeExact(encrypted, key);
            return Math.max(0, Math.min(result, getMaxHealth()));
        } catch (Throwable e) {
            int encrypted = parseIntSafe(encStr);
            int key = parseIntSafe(this.entityData.get(ARONA_VALUE_KEY));
            return Math.max(0, Math.min(decryptCore(encrypted, key), getMaxHealth()));
        }
    }

    /**
     * 每 tick 轮换加密密钥：从 0000-9999 中随机选取。
     * 解密旧密钥下的密文，以新密钥重新加密，确保密钥变化时血量不变。
     */
    private void getKey() {
        String encStr = this.entityData.get(ARONA_VALUE);
        String oldKeyStr = this.entityData.get(ARONA_VALUE_KEY);
        int oldKey = parseIntSafe(oldKeyStr);

        // 1. 用旧密钥解密，获取当前真实血量
        float currentHealth;
        if (encStr.isEmpty()) {
            currentHealth = getMaxHealth();
        } else {
            try {
                int encrypted = parseIntSafe(encStr);
                int result = (int) DECRYPT_MH.invokeExact(encrypted, oldKey);
                currentHealth = Math.max(0, Math.min(result, getMaxHealth()));
            } catch (Throwable e) {
                currentHealth = Math.max(0, Math.min(oldKey - parseIntSafe(encStr), getMaxHealth()));
            }
        }

        // 2. 生成新密钥（确保与当前密钥不同）
        int newKey;
        do {
            newKey = this.random.nextInt(10000);
        } while (newKey == oldKey);

        // 3. 写入新密钥并以新密钥重加密
        this.entityData.set(ARONA_VALUE_KEY, String.valueOf(newKey));
        pendingHealth = currentHealth;
        commitPendingHealth();
    }

    // ==================== 生命周期覆写 ====================

    /**
     * 返回解密后的真实血量。
     * 客户端读取原版 DATA_HEALTH_ID（蜜罐值），服务端解密 ARONA_VALUE。
     */
    @Override
    public float getHealth() {
        return decryptHealth();
    }


    @Override
    public void setHealth(float newHealth) {
    }

    /**
     * 伤害入口：每次最多扣除最大生命值的 2%，并直接写回加密的真实生命值。
     */
    @Override
    public void actuallyHurt(DamageSource source, float amount) {

        float cappedDamage = Math.min(Math.max(amount, 0.0F), getMaxHealth() * 0.02F);
        if (cappedDamage <= 0.0F) return;

        // 从加密血量扣减
        float current = decryptHealth();
        float newHealth = Math.max(0, current - cappedDamage);
        pendingHealth = newHealth;
        commitPendingHealth();
        if (isDying()) {
            stopResurrectionForDeath();
        } else {
            Entity attacker = source.getEntity();
            recordDamageForAnger(attacker instanceof LivingEntity le ? le : null);
        }

        // 同步原版血量（绕过自身 setHealth 限制，直接调用父类）
        this.gameEvent(GameEvent.ENTITY_DAMAGE);
    }

    private void recordDamageForAnger(@Nullable LivingEntity attacker) {
        if (this.level().isClientSide || isAngry()) return;

        long currentTick = this.level().getGameTime();
        while (!recentDamageTicks.isEmpty()
                && currentTick - recentDamageTicks.peekFirst() > ANGER_DAMAGE_WINDOW_TICKS) {
            recentDamageTicks.removeFirst();
        }
        recentDamageTicks.addLast(currentTick);

        if (recentDamageTicks.size() < ANGER_DAMAGE_THRESHOLD) return;

        setAngry(true);
        if (attacker != null) {
            setTarget(attacker);
        }
        recentDamageTicks.clear();
        if (!EcaAPI.isResurrectionRunning()) {
            EcaAPI.startResurrection();
        }
        if (!EcaAPI.isResurrectionTracked(this)) {
            EcaAPI.addResurrectionTarget(this);
        }
    }

    /** 真实死亡后，该阿罗娜不再由 ECA 复活。 */
    private void stopResurrectionForDeath() {
        recentDamageTicks.clear();
        if (EcaAPI.isResurrectionTracked(this)) {
            EcaAPI.removeResurrectionTarget(this);
        }
    }

    /**
     * 治疗入口：解密后加血，再加密写回。
     */
    @Override
    public void heal(float amount) {
        if (amount <= 0) return;
        float current = decryptHealth();
        float newHealth = Math.min(current + amount, getMaxHealth());
        pendingHealth = newHealth;
        commitPendingHealth();
    }

    @Override
    public void tick() {
        super.tick();
        getKey();
        this.entityData.set(LivingEntity.DATA_HEALTH_ID, decryptHealth());
    }

    /**
     * 检测真实血量是否 <= 0，即是否应当死亡。
     */
    private boolean isDying() {
        return decryptHealth() <= 0;
    }

    @Override
    public void die(DamageSource source) {
        if (isDying()) {
            stopResurrectionForDeath();
            super.die(source);
        }
    }

    @Override
    protected void tickDeath() {
        if (isDying()){
            super.tickDeath();
        }
    }

    @Override
    public boolean isAlive() {
        return !isDying();
    }

    @Override
    public boolean isDeadOrDying() {
        return isDying();
    }

    @Override
    public void kill() {
        if (isDying()) {
            stopResurrectionForDeath();
            super.kill();
        }
    }

    @Override
    public void remove(Entity.RemovalReason reason) {
        if (this.level().isClientSide) {
            super.remove(reason);
            return;
        }
        if (!isDying()) {
            return;
        }
        stopResurrectionForDeath();
        super.remove(reason);
    }

    @Override
    public void setRemoved(Entity.RemovalReason reason) {
        if (this.level().isClientSide) {
            super.setRemoved(reason);
            return;
        }
        if (!isDying()) {
            return;
        }
        stopResurrectionForDeath();
        super.setRemoved(reason);
    }

    @Override
    public boolean isRemoved() {
        if (this.level().isClientSide) {
            return super.isRemoved();
        }
        if (!isDying()) {
            return false;
        }
        return super.isRemoved();
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public boolean startRiding(@NotNull Entity entity) {
        return false;
    }
    @Override
    public void teleportTo(double x, double y, double z) {
    }

    @Override
    public void knockback(double strength, double x, double z) {
    }

    @Override
    public boolean canStandOnFluid(FluidState fluidState) {
        return fluidState.is(FluidTags.WATER);
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    @Override
    public void setInvisible(boolean invisible) {
    }

    @Override
    public boolean isInvisible() {
        return false;
    }

    @Override
    public boolean isNoAi() {
        return false;
    }

    @Override
    public void setNoAi(boolean noAi) {
    }

    // ==================== GeckoLib 动画 ====================

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "idle", 0, state -> state.setAndContinue(IDLE_ANIMATION)));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.animationCache;
    }
}
