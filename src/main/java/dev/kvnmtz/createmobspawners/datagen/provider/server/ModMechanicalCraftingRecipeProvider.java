package dev.kvnmtz.createmobspawners.datagen.provider.server;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.api.data.recipe.MechanicalCraftingRecipeGen;
import dev.kvnmtz.createmobspawners.CreateMobSpawnersMod;
import dev.kvnmtz.createmobspawners.common.block.registry.ModBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.Tags;

import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

@SuppressWarnings("unused")
public class ModMechanicalCraftingRecipeProvider extends MechanicalCraftingRecipeGen {

    public static final TagKey<Item> PLATES_BRASS =
            ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "plates/brass"));

    public ModMechanicalCraftingRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, CreateMobSpawnersMod.MOD_ID);
    }

    public Ingredient tagIngredientWithFallback(TagKey<Item> tag, ItemLike fallbackItem) {
        return Ingredient.fromValues(Stream.of(
                new Ingredient.ItemValue(new ItemStack(fallbackItem)),
                new Ingredient.TagValue(tag)
        ));
    }

    GeneratedRecipe MECHANICAL_SPAWNER = create(ModBlocks.MECHANICAL_SPAWNER::asItem)
            .recipe(b -> b
                    .key('A', AllBlocks.BRASS_CASING)
                    .key('B', tagIngredientWithFallback(PLATES_BRASS, AllItems.BRASS_SHEET))
                    .key('C', AllBlocks.SHAFT)
                    .key('D', Tags.Items.CHAINS)
                    .key('E', Tags.Items.INGOTS_NETHERITE)
                    .key('F', Items.END_CRYSTAL)
                    .key('G', Items.TOTEM_OF_UNDYING)
                    .key('H', AllItems.PRECISION_MECHANISM)
                    .patternLine("ABCBA")
                    .patternLine("DEFED")
                    .patternLine("DEGED")
                    .patternLine("DEHED")
                    .patternLine("ABCBA")
            );
}
