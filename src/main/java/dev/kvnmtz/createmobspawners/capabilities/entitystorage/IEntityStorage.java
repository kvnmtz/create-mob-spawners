package dev.kvnmtz.createmobspawners.capabilities.entitystorage;

public interface IEntityStorage {
    default boolean hasStoredEntity() {
        return getStoredEntityData().getEntityType().isPresent();
    }

    StoredEntityData getStoredEntityData();

    void setStoredEntityData(StoredEntityData entity);
}
