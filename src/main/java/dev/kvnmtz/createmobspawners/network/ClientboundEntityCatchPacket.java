package dev.kvnmtz.createmobspawners.network;

import dev.kvnmtz.createmobspawners.items.SoulCatcherItem;
import dev.kvnmtz.createmobspawners.utils.ParticleUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ClientboundEntityCatchPacket {
    public enum EntityCatchState {
        STARTED,
        IN_PROGRESS,
        FINISHED,
        CANCELED,
    }

    private final int entityId;
    private final int playerId;
    private final EntityCatchState state;

    public ClientboundEntityCatchPacket(int entityId, int playerId, EntityCatchState state) {
        this.entityId = entityId;
        this.playerId = playerId;
        this.state = state;
    }

    public ClientboundEntityCatchPacket(FriendlyByteBuf buffer) {
        this(buffer.readInt(), buffer.readInt(), EntityCatchState.values()[buffer.readByte()]);
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeInt(entityId);
        buffer.writeInt(playerId);
        buffer.writeByte(state.ordinal());
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            var level = Minecraft.getInstance().level;
            if (level == null) return;

            var entity = Minecraft.getInstance().level.getEntity(entityId);
            if (entity == null) return;

            if (state == EntityCatchState.CANCELED) {
                SoulCatcherItem.removeShrinkingEntity(entity);
                return;
            }

            var player = Minecraft.getInstance().level.getEntity(playerId);
            if (player == null) return;

            var entityBoundingBox = entity.getBoundingBox();
            var entityCenter = entityBoundingBox.getCenter();
            var playerCenter = player.getBoundingBox().getCenter();
            var direction = entityCenter.subtract(playerCenter).normalize();
            var pointInFrontOfPlayer = playerCenter.add(direction.multiply(0.66f, 0.66f, 0.66f));

            switch (state) {
                case STARTED:
                    SoulCatcherItem.addShrinkingEntity(entity);
                    ParticleUtils.drawParticleLine(ParticleTypes.WITCH, level, entityBoundingBox.getCenter(), pointInFrontOfPlayer, 0.5, Vec3.ZERO);
                    ParticleUtils.drawParticles(ParticleTypes.WITCH, level, entityCenter, ParticleUtils.getParticleCountForEntity(entity), entityBoundingBox.getXsize() / 3, entityBoundingBox.getYsize() / 3, entityBoundingBox.getZsize() / 3, Vec3.ZERO);
                    break;
                case IN_PROGRESS:
                    ParticleUtils.drawParticleLine(ParticleTypes.WITCH, level, entityBoundingBox.getCenter(), pointInFrontOfPlayer, 0.5, Vec3.ZERO);
                    break;
                case FINISHED:
                    SoulCatcherItem.removeShrinkingEntity(entity);
                    ParticleUtils.drawParticles(ParticleTypes.REVERSE_PORTAL, level, entityCenter, ParticleUtils.getParticleCountForEntity(entity), entityBoundingBox.getXsize() / 3, entityBoundingBox.getYsize() / 3, entityBoundingBox.getZsize() / 3, Vec3.ZERO);
                    break;
            }
        }));
        ctx.get().setPacketHandled(true);
    }
}
