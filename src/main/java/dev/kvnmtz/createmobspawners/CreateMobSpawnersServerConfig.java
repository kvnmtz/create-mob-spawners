package dev.kvnmtz.createmobspawners;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

public class CreateMobSpawnersServerConfig {
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> soulCatcherEntityBlacklist;
    public final ForgeConfigSpec.DoubleValue soulCatcherMaxDistance;

    public final ForgeConfigSpec.DoubleValue mechanicalSpawnerMaxStressImpact;
    public final ForgeConfigSpec.DoubleValue mechanicalSpawnerMinRpm;

    public CreateMobSpawnersServerConfig(ForgeConfigSpec.Builder builder) {
        builder.push("soul_catcher");
        {
            soulCatcherEntityBlacklist = builder.comment("Entity ids that should not be capturable").defineListAllowEmpty("entity_blacklist", List.of("minecraft:iron_golem", "minecraft:snow_golem", "minecraft:warden"), CreateMobSpawnersServerConfig::validateEntityId);
            soulCatcherMaxDistance = builder.comment("Maximum distance at which entities can be captured").defineInRange("max_distance", 15.0, 5.0, 50.0);
        }
        builder.pop();

        builder.push("mechanical_spawner");
        {
            mechanicalSpawnerMaxStressImpact = builder.comment("Maximum stress impact (impact scales with entity health)").defineInRange("max_stress_impact", 80.0, 1.0, Double.MAX_VALUE);
            mechanicalSpawnerMinRpm = builder.comment("Minimum RPM required to progress").defineInRange("min_rpm", 128.0, 1.0, Double.MAX_VALUE);
        }
        builder.pop();
    }

    private static boolean validateEntityId(final Object obj) {
        return obj instanceof final String entityId && ForgeRegistries.ENTITY_TYPES.containsKey(new ResourceLocation(entityId));
    }
}
