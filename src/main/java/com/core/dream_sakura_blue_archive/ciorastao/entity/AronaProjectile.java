package com.core.dream_sakura_blue_archive.ciorastao.entity;

import com.core.dream_sakura.power.DreamSakuraDamageSource;
import com.core.dream_sakura_blue_archive.ciorastao.items.RegistryItem;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;

/**
 * 阿罗娜愤怒状态下发射的弹射物，外观为钻石，
 * 命中后造成目标最大生命值 2% 的梦之樱伤害。
 */
public class AronaProjectile extends ThrowableItemProjectile {

    public AronaProjectile(EntityType<? extends ThrowableItemProjectile> type, Level level) {
        super(type, level);
        setNoGravity(true);
    }

    public AronaProjectile(Level level, LivingEntity owner) {
        super(RegistryEntity.ARONA_PROJECTILE.get(), owner, level);
        setNoGravity(true);
    }

    @Override
    protected Item getDefaultItem() {
        return RegistryItem.PYROXENE.get();
    }

    /** 弹射物永远不命中发射者自身，防止阿罗娜被自己的弹射物打到。 */
    @Override
    public boolean canHitEntity(Entity target) {
        if (target == this.getOwner()) return false;
        return super.canHitEntity(target);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (this.level().isClientSide) return;
        if (result.getEntity() instanceof LivingEntity target) {
            float damage = 2.0F + target.getMaxHealth() * 0.02F;
            Entity attacker = this.getOwner() != null ? this.getOwner() : this;
            DreamSakuraDamageSource.applyDreamSakuraDamage(
                    attacker, target, damage, ItemStack.EMPTY);
        }
    }
}
