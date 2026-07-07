package com.core.dream_sakura_blue_archive.ciorastao.util;

import com.core.dream_sakura.skill.SkillBinding;

import java.util.function.Supplier;

public class RegistryActiveSkill {
    public static final Supplier<SkillBinding> TENDOUARIS_HALO_Skill = () -> createHaloSkill("tendouaris_halo");
    public static final Supplier<SkillBinding> Hoshino_Halo_Skill = () -> createHaloSkill("hoshino_halo");
    public static final Supplier<SkillBinding> Hina_Halo_Skill = () -> createHaloSkill("hina_halo");
    public static final Supplier<SkillBinding> SHIROKO_Halo_Skill = () -> createHaloSkill("shiroko_halo");

    public static SkillBinding createHaloSkill(String itemId) {
        return HaloSkillRuntime.createBinding(itemId);
    }
}
