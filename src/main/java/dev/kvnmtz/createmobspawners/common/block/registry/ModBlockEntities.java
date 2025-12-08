package dev.kvnmtz.createmobspawners.common.block.registry;

import com.tterrag.registrate.util.entry.BlockEntityEntry;
import dev.kvnmtz.createmobspawners.client.block.entity.renderer.MechanicalSpawnerBlockEntityRenderer;
import dev.kvnmtz.createmobspawners.common.block.entity.MechanicalSpawnerBlockEntity;

import static dev.kvnmtz.createmobspawners.CreateMobSpawnersMod.REGISTRATE;

public abstract class ModBlockEntities {

    public static final BlockEntityEntry<MechanicalSpawnerBlockEntity> MECHANICAL_SPAWNER = REGISTRATE
            .blockEntity("mechanical_spawner", MechanicalSpawnerBlockEntity::new)
            .validBlocks(ModBlocks.MECHANICAL_SPAWNER)
            .renderer(() -> MechanicalSpawnerBlockEntityRenderer::new)
            .register();

    public static void init() {
        // load class
    }
}
