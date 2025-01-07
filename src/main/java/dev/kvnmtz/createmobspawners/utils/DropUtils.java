package dev.kvnmtz.createmobspawners.utils;

import net.minecraft.world.Containers;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.HashSet;
import java.util.Set;

public class DropUtils {
    /**
     * {@link Containers#dropItemStack(Level, double, double, double, ItemStack)}
     * but with the dropped entities as return value
     */
    public static Set<ItemEntity> dropItemStack(Level level, double x, double y, double z, ItemStack itemStack) {
        var $$5 = EntityType.ITEM.getWidth();
        var $$6 = (double) 1.0F - $$5;
        var $$7 = $$5 / (double) 2.0F;
        var $$8 = Math.floor(x) + level.random.nextDouble() * $$6 + $$7;
        var $$9 = Math.floor(y) + level.random.nextDouble() * $$6;
        var $$10 = Math.floor(z) + level.random.nextDouble() * $$6 + $$7;

        var droppedItemEntities = new HashSet<ItemEntity>();

        while (!itemStack.isEmpty()) {
            var $$11 = new ItemEntity(level, $$8, $$9, $$10, itemStack.split(level.random.nextInt(21) + 10));
            $$11.setDeltaMovement(level.random.triangle(0.0F, 0.11485000171139836), level.random.triangle(0.2, 0.11485000171139836), level.random.triangle(0.0F, 0.11485000171139836));
            level.addFreshEntity($$11);
            droppedItemEntities.add($$11);
        }

        return droppedItemEntities;
    }
}
