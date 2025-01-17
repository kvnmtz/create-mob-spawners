package dev.kvnmtz.createmobspawners.recipe;

import com.google.gson.JsonObject;
import com.simibubi.create.foundation.fluid.FluidIngredient;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.wrapper.RecipeWrapper;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class SpawningRecipe implements Recipe<RecipeWrapper> {
    protected final ResourceLocation id;
    protected final FluidIngredient fluidIngredient;
    protected final int baseSpawningDurationTicks;
    protected final int additionalSpawnTries;

    public SpawningRecipe(ResourceLocation id, FluidIngredient fluidIngredient, int baseSpawningDurationTicks, int additionalSpawnTries) {
        this.id = id;
        this.fluidIngredient = fluidIngredient;
        this.baseSpawningDurationTicks = baseSpawningDurationTicks;
        this.additionalSpawnTries = additionalSpawnTries;
    }

    public FluidIngredient getFluidIngredient() {
        return fluidIngredient;
    }

    public int getBaseSpawningDurationTicks() {
        return baseSpawningDurationTicks;
    }

    public int getAdditionalSpawnTries() {
        return additionalSpawnTries;
    }

    @Override
    public boolean matches(RecipeWrapper recipeWrapper, Level level) {
        return false;
    }

    @Override
    public ItemStack assemble(RecipeWrapper recipeWrapper, RegistryAccess registryAccess) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        return ItemStack.EMPTY;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Serializer.INSTANCE;
    }

    @Override
    public RecipeType<?> getType() {
        return Type.INSTANCE;
    }

    public static class Type implements RecipeType<SpawningRecipe> {
        public static final Type INSTANCE = new Type();
    }

    public static class Serializer implements RecipeSerializer<SpawningRecipe> {
        public static final Serializer INSTANCE = new Serializer();

        @Override
        public SpawningRecipe fromJson(ResourceLocation id, JsonObject jsonObject) {
            var fluidIngredient = FluidIngredient.deserialize(jsonObject.get("input"));
            var baseSpawningDurationTicks = jsonObject.get("base_spawning_duration_ticks").getAsInt();
            var additionalSpawnTries = jsonObject.get("additional_spawn_tries").getAsInt();
            return new SpawningRecipe(id, fluidIngredient, baseSpawningDurationTicks, additionalSpawnTries);
        }

        @Override
        public @Nullable SpawningRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
            var fluidIngredient = FluidIngredient.read(buffer);
            var baseSpawningDurationTicks = buffer.readInt();
            var additionalSpawnTries = buffer.readInt();
            return new SpawningRecipe(id, fluidIngredient, baseSpawningDurationTicks, additionalSpawnTries);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, SpawningRecipe spawningRecipe) {
            spawningRecipe.fluidIngredient.write(buffer);
            buffer.writeInt(spawningRecipe.baseSpawningDurationTicks);
            buffer.writeInt(spawningRecipe.additionalSpawnTries);
        }
    }
}
