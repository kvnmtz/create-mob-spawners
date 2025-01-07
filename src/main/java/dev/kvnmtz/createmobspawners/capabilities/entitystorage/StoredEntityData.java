package dev.kvnmtz.createmobspawners.capabilities.entitystorage;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.extensions.IForgeEntity;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.Optional;

/**
 * Credit goes to EnderIO for entity data serialization and basic soul vial functionality
 */
public class StoredEntityData implements INBTSerializable<Tag> {
    private CompoundTag entityTag = new CompoundTag();
    private float maxHealth = 0.0f;

    /**
     * Should match key from {@link IForgeEntity#serializeNBT()}.
     */
    public static final String KEY_ID = "id";

    /**
     * Should match key from {@link Entity#saveWithoutId(CompoundTag)}
     */
    public static final String KEY_ENTITY = "Entity";
    private static final String KEY_MAX_HEALTH = "MaxHealth";

    public StoredEntityData() {
    }

    public static StoredEntityData of(LivingEntity entity) {
        var data = new StoredEntityData();
        data.entityTag = entity.serializeNBT();
        data.maxHealth = entity.getMaxHealth();
        return data;
    }

    public static StoredEntityData empty() {
        var data = new StoredEntityData();
        data.maxHealth = 0.0f;
        return data;
    }

    public boolean isEmpty() {
        return maxHealth == 0.0f;
    }

    public Optional<ResourceLocation> getEntityType() {
        var tag = entityTag;
        if (tag.contains(KEY_ID)) {
            return Optional.of(new ResourceLocation(tag.getString(KEY_ID)));
        }

        return Optional.empty();
    }

    public Optional<String> getEntityDisplayName() {
        var optEntityTypeResourceLocation = getEntityType();
        if (optEntityTypeResourceLocation.isPresent()) {
            var optEntityType = EntityType.byString(optEntityTypeResourceLocation.get().toString());
            if (optEntityType.isPresent()) {
                var entityType = optEntityType.get();
                return Optional.of(entityType.getDescription().getString());
            }
        }

        return Optional.empty();
    }

    public CompoundTag getEntityTag() {
        return entityTag;
    }

    @Override
    public Tag serializeNBT() {
        var compound = new CompoundTag();
        compound.put(KEY_ENTITY, entityTag);
        if (maxHealth > 0.0f) {
            compound.putFloat(KEY_MAX_HEALTH, maxHealth);
        }
        return compound;
    }

    @Override
    public void deserializeNBT(Tag tag) {
        if (tag instanceof CompoundTag compoundTag) {
            entityTag = compoundTag.getCompound(KEY_ENTITY);
            if (compoundTag.contains(KEY_MAX_HEALTH)) {
                maxHealth = compoundTag.getFloat(KEY_MAX_HEALTH);
            }
        }
    }
}
