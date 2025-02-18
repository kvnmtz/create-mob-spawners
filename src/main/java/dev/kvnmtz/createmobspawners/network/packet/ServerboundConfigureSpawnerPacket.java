package dev.kvnmtz.createmobspawners.network.packet;

import com.simibubi.create.foundation.networking.BlockEntityConfigurationPacket;
import dev.kvnmtz.createmobspawners.CreateMobSpawners;
import dev.kvnmtz.createmobspawners.block.custom.entity.MechanicalSpawnerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

public class ServerboundConfigureSpawnerPacket extends BlockEntityConfigurationPacket<MechanicalSpawnerBlockEntity> {
    private int width;
    private int height;
    private int yOffset;

    public ServerboundConfigureSpawnerPacket(BlockPos pos, int width, int height, int yOffset) {
        super(pos);
        this.width = width;
        this.height = height;
        this.yOffset = yOffset;
    }

    public ServerboundConfigureSpawnerPacket(FriendlyByteBuf buffer) {
        super(buffer);
    }

    @Override
    protected void readSettings(FriendlyByteBuf buffer) {
        width = buffer.readByte();
        height = buffer.readByte();
        yOffset = buffer.readByte();
    }

    @Override
    protected void writeSettings(FriendlyByteBuf buffer) {
        buffer.writeByte(width);
        buffer.writeByte(height);
        buffer.writeByte(yOffset);
    }

    @Override
    protected void applySettings(MechanicalSpawnerBlockEntity be) {
        if (!CreateMobSpawners.SERVER_CONFIG.mechanicalSpawnerConfigurationAllowed.get()) return;

        if (width % 2 == 0 || height % 2 == 0) {
            return;
        }

        if (width < CreateMobSpawners.SERVER_CONFIG.mechanicalSpawnerAreaMinWidth.get() || width > CreateMobSpawners.SERVER_CONFIG.mechanicalSpawnerAreaMaxWidth.get()) {
            return;
        }
        if (height < CreateMobSpawners.SERVER_CONFIG.mechanicalSpawnerAreaMinHeight.get() || height > CreateMobSpawners.SERVER_CONFIG.mechanicalSpawnerAreaMaxHeight.get()) {
            return;
        }
        if (yOffset < CreateMobSpawners.SERVER_CONFIG.mechanicalSpawnerAreaMinHeightOffset.get() || yOffset > CreateMobSpawners.SERVER_CONFIG.mechanicalSpawnerAreaMaxHeightOffset.get()) {
            return;
        }

        be.setSpawningAreaWidth(width);
        be.setSpawningAreaHeight(height);
        be.setSpawningAreaHeightOffset(yOffset);
    }
}
