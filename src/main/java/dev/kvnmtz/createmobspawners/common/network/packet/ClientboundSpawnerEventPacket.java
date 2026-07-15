package dev.kvnmtz.createmobspawners.common.network.packet;

import dev.kvnmtz.createmobspawners.CreateMobSpawnersMod;
import dev.kvnmtz.createmobspawners.common.network.codec.ModStreamCodecs;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public record ClientboundSpawnerEventPacket(BlockPos spawnerPosition,
                                            int spawnedEntityId,
                                            Vec3 spawnedEntityCenter) implements CustomPacketPayload {

    public static final Type<ClientboundSpawnerEventPacket> TYPE =
            new Type<>(CreateMobSpawnersMod.asResource("spawner_event"));

    public static final StreamCodec<ByteBuf, ClientboundSpawnerEventPacket> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            ClientboundSpawnerEventPacket::spawnerPosition,
            ByteBufCodecs.VAR_INT,
            ClientboundSpawnerEventPacket::spawnedEntityId,
            ModStreamCodecs.VEC3,
            ClientboundSpawnerEventPacket::spawnedEntityCenter,
            ClientboundSpawnerEventPacket::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
