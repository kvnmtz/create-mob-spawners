package dev.kvnmtz.createmobspawners;

import com.mojang.logging.LogUtils;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.item.ItemDescription;
import com.simibubi.create.foundation.item.TooltipModifier;
import dev.kvnmtz.createmobspawners.client.CreateMobSpawnersModClient;
import dev.kvnmtz.createmobspawners.common.block.registry.ModBlockEntities;
import dev.kvnmtz.createmobspawners.common.block.registry.ModBlocks;
import dev.kvnmtz.createmobspawners.common.config.ModServerConfig;
import dev.kvnmtz.createmobspawners.common.item.registry.ModCreativeModeTabs;
import dev.kvnmtz.createmobspawners.common.item.registry.ModItems;
import dev.kvnmtz.createmobspawners.common.item.tooltip.ModKineticStats;
import dev.kvnmtz.createmobspawners.common.network.registry.CreateAddonPackets;
import dev.kvnmtz.createmobspawners.common.recipe.registry.ModIngredients;
import dev.kvnmtz.createmobspawners.common.recipe.registry.ModRecipes;
import net.createmod.catnip.lang.FontHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import org.slf4j.Logger;

@Mod(CreateMobSpawnersMod.MOD_ID)
public final class CreateMobSpawnersMod {

    public static final String MOD_ID = "create_mob_spawners";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final CreateRegistrate REGISTRATE = CreateRegistrate.create(MOD_ID)
            .defaultCreativeTab(ModCreativeModeTabs.CREATE_MOB_SPAWNERS_TAB.getKey())
            .setTooltipModifierFactory(item -> new ItemDescription.Modifier(item, FontHelper.Palette.PURPLE)
                    .andThen(TooltipModifier.mapNull(ModKineticStats.create(item))));

    public CreateMobSpawnersMod(ModContainer container) {
        var eventBus = container.getEventBus();
        if (eventBus != null) {
            REGISTRATE.registerEventListeners(eventBus);
            ModItems.init();
            ModBlocks.init();
            ModBlockEntities.init();
            CreateAddonPackets.init();
            ModCreativeModeTabs.init(eventBus);
            ModIngredients.init(eventBus);
            ModRecipes.init(eventBus);
        }

        container.registerConfig(ModConfig.Type.SERVER, ModServerConfig.CONFIG_SPEC);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            CreateMobSpawnersModClient.init();
        }
    }

    public static ResourceLocation asResource(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
