package dev.kvnmtz.createmobspawners.capabilities.entitystorage;

import dev.kvnmtz.createmobspawners.capabilities.registry.ModCapabilities;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EntityStorageItemStackCapabilityProvider implements ICapabilityProvider {
    private final EntityStorageItemStack backend;
    private final LazyOptional<EntityStorageItemStack> optionalData;

    public EntityStorageItemStackCapabilityProvider(ItemStack stack) {
        this.backend = new EntityStorageItemStack(stack);
        this.optionalData = LazyOptional.of(() -> backend);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction direction) {
        return ModCapabilities.ENTITY_STORAGE.orEmpty(capability, optionalData.cast());
    }
}
