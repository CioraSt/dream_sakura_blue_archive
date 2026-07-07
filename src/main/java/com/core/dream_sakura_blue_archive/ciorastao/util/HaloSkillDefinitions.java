package com.core.dream_sakura_blue_archive.ciorastao.util;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class HaloSkillDefinitions {
    public enum DamageKind {
        MYSTIC,
        EXPLOSIVE,
        PIERCING
    }

    public enum ActiveKind {
        LINE,
        SINGLE,
        CONE,
        CONE_MULTI,
        MULTI_TARGET,
        TRIPLE_LINE,
        AREA,
        ALLY_BUFF,
        ALLY_HEAL,
        ALLY_SHIELD,
        ALLY_SHIELD_BEACON,
        SELF_BUFF,
        SELF_SHIELD,
        DRONE,
        MARK,
        MARK_DAMAGE,
        FEAR,
        ALLY_CHARM,
        DELAYED_BOMB,
        OVERDRIVE,
        FUTURE_SHIELD,
        EXTEND_BUFF,
        REVEAL_ZONE,
        LINE_SPEED,
        ANCHOR_STEALTH,
        ALLY_MARK,
        AREA_HEAL_DAMAGE
    }

    public enum PassiveKind {
        STATIC_ATTRIBUTE,
        ON_HIT_EXTRA,
        LOW_HEALTH_BUFF,
        ACTIVE_AFTER_BUFF,
        PERIODIC_AURA,
        MARK_VULNERABILITY,
        DAMAGE_REDUCTION,
        SHIELD_BOOST
    }

    public static final class PassiveSpec {
        public final PassiveKind kind;
        public final String attributeName;
        public final int operation;
        public final float[] values;
        public final float[] altValues;
        public final int cooldownTicks;
        public final int everyHits;

        private PassiveSpec(PassiveKind kind, String attributeName, int operation, float[] values,
                            float[] altValues, int cooldownTicks, int everyHits) {
            this.kind = kind;
            this.attributeName = attributeName;
            this.operation = operation;
            this.values = values;
            this.altValues = altValues;
            this.cooldownTicks = cooldownTicks;
            this.everyHits = everyHits;
        }
    }

    public static final class Definition {
        public final String itemId;
        public final String label;
        public final String status;
        public final int rarity;
        public final DamageKind damageKind;
        public final ActiveKind activeKind;
        public final int cooldownMs;
        public final float[] activeValues;
        public final float[] activeAltValues;
        public final List<PassiveSpec> passives;

        public Definition(String itemId, String label, String status, int rarity, DamageKind damageKind,
                          ActiveKind activeKind, int cooldownMs, float[] activeValues, float[] activeAltValues,
                          List<PassiveSpec> passives) {
            this.itemId = itemId;
            this.label = label;
            this.status = status;
            this.rarity = rarity;
            this.damageKind = damageKind;
            this.activeKind = activeKind;
            this.cooldownMs = cooldownMs;
            this.activeValues = activeValues;
            this.activeAltValues = activeAltValues;
            this.passives = passives;
        }

        public boolean usesLegacyPassiveHooks() {
            return "tendouaris_halo".equals(itemId) || "hoshino_halo".equals(itemId) || "hina_halo".equals(itemId);
        }
    }

    private static final Map<String, Definition> DEFINITIONS = new LinkedHashMap<>();

    static {
        add(new Definition("tendouaris_halo", "tendouaris", "\u5df2\u5b9e\u88c5", 3, DamageKind.MYSTIC, ActiveKind.LINE, 60000, f(3.11f, 3.57f, 4.51f, 5f, 6f), f(0f, 0f, 0f, 0f, 0f), defaultPassive("tendouaris_halo")));
        add(new Definition("tendouaris_maid_halo", "tendouaris maid", "\u4ec5\u6587\u6848", 3, DamageKind.MYSTIC, ActiveKind.SINGLE, 40000, f(3.51f, 4.52f, 5.62f, 6.71f, 7.8f), f(0f, 0f, 0f, 0f, 0f), defaultPassive("tendouaris_maid_halo")));
        add(new Definition("hoshino_halo", "hoshino", "\u5df2\u5b9e\u88c5", 3, DamageKind.PIERCING, ActiveKind.CONE_MULTI, 40000, f(4.35f, 5.01f, 5.66f, 6.32f, 6.97f), f(0f, 0f, 1f, 1.2f, 1.4f), defaultPassive("hoshino_halo")));
        add(new Definition("hoshino_swimsuit_halo", "hoshino swimsuit", "\u4ec5\u6587\u6848", 3, DamageKind.EXPLOSIVE, ActiveKind.ALLY_BUFF, 75000, f(0.2f, 0.26f, 0.32f, 0.39f, 0.45f), f(0f, 0f, 0f, 0f, 0f), defaultPassive("hoshino_swimsuit_halo")));
        add(new Definition("hina_halo", "hina", "\u534a\u5b9e\u88c5", 3, DamageKind.EXPLOSIVE, ActiveKind.CONE, 70000, f(3.51f, 4.52f, 5.62f, 6.71f, 7.8f), f(0f, 0f, 0f, 0f, 0f), defaultPassive("hina_halo")));
        add(new Definition("hina_swimsuit_halo", "hina swimsuit", "\u4ec5\u6587\u6848", 3, DamageKind.EXPLOSIVE, ActiveKind.MULTI_TARGET, 40000, f(1.71f, 2.2f, 2.74f, 3.27f, 3.8f), f(0f, 0f, 0f, 0f, 0f), defaultPassive("hina_swimsuit_halo")));
        add(new Definition("hina_dress_halo", "hina dress", "\u4ec5\u6587\u6848", 3, DamageKind.EXPLOSIVE, ActiveKind.TRIPLE_LINE, 60000, f(1.62f, 2.09f, 2.59f, 3.1f, 3.6f), f(3.42f, 4.41f, 5.47f, 6.54f, 7.6f), defaultPassive("hina_dress_halo")));
        add(new Definition("sakuluna_halo", "sakuluna", "\u4ec5\u6587\u6848", 3, DamageKind.MYSTIC, ActiveKind.ANCHOR_STEALTH, 120000, f(8f, 10f, 13f, 15f, 18f), f(0f, 0f, 0f, 0f, 0f), defaultPassive("sakuluna_halo")));
        add(new Definition("mari_halo", "mari", "\u4ec5\u7b56\u5212", 2, DamageKind.MYSTIC, ActiveKind.ALLY_SHIELD, 40000, f(0.2f, 0.26f, 0.32f, 0.39f, 0.45f), f(0f, 0f, 0f, 0f, 0f), defaultPassive("mari_halo")));
        add(new Definition("mari_gym_halo", "mari gym", "\u4ec5\u7b56\u5212", 3, DamageKind.MYSTIC, ActiveKind.ALLY_HEAL, 50000, f(0.25f, 0.32f, 0.4f, 0.47f, 0.55f), f(0f, 0f, 0f, 0f, 0f), defaultPassive("mari_gym_halo")));
        add(new Definition("shiroko_halo", "shiroko", "\u534a\u5b9e\u88c5\uff08\u4e3b\u52a8\u5360\u4f4d\uff09", 3, DamageKind.EXPLOSIVE, ActiveKind.DRONE, 45000, f(3.24f, 4.18f, 5.18f, 6.19f, 7.2f), f(0f, 0f, 0f, 0f, 0f), defaultPassive("shiroko_halo")));
        add(new Definition("kluonuoya_halo", "kluonuoya", "\u534a\u5b9e\u88c5\uff08\u6807\u8bb0\u672a\u4f7f\u7528\uff09", 3, DamageKind.MYSTIC, ActiveKind.MARK, 50000, f(0.14f, 0.17f, 0.22f, 0.26f, 0.3f), f(0f, 0f, 0f, 0f, 0f), defaultPassive("kluonuoya_halo")));
        add(new Definition("serlka_halo", "serlka", "\u4ec5\u7b56\u5212\uff08\u4ee3\u7801\u6709\u6ce8\u91ca\u75d5\u8ff9\uff09", 2, DamageKind.EXPLOSIVE, ActiveKind.SELF_BUFF, 45000, f(0.16f, 0.2f, 0.25f, 0.3f, 0.35f), f(0.14f, 0.17f, 0.22f, 0.26f, 0.3f), defaultPassive("serlka_halo")));
        add(new Definition("nonomi_halo", "nonomi", "\u4ec5\u7b56\u5212\uff08\u4ee3\u7801\u6709\u6ce8\u91ca\u75d5\u8ff9\uff09", 2, DamageKind.PIERCING, ActiveKind.CONE_MULTI, 50000, f(2.61f, 3.36f, 4.18f, 4.99f, 5.8f), f(0f, 0f, 0f, 0f, 0f), defaultPassive("nonomi_halo")));
        add(new Definition("shirasuazusa_halo", "shirasuazusa", "\u4ec5\u7b56\u5212\uff08\u4ee3\u7801\u6709\u6ce8\u91ca\u75d5\u8ff9\uff09", 3, DamageKind.EXPLOSIVE, ActiveKind.SINGLE, 45000, f(3.42f, 4.41f, 5.47f, 6.54f, 7.6f), f(0f, 0f, 0f, 0f, 0f), defaultPassive("shirasuazusa_halo")));
        add(new Definition("shiroko_cycling_halo", "shiroko cycling", "\u4ec5\u7b56\u5212", 3, DamageKind.PIERCING, ActiveKind.LINE_SPEED, 35000, f(2.34f, 3.02f, 3.74f, 4.47f, 5.2f), f(5f, 7f, 9f, 10f, 12f), defaultPassive("shiroko_cycling_halo")));
        add(new Definition("shiroko_swimsuit_halo", "shiroko swimsuit", "\u4ec5\u7b56\u5212", 3, DamageKind.MYSTIC, ActiveKind.REVEAL_ZONE, 45000, f(6f, 8f, 10f, 12f, 14f), f(0.16f, 0.2f, 0.25f, 0.3f, 0.35f), defaultPassive("shiroko_swimsuit_halo")));
        add(new Definition("ayaneko_halo", "ayaneko", "\u4ec5\u7b56\u5212", 2, DamageKind.MYSTIC, ActiveKind.ALLY_HEAL, 45000, f(7f, 9f, 12f, 14f, 16f), f(0f, 0f, 0f, 0f, 0f), defaultPassive("ayaneko_halo")));
        add(new Definition("ayane_swimsuit_halo", "ayane swimsuit", "\u4ec5\u7b56\u5212", 3, DamageKind.EXPLOSIVE, ActiveKind.DRONE, 70000, f(3.42f, 4.41f, 5.47f, 6.54f, 7.6f), f(0f, 0f, 0f, 0f, 0f), defaultPassive("ayane_swimsuit_halo")));
        add(new Definition("yume_halo", "yume", "\u4ec5\u7b56\u5212", 3, DamageKind.MYSTIC, ActiveKind.ALLY_SHIELD, 95000, f(0.32f, 0.41f, 0.5f, 0.6f, 0.7f), f(0.16f, 0.2f, 0.25f, 0.3f, 0.35f), defaultPassive("yume_halo")));
        add(new Definition("kayoko_halo", "kayoko", "\u4ec5\u7b56\u5212", 2, DamageKind.MYSTIC, ActiveKind.FEAR, 45000, f(2.2f, 2.9f, 3.6f, 4.3f, 5f), f(0.11f, 0.14f, 0.18f, 0.22f, 0.25f), defaultPassive("kayoko_halo")));
        add(new Definition("kayoko_newyear_halo", "kayoko newyear", "\u4ec5\u7b56\u5212", 3, DamageKind.MYSTIC, ActiveKind.ALLY_CHARM, 55000, f(0.32f, 0.41f, 0.5f, 0.6f, 0.7f), f(0f, 0f, 0f, 0f, 0f), defaultPassive("kayoko_newyear_halo")));
        add(new Definition("tendouaris_battle_halo", "tendouaris battle", "\u4ec5\u7b56\u5212", 3, DamageKind.MYSTIC, ActiveKind.LINE, 60000, f(4.05f, 5.22f, 6.48f, 7.74f, 9f), f(0f, 0f, 0f, 0f, 0f), defaultPassive("tendouaris_battle_halo")));
        add(new Definition("yuzu_halo", "yuzu", "\u4ec5\u7b56\u5212", 3, DamageKind.EXPLOSIVE, ActiveKind.SINGLE, 40000, f(3.15f, 4.06f, 5.04f, 6.02f, 7f), f(0f, 0f, 0f, 0f, 0f), defaultPassive("yuzu_halo")));
        add(new Definition("yuzu_battle_halo", "yuzu battle", "\u4ec5\u7b56\u5212", 3, DamageKind.EXPLOSIVE, ActiveKind.MARK_DAMAGE, 45000, f(3.42f, 4.41f, 5.47f, 6.54f, 7.6f), f(0f, 0f, 0f, 0f, 0f), defaultPassive("yuzu_battle_halo")));
        add(new Definition("yuzu_maid_halo", "yuzu maid", "\u4ec5\u7b56\u5212", 3, DamageKind.EXPLOSIVE, ActiveKind.DELAYED_BOMB, 42000, f(3.24f, 4.18f, 5.18f, 6.19f, 7.2f), f(0f, 0f, 0f, 0f, 0f), defaultPassive("yuzu_maid_halo")));
        add(new Definition("momoi_halo", "momoi", "\u4ec5\u7b56\u5212", 2, DamageKind.PIERCING, ActiveKind.CONE, 35000, f(2.43f, 3.13f, 3.89f, 4.64f, 5.4f), f(0f, 0f, 0f, 0f, 0f), defaultPassive("momoi_halo")));
        add(new Definition("momoi_maid_halo", "momoi maid", "\u4ec5\u7b56\u5212", 3, DamageKind.PIERCING, ActiveKind.AREA, 40000, f(2.97f, 3.83f, 4.75f, 5.68f, 6.6f), f(0f, 0f, 0f, 0f, 0f), defaultPassive("momoi_maid_halo")));
        add(new Definition("midori_halo", "midori", "\u4ec5\u7b56\u5212", 2, DamageKind.PIERCING, ActiveKind.SINGLE, 38000, f(2.79f, 3.6f, 4.46f, 5.33f, 6.2f), f(0f, 0f, 0f, 0f, 0f), defaultPassive("midori_halo")));
        add(new Definition("midori_maid_halo", "midori maid", "\u4ec5\u7b56\u5212", 3, DamageKind.PIERCING, ActiveKind.LINE, 42000, f(3.24f, 4.18f, 5.18f, 6.19f, 7.2f), f(0f, 0f, 0f, 0f, 0f), defaultPassive("midori_maid_halo")));
        add(new Definition("kaiyi_halo", "kaiyi", "\u4ec5\u7b56\u5212", 3, DamageKind.MYSTIC, ActiveKind.OVERDRIVE, 70000, f(0.32f, 0.41f, 0.5f, 0.6f, 0.7f), f(0f, 0f, 0f, 0f, 0f), defaultPassive("kaiyi_halo")));
        add(new Definition("shirasuazusa_swimsuit_halo", "shirasuazusa swimsuit", "\u4ec5\u7b56\u5212", 3, DamageKind.MYSTIC, ActiveKind.SINGLE, 45000, f(3.42f, 4.41f, 5.47f, 6.54f, 7.6f), f(0f, 0f, 0f, 0f, 0f), defaultPassive("shirasuazusa_swimsuit_halo")));
        add(new Definition("mari_idol_halo", "mari idol", "\u4ec5\u7b56\u5212", 3, DamageKind.MYSTIC, ActiveKind.ALLY_BUFF, 60000, f(0.16f, 0.2f, 0.25f, 0.3f, 0.35f), f(0f, 0f, 0f, 0f, 0f), defaultPassive("mari_idol_halo")));
        add(new Definition("seia_halo", "seia", "\u4ec5\u7b56\u5212", 3, DamageKind.MYSTIC, ActiveKind.FUTURE_SHIELD, 65000, f(0.34f, 0.44f, 0.54f, 0.64f, 0.75f), f(0f, 0f, 0f, 0f, 0f), defaultPassive("seia_halo")));
        add(new Definition("seia_swimsuit_halo", "seia swimsuit", "\u4ec5\u7b56\u5212", 3, DamageKind.MYSTIC, ActiveKind.EXTEND_BUFF, 55000, f(4f, 6f, 7f, 9f, 10f), f(0f, 0f, 0f, 0f, 0f), defaultPassive("seia_swimsuit_halo")));
        add(new Definition("natsu_halo", "natsu", "\u4ec5\u7b56\u5212", 3, DamageKind.EXPLOSIVE, ActiveKind.SELF_SHIELD, 45000, f(0.36f, 0.46f, 0.58f, 0.69f, 0.8f), f(0f, 0f, 0f, 0f, 0f), defaultPassive("natsu_halo")));
        add(new Definition("natsu_band_halo", "natsu band", "\u4ec5\u7b56\u5212", 3, DamageKind.EXPLOSIVE, ActiveKind.ALLY_HEAL, 50000, f(7f, 9f, 12f, 14f, 16f), f(0f, 0f, 0f, 0f, 0f), defaultPassive("natsu_band_halo")));
        add(new Definition("shun_halo", "shun", "\u4ec5\u7b56\u5212", 3, DamageKind.PIERCING, ActiveKind.SINGLE, 45000, f(3.24f, 4.18f, 5.18f, 6.19f, 7.2f), f(0f, 0f, 0f, 0f, 0f), defaultPassive("shun_halo")));
        add(new Definition("karena_halo", "karena", "\u4ec5\u7b56\u5212", 2, DamageKind.MYSTIC, ActiveKind.ALLY_MARK, 35000, f(0.18f, 0.23f, 0.29f, 0.34f, 0.4f), f(0f, 0f, 0f, 0f, 0f), defaultPassive("karena_halo")));
        add(new Definition("juguang_halo", "juguang", "\u4ec5\u7b56\u5212", 2, DamageKind.MYSTIC, ActiveKind.AREA_HEAL_DAMAGE, 60000, f(2.25f, 2.9f, 3.6f, 4.3f, 5f), f(0.14f, 0.17f, 0.22f, 0.26f, 0.3f), defaultPassive("juguang_halo")));
        add(new Definition("juwang_halo", "juwang", "\u4ec5\u7b56\u5212", 2, DamageKind.PIERCING, ActiveKind.REVEAL_ZONE, 55000, f(7f, 9f, 12f, 14f, 16f), f(0.11f, 0.14f, 0.18f, 0.22f, 0.25f), defaultPassive("juwang_halo")));
        add(new Definition("kuroko_halo", "kuroko", "\u4ec5\u7b56\u5212", 3, DamageKind.MYSTIC, ActiveKind.DRONE, 65000, f(7f, 9f, 12f, 14f, 16f), f(3.42f, 4.41f, 5.47f, 6.54f, 7.6f), defaultPassive("kuroko_halo")));
        add(new Definition("ren_halo", "ren", "\u4ec5\u7b56\u5212", 3, DamageKind.MYSTIC, ActiveKind.ALLY_SHIELD_BEACON, 120000, f(0.4f, 0.52f, 0.65f, 0.77f, 0.9f), f(4f, 6f, 7f, 9f, 10f), defaultPassive("ren_halo")));
    }

    public static Definition get(String itemId) {
        return DEFINITIONS.get(itemId);
    }

    public static boolean contains(String itemId) {
        return DEFINITIONS.containsKey(itemId);
    }

    public static Collection<Definition> all() {
        return Collections.unmodifiableCollection(DEFINITIONS.values());
    }

    private static void add(Definition definition) {
        DEFINITIONS.put(definition.itemId, definition);
    }

    private static List<PassiveSpec> defaultPassive(String itemId) {
        return switch (itemId) {
            case "tendouaris_halo" -> List.of(
                    attr("attributeslib:crit_damage", f(0.25f, 0.5f, 0.75f, 1f, 1.25f, 1.5f, 1.75f, 2f, 2.25f, 2.5f), 2),
                    attr("minecraft:generic.attack_damage", f(0.2f, 0.35f, 0.5f, 0.65f, 0.8f, 1f, 1.25f, 1.5f, 1.8f, 2f), 2),
                    afterActive("attributeslib:crit_damage", f(0.25f, 0.5f, 0.75f, 1f, 1.5f, 2f, 3f, 4f, 5f, 6f), 2)
            );
            case "hoshino_halo" -> List.of(
                    lowHpBuff(f(0.04f, 0.05f, 0.06f, 0.08f, 0.1f, 0.12f, 0.15f, 0.18f, 0.22f, 0.25f), f(0.2f, 0.2f, 0.2f, 0.2f, 0.2f, 0.2f, 0.2f, 0.2f, 0.2f, 0.2f), 2400),
                    attr("minecraft:generic.armor", f(5f, 8f, 12f, 16f, 21f, 27f, 34f, 40f, 45f, 50f), 0),
                    attr("minecraft:generic.armor_toughness", f(1f, 1f, 2f, 2f, 3f, 4f, 5f, 6f, 8f, 10f), 0)
            );
            case "hina_halo" -> List.of(
                    attr("minecraft:generic.attack_speed", f(0.1f, 0.13f, 0.16f, 0.19f, 0.22f, 0.24f, 0.27f, 0.3f, 0.33f, 0.35f), 2),
                    onHitExtra(3, f(0.14f, 0.17f, 0.21f, 0.24f, 0.28f, 0.31f, 0.35f, 0.39f, 0.42f, 0.45f), 0),
                    onHitExtra(0, f(0.24f, 0.3f, 0.37f, 0.43f, 0.5f, 0.56f, 0.62f, 0.69f, 0.74f, 0.8f), 0)
            );
            case "tendouaris_maid_halo" -> List.of(onHitExtra(16, f(0.45f, 0.57f, 0.69f, 0.81f, 0.93f, 1.05f, 1.17f, 1.29f, 1.4f, 1.5f), 0), attr("attributeslib:crit_damage", f(0.14f, 0.17f, 0.21f, 0.24f, 0.28f, 0.31f, 0.35f, 0.39f, 0.42f, 0.45f), 2), onHitExtra(0, f(0.24f, 0.3f, 0.37f, 0.43f, 0.5f, 0.56f, 0.62f, 0.69f, 0.74f, 0.8f), 0));
            case "kluonuoya_halo" -> List.of(attr("minecraft:generic.luck", f(0.08f, 0.1f, 0.12f, 0.14f, 0.16f, 0.18f, 0.2f, 0.22f, 0.24f, 0.25f), 0), markVulnerability(f(0.09f, 0.12f, 0.15f, 0.18f, 0.2f, 0.22f, 0.24f, 0.26f, 0.28f, 0.3f)), periodicAura(f(0.06f, 0.08f, 0.1f, 0.12f, 0.14f, 0.15f, 0.17f, 0.18f, 0.19f, 0.2f)));
            case "mari_halo", "mari_gym_halo", "ayaneko_halo", "mari_idol_halo", "karena_halo" -> List.of(attr("minecraft:generic.max_health", support(f(0.04f, 0.12f)), 2), periodicAura(support(f(0.04f, 0.2f))), shieldBoost(support(f(0.1f, 0.35f))));
            case "hoshino_swimsuit_halo", "yume_halo", "ren_halo", "natsu_halo", "natsu_band_halo" -> List.of(attr("minecraft:generic.armor", tank(f(2f, 24f)), 0), attr("minecraft:generic.attack_damage", support(f(0.08f, 0.25f)), 2), damageReduction(support(f(0.06f, 0.25f))));
            case "serlka_halo", "momoi_halo", "midori_halo", "juguang_halo", "juwang_halo" -> List.of(attr("minecraft:generic.luck", f(1f, 1f, 1f, 2f, 2f, 2f, 3f, 3f, 4f, 5f), 0), attr("minecraft:generic.attack_damage", support(f(0.08f, 0.3f)), 2), lowHpBuff(support(f(0.08f, 0.25f)), support(f(0.08f, 0.25f)), 900));
            case "shiroko_halo", "shiroko_cycling_halo", "shiroko_swimsuit_halo", "kuroko_halo" -> List.of(attr("minecraft:generic.movement_speed", support(f(0.04f, 0.3f)), 2), attr("minecraft:generic.attack_damage", support(f(0.1f, 0.35f)), 2), lowHpBuff(support(f(0.12f, 0.4f)), support(f(0.12f, 0.4f)), 1200));
            case "hina_swimsuit_halo", "hina_dress_halo", "nonomi_halo", "shirasuazusa_halo", "shirasuazusa_swimsuit_halo", "tendouaris_battle_halo", "yuzu_halo", "yuzu_battle_halo", "yuzu_maid_halo", "momoi_maid_halo", "midori_maid_halo", "shun_halo" -> List.of(attr("minecraft:generic.attack_damage", support(f(0.1f, 0.35f)), 2), attr("attributeslib:crit_damage", support(f(0.1f, 0.45f)), 2), onHitExtra(0, support(f(0.24f, 0.9f)), 0));
            case "kayoko_halo", "kayoko_newyear_halo", "seia_halo", "seia_swimsuit_halo", "kaiyi_halo", "ayane_swimsuit_halo" -> List.of(attr("attributeslib:crit_damage", support(f(0.1f, 0.45f)), 2), markVulnerability(support(f(0.08f, 0.3f))), activeAfterBuff("minecraft:generic.attack_damage", support(f(0.08f, 0.35f)), 2));
            default -> List.of(attr("minecraft:generic.attack_damage", support(f(0.06f, 0.25f)), 2));
        };
    }

    private static PassiveSpec attr(String attributeName, float[] values, int operation) {
        return new PassiveSpec(PassiveKind.STATIC_ATTRIBUTE, attributeName, operation, values, empty10(), 0, 0);
    }

    private static PassiveSpec activeAfterBuff(String attributeName, float[] values, int operation) {
        return new PassiveSpec(PassiveKind.ACTIVE_AFTER_BUFF, attributeName, operation, values, empty10(), 600, 0);
    }

    private static PassiveSpec afterActive(String attributeName, float[] values, int operation) {
        return new PassiveSpec(PassiveKind.ACTIVE_AFTER_BUFF, attributeName, operation, values, empty10(), 400, 0);
    }

    private static PassiveSpec onHitExtra(int everyHits, float[] values, int cooldownTicks) {
        return new PassiveSpec(PassiveKind.ON_HIT_EXTRA, "", 0, values, empty10(), cooldownTicks, everyHits);
    }

    private static PassiveSpec lowHpBuff(float[] values, float[] altValues, int cooldownTicks) {
        return new PassiveSpec(PassiveKind.LOW_HEALTH_BUFF, "", 0, values, altValues, cooldownTicks, 0);
    }

    private static PassiveSpec markVulnerability(float[] values) {
        return new PassiveSpec(PassiveKind.MARK_VULNERABILITY, "", 0, values, empty10(), 0, 0);
    }

    private static PassiveSpec periodicAura(float[] values) {
        return new PassiveSpec(PassiveKind.PERIODIC_AURA, "", 0, values, empty10(), 0, 0);
    }

    private static PassiveSpec damageReduction(float[] values) {
        return new PassiveSpec(PassiveKind.DAMAGE_REDUCTION, "", 0, values, empty10(), 0, 0);
    }

    private static PassiveSpec shieldBoost(float[] values) {
        return new PassiveSpec(PassiveKind.SHIELD_BOOST, "", 0, values, empty10(), 0, 0);
    }

    private static float[] support(float[] endpoints) {
        return scale(endpoints[0], endpoints[1]);
    }

    private static float[] tank(float[] endpoints) {
        return scale(endpoints[0], endpoints[1]);
    }

    private static float[] scale(float min, float max) {
        float[] values = new float[10];
        for (int i = 0; i < values.length; i++) {
            values[i] = min + (max - min) * i / 9.0f;
        }
        return values;
    }

    private static float[] empty10() {
        return f(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f);
    }

    private static float[] f(float... values) {
        return values;
    }

    private HaloSkillDefinitions() {
    }
}
