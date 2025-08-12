package dev.kvnmtz.createmobspawners.recipe.custom;

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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class SpawningRecipe implements Recipe<RecipeWrapper> {
    protected final ResourceLocation id;
    protected final FluidIngredient fluidIngredient;
    protected final int spawnTicksAtMaxSpeed;
    protected final int additionalSpawnAttempts;
    private final List<ResourceLocation> blacklist;
    private final List<ResourceLocation> whitelist;
    private final @Nullable Integer particleColor;

    public SpawningRecipe(ResourceLocation id, FluidIngredient fluidIngredient, int spawnTicksAtMaxSpeed, int additionalSpawnAttempts, List<ResourceLocation> blacklist, List<ResourceLocation> whitelist, @Nullable Integer particleColor) {
        if (!blacklist.isEmpty() && !whitelist.isEmpty()) {
            throw new IllegalArgumentException("Spawning recipe cannot have both blacklist and whitelist");
        }

        this.id = id;
        this.fluidIngredient = fluidIngredient;
        this.spawnTicksAtMaxSpeed = spawnTicksAtMaxSpeed;
        this.additionalSpawnAttempts = additionalSpawnAttempts;
        this.blacklist = blacklist;
        this.whitelist = whitelist;
        this.particleColor = particleColor;
    }

    public FluidIngredient getFluidIngredient() {
        return fluidIngredient;
    }

    public int getSpawnTicksAtMaxSpeed() {
        return spawnTicksAtMaxSpeed;
    }

    public int getAdditionalSpawnAttempts() {
        return additionalSpawnAttempts;
    }

    public List<ResourceLocation> getBlacklist() {
        return blacklist;
    }

    public List<ResourceLocation> getWhitelist() {
        return whitelist;
    }

    public @Nullable Integer getParticleColor() {
        return particleColor;
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
            var spawnTicksAtMaxSpeed = jsonObject.get("spawn_ticks_at_max_speed").getAsInt();
            var additionalSpawnAttempts = jsonObject.get("additional_spawn_attempts").getAsInt();

            var blacklist = new ArrayList<ResourceLocation>();
            var whitelist = new ArrayList<ResourceLocation>();

            var blacklistObj = jsonObject.get("spawnable_entity_blacklist");
            if (blacklistObj != null) {
                blacklist.addAll(blacklistObj.getAsJsonArray().asList().stream().map(e -> new ResourceLocation(e.getAsString())).toList());
            }

            var whitelistObj = jsonObject.get("spawnable_entity_whitelist");
            if (whitelistObj != null) {
                whitelist.addAll(whitelistObj.getAsJsonArray().asList().stream().map(e -> new ResourceLocation(e.getAsString())).toList());
            }

            Integer optParticleColor = null;
            var particleColorObj = jsonObject.get("particle_color");
            if (particleColorObj != null) {
                var particleColorHex = particleColorObj.getAsString();
                if (particleColorHex.startsWith("#")) {
                    particleColorHex = particleColorHex.substring(1);
                }
                optParticleColor = Integer.parseInt(particleColorHex, 16);
            }

            return new SpawningRecipe(id, fluidIngredient, spawnTicksAtMaxSpeed, additionalSpawnAttempts, blacklist, whitelist, optParticleColor);
        }

        @Override
        public @Nullable SpawningRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
            var fluidIngredient = FluidIngredient.read(buffer);
            var spawnTicksAtMaxSpeed = buffer.readInt();
            var additionalSpawnTries = buffer.readInt();

            var blacklist = new ArrayList<ResourceLocation>();
            var whitelist = new ArrayList<ResourceLocation>();

            var blacklistLength = buffer.readInt();
            for (int i = 0; i < blacklistLength; i++) {
                blacklist.add(buffer.readResourceLocation());
            }

            var whitelistLength = buffer.readInt();
            for (int i = 0; i < whitelistLength; i++) {
                whitelist.add(buffer.readResourceLocation());
            }

            var particleColor = buffer.readOptional(FriendlyByteBuf::readInt);

            return new SpawningRecipe(id, fluidIngredient, spawnTicksAtMaxSpeed, additionalSpawnTries, blacklist, whitelist, particleColor.orElse(null));
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, SpawningRecipe spawningRecipe) {
            spawningRecipe.fluidIngredient.write(buffer);
            buffer.writeInt(spawningRecipe.spawnTicksAtMaxSpeed);
            buffer.writeInt(spawningRecipe.additionalSpawnAttempts);

            buffer.writeInt(spawningRecipe.blacklist.size());
            for (var entityId : spawningRecipe.blacklist) {
                buffer.writeResourceLocation(entityId);
            }

            buffer.writeInt(spawningRecipe.whitelist.size());
            for (var entityId : spawningRecipe.whitelist) {
                buffer.writeResourceLocation(entityId);
            }

            buffer.writeOptional(Optional.ofNullable(spawningRecipe.particleColor), FriendlyByteBuf::writeInt);
        }
    }
}
