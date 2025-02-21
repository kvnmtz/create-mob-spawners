package dev.kvnmtz.createmobspawners.network.packet;

import dev.kvnmtz.createmobspawners.block.custom.entity.MechanicalSpawnerBlockEntity;
import dev.kvnmtz.createmobspawners.utils.ParticleUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ClientboundSpawnerEventPacket {
    private final BlockPos spawnerPosition;
    private final int spawnedEntityId;

    public ClientboundSpawnerEventPacket(BlockPos spawnerPosition, int spawnedEntityId) {
        this.spawnerPosition = spawnerPosition;
        this.spawnedEntityId = spawnedEntityId;
    }

    public ClientboundSpawnerEventPacket(FriendlyByteBuf buffer) {
        this(buffer.readBlockPos(), buffer.readInt());
    }

    public BlockPos getSpawnerPosition() {
        return spawnerPosition;
    }

    public int getSpawnedEntityId() {
        return spawnedEntityId;
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(spawnerPosition);
        buffer.writeInt(spawnedEntityId);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientHandler.handle(this)));
        ctx.get().setPacketHandled(true);
    }

    private static class ClientHandler {
        public static void handle(ClientboundSpawnerEventPacket packet) {
            var level = Minecraft.getInstance().level;
            if (level == null) return;

            var entity = Minecraft.getInstance().level.getEntity(packet.getSpawnedEntityId());
            if (entity == null) return;

            var entityBoundingBox = entity.getBoundingBox();
            var entityCenter = entityBoundingBox.getCenter();
            var spawnerCenter = packet.getSpawnerPosition().getCenter();

            ParticleUtils.drawParticleLine(ParticleTypes.WITCH, level, spawnerCenter, entityCenter, 0.5, Vec3.ZERO);
            ParticleUtils.drawPotionEffectLikeParticles(ParticleTypes.WITCH, level, entityBoundingBox, entity.position(), new Vec3(0.1, 0.1, 0.1), ParticleUtils.getParticleCountForEntity(entity), 0.75f);
            if (level.getBlockEntity(packet.getSpawnerPosition()) instanceof MechanicalSpawnerBlockEntity be) {
                var optColor = be.getParticleColor();
                if (optColor.isPresent()) {
                    int color = optColor.get();
                    var r = (color >> 16) & 0xFF;
                    var g = (color >> 8) & 0xFF;
                    var b = color & 0xFF;
                    ParticleUtils.drawParticles(ParticleTypes.ENTITY_EFFECT, level, spawnerCenter, 40, 0.5, 0.5, 0.5, new Vec3(r / 255.0, g / 255.0, b / 255.0));
                }
            }
        }
    }
}
