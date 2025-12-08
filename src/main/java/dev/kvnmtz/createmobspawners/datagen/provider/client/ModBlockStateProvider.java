package dev.kvnmtz.createmobspawners.datagen.provider.client;

import dev.kvnmtz.createmobspawners.CreateMobSpawnersMod;
import dev.kvnmtz.createmobspawners.common.block.MechanicalSpawnerBlock;
import dev.kvnmtz.createmobspawners.common.block.registry.ModBlocks;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ModBlockStateProvider extends BlockStateProvider {

    public ModBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, CreateMobSpawnersMod.MOD_ID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        mechanicalSpawnerBlock();
    }

    private void mechanicalSpawnerBlock() {
        var model = new ModelFile.ExistingModelFile(
            ResourceLocation.fromNamespaceAndPath(CreateMobSpawnersMod.MOD_ID, "block/mechanical_spawner"), 
            models().existingFileHelper
        );

        getVariantBuilder(ModBlocks.MECHANICAL_SPAWNER.get())
            .forAllStates(state -> {
                var facing = state.getValue(MechanicalSpawnerBlock.FACING);
                
                return switch (facing) {
                    case NORTH -> ConfiguredModel.builder()
                        .modelFile(model)
                        .build();
                    case SOUTH -> ConfiguredModel.builder()
                        .modelFile(model)
                        .rotationY(180)
                        .build();
                    case EAST -> ConfiguredModel.builder()
                        .modelFile(model)
                        .rotationY(90)
                        .build();
                    case WEST -> ConfiguredModel.builder()
                        .modelFile(model)
                        .rotationY(270)
                        .build();
                    default -> throw new IllegalStateException("Unexpected value: " + facing);
                };
            });
    }
}