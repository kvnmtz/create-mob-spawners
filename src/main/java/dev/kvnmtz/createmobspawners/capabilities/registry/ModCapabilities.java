package dev.kvnmtz.createmobspawners.capabilities.registry;

import dev.kvnmtz.createmobspawners.CreateMobSpawners;
import dev.kvnmtz.createmobspawners.capabilities.entitystorage.IEntityStorage;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CreateMobSpawners.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModCapabilities {
    public static final Capability<IEntityStorage> ENTITY_STORAGE = CapabilityManager.get(new CapabilityToken<>() {});

    @SubscribeEvent
    public static void register(RegisterCapabilitiesEvent event) {
        event.register(IEntityStorage.class);
    }
}
