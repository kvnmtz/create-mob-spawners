package dev.kvnmtz.createmobspawners.recipe.registry;

import dev.kvnmtz.createmobspawners.CreateMobSpawners;
import dev.kvnmtz.createmobspawners.recipe.custom.SpawningRecipe;
import dev.kvnmtz.createmobspawners.recipe.ingredient.SoulCatcherIngredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(modid = CreateMobSpawners.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, CreateMobSpawners.MOD_ID);
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, CreateMobSpawners.MOD_ID);

    public static final RegistryObject<RecipeSerializer<SpawningRecipe>> SPAWNING_SERIALIZER = SERIALIZERS.register("spawning", () -> SpawningRecipe.Serializer.INSTANCE);
    public static final RegistryObject<RecipeType<SpawningRecipe>> SPAWNING = RECIPE_TYPES.register("spawning", () -> SpawningRecipe.Type.INSTANCE);

    public static void register(IEventBus eventBus) {
        SERIALIZERS.register(eventBus);
        RECIPE_TYPES.register(eventBus);
    }

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            CraftingHelper.register(CreateMobSpawners.asResource("soul_catcher"), SoulCatcherIngredient.Serializer.INSTANCE);
        });
    }
}
