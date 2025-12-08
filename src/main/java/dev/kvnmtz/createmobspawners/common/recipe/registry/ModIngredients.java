package dev.kvnmtz.createmobspawners.common.recipe.registry;

import dev.kvnmtz.createmobspawners.CreateMobSpawnersMod;
import dev.kvnmtz.createmobspawners.common.recipe.ingredient.SoulIngredient;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.crafting.IngredientType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public abstract class ModIngredients {

    public static final DeferredRegister<IngredientType<?>> INGREDIENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.INGREDIENT_TYPES, CreateMobSpawnersMod.MOD_ID);

    public static final Supplier<IngredientType<SoulIngredient>> SOUL =
            INGREDIENT_TYPES.register(
                    "soul",
                    () -> new IngredientType<>(SoulIngredient.CODEC, SoulIngredient.STREAM_CODEC)
            );

    public static void init(IEventBus eventBus) {
        INGREDIENT_TYPES.register(eventBus);
    }
}
