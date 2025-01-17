package dev.kvnmtz.createmobspawners.utils;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class ParticleUtils {

    public static void drawParticleLine(ParticleOptions particleType, Level level, Vec3 pos1, Vec3 pos2, double space, Vec3 speed) {
        var direction = pos2.subtract(pos1).normalize();
        var distance = pos1.distanceTo(pos2);

        var lastPosition = pos1;
        for (double i = 0; i < distance; i += space) {
            var position = lastPosition.add(direction.scale(space));
            level.addParticle(particleType, position.x, position.y, position.z, speed.x, speed.y, speed.z);
            lastPosition = position;
        }
    }

    public static void drawParticlesWithRandomSpeed(ParticleOptions particleType, Level level, Vec3 position, int amount, double xOffset, double yOffset, double zOffset, double maxSpeed) {
        var random = level.random;
        for (var i = 0; i < amount; i++) {
            var x = position.x + xOffset * random.nextGaussian();
            var y = position.y + yOffset * random.nextGaussian();
            var z = position.z + zOffset * random.nextGaussian();
            var speedX = maxSpeed * random.nextGaussian();
            var speedY = maxSpeed * random.nextGaussian();
            var speedZ = maxSpeed * random.nextGaussian();
            level.addParticle(particleType, x, y, z, speedX, speedY, speedZ);
        }
    }

    public static void drawParticles(ParticleOptions particleType, Level level, Vec3 position, int amount, double xOffset, double yOffset, double zOffset, Vec3 speed) {
        var random = level.random;
        for (var i = 0; i < amount; i++) {
            var x = position.x + xOffset * random.nextGaussian();
            var y = position.y + yOffset * random.nextGaussian();
            var z = position.z + zOffset * random.nextGaussian();
            level.addParticle(particleType, x, y, z, speed.x, speed.y, speed.z);
        }
    }

    public static int getParticleCountForEntity(Entity entity) {
        var boundingBox = entity.getBoundingBox();
        var volume = BoundingBoxUtils.getBoundingBoxVolume(boundingBox);
        return (int) (24.58 * Math.pow(volume, 0.35));
    }

    public static int getParticleCountForBoundingBox(AABB boundingBox) {
        var volume = BoundingBoxUtils.getBoundingBoxVolume(boundingBox);
        return (int) (24.58 * Math.pow(volume, 0.35));
    }

    public static void drawPotionEffectLikeParticles(ParticleOptions particleType, Level level, AABB boundingBox, Vec3 position, Vec3 speed, int amount, float scale) {
        var random = level.random;
        for (var i = 0; i < amount; i++) {
            var x = position.x + boundingBox.getXsize() * (((double)2.0F * random.nextDouble() - (double)1.0F) * scale);
            var y = position.y + boundingBox.getYsize() * random.nextDouble();
            var z = position.z + boundingBox.getZsize() * (((double)2.0F * random.nextDouble() - (double)1.0F) * scale);
            level.addParticle(particleType, x, y, z, speed.x, speed.y, speed.z);
        }
    }

    public static void drawPotionEffectParticles(Level level, AABB boundingBox, Vec3 position, int color, int amount) {
        double d0 = (double)(color >> 16 & 255) / (double)255.0F;
        double d1 = (double)(color >> 8 & 255) / (double)255.0F;
        double d2 = (double)(color & 255) / (double)255.0F;
        drawPotionEffectLikeParticles(ParticleTypes.ENTITY_EFFECT, level, boundingBox, position, new Vec3(d0, d1, d2), amount, 0.5f);
    }
}
