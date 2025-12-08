package dev.kvnmtz.createmobspawners.common.network.packet;

import com.simibubi.create.foundation.networking.BlockEntityConfigurationPacket;
import dev.kvnmtz.createmobspawners.CreateMobSpawnersMod;
import dev.kvnmtz.createmobspawners.common.block.entity.MechanicalSpawnerBlockEntity;
import dev.kvnmtz.createmobspawners.common.config.ModServerConfig;
import dev.kvnmtz.createmobspawners.common.network.registry.CreateAddonPackets;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;

public class ServerboundConfigureSpawnerPacket extends BlockEntityConfigurationPacket<MechanicalSpawnerBlockEntity> {

    private final int width;
    private final int height;
    private final int yOffset;

    public ServerboundConfigureSpawnerPacket(BlockPos pos, int width, int height, int yOffset) {
        super(pos);
        this.width = width;
        this.height = height;
        this.yOffset = yOffset;
    }

    public static final Type<ServerboundConfigureSpawnerPacket> TYPE
            = new Type<>(CreateMobSpawnersMod.asResource("configure_spawner"));

    public static final StreamCodec<FriendlyByteBuf, ServerboundConfigureSpawnerPacket> STREAM_CODEC =
            StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            packet -> packet.pos,
            ByteBufCodecs.VAR_INT,
            ServerboundConfigureSpawnerPacket::width,
            ByteBufCodecs.VAR_INT,
            ServerboundConfigureSpawnerPacket::height,
            ByteBufCodecs.VAR_INT,
            ServerboundConfigureSpawnerPacket::yOffset,
            ServerboundConfigureSpawnerPacket::new
    );

    @Override
    protected void applySettings(ServerPlayer serverPlayer, MechanicalSpawnerBlockEntity be) {
        if (!ModServerConfig.CONFIG.mechanicalSpawnerConfigurationAllowed.get()) {
            return;
        }

        if (width % 2 == 0 || height % 2 == 0) {
            return;
        }

        if (width < ModServerConfig.CONFIG.mechanicalSpawnerAreaMinWidth.get() || width > ModServerConfig.CONFIG.mechanicalSpawnerAreaMaxWidth.get()) {
            return;
        }
        if (height < ModServerConfig.CONFIG.mechanicalSpawnerAreaMinHeight.get() || height > ModServerConfig.CONFIG.mechanicalSpawnerAreaMaxHeight.get()) {
            return;
        }
        if (yOffset < ModServerConfig.CONFIG.mechanicalSpawnerAreaMinHeightOffset.get() || yOffset > ModServerConfig.CONFIG.mechanicalSpawnerAreaMaxHeightOffset.get()) {
            return;
        }

        be.setSpawningAreaWidth(width);
        be.setSpawningAreaHeight(height);
        be.setSpawningAreaHeightOffset(yOffset);
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return CreateAddonPackets.CONFIGURE_SPAWNER;
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public int yOffset() {
        return yOffset;
    }
}
