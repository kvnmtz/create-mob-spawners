package dev.kvnmtz.createmobspawners.datagen.provider.server;

import com.simibubi.create.content.fluids.potion.PotionFluid;
import dev.kvnmtz.createmobspawners.common.item.registry.ModItems;
import dev.kvnmtz.createmobspawners.datagen.builder.SpawningRecipeBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends RecipeProvider {

    public ModRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(@NotNull RecipeOutput recipeOutput) {
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.EMPTY_SOUL_CATCHER.get())
                .pattern("ABA")
                .pattern("CDE")
                .pattern("ABA")
                .define('A', getCreateItem("brass_sheet"))
                .define('B', Items.SHULKER_SHELL)
                .define('C', getCreateItem("brass_casing"))
                .define('D', Items.NETHER_STAR)
                .define('E', getCreateItem("cogwheel"))
                .unlockedBy("has_nether_star", has(Items.NETHER_STAR))
                .unlockedBy("has_shulker_shell", has(Items.SHULKER_SHELL))
                .save(recipeOutput);

        buildSpawningRecipes(recipeOutput);
    }

    private static void buildSpawningRecipes(@NotNull RecipeOutput recipeOutput) {
        SpawningRecipeBuilder.spawning(
                Potions.REGENERATION,
                PotionFluid.BottleType.REGULAR,
                200,
                100
        ).saveByPotionIdentifier(recipeOutput);

        SpawningRecipeBuilder.spawning(
                Potions.REGENERATION,
                PotionFluid.BottleType.SPLASH,
                200,
                100
        ).saveByPotionIdentifier(recipeOutput);

        SpawningRecipeBuilder.spawning(
                Potions.REGENERATION,
                PotionFluid.BottleType.LINGERING,
                100,
                80,
                1
        ).saveByPotionIdentifier(recipeOutput);

        SpawningRecipeBuilder.spawning(
                Potions.LONG_REGENERATION,
                PotionFluid.BottleType.REGULAR,
                50,
                80
        ).saveByPotionIdentifier(recipeOutput);

        SpawningRecipeBuilder.spawning(
                Potions.LONG_REGENERATION,
                PotionFluid.BottleType.SPLASH,
                50,
                80
        ).saveByPotionIdentifier(recipeOutput);

        SpawningRecipeBuilder.spawning(
                Potions.LONG_REGENERATION,
                PotionFluid.BottleType.LINGERING,
                20,
                70,
                1
        ).saveByPotionIdentifier(recipeOutput);

        SpawningRecipeBuilder.spawning(
                Potions.STRONG_REGENERATION,
                PotionFluid.BottleType.REGULAR,
                200,
                70,
                2
        ).saveByPotionIdentifier(recipeOutput);

        SpawningRecipeBuilder.spawning(
                Potions.STRONG_REGENERATION,
                PotionFluid.BottleType.SPLASH,
                200,
                70,
                2
        ).saveByPotionIdentifier(recipeOutput);

        SpawningRecipeBuilder.spawning(
                Potions.STRONG_REGENERATION,
                PotionFluid.BottleType.LINGERING,
                100,
                60,
                3
        ).saveByPotionIdentifier(recipeOutput);
    }

    private ItemLike getCreateItem(String name) {
        return BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("create", name));
    }
}