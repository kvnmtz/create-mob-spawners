package dev.kvnmtz.createmobspawners.common.config;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

public class ModServerConfig {

    public static final ModServerConfig CONFIG;
    public static final ModConfigSpec CONFIG_SPEC;

    static {
        var pair = new ModConfigSpec.Builder().configure(ModServerConfig::new);

        CONFIG = pair.getLeft();
        CONFIG_SPEC = pair.getRight();
    }

    public final ModConfigSpec.ConfigValue<List<? extends String>> soulCatcherEntityBlacklist;
    public final ModConfigSpec.DoubleValue soulCatcherMaxDistance;
    public final ModConfigSpec.DoubleValue soulCatcherMaxHealthPercentage;
    public final ModConfigSpec.BooleanValue soulCatcherAllowBosses;
    public final ModConfigSpec.DoubleValue soulCatcherBossMaxHealthPercentage;
    public final ModConfigSpec.BooleanValue soulCatcherRequireWeakness;

    public final ModConfigSpec.DoubleValue mechanicalSpawnerMaxStressImpact;
    public final ModConfigSpec.DoubleValue mechanicalSpawnerMinRpm;
    public final ModConfigSpec.IntValue mechanicalSpawnerMaxNearbyEntities;
    public final ModConfigSpec.BooleanValue mechanicalSpawnerConfigurationAllowed;
    public final ModConfigSpec.IntValue mechanicalSpawnerAreaMinWidth;
    public final ModConfigSpec.IntValue mechanicalSpawnerAreaMaxWidth;
    public final ModConfigSpec.IntValue mechanicalSpawnerAreaDefaultWidth;
    public final ModConfigSpec.IntValue mechanicalSpawnerAreaMinHeight;
    public final ModConfigSpec.IntValue mechanicalSpawnerAreaMaxHeight;
    public final ModConfigSpec.IntValue mechanicalSpawnerAreaDefaultHeight;
    public final ModConfigSpec.IntValue mechanicalSpawnerAreaMinHeightOffset;
    public final ModConfigSpec.IntValue mechanicalSpawnerAreaMaxHeightOffset;
    public final ModConfigSpec.IntValue mechanicalSpawnerAreaDefaultHeightOffset;

    private ModServerConfig(ModConfigSpec.Builder builder) {
        builder.push("soul_catcher");
        {
            soulCatcherEntityBlacklist = builder
                    .comment("Entity ids that should not be capturable")
                    .defineListAllowEmpty(
                            "entity_blacklist",
                            () -> List.of("minecraft:iron_golem", "minecraft:snow_golem", "minecraft:warden"),
                            () -> "minecraft:pig",
                            o -> o instanceof String entityId && BuiltInRegistries.ENTITY_TYPE.containsKey(ResourceLocation.parse(entityId))
                    );

            soulCatcherMaxDistance = builder
                    .comment("Maximum distance at which entities can be captured")
                    .defineInRange("max_distance", 15.0, 5.0, 50.0);

            soulCatcherMaxHealthPercentage = builder
                    .comment(
                            "Entity health needs to be at or below this percentage to be able to be caught",
                            "1.0 = 100% -> Entities can be caught at full health",
                            "0.0 = Entities can only be caught at half a heart"
                    )
                    .defineInRange("max_health_percentage", 1.0, 0.0, 1.0);

            soulCatcherAllowBosses = builder
                    .comment("Allow bosses to be captured")
                    .define("allow_bosses", false);

            soulCatcherBossMaxHealthPercentage = builder
                    .comment(
                            "(This option is only relevant if bosses are allowed to be captured, they cannot be weakened so the remaining health needs to be checked)",
                            "Boss entity health needs to be at or below this percentage to be able to be caught",
                            "1.0 = 100% -> Entities can be caught at full health", "0.0 = Entities can only be caught at half a heart"
                    )
                    .defineInRange("max_boss_health_percentage", 0.05, 0.0, 1.0);

            soulCatcherRequireWeakness = builder
                    .comment("Require weakening a mob by applying Weakness before it's capturable")
                    .define("require_weakness", true);
        }
        builder.pop();

        builder.push("mechanical_spawner");
        {
            mechanicalSpawnerMaxStressImpact = builder
                    .comment("Maximum stress impact (impact scales with entity health)")
                    .defineInRange("max_stress_impact", 80.0, 1.0, Double.MAX_VALUE);

            mechanicalSpawnerMinRpm = builder
                    .comment("Minimum RPM required to progress")
                    .defineInRange("min_rpm", 128.0, 1.0, Double.MAX_VALUE);

            mechanicalSpawnerMaxNearbyEntities = builder
                    .comment(
                            "Maximum number of nearby spawned entities",
                            "If exceeded, the spawner will stall"
                    )
                    .defineInRange("max_nearby_entities", 6, 1, Integer.MAX_VALUE);

            mechanicalSpawnerConfigurationAllowed = builder
                    .comment(
                            "Whether the configuration of the spawning area by players is allowed or not",
                            "true = allowed"
                    )
                    .define("player_configuration_allowed", true);

            mechanicalSpawnerAreaMinWidth = builder
                    .comment("Minimum square width of the spawning area")
                    .defineInRange("spawn_area_min_width", 3, 3, Integer.MAX_VALUE);

            mechanicalSpawnerAreaMaxWidth = builder
                    .comment("Maximum square width of the spawning area")
                    .defineInRange("spawn_area_max_width", 11, 3, Integer.MAX_VALUE);

            mechanicalSpawnerAreaDefaultWidth = builder
                    .comment("Default square width of the spawning area")
                    .defineInRange("spawn_area_default_width", 9, 3, Integer.MAX_VALUE);

            mechanicalSpawnerAreaMinHeight = builder
                    .comment("Minimum height of the spawning area")
                    .defineInRange("spawn_area_min_height", 1, 1, Integer.MAX_VALUE);

            mechanicalSpawnerAreaMaxHeight = builder
                    .comment("Maximum height of the spawning area")
                    .defineInRange("spawn_area_max_height", 7, 1, Integer.MAX_VALUE);

            mechanicalSpawnerAreaDefaultHeight = builder
                    .comment("Default height of the spawning area")
                    .defineInRange("spawn_area_default_height", 3, 1, Integer.MAX_VALUE);

            mechanicalSpawnerAreaMinHeightOffset = builder
                    .comment("Minimum height offset of the spawning area")
                    .defineInRange("spawn_area_min_height_offset", -4, Integer.MIN_VALUE, Integer.MAX_VALUE);

            mechanicalSpawnerAreaMaxHeightOffset = builder
                    .comment("Maximum height offset of the spawning area")
                    .defineInRange("spawn_area_max_height_offset", 4, Integer.MIN_VALUE, Integer.MAX_VALUE);

            mechanicalSpawnerAreaDefaultHeightOffset = builder
                    .comment("Default height offset of the spawning area")
                    .defineInRange("spawn_area_default_height_offset", 0, Integer.MIN_VALUE, Integer.MAX_VALUE);
        }
        builder.pop();
    }
}
