package dev.kvnmtz.createmobspawners.common.compat.waila;

import dev.kvnmtz.createmobspawners.common.block.MechanicalSpawnerBlock;
import dev.kvnmtz.createmobspawners.common.block.entity.MechanicalSpawnerBlockEntity;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public class CreateMobSpawnersWailaPlugin implements IWailaPlugin {

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(MechanicalSpawnerComponentProvider.INSTANCE, MechanicalSpawnerBlockEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(MechanicalSpawnerComponentProvider.INSTANCE, MechanicalSpawnerBlock.class);
    }
}
