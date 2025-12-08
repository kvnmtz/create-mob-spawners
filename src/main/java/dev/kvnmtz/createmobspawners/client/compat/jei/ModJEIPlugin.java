package dev.kvnmtz.createmobspawners.client.compat.jei;

import dev.kvnmtz.createmobspawners.CreateMobSpawnersMod;
import dev.kvnmtz.createmobspawners.client.compat.jei.category.SpawningCategory;
import dev.kvnmtz.createmobspawners.common.block.registry.ModBlocks;
import dev.kvnmtz.createmobspawners.common.recipe.registry.ModRecipes;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.ParametersAreNonnullByDefault;

@JeiPlugin
@OnlyIn(Dist.CLIENT)
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ModJEIPlugin implements IModPlugin {

    @Override
    public ResourceLocation getPluginUid() {
        return CreateMobSpawnersMod.asResource("jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        var guiHelper = registration.getJeiHelpers().getGuiHelper();
        registration.addRecipeCategories(new SpawningCategory(guiHelper));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        var level = Minecraft.getInstance().level;
        if (level == null) return;

        var recipeManager = level.getRecipeManager();
        var spawningRecipes = recipeManager.getAllRecipesFor(ModRecipes.SPAWNING.get());
        registration.addRecipes(SpawningCategory.RECIPE_TYPE, spawningRecipes);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(VanillaTypes.ITEM_STACK, ModBlocks.MECHANICAL_SPAWNER.asStack(),
                SpawningCategory.RECIPE_TYPE);
    }
}
