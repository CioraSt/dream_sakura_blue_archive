package com.core.dream_sakura_blue_archive.ciorastao.util;

import com.core.dream_sakura.skill.SkillBinding;
import com.core.dream_sakura_blue_archive.ciorastao.network.C2SHaloSkillPacket;
import com.core.dream_sakura_blue_archive.ciorastao.network.NetworkHandler;
import org.lwjgl.glfw.GLFW;

import java.util.function.Supplier;

/**
 * 客户端键位桥。外部 API 的 SkillBinding 冷却是全局实例字段，不能区分玩家和 NBT 变体，
 * 因而这里固定为 0；权威冷却全部由服务器端 HaloSkills 按物品栈维护。
 */
public final class RegistryActiveSkill {
    public static final Supplier<SkillBinding> TENDOUARIS_HALO_Skill = binding("TENDOUARIS Halo Skill", "tendouaris_halo");
    public static final Supplier<SkillBinding> Hoshino_Halo_Skill = binding("Hoshino Halo Skill", "hoshino_halo");
    public static final Supplier<SkillBinding> Hina_Halo_Skill = binding("Hina Halo Skill", "hina_halo");
    public static final Supplier<SkillBinding> SAKULUNA_Halo_Skill = binding("Sakuluna Halo Skill", "sakuluna_halo");
    public static final Supplier<SkillBinding> SHIROKO_Halo_Skill = binding("SHIROKO Halo Skill", "shiroko_halo");

    private static Supplier<SkillBinding> binding(String description, String itemId) {
        return () -> new SkillBinding(GLFW.GLFW_KEY_J, description, 0, itemId,
                (player, stack) -> {
                    if (player.level().isClientSide) NetworkHandler.sendToServer(new C2SHaloSkillPacket());
                });
    }

    private RegistryActiveSkill() {}
}
