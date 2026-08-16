package ru.datapeice.anomalia.api;

import net.minecraft.text.Text;

public enum AnomalyType {
    SKYWARD_LIFT("skyward_lift"),
    HEAVY_GRAVITY("heavy_gravity"),
    ZERO_GRAVITY("zero_gravity"),
    CREATIVE_FLIGHT("creative_flight"),
    SUPER_TRAMPOLINE("super_trampoline"),
    ZERO_FRICTION("zero_friction"),
    ENDER_GLITCH("ender_glitch"),
    VORTEX_TORNADO("vortex_tornado"),
    SPEED_RAMP("speed_ramp"),
    BOOMERANG_PROJECTILES("boomerang_projectiles"),
    SUPER_SPEED("super_speed"),
    TURTLE_PACE("turtle_pace"),
    HIGH_JUMP("high_jump"),
    MAGNETIC_CHEST("magnetic_chest"),
    REPULSION_AURA("repulsion_aura"),
    HORIZONTAL_WIND("horizontal_wind"),
    BLINK_STEP("blink_step"),
    EARTHQUAKE("earthquake"),

    BLOCK_ROULETTE("block_roulette"),
    LIVING_ESCAPISTS("living_escapists"),
    ROCKET_BLOCKS("rocket_blocks"),
    PROLIFERATION("proliferation"),
    TELE_PLACEMENT("tele_placement"),
    SOUND_JUMPSCARE("sound_jumpscare"),
    CHICKEN_FOUNTAIN("chicken_fountain"),
    WATER_GEYSER("water_geyser"),
    MIDAS_GLASS("midas_glass"),
    INSTA_MINE("insta_mine"),
    GRAVITY_BLOCKS("gravity_blocks"),
    BLOCK_REJECTION("block_rejection"),
    ANVIL_RAIN("anvil_rain"),
    ORE_TRANSMUTATION("ore_transmutation"),
    INSTANT_GROWTH("instant_growth"),
    ICE_AGE("ice_age"),
    LAVA_SPONGE("lava_sponge"),
    BLOCK_DUPLICATION("block_duplication"),
    EXPLODING_ORES("exploding_ores"),
    BEDROCK_TRAP("bedrock_trap"),
    SAND_COLLAPSE("sand_collapse"),

    BOSS_APOCALYPSE("boss_apocalypse"),
    BODY_SWAP("body_swap"),
    KILLER_ANIMALS("killer_animals"),
    HEAVY_ARTILLERY("heavy_artillery"),
    CELL_DIVISION("cell_division"),
    DISCO_CHAOS("disco_chaos"),
    MICRO_FAST("micro_fast"),
    COMBUSTION_MOBS("combustion_mobs"),
    LIGHTNING_STRIKER("lightning_striker"),
    ANGRY_BEES_SWARM("angry_bees_swarm"),
    CREEPER_CHAIN("creeper_chain"),
    FLYING_PIGS("flying_pigs"),
    INVISO_MOBS("inviso_mobs"),
    SLIME_APOCALYPSE("slime_apocalypse"),
    PHANTOM_SWARM("phantom_swarm"),
    ARMORED_MOBS("armored_mobs"),

    DAMAGE_INVERSION("damage_inversion"),
    STOMACH_FLIP("stomach_flip"),
    KEEP_MOVING("keep_moving"),
    ITEM_ERUPTION("item_eruption"),
    HOT_HANDS("hot_hands"),
    CURSED_ENCHANTS("cursed_enchants"),
    GLASS_CANNON("glass_cannon"),
    POTION_ROULETTE("potion_roulette"),
    VAMPIRISM("vampirism"),
    THORNS_AURA("thorns_aura"),
    RANDOM_DROP_ON_HIT("random_drop_on_hit"),
    FIRE_FEET("fire_feet"),
    GOLDEN_TOUCH("golden_touch"),
    RADIO_NOISE("radio_noise");

    private final String id;

    AnomalyType(String id) {
        this.id = id;
    }

    public String getTranslationKey() {
        return "anomaly.anomalia." + id;
    }

    public String getName() {
        return Text.translatable(getTranslationKey()).getString();
    }
}

