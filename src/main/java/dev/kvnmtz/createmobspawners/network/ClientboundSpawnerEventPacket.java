package dev.kvnmtz.createmobspawners.network;

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

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(spawnerPosition);
        buffer.writeInt(spawnedEntityId);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            var level = Minecraft.getInstance().level;
            if (level == null) return;

            var entity = Minecraft.getInstance().level.getEntity(spawnedEntityId);
            if (entity == null) return;

            var entityBoundingBox = entity.getBoundingBox();
            var entityCenter = entityBoundingBox.getCenter();
            var spawnerCenter = spawnerPosition.getCenter();

            ParticleUtils.drawParticleLine(ParticleTypes.WITCH, level, spawnerCenter, entityCenter, 0.5, Vec3.ZERO);
            ParticleUtils.drawParticles(ParticleTypes.WITCH, level, entityCenter, ParticleUtils.getParticleCountForEntity(entity), entityBoundingBox.getXsize() / 3, entityBoundingBox.getYsize() / 3, entityBoundingBox.getZsize() / 3, Vec3.ZERO);
            ParticleUtils.drawParticles(ParticleTypes.ENTITY_EFFECT, level, spawnerCenter, 40, 0.5, 0.5, 0.5, new Vec3(205 / 255.0, 92 / 255.0, 171 / 255.0));
        }));
        ctx.get().setPacketHandled(true);
    }
}