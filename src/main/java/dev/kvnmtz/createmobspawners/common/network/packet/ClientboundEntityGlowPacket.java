package dev.kvnmtz.createmobspawners.common.network.packet;

import dev.kvnmtz.createmobspawners.CreateMobSpawnersMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

public record ClientboundEntityGlowPacket(int entityId) implements CustomPacketPayload {

    public static final Type<ClientboundEntityGlowPacket> TYPE
            = new Type<>(CreateMobSpawnersMod.asResource("entity_glow"));

    public static final StreamCodec<ByteBuf, ClientboundEntityGlowPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            ClientboundEntityGlowPacket::entityId,
            ClientboundEntityGlowPacket::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
