package dev.kvnmtz.createmobspawners.common.recipe.registry;

import dev.kvnmtz.createmobspawners.CreateMobSpawnersMod;
import dev.kvnmtz.createmobspawners.common.recipe.SpawningRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public abstract class ModRecipes {

    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
            DeferredRegister.create(Registries.RECIPE_TYPE, CreateMobSpawnersMod.MOD_ID);

    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, CreateMobSpawnersMod.MOD_ID);

    public static final Supplier<RecipeType<SpawningRecipe>> SPAWNING =
            RECIPE_TYPES.register(
                    "spawning",
                    () -> RecipeType.simple(CreateMobSpawnersMod.asResource("spawning"))
            );

    public static final Supplier<RecipeSerializer<SpawningRecipe>> SPAWNING_SERIALIZER =
            RECIPE_SERIALIZERS.register("spawning", SpawningRecipe.Serializer::new);
    
    public static void init(IEventBus eventBus) {
        RECIPE_TYPES.register(eventBus);
        RECIPE_SERIALIZERS.register(eventBus);
    }
}
