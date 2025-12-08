package dev.kvnmtz.createmobspawners.common.util;

import net.minecraft.world.Containers;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.HashSet;
import java.util.Set;

public abstract class DropUtils {

    /**
     * Like {@link Containers#dropItemStack(Level, double, double, double, ItemStack)},
     * but with the dropped entities as return value
     */
    public static Set<ItemEntity> dropItemStack(Level level, double x, double y, double z, ItemStack stack) {
        double d = EntityType.ITEM.getWidth();
        var e = 1.0 - d;
        var f = d / 2.0;
        var g = Math.floor(x) + level.random.nextDouble() * e + f;
        var h = Math.floor(y) + level.random.nextDouble() * e;
        var i = Math.floor(z) + level.random.nextDouble() * e + f;

        var droppedItemEntities = new HashSet<ItemEntity>();

        while (!stack.isEmpty()) {
            var itemEntity = new ItemEntity(level, g, h, i, stack.split(level.random.nextInt(21) + 10));
            itemEntity.setDeltaMovement(
                    level.random.triangle(0.0F, 0.11485000171139836),
                    level.random.triangle(0.2, 0.11485000171139836),
                    level.random.triangle(0.0F, 0.11485000171139836)
            );
            level.addFreshEntity(itemEntity);
            droppedItemEntities.add(itemEntity);
        }

        return droppedItemEntities;
    }
}
