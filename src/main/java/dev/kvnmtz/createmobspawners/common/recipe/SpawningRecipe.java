package dev.kvnmtz.createmobspawners.common.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.kvnmtz.createmobspawners.common.recipe.registry.ModRecipes;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import net.neoforged.neoforge.items.wrapper.RecipeWrapper;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Optional;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public record SpawningRecipe(SizedFluidIngredient fluid, int spawnTicksAtMaxSpeed, int additionalSpawnAttempts,
                             Optional<List<ResourceLocation>> blacklist, Optional<List<ResourceLocation>> whitelist,
                             Optional<Integer> particleColor) implements Recipe<RecipeWrapper> {

    public SpawningRecipe {
        if (blacklist.isPresent() && !blacklist.get().isEmpty() && whitelist.isPresent() && !whitelist.get().isEmpty()) {
            throw new IllegalArgumentException("Spawning recipe cannot have both blacklist and whitelist");
        }
    }

    @Override
    public boolean matches(RecipeWrapper input, Level level) {
        return false;
    }

    @Override
    public ItemStack assemble(RecipeWrapper input, HolderLookup.Provider registries) {
        return ItemStack.EMPTY.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 1;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return ItemStack.EMPTY.copy();
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.SPAWNING_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.SPAWNING.get();
    }

    public static class Serializer implements RecipeSerializer<SpawningRecipe> {

        public static final Codec<Integer> HEX_COLOR_CODEC = Codec.STRING.comapFlatMap(
                inputString -> {
                    try {
                        var clean = inputString.startsWith("#") ? inputString.substring(1) : inputString;
                        return DataResult.success(Integer.parseInt(clean, 16));
                    } catch (NumberFormatException e) {
                        return DataResult.error(() -> "Invalid hex color format: " + inputString);
                    }
                },
                intValue -> String.format("#%06X", intValue)
        );

        public static final MapCodec<SpawningRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                SizedFluidIngredient.NESTED_CODEC.fieldOf("input").forGetter(r -> r.fluid),
                Codec.INT.fieldOf("spawn_ticks_at_max_speed").forGetter(r -> r.spawnTicksAtMaxSpeed),
                Codec.INT.fieldOf("additional_spawn_attempts").forGetter(r -> r.additionalSpawnAttempts),
                ResourceLocation.CODEC.listOf().optionalFieldOf("blacklist").forGetter(r -> r.blacklist),
                ResourceLocation.CODEC.listOf().optionalFieldOf("whitelist").forGetter(r -> r.whitelist),
                HEX_COLOR_CODEC.optionalFieldOf("particle_color").forGetter(r -> r.particleColor)
        ).apply(inst, SpawningRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, SpawningRecipe> STREAM_CODEC =
                StreamCodec.composite(
                        SizedFluidIngredient.STREAM_CODEC,
                        SpawningRecipe::fluid,
                        ByteBufCodecs.VAR_INT,
                        SpawningRecipe::spawnTicksAtMaxSpeed,
                        ByteBufCodecs.VAR_INT,
                        SpawningRecipe::additionalSpawnAttempts,
                        ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC.apply(ByteBufCodecs.list())),
                        SpawningRecipe::blacklist,
                        ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC.apply(ByteBufCodecs.list())),
                        SpawningRecipe::whitelist,
                        ByteBufCodecs.optional(ByteBufCodecs.VAR_INT),
                        SpawningRecipe::particleColor,
                        SpawningRecipe::new
                );

        @Override
        public MapCodec<SpawningRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, SpawningRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
