package dev.kvnmtz.createmobspawners.utils;

import net.minecraft.world.phys.AABB;

public class BoundingBoxUtils {
    public static double getBoundingBoxVolume(AABB boundingBox) {
        return Math.abs(boundingBox.getXsize() * boundingBox.getYsize() * boundingBox.getZsize());
    }
}
