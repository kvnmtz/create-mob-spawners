package dev.kvnmtz.createmobspawners.datagen;

import dev.kvnmtz.createmobspawners.CreateMobSpawnersMod;
import dev.kvnmtz.createmobspawners.datagen.provider.client.ModBlockStateProvider;
import dev.kvnmtz.createmobspawners.datagen.provider.client.ModLanguageProvider;
import dev.kvnmtz.createmobspawners.datagen.provider.server.ModMechanicalCraftingRecipeProvider;
import dev.kvnmtz.createmobspawners.datagen.provider.server.ModRecipeProvider;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@EventBusSubscriber(modid = CreateMobSpawnersMod.MOD_ID)
public class CreateMobSpawnersDatagen {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        var generator = event.getGenerator();
        var output = generator.getPackOutput();
        var lookupProvider = event.getLookupProvider();
        var existingFileHelper = event.getExistingFileHelper();

        generator.addProvider(event.includeServer(), new ModRecipeProvider(output, lookupProvider));
        generator.addProvider(event.includeServer(), new ModMechanicalCraftingRecipeProvider(output, lookupProvider));

        generator.addProvider(event.includeClient(), new ModLanguageProvider(output));
        generator.addProvider(event.includeClient(), new ModBlockStateProvider(output, existingFileHelper));
    }
}