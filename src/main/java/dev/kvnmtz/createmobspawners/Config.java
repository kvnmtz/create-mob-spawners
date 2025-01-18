package dev.kvnmtz.createmobspawners;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber(modid = CreateMobSpawners.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> SOUL_CATCHER_ENTITY_BLACKLIST = BUILDER
            .push("soul_catcher")
            .comment("Entity ids that should not be capturable")
            .defineListAllowEmpty(
                    "entity_blacklist",
                    List.of(
                            "minecraft:iron_golem",
                            "minecraft:snow_golem",
                            "minecraft:warden"
                    ),
                    Config::validateEntityId
            );

    static final ForgeConfigSpec SPEC = BUILDER.build();

    public static Set<EntityType<?>> soulCatcherEntityBlacklist;

    private static boolean validateEntityId(final Object obj) {
        return obj instanceof final String entityId && ForgeRegistries.ENTITY_TYPES.containsKey(new ResourceLocation(entityId));
    }

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        soulCatcherEntityBlacklist = SOUL_CATCHER_ENTITY_BLACKLIST.get().stream()
                .map(entityId -> ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation(entityId)))
                .collect(Collectors.toSet());
    }
}
