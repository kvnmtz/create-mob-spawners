package dev.kvnmtz.createmobspawners.common.network.registry;

import dev.kvnmtz.createmobspawners.client.network.ClientPayloadHandler;
import dev.kvnmtz.createmobspawners.common.network.packet.ClientboundCatchEntityPacket;
import dev.kvnmtz.createmobspawners.common.network.packet.ClientboundEntityGlowPacket;
import dev.kvnmtz.createmobspawners.common.network.packet.ClientboundReleaseEntityPacket;
import dev.kvnmtz.createmobspawners.common.network.packet.ClientboundSpawnerEventPacket;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public abstract class PayloadHandler {

    public static void handle(ClientboundCatchEntityPacket payload, IPayloadContext context) {
        if (FMLEnvironment.dist != Dist.CLIENT) {
            return;
        }

        ClientPayloadHandler.handle(payload, context);
    }

    public static void handle(ClientboundReleaseEntityPacket payload, IPayloadContext context) {
        if (FMLEnvironment.dist != Dist.CLIENT) {
            return;
        }

        ClientPayloadHandler.handle(payload, context);
    }

    public static void handle(ClientboundSpawnerEventPacket payload, IPayloadContext context) {
        if (FMLEnvironment.dist != Dist.CLIENT) {
            return;
        }

        ClientPayloadHandler.handle(payload, context);
    }

    public static void handle(ClientboundEntityGlowPacket payload, IPayloadContext context) {
        if (FMLEnvironment.dist != Dist.CLIENT) {
            return;
        }

        ClientPayloadHandler.handle(payload, context);
    }
}
