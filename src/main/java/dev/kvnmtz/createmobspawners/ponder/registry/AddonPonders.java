package dev.kvnmtz.createmobspawners.ponder.registry;

import com.tterrag.registrate.util.entry.ItemProviderEntry;
import com.tterrag.registrate.util.entry.RegistryEntry;
import dev.kvnmtz.createmobspawners.block.registry.ModBlocks;
import dev.kvnmtz.createmobspawners.item.registry.ModItems;
import dev.kvnmtz.createmobspawners.ponder.scenes.SoulCatcherScenes;
import dev.kvnmtz.createmobspawners.ponder.scenes.SpawnerScenes;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.minecraft.resources.ResourceLocation;

public class AddonPonders {
    public static void register(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        PonderSceneRegistrationHelper<ItemProviderEntry<?>> HELPER = helper.withKeyFunction(RegistryEntry::getId);

        HELPER.forComponents(ModBlocks.MECHANICAL_SPAWNER).addStoryBoard("spawner", SpawnerScenes::spawner);
        HELPER.forComponents(ModItems.EMPTY_SOUL_CATCHER, ModItems.SOUL_CATCHER).addStoryBoard("soul_catcher", SoulCatcherScenes::soulCatcher);
    }
}
