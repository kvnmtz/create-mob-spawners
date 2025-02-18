package dev.kvnmtz.createmobspawners.network;

import dev.kvnmtz.createmobspawners.CreateMobSpawners;
import dev.kvnmtz.createmobspawners.network.packet.ClientboundEntityCatchPacket;
import dev.kvnmtz.createmobspawners.network.packet.ClientboundEntityReleasePacket;
import dev.kvnmtz.createmobspawners.network.packet.ClientboundSpawnerEventPacket;
import dev.kvnmtz.createmobspawners.network.packet.ServerboundConfigureSpawnerPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class PacketHandler {
    private static final String PROTOCOL_VERSION = "1";
    private static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            CreateMobSpawners.asResource("main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int currentPacketId = 0;

    public static void register() {
        INSTANCE.messageBuilder(ClientboundSpawnerEventPacket.class, currentPacketId++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(ClientboundSpawnerEventPacket::encode)
                .decoder(ClientboundSpawnerEventPacket::new)
                .consumerMainThread(ClientboundSpawnerEventPacket::handle)
                .add();

        INSTANCE.messageBuilder(ClientboundEntityCatchPacket.class, currentPacketId++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(ClientboundEntityCatchPacket::encode)
                .decoder(ClientboundEntityCatchPacket::new)
                .consumerMainThread(ClientboundEntityCatchPacket::handle)
                .add();

        INSTANCE.messageBuilder(ClientboundEntityReleasePacket.class, currentPacketId++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(ClientboundEntityReleasePacket::encode)
                .decoder(ClientboundEntityReleasePacket::new)
                .consumerMainThread(ClientboundEntityReleasePacket::handle)
                .add();

        INSTANCE.messageBuilder(ServerboundConfigureSpawnerPacket.class, currentPacketId++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(ServerboundConfigureSpawnerPacket::write)
                .decoder(ServerboundConfigureSpawnerPacket::new)
                .consumerMainThread((packet, contextSupplier) -> {
                    var context = contextSupplier.get();
                    if (packet.handle(context)) {
                        context.setPacketHandled(true);
                    }
                })
                .add();
    }

    public static void sendToNearbyPlayers(Object packet, Vec3 position, double radius, ResourceKey<Level> dimension) {
        INSTANCE.send(PacketDistributor.NEAR.with(PacketDistributor.TargetPoint.p(position.x, position.y, position.z, radius, dimension)), packet);
    }

    public static void sendToAllPlayers(Object packet) {
        INSTANCE.send(PacketDistributor.ALL.noArg(), packet);
    }

    public static void sendToServer(Object packet) {
        INSTANCE.sendToServer(packet);
    }
}
