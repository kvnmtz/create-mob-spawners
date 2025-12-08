package dev.kvnmtz.createmobspawners.client.network;

import dev.kvnmtz.createmobspawners.client.item.SoulCatcherItemClient;
import dev.kvnmtz.createmobspawners.client.mixin.EntityInvoker;
import dev.kvnmtz.createmobspawners.common.block.entity.MechanicalSpawnerBlockEntity;
import dev.kvnmtz.createmobspawners.common.network.packet.ClientboundCatchEntityPacket;
import dev.kvnmtz.createmobspawners.common.network.packet.ClientboundEntityGlowPacket;
import dev.kvnmtz.createmobspawners.common.network.packet.ClientboundReleaseEntityPacket;
import dev.kvnmtz.createmobspawners.common.network.packet.ClientboundSpawnerEventPacket;
import dev.kvnmtz.createmobspawners.common.util.ParticleUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.IPayloadContext;

@OnlyIn(Dist.CLIENT)
public abstract class ClientPayloadHandler {

    public static void handle(ClientboundCatchEntityPacket payload, IPayloadContext context) {
        var level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }

        var entity = level.getEntity(payload.entityId());
        if (entity == null) {
            return;
        }

        if (payload.catchState() == ClientboundCatchEntityPacket.EntityCatchState.CANCELED) {
            SoulCatcherItemClient.removeShrinkingEntity(entity);
            return;
        }

        var player = level.getEntity(payload.playerId());
        if (player == null) {
            return;
        }

        var entityBoundingBox = entity.getBoundingBox();
        var entityCenter = entityBoundingBox.getCenter();
        var playerCenter = player.getBoundingBox().getCenter();
        var direction = entityCenter.subtract(playerCenter).normalize();
        var pointInFrontOfPlayer = playerCenter.add(direction.multiply(0.66f, 0.66f, 0.66f));

        switch (payload.catchState()) {
            case STARTED:
                SoulCatcherItemClient.addShrinkingEntity(entity);
                ParticleUtils.drawParticleLine(ParticleTypes.WITCH, level, entityBoundingBox.getCenter(),
                        pointInFrontOfPlayer, 0.5, Vec3.ZERO);
                ParticleUtils.drawPotionEffectLikeParticles(ParticleTypes.WITCH, level, entityBoundingBox,
                        new Vec3(0.1, 0.1, 0.1), ParticleUtils.getParticleCountForEntity(entity));
                break;
            case IN_PROGRESS:
                ParticleUtils.drawParticleLine(ParticleTypes.WITCH, level, entityBoundingBox.getCenter(),
                        pointInFrontOfPlayer, 0.5, Vec3.ZERO);
                break;
            case FINISHED:
                ParticleUtils.drawParticles(ParticleTypes.REVERSE_PORTAL, level, entityCenter,
                        ParticleUtils.getParticleCountForEntity(entity), entityBoundingBox.getXsize() / 3,
                        entityBoundingBox.getYsize() / 3, entityBoundingBox.getZsize() / 3, Vec3.ZERO);
                break;
        }
    }

    public static void handle(ClientboundReleaseEntityPacket payload, IPayloadContext context) {
        var level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }

        var entity = level.getEntity(payload.entityId());
        if (entity == null) {
            return;
        }

        var player = level.getEntity(payload.playerId());
        if (player == null) {
            return;
        }

        var entityBoundingBox = entity.getBoundingBox();
        var entityCenter = entityBoundingBox.getCenter();
        var playerCenter = player.getBoundingBox().getCenter();
        var direction = entityCenter.subtract(playerCenter).normalize();
        var pointInFrontOfPlayer = playerCenter.add(direction.multiply(0.66f, 0.66f, 0.66f));

        ParticleUtils.drawParticleLine(ParticleTypes.WITCH, level, entityCenter, pointInFrontOfPlayer, 0.5,
                Vec3.ZERO);
        ParticleUtils.drawPotionEffectLikeParticles(ParticleTypes.WITCH, level, entityBoundingBox, new Vec3(0.1,
                0.1, 0.1), ParticleUtils.getParticleCountForEntity(entity));
    }

    public static void handle(ClientboundSpawnerEventPacket payload, IPayloadContext context) {
        var level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }

        var entity = Minecraft.getInstance().level.getEntity(payload.spawnedEntityId());
        if (entity == null) {
            return;
        }

        var entityBoundingBox = entity.getBoundingBox();
        var entityCenter = entityBoundingBox.getCenter();
        var spawnerCenter = payload.spawnerPosition().getCenter();

        ParticleUtils.drawParticleLine(ParticleTypes.WITCH, level, spawnerCenter, entityCenter, 0.5, Vec3.ZERO);
        ParticleUtils.drawPotionEffectLikeParticles(ParticleTypes.WITCH, level, entityBoundingBox, new Vec3(0.1, 0.1, 0.1), ParticleUtils.getParticleCountForEntity(entity));

        if (level.getBlockEntity(payload.spawnerPosition()) instanceof MechanicalSpawnerBlockEntity be) {
            var optColor = be.getParticleColor();
            if (optColor.isPresent()) {
                var color = optColor.get();
                var spawnerBoundingBox = new AABB(payload.spawnerPosition());
                ParticleUtils.drawPotionEffectParticles(level, spawnerBoundingBox, color, ParticleUtils.getParticleCountForBoundingBox(spawnerBoundingBox));
            }
        }
    }

    public static void handle(ClientboundEntityGlowPacket payload, IPayloadContext context) {
        var level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }

        var entity = Minecraft.getInstance().level.getEntity(payload.entityId());
        if (entity == null) {
            return;
        }

        // make entity glow clientsided
        ((EntityInvoker) entity).invokeSetSharedFlag(6, true);
    }
}
