package dev.kvnmtz.createmobspawners.capabilities.entitystorage;

public interface IEntityStorage {
    default boolean hasStoredEntity() {
        return getStoredEntityData().getEntityTypeResourceLocation().isPresent();
    }

    StoredEntityData getStoredEntityData();

    void setStoredEntityData(StoredEntityData entity);
}
