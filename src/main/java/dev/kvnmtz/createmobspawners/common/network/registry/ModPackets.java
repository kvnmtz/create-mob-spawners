package dev.kvnmtz.createmobspawners.common.network.registry;

import dev.kvnmtz.createmobspawners.CreateMobSpawnersMod;
import dev.kvnmtz.createmobspawners.common.network.packet.ClientboundCatchEntityPacket;
import dev.kvnmtz.createmobspawners.common.network.packet.ClientboundEntityGlowPacket;
import dev.kvnmtz.createmobspawners.common.network.packet.ClientboundReleaseEntityPacket;
import dev.kvnmtz.createmobspawners.common.network.packet.ClientboundSpawnerEventPacket;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

@EventBusSubscriber(modid = CreateMobSpawnersMod.MOD_ID)
public abstract class ModPackets {

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar("1");
        registrar.playToClient(
                ClientboundCatchEntityPacket.TYPE,
                ClientboundCatchEntityPacket.STREAM_CODEC,
                PayloadHandler::handle
        );
        registrar.playToClient(
                ClientboundReleaseEntityPacket.TYPE,
                ClientboundReleaseEntityPacket.STREAM_CODEC,
                PayloadHandler::handle
        );
        registrar.playToClient(
                ClientboundSpawnerEventPacket.TYPE,
                ClientboundSpawnerEventPacket.STREAM_CODEC,
                PayloadHandler::handle
        );
        registrar.playToClient(
                ClientboundEntityGlowPacket.TYPE,
                ClientboundEntityGlowPacket.STREAM_CODEC,
                PayloadHandler::handle
        );
    }
}
