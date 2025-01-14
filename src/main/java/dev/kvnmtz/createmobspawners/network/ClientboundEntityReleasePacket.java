package dev.kvnmtz.createmobspawners.network;

import dev.kvnmtz.createmobspawners.utils.ParticleUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ClientboundEntityReleasePacket {
    private final int entityId;
    private final int playerId;

    public ClientboundEntityReleasePacket(int entityId, int playerId) {
        this.entityId = entityId;
        this.playerId = playerId;
    }

    public ClientboundEntityReleasePacket(FriendlyByteBuf buffer) {
        this(buffer.readInt(), buffer.readInt());
    }

    public int getEntityId() {
        return entityId;
    }

    public int getPlayerId() {
        return playerId;
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeInt(entityId);
        buffer.writeInt(playerId);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientboundEntityReleasePacketHandler.handle(this)));
        ctx.get().setPacketHandled(true);
    }

    private static class ClientboundEntityReleasePacketHandler {
        public static void handle(ClientboundEntityReleasePacket packet) {
            var level = Minecraft.getInstance().level;
            if (level == null) return;

            var entity = Minecraft.getInstance().level.getEntity(packet.getEntityId());
            if (entity == null) return;

            var player = Minecraft.getInstance().level.getEntity(packet.getPlayerId());
            if (player == null) return;

            var entityBoundingBox = entity.getBoundingBox();
            var entityCenter = entityBoundingBox.getCenter();
            var playerCenter = player.getBoundingBox().getCenter();
            var direction = entityCenter.subtract(playerCenter).normalize();
            var pointInFrontOfPlayer = playerCenter.add(direction.multiply(0.66f, 0.66f, 0.66f));

            ParticleUtils.drawParticleLine(ParticleTypes.WITCH, level, entityCenter, pointInFrontOfPlayer, 0.5, Vec3.ZERO);
            ParticleUtils.drawParticles(ParticleTypes.WITCH, level, entityCenter, ParticleUtils.getParticleCountForEntity(entity), entityBoundingBox.getXsize() / 3, entityBoundingBox.getYsize() / 3, entityBoundingBox.getZsize() / 3, Vec3.ZERO);
        }
    }
}
