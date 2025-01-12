package dev.kvnmtz.createmobspawners.ponder;

import com.simibubi.create.foundation.ponder.PonderRegistrationHelper;
import dev.kvnmtz.createmobspawners.CreateMobSpawners;
import dev.kvnmtz.createmobspawners.blocks.registry.ModBlocks;
import dev.kvnmtz.createmobspawners.ponder.scenes.SpawnerScenes;

public class PonderIndex {
    private static final PonderRegistrationHelper HELPER = new PonderRegistrationHelper(CreateMobSpawners.MOD_ID);

    public static void register() {
        HELPER.addStoryBoard(ModBlocks.SPAWNER.getId(), "spawner", SpawnerScenes::spawner);
    }
}
