package dev.kvnmtz.createmobspawners.block.custom.entity.registry;

import com.tterrag.registrate.util.entry.BlockEntityEntry;
import dev.kvnmtz.createmobspawners.block.custom.entity.MechanicalSpawnerBlockEntity;
import dev.kvnmtz.createmobspawners.block.custom.entity.renderer.MechanicalSpawnerBlockEntityRenderer;
import dev.kvnmtz.createmobspawners.block.registry.ModBlocks;

import static dev.kvnmtz.createmobspawners.CreateMobSpawners.REGISTRATE;

public class ModBlockEntities {
    public static final BlockEntityEntry<MechanicalSpawnerBlockEntity> MECHANICAL_SPAWNER = REGISTRATE
            .blockEntity("spawner_be", MechanicalSpawnerBlockEntity::new)
            .validBlocks(ModBlocks.MECHANICAL_SPAWNER)
            .renderer(() -> MechanicalSpawnerBlockEntityRenderer::new)
            .register();

    public static void register() {
        // init class
    }
}
