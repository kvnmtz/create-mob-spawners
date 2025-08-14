package dev.kvnmtz.createmobspawners.recipe.ingredient;

import com.google.gson.JsonObject;
import dev.kvnmtz.createmobspawners.capabilities.entitystorage.StoredEntityData;
import dev.kvnmtz.createmobspawners.capabilities.registry.ModCapabilities;
import dev.kvnmtz.createmobspawners.item.custom.SoulCatcherItem;
import dev.kvnmtz.createmobspawners.item.registry.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.IIngredientSerializer;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

public class SoulCatcherIngredient extends Ingredient {
    private final ResourceLocation requiredEntityType;

    public SoulCatcherIngredient(ResourceLocation requiredEntityType) {
        super(Stream.of(new ItemValue(createDisplayItem(requiredEntityType))));
        this.requiredEntityType = requiredEntityType;
    }

    private static ItemStack createDisplayItem(ResourceLocation entityType) {
        var stack = ModItems.SOUL_CATCHER.get().getDefaultInstance();

        var dataTag = new CompoundTag();
        {
            var entityTag = new CompoundTag();
            entityTag.putString(StoredEntityData.KEY_ID, entityType.toString());

            dataTag.put(StoredEntityData.KEY_ENTITY, entityTag);
        }

        StoredEntityData entityData = StoredEntityData.empty();
        entityData.deserializeNBT(dataTag);

        stack.getCapability(ModCapabilities.ENTITY_STORAGE)
                .ifPresent(storage -> storage.setStoredEntityData(entityData));
        
        return stack;
    }

    @Override
    public boolean test(ItemStack stack) {
        if (stack == null)
            return false;

        if (stack.getItem() != ModItems.SOUL_CATCHER.get())
            return false;
        
        var entityData = SoulCatcherItem.getEntityData(stack);
        if (entityData.isEmpty())
            return false;
        
        var entityType = entityData.get().getEntityTypeResourceLocation();
        return entityType.isPresent() && entityType.get().equals(requiredEntityType);
    }

    @Override
    @NotNull
    public IIngredientSerializer<? extends Ingredient> getSerializer() {
        return Serializer.INSTANCE;
    }

    public static class Serializer implements IIngredientSerializer<SoulCatcherIngredient> {
        public static final Serializer INSTANCE = new Serializer();

        @Override
        @NotNull
        public SoulCatcherIngredient parse(@NotNull FriendlyByteBuf buffer) {
            return new SoulCatcherIngredient(buffer.readResourceLocation());
        }

        @Override
        @NotNull
        public SoulCatcherIngredient parse(@NotNull JsonObject json) {
            var entityType = new ResourceLocation(json.get("entity_type").getAsString());
            return new SoulCatcherIngredient(entityType);
        }

        @Override
        public void write(@NotNull FriendlyByteBuf buffer, @NotNull SoulCatcherIngredient ingredient) {
            buffer.writeResourceLocation(ingredient.requiredEntityType);
        }
    }
}