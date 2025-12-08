package dev.kvnmtz.createmobspawners.common.network.packet;

import dev.kvnmtz.createmobspawners.CreateMobSpawnersMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

public record ClientboundReleaseEntityPacket(int entityId, int playerId) implements CustomPacketPayload {

    public static final Type<ClientboundReleaseEntityPacket> TYPE
            = new Type<>(CreateMobSpawnersMod.asResource("release_entity"));

    public static final StreamCodec<ByteBuf, ClientboundReleaseEntityPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            ClientboundReleaseEntityPacket::entityId,
            ByteBufCodecs.VAR_INT,
            ClientboundReleaseEntityPacket::playerId,
            ClientboundReleaseEntityPacket::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
