package dev.kvnmtz.createmobspawners.compat.jei;

import dev.kvnmtz.createmobspawners.CreateMobSpawners;
import dev.kvnmtz.createmobspawners.blocks.registry.ModBlocks;
import dev.kvnmtz.createmobspawners.compat.jei.spawning.SpawningCategory;
import dev.kvnmtz.createmobspawners.recipe.SpawningRecipe;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

@JeiPlugin
public class CreateModSpawnersJEIPlugin implements IModPlugin {
    @Override
    public @NotNull ResourceLocation getPluginUid() {
        return CreateMobSpawners.asResource("jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        var guiHelper = registration.getJeiHelpers().getGuiHelper();
        registration.addRecipeCategories(new SpawningCategory(guiHelper));
    }

    @Override
    public void registerRecipes(@NotNull IRecipeRegistration registration) {
        var level = Minecraft.getInstance().level;
        if (level == null) return;

        var recipeManager = level.getRecipeManager();
        var spawningRecipes = recipeManager.getAllRecipesFor(SpawningRecipe.Type.INSTANCE);
        registration.addRecipes(SpawningCategory.SPAWNING_RECIPE_TYPE, spawningRecipes);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(ModBlocks.SPAWNER.get(), SpawningCategory.SPAWNING_RECIPE_TYPE);
    }
}
