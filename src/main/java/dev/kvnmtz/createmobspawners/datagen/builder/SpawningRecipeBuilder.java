package dev.kvnmtz.createmobspawners.datagen.builder;

import com.simibubi.create.AllDataComponents;
import com.simibubi.create.content.fluids.potion.PotionFluid;
import dev.kvnmtz.createmobspawners.CreateMobSpawnersMod;
import dev.kvnmtz.createmobspawners.common.recipe.SpawningRecipe;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.advancements.Criterion;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.neoforged.neoforge.fluids.crafting.DataComponentFluidIngredient;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class SpawningRecipeBuilder implements RecipeBuilder {

    private final SizedFluidIngredient fluid;
    private final int spawnTicksAtMaxSpeed;
    private final int additionalSpawnAttempts;
    private Optional<List<ResourceLocation>> blacklist = Optional.empty();
    private Optional<List<ResourceLocation>> whitelist = Optional.empty();
    private Optional<Integer> particleColor = Optional.empty();

    private SpawningRecipeBuilder(SizedFluidIngredient fluid, int spawnTicksAtMaxSpeed, int additionalSpawnAttempts) {
        this.fluid = fluid;
        this.spawnTicksAtMaxSpeed = spawnTicksAtMaxSpeed;
        this.additionalSpawnAttempts = additionalSpawnAttempts;
    }

    public static SpawningRecipeBuilder spawning(SizedFluidIngredient fluid, int spawnTicksAtMaxSpeed,
                                                 int additionalSpawnAttempts) {
        return new SpawningRecipeBuilder(fluid, spawnTicksAtMaxSpeed, additionalSpawnAttempts);
    }

    public static SpawningRecipeBuilder spawning(SizedFluidIngredient fluid, int spawnTicksAtMaxSpeed) {
        return spawning(fluid, spawnTicksAtMaxSpeed, 0);
    }

    public static SpawningRecipeBuilder spawning(Holder<Potion> potion, PotionFluid.BottleType bottleType,
                                                 int fluidAmount, int spawnTicksAtMaxSpeed,
                                                 int additionalSpawnAttempts) {

        return spawning(
                new SizedFluidIngredient(DataComponentFluidIngredient.of(true, PotionFluid.of(fluidAmount,
                        new PotionContents(potion), bottleType)), fluidAmount),
                spawnTicksAtMaxSpeed,
                additionalSpawnAttempts
        );
    }

    public static SpawningRecipeBuilder spawning(Holder<Potion> potion, PotionFluid.BottleType bottleType,
                                                 int fluidAmount, int spawnTicksAtMaxSpeed) {
        return spawning(potion, bottleType, fluidAmount, spawnTicksAtMaxSpeed, 0);
    }

    public SpawningRecipeBuilder blacklist(ResourceLocation entityId) {
        if (whitelist.isPresent() && !whitelist.get().isEmpty()) {
            throw new IllegalStateException("Cannot add blacklist entries when whitelist is already configured");
        }

        if (blacklist.isPresent()) {
            blacklist.get().add(entityId);
        } else {
            blacklist = Optional.of(List.of(entityId));
        }

        return this;
    }

    public SpawningRecipeBuilder blacklist(String namespace, String path) {
        return blacklist(ResourceLocation.fromNamespaceAndPath(namespace, path));
    }

    public SpawningRecipeBuilder whitelist(ResourceLocation entityId) {
        if (whitelist.isPresent() && !whitelist.get().isEmpty()) {
            throw new IllegalStateException("Cannot add whitelist entries when blacklist is already configured");
        }

        if (whitelist.isPresent()) {
            whitelist.get().add(entityId);
        } else {
            whitelist = Optional.of(List.of(entityId));
        }
        return this;
    }

    public SpawningRecipeBuilder whitelist(String namespace, String path) {
        return whitelist(ResourceLocation.fromNamespaceAndPath(namespace, path));
    }

    public SpawningRecipeBuilder particleColor(int color) {
        this.particleColor = Optional.of(color);
        return this;
    }

    public SpawningRecipeBuilder particleColor(String hexColor) {
        var clean = hexColor.startsWith("#") ? hexColor.substring(1) : hexColor;
        try {
            var color = Integer.parseInt(clean, 16);
            return particleColor(color);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid hex color format: " + hexColor, e);
        }
    }

    @Override
    public RecipeBuilder unlockedBy(String name, Criterion<?> criterion) {
        return this;
    }

    @Override
    public RecipeBuilder group(@Nullable String group) {
        return this;
    }

    @Override
    public Item getResult() {
        return Items.AIR;
    }

    @Override
    public void save(RecipeOutput output, ResourceLocation id) {
        var recipe = new SpawningRecipe(
                fluid,
                spawnTicksAtMaxSpeed,
                additionalSpawnAttempts,
                blacklist,
                whitelist,
                particleColor
        );

        output.accept(id, recipe, null);
    }

    public void saveByPotionIdentifier(RecipeOutput output) {
        var fluidStack = fluid.getFluids()[0];
        if (!(fluidStack.getFluid() instanceof PotionFluid)) {
            throw new IllegalArgumentException("saveByPotionIdentifier requires a potion fluid");
        }

        var recipe = new SpawningRecipe(
                fluid,
                spawnTicksAtMaxSpeed,
                additionalSpawnAttempts,
                blacklist,
                whitelist,
                particleColor
        );

        var contents = fluidStack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
        var potionName = Objects.requireNonNull(contents.potion().get().getKey()).location().getPath().toLowerCase();

        var bottleType = fluidStack.getOrDefault(AllDataComponents.POTION_FLUID_BOTTLE_TYPE,
                PotionFluid.BottleType.REGULAR);
        var bottleTypeName = bottleType.name().toLowerCase();

        var id = CreateMobSpawnersMod.asResource(String.format("spawning/%s_%s", potionName, bottleTypeName));

        output.accept(id, recipe, null);
    }

    public void save(RecipeOutput output, String path) {
        save(output, CreateMobSpawnersMod.asResource(path));
    }
}
