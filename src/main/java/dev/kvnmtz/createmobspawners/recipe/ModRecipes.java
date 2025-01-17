package dev.kvnmtz.createmobspawners.recipe;

import dev.kvnmtz.createmobspawners.CreateMobSpawners;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, CreateMobSpawners.MOD_ID);
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, CreateMobSpawners.MOD_ID);

    public static final RegistryObject<RecipeSerializer<SpawningRecipe>> SPAWNING_SERIALIZER = SERIALIZERS.register("spawning", () -> SpawningRecipe.Serializer.INSTANCE);
    public static final RegistryObject<RecipeType<SpawningRecipe>> SPAWNING = RECIPE_TYPES.register("spawning", () -> SpawningRecipe.Type.INSTANCE);

    public static void register(IEventBus eventBus) {
        SERIALIZERS.register(eventBus);
        RECIPE_TYPES.register(eventBus);
    }
}
