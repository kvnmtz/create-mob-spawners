package dev.kvnmtz.createmobspawners;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

public class CreateMobSpawnersServerConfig {
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> soulCatcherEntityBlacklist;
    public final ForgeConfigSpec.DoubleValue soulCatcherMaxDistance;
    public final ForgeConfigSpec.DoubleValue soulCatcherMaxHealthPercentage;

    public final ForgeConfigSpec.DoubleValue mechanicalSpawnerMaxStressImpact;
    public final ForgeConfigSpec.DoubleValue mechanicalSpawnerMinRpm;
    public final ForgeConfigSpec.IntValue mechanicalSpawnerMaxNearbyEntities;
    public final ForgeConfigSpec.BooleanValue mechanicalSpawnerConfigurationAllowed;
    public final ForgeConfigSpec.IntValue mechanicalSpawnerAreaMinWidth;
    public final ForgeConfigSpec.IntValue mechanicalSpawnerAreaMaxWidth;
    public final ForgeConfigSpec.IntValue mechanicalSpawnerAreaDefaultWidth;
    public final ForgeConfigSpec.IntValue mechanicalSpawnerAreaMinHeight;
    public final ForgeConfigSpec.IntValue mechanicalSpawnerAreaMaxHeight;
    public final ForgeConfigSpec.IntValue mechanicalSpawnerAreaDefaultHeight;
    public final ForgeConfigSpec.IntValue mechanicalSpawnerAreaMinHeightOffset;
    public final ForgeConfigSpec.IntValue mechanicalSpawnerAreaMaxHeightOffset;
    public final ForgeConfigSpec.IntValue mechanicalSpawnerAreaDefaultHeightOffset;

    public CreateMobSpawnersServerConfig(ForgeConfigSpec.Builder builder) {
        builder.push("soul_catcher");
        {
            soulCatcherEntityBlacklist = builder.comment("Entity ids that should not be capturable").defineListAllowEmpty("entity_blacklist", List.of("minecraft:iron_golem", "minecraft:snow_golem", "minecraft:warden"), CreateMobSpawnersServerConfig::validateEntityId);
            soulCatcherMaxDistance = builder.comment("Maximum distance at which entities can be captured").defineInRange("max_distance", 15.0, 5.0, 50.0);
            soulCatcherMaxHealthPercentage = builder.comment("Entity health needs to be at or below this percentage to be able to be caught", "1.0 = 100% -> Entities can be caught at full health", "0.0 = Entities can only be caught at half a heart").defineInRange("max_health_percentage", 1.0, 0.0, 1.0);
        }
        builder.pop();

        builder.push("mechanical_spawner");
        {
            mechanicalSpawnerMaxStressImpact = builder.comment("Maximum stress impact (impact scales with entity health)").defineInRange("max_stress_impact", 80.0, 1.0, Double.MAX_VALUE);
            mechanicalSpawnerMinRpm = builder.comment("Minimum RPM required to progress").defineInRange("min_rpm", 128.0, 1.0, Double.MAX_VALUE);
            mechanicalSpawnerMaxNearbyEntities = builder.comment("Maximum number of nearby spawned entities", "If exceeded, the spawner will stall").defineInRange("max_nearby_entities", 6, 1, Integer.MAX_VALUE);
            mechanicalSpawnerConfigurationAllowed = builder.comment("Whether the configuration of the spawning area by players is allowed or not", "true = allowed").define("player_configuration_allowed", true);
            mechanicalSpawnerAreaMinWidth = builder.comment("Minimum square width of the spawning area").defineInRange("spawn_area_min_width", 3, 3, Integer.MAX_VALUE);
            mechanicalSpawnerAreaMaxWidth = builder.comment("Maximum square width of the spawning area").defineInRange("spawn_area_max_width", 11, 3, Integer.MAX_VALUE);
            mechanicalSpawnerAreaDefaultWidth = builder.comment("Default square width of the spawning area").defineInRange("spawn_area_default_width", 9, 3, Integer.MAX_VALUE);
            mechanicalSpawnerAreaMinHeight = builder.comment("Minimum height of the spawning area").defineInRange("spawn_area_min_height", 1, 1, Integer.MAX_VALUE);
            mechanicalSpawnerAreaMaxHeight = builder.comment("Maximum height of the spawning area").defineInRange("spawn_area_max_height", 7, 1, Integer.MAX_VALUE);
            mechanicalSpawnerAreaDefaultHeight = builder.comment("Default height of the spawning area").defineInRange("spawn_area_default_height", 3, 1, Integer.MAX_VALUE);
            mechanicalSpawnerAreaMinHeightOffset = builder.comment("Minimum height offset of the spawning area").defineInRange("spawn_area_min_height_offset", -4, Integer.MIN_VALUE, Integer.MAX_VALUE);
            mechanicalSpawnerAreaMaxHeightOffset = builder.comment("Maximum height offset of the spawning area").defineInRange("spawn_area_max_height_offset", 4, Integer.MIN_VALUE, Integer.MAX_VALUE);
            mechanicalSpawnerAreaDefaultHeightOffset = builder.comment("Default height offset of the spawning area").defineInRange("spawn_area_default_height_offset", 0, Integer.MIN_VALUE, Integer.MAX_VALUE);
        }
        builder.pop();
    }

    private static boolean validateEntityId(final Object obj) {
        return obj instanceof final String entityId && ForgeRegistries.ENTITY_TYPES.containsKey(new ResourceLocation(entityId));
    }
}
