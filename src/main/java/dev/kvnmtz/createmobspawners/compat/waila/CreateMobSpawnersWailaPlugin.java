package dev.kvnmtz.createmobspawners.compat.waila;

import dev.kvnmtz.createmobspawners.block.custom.MechanicalSpawnerBlock;
import dev.kvnmtz.createmobspawners.block.custom.entity.MechanicalSpawnerBlockEntity;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public class CreateMobSpawnersWailaPlugin implements IWailaPlugin {
    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(MechanicalSpawnerComponentProvider.INSTANCE, MechanicalSpawnerBlockEntity.class);
        registration.registerFluidStorage(MechanicalSpawnerComponentProvider.INSTANCE, MechanicalSpawnerBlockEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(MechanicalSpawnerComponentProvider.INSTANCE, MechanicalSpawnerBlock.class);
        registration.registerFluidStorageClient(MechanicalSpawnerComponentProvider.INSTANCE);
    }
}
