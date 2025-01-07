package dev.kvnmtz.createmobspawners.capabilities.entitystorage;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;

public class EntityStorageItemStack implements IEntityStorage {
    private final ItemStack stack;

    public EntityStorageItemStack(ItemStack stack) {
        this.stack = stack;
    }

    @Override
    public StoredEntityData getStoredEntityData() {
        var tag = stack.getOrCreateTag();
        var entity = StoredEntityData.empty();
        if (tag.contains(BlockItem.BLOCK_ENTITY_TAG)) {
            var entityTag = tag.getCompound(BlockItem.BLOCK_ENTITY_TAG).getCompound("EntityStorage");
            entity.deserializeNBT(entityTag);
        }
        return entity;
    }

    @Override
    public void setStoredEntityData(StoredEntityData entity) {
        var tag = stack.getOrCreateTag();
        var entityTag = new CompoundTag();
        entityTag.put("EntityStorage", entity.serializeNBT());
        tag.put(BlockItem.BLOCK_ENTITY_TAG, entityTag);
    }
}
