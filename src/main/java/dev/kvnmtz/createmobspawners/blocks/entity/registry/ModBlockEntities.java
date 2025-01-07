package dev.kvnmtz.createmobspawners.blocks.entity.registry;

import dev.kvnmtz.createmobspawners.CreateMobSpawners;
import dev.kvnmtz.createmobspawners.blocks.entity.MechanicalSpawnerBlockEntity;
import dev.kvnmtz.createmobspawners.blocks.registry.ModBlocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, CreateMobSpawners.MOD_ID);

    public static final RegistryObject<BlockEntityType<MechanicalSpawnerBlockEntity>> SPAWNER_BE = BLOCK_ENTITIES.register("spawner_be", () -> BlockEntityType.Builder.of(MechanicalSpawnerBlockEntity::new, ModBlocks.SPAWNER.get()).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
