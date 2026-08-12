package com.core.dream_sakura_blue_archive.ciorastao.halo;

import com.core.dream_sakura_blue_archive.ciorastao.effect.RegistryEffect;
import com.core.dream_sakura_blue_archive.ciorastao.network.NetworkHandler;
import com.core.dream_sakura_blue_archive.ciorastao.network.S2CHaloSkillVisualPacket;
import com.core.dream_sakura_blue_archive.ciorastao.util.HaloLevelManager;
import com.core.dream_sakura_blue_archive.ciorastao.util.OtherHelper;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** 服务器端、变体感知的主动技能分派器。 */
public final class HaloSkills {
    public static void activate(ServerPlayer player) {
        HaloProfile.equipped(player).ifPresent(equipped -> {
            String id = equipped.profile().id();
            ItemStack stack = equipped.stack();
            if ("sakuluna_halo".equals(id) && player.isShiftKeyDown()) {
                activateSakulunaFlight(player, stack);
                return;
            }
            switch (id) {
                case "tendouaris_halo" -> activateAlice(player, stack);
                case "tendouaris_maid_halo" -> activateAliceMaid(player, stack);
                case "hoshino_halo" -> activateHoshino(player, stack);
                case "hoshino_swimsuit_halo" -> activateHoshinoSwimsuit(player, stack);
                case "hina_halo" -> activateHina(player, stack);
                case "hina_swimsuit_halo" -> activateHinaSwimsuit(player, stack);
                case "hina_dress_halo" -> activateHinaDress(player, stack);
                case "sakuluna_halo" -> activateSakuluna(player, stack);
            }
        });
    }

    private static void activateAlice(ServerPlayer player, ItemStack stack) {
        if (!beginCooldown(player, stack, "Alice", 1200)) return;
        CompoundTag state = HaloRuntime.state(player);
        long now = player.level().getGameTime();
        int level = HaloLevelManager.getHaloLevel(stack);
        int charge = Math.min(2, stack.getOrCreateTag().getInt("DBAAliceCharge"));
        float multiplier = active(level, 3.11f,3.57f,4.51f,5f,6f)
                * (charge == 1 ? 1.5f : charge >= 2 ? 2f : 1f);
        stack.getOrCreateTag().putInt("DBAAliceCharge", 0);
        stack.getOrCreateTag().putLong("DBAAliceNextCharge", now + 500);
        stack.getOrCreateTag().putLong("DBAAliceBurstEnd", now + 400);
        state.putLong("AliceFireTick", now + 60);
        state.putFloat("AliceMultiplier", multiplier);
        NetworkHandler.sendTrackingAndSelf(new S2CHaloSkillVisualPacket(player.getId(),
                S2CHaloSkillVisualPacket.ALICE_SWORD_CHARGE, 60), player);
        player.displayClientMessage(Component.literal("光之剑充能中……"), true);
    }

    private static void activateAliceMaid(ServerPlayer player, ItemStack stack) {
        if (!beginCooldown(player, stack, "AliceMaid", 800)) return;
        LivingEntity target = aimedTargets(player, 20, 1.8).stream().findFirst().orElse(null);
        if (target == null) {
            List<LivingEntity> nearby = enemies(player, player.getBoundingBox().inflate(20));
            nearby.sort(Comparator.comparingDouble(player::distanceToSqr));
            target = nearby.stream().findFirst().orElse(null);
        }
        if (target == null) {
            player.displayClientMessage(Component.literal("范围内没有目标"), true);
            refundCooldown(stack, "AliceMaid");
            return;
        }
        float amount = attack(player) * active(HaloLevelManager.getHaloLevel(stack),
                5.94f,6.83f,8.61f,9.50f,11.28f);
        HaloDamage.deal(player, target, HaloAttackType.MAGIC, amount, .5f);
        particles(target, ParticleTypes.END_ROD);
    }

    private static void activateHoshino(ServerPlayer player, ItemStack stack) {
        if (!beginCooldown(player, stack, "Hoshino", 800)) return;
        int level = HaloLevelManager.getHaloLevel(stack);
        CompoundTag state = HaloRuntime.state(player);
        state.putInt("HoshinoHits", 4);
        state.putLong("HoshinoNextHit", player.level().getGameTime());
        state.putFloat("HoshinoMultiplier", active(level,4.35f,5.01f,5.66f,6.32f,6.97f) / 4f);
        state.putFloat("HoshinoStun", active(level,0,0,1f,1.2f,1.4f));
        state.putFloat("HoshinoShield", player.getMaxHealth() * passive(level,
                1.60f,2f,2.60f,3.20f,4.80f,5.50f,6.40f,7.20f,8.80f,10f));
        state.putLong("HoshinoShieldEnd", player.level().getGameTime() + 800);
    }

    private static void activateHoshinoSwimsuit(ServerPlayer player, ItemStack stack) {
        if (!beginCooldown(player, stack, "HoshinoSwimsuit", 1000)) return;
        int level = HaloLevelManager.getHaloLevel(stack);
        float bonus = active(level,1.5f,2f,3f,4f,5f);
        long end = player.level().getGameTime() + 1000;
        for (ServerPlayer ally : player.serverLevel().players()) {
            if (ally.distanceToSqr(player) <= 64) {
                CompoundTag state = HaloRuntime.state(ally);
                state.putLong("HoshinoSupportEnd", Math.max(end, state.getLong("HoshinoSupportEnd")));
                state.putFloat("HoshinoSupportBonus", Math.max(bonus, state.getFloat("HoshinoSupportBonus")));
            }
        }
        CompoundTag own = HaloRuntime.state(player);
        own.putLong("HoshinoSwimsuitActiveEnd", end);
        player.displayClientMessage(Component.literal("水上支援已展开"), true);
    }

    private static void activateHina(ServerPlayer player, ItemStack stack) {
        if (!beginCooldown(player, stack, "Hina", 1400)) return;
        float amount = attack(player) * active(HaloLevelManager.getHaloLevel(stack),
                6.36f,7.31f,9.22f,10.17f,12.08f);
        for (LivingEntity target : coneTargets(player, 10, .5)) {
            HaloDamage.deal(player, target, HaloAttackType.PHYSICAL, amount, .5f);
            particles(target, ParticleTypes.CRIT);
        }
    }

    private static void activateHinaSwimsuit(ServerPlayer player, ItemStack stack) {
        if (!beginCooldown(player, stack, "HinaSwimsuit", 600)) return;
        float amount = attack(player) * active(HaloLevelManager.getHaloLevel(stack),
                2.32f,3.69f,4.58f,5.12f,6.66f);
        List<LivingEntity> targets = enemies(player, player.getBoundingBox().inflate(16));
        targets.sort(Comparator.comparingDouble(player::distanceToSqr));
        targets.stream().limit(5).forEach(target -> {
            HaloDamage.deal(player, target, HaloAttackType.PHYSICAL, amount, .5f);
            particles(target, ParticleTypes.CRIT);
        });
        CompoundTag state = HaloRuntime.state(player);
        state.putInt("HinaSwimsuitStacks", Math.min(5, state.getInt("HinaSwimsuitStacks") + 1));
        HaloAttributes.refresh(player, stack, "hina_swimsuit_halo", true);
    }

    private static void activateHinaDress(ServerPlayer player, ItemStack stack) {
        CompoundTag state = HaloRuntime.state(player);
        long now = player.level().getGameTime();
        int shots = state.getInt("HinaDressShots");
        if (shots == 0 && !cooldownReady(player, stack, "HinaDress")) return;
        if (shots > 0 && now < state.getLong("HinaDressNextShot")) {
            player.displayClientMessage(Component.literal("下一发尚未准备完成"), true);
            return;
        }
        if (shots == 0) state.putLong("HinaDressSessionEnd", now + 400);
        boolean third = shots == 2;
        float multiplier = third
                ? active(HaloLevelManager.getHaloLevel(stack),6.80f,7.77f,9.87f,10.85f,12.88f)
                : active(HaloLevelManager.getHaloLevel(stack),3.24f,3.72f,4.71f,5.19f,6.18f);
        fireDressLine(player, attack(player) * multiplier);
        if (third) {
            state.remove("HinaDressShots");
            state.remove("HinaDressSessionEnd");
            stack.getOrCreateTag().putLong(cooldownKey("HinaDress"), now + 1200);
        } else {
            state.putInt("HinaDressShots", shots + 1);
            state.putLong("HinaDressNextShot", now + 20);
            player.displayClientMessage(Component.literal("集中射击 " + (shots + 1) + "/3"), true);
        }
    }

    private static void activateSakuluna(ServerPlayer player, ItemStack stack) {
        if (!beginCooldown(player, stack, "Sakuluna", 1800)) return;
        int seconds = Math.round(active(HaloLevelManager.getHaloLevel(stack),5,15,25,40,60));
        CompoundTag state = HaloRuntime.state(player);
        state.putLong("SakulunaVanishEnd", player.level().getGameTime() + seconds * 20L);
        state.putBoolean("SakulunaWasInvisible", player.isInvisible());
        state.putBoolean("SakulunaWasNoPhysics", player.noPhysics);
        player.setInvisible(true);
        player.noPhysics = true;
        player.displayClientMessage(Component.literal("存在已被抹去"), true);
    }

    private static void activateSakulunaFlight(ServerPlayer player, ItemStack stack) {
        if (!beginCooldown(player, stack, "SakulunaFlight", 600)) return;
        int level = HaloLevelManager.getHaloLevel(stack);
        int seconds = Math.round(passive(level,5,6,8,11,13,16,20,24,27,30));
        CompoundTag state = HaloRuntime.state(player);
        state.putLong("SakulunaFlightEnd", player.level().getGameTime() + seconds * 20L);
        player.getAbilities().mayfly = true;
        player.onUpdateAbilities();
        player.displayClientMessage(Component.literal("浮光掠影：可创造飞行 " + seconds + " 秒"), true);
    }

    public static void fireAlice(ServerPlayer player, float multiplier) {
        float amount = attack(player) * multiplier;
        for (LivingEntity target : aimedTargets(player, 32, 1.5)) {
            HaloDamage.deal(player, target, HaloAttackType.MAGIC, amount, .5f);
            particles(target, ParticleTypes.FLASH);
        }
        Vec3 start = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        for (int i = 0; i <= 32; i += 2) {
            Vec3 point = start.add(look.scale(i));
            player.serverLevel().sendParticles(ParticleTypes.SONIC_BOOM,
                    point.x, point.y, point.z, 1, 0, 0, 0, 0);
        }
    }

    static void hoshinoHit(ServerPlayer player, CompoundTag state) {
        float amount = attack(player) * state.getFloat("HoshinoMultiplier");
        float stunSeconds = state.getFloat("HoshinoStun");
        AABB area = player.getBoundingBox().inflate(2.5, 1.5, 2.5)
                .move(player.getLookAngle().multiply(2, 0, 2));
        for (LivingEntity target : enemies(player, area)) {
            target.invulnerableTime = 0;
            HaloDamage.deal(player, target, HaloAttackType.EXPLOSION, amount, .5f);
            stun(target, stunSeconds);
            particles(target, ParticleTypes.EXPLOSION);
        }
    }

    private static void fireDressLine(ServerPlayer player, float amount) {
        List<LivingEntity> targets = aimedTargets(player, 32, 1.5);
        float multiplier = 1;
        for (LivingEntity target : targets) {
            HaloDamage.deal(player, target, HaloAttackType.PHYSICAL, amount * multiplier, .5f);
            particles(target, ParticleTypes.CRIT);
            multiplier = Math.max(.10f, multiplier * .55f);
        }
    }

    static List<LivingEntity> aimedTargets(Player player, double range, double width) {
        Vec3 start = player.getEyePosition();
        Vec3 look = player.getLookAngle().normalize();
        Vec3 end = start.add(look.scale(range));
        List<LivingEntity> targets = enemies(player,
                player.getBoundingBox().expandTowards(look.scale(range)).inflate(width));
        targets.removeIf(target -> target.getBoundingBox().inflate(width).clip(start, end).isEmpty());
        targets.sort(Comparator.comparingDouble(target -> projection(start, look, target.getBoundingBox().getCenter())));
        return targets;
    }

    private static List<LivingEntity> coneTargets(Player player, double range, double minDot) {
        Vec3 look = player.getLookAngle().normalize();
        Vec3 eye = player.getEyePosition();
        List<LivingEntity> result = enemies(player, player.getBoundingBox().inflate(range));
        result.removeIf(target -> look.dot(target.getBoundingBox().getCenter().subtract(eye).normalize()) < minDot
                || target.distanceToSqr(player) > range * range);
        return result;
    }

    private static List<LivingEntity> enemies(Player player, AABB box) {
        return new ArrayList<>(player.level().getEntitiesOfClass(LivingEntity.class, box,
                e -> e != player && e.isAlive() && !e.isAlliedTo(player)));
    }

    private static double projection(Vec3 start, Vec3 direction, Vec3 point) {
        return Math.max(0, point.subtract(start).dot(direction));
    }

    static void stun(LivingEntity target, float seconds) {
        if (seconds <= 0 || RegistryEffect.STUN_EFFECT == null) return;
        target.addEffect(new MobEffectInstance(RegistryEffect.STUN_EFFECT.get(),
                Math.max(1, Math.round(seconds * 20)), 0, false, true, true));
    }

    private static void particles(LivingEntity target, net.minecraft.core.particles.ParticleOptions particle) {
        if (target.level() instanceof ServerLevel level) level.sendParticles(particle,
                target.getX(), target.getY() + target.getBbHeight() * .5, target.getZ(), 1, 0, 0, 0, 0);
    }

    private static float attack(Player player) {
        return (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE);
    }

    private static float active(int level, float... values) {
        return OtherHelper.getActiveValue(level, values);
    }

    static float passive(int level, float... values) {
        return OtherHelper.getPassiveValue(level, values);
    }

    private static boolean beginCooldown(ServerPlayer player, ItemStack stack, String skill, long ticks) {
        if (!cooldownReady(player, stack, skill)) return false;
        stack.getOrCreateTag().putLong(cooldownKey(skill), player.level().getGameTime() + ticks);
        return true;
    }

    private static boolean cooldownReady(ServerPlayer player, ItemStack stack, String skill) {
        long remaining = stack.getOrCreateTag().getLong(cooldownKey(skill)) - player.level().getGameTime();
        if (remaining <= 0) return true;
        player.displayClientMessage(Component.literal("技能冷却中：" + String.format("%.1f", remaining / 20f) + " 秒"), true);
        return false;
    }

    private static void refundCooldown(ItemStack stack, String skill) {
        stack.getOrCreateTag().remove(cooldownKey(skill));
    }

    private static String cooldownKey(String skill) {
        return "DBACooldown" + skill;
    }

    private HaloSkills() {}
}
