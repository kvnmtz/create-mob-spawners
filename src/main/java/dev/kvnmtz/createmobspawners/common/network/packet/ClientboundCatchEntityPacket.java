package dev.kvnmtz.createmobspawners.common.network.packet;

import dev.kvnmtz.createmobspawners.CreateMobSpawnersMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

public record ClientboundCatchEntityPacket(int entityId, int playerId,
                                           ClientboundCatchEntityPacket.EntityCatchState catchState) implements CustomPacketPayload {

    public static final Type<ClientboundCatchEntityPacket> TYPE
            = new Type<>(CreateMobSpawnersMod.asResource("catch_entity"));

    public static final StreamCodec<ByteBuf, EntityCatchState> CATCH_STATE_CODEC = ByteBufCodecs.idMapper(
            i -> EntityCatchState.values()[i],
            EntityCatchState::ordinal
    );

    public static final StreamCodec<ByteBuf, ClientboundCatchEntityPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            ClientboundCatchEntityPacket::entityId,
            ByteBufCodecs.VAR_INT,
            ClientboundCatchEntityPacket::playerId,
            CATCH_STATE_CODEC,
            ClientboundCatchEntityPacket::catchState,
            ClientboundCatchEntityPacket::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public enum EntityCatchState {
        STARTED,
        IN_PROGRESS,
        FINISHED,
        CANCELED,
    }
}
