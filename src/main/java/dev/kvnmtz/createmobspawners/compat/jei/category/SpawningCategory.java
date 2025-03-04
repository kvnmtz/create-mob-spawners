package dev.kvnmtz.createmobspawners.compat.jei.category;

import com.simibubi.create.compat.jei.EmptyBackground;
import com.simibubi.create.compat.jei.category.CreateRecipeCategory;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import dev.kvnmtz.createmobspawners.CreateMobSpawners;
import dev.kvnmtz.createmobspawners.block.custom.entity.MechanicalSpawnerBlockEntity;
import dev.kvnmtz.createmobspawners.block.registry.ModBlocks;
import dev.kvnmtz.createmobspawners.gui.animations.AnimatedSpawner;
import dev.kvnmtz.createmobspawners.gui.registry.ModGuiTextures;
import dev.kvnmtz.createmobspawners.item.registry.ModItems;
import dev.kvnmtz.createmobspawners.recipe.custom.SpawningRecipe;
import dev.kvnmtz.createmobspawners.utils.DrawStringUtils;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.forge.ForgeTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SpawningCategory implements IRecipeCategory<SpawningRecipe> {
    public static final ResourceLocation UID = CreateMobSpawners.asResource("spawning");
    public static final RecipeType<SpawningRecipe> SPAWNING_RECIPE_TYPE = new RecipeType<>(UID, SpawningRecipe.class);

    private final IDrawable icon;
    private final AnimatedSpawner spawner = new AnimatedSpawner();

    public SpawningCategory(IGuiHelper helper) {
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModBlocks.MECHANICAL_SPAWNER.get()));
        // only for REI compatibility
        this.background = new EmptyBackground(getWidth(), getHeight());
    }

    @Override
    public RecipeType<SpawningRecipe> getRecipeType() {
        return SPAWNING_RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("create_mob_spawners.jei.spawning.title");
    }

    @Override
    public int getWidth() {
        return 196;
    }

    @Override
    public int getHeight() {
        return 107;
    }

    @Override
    public @Nullable IDrawable getIcon() {
        return icon;
    }

    private static final int START_Y = 23;

    // only for REI compatibility
    private final IDrawable background;

    // only for REI compatibility
    @Override
    public @Nullable IDrawable getBackground() {
        return background;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, SpawningRecipe recipe, IFocusGroup iFocusGroup) {
        builder
                .addSlot(RecipeIngredientRole.INPUT, getWidth() / 2 - AllGuiTextures.JEI_LONG_ARROW.getWidth() / 2 - 16 - 16, START_Y - 8 - 10)
                .setBackground(CreateRecipeCategory.getRenderedSlot(), -1, -1)
                .addIngredient(VanillaTypes.ITEM_STACK, ModItems.SOUL_CATCHER.get().getDefaultInstance());

        builder
                .addSlot(RecipeIngredientRole.INPUT, getWidth() / 2 - AllGuiTextures.JEI_LONG_ARROW.getWidth() / 2 - 16 - 16, START_Y - 8 + 10)
                .setBackground(CreateRecipeCategory.getRenderedSlot(), -1, -1)
                .addIngredients(
                        ForgeTypes.FLUID_STACK,
                        CreateRecipeCategory.withImprovedVisibility(recipe.getFluidIngredient().getMatchingFluidStacks())
                )
                .addRichTooltipCallback(
                        CreateRecipeCategory.addFluidTooltip(recipe.getFluidIngredient().getRequiredAmount())
                );
    }

    @Override
    public void draw(SpawningRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY) {
        var font = Minecraft.getInstance().font;

        AllGuiTextures.JEI_SHADOW.render(graphics, getWidth() / 2 - AllGuiTextures.JEI_SHADOW.getWidth() / 2, START_Y - AllGuiTextures.JEI_SHADOW.getHeight() / 2 + 9);
        AllGuiTextures.JEI_LONG_ARROW.render(graphics, getWidth() / 2 - AllGuiTextures.JEI_LONG_ARROW.getWidth() / 2, START_Y - AllGuiTextures.JEI_LONG_ARROW.getHeight() / 2);

        var questionMarkPosX = getWidth() / 2 + AllGuiTextures.JEI_LONG_ARROW.getWidth() / 2 + 16;
        var questionMarkPosY = START_Y - AllGuiTextures.JEI_QUESTION_MARK.getHeight() / 2;
        var questionMarkRect = new Rect2i(questionMarkPosX, questionMarkPosY, AllGuiTextures.JEI_QUESTION_MARK.getWidth(), AllGuiTextures.JEI_QUESTION_MARK.getHeight());
        AllGuiTextures.JEI_QUESTION_MARK.render(graphics, questionMarkPosX, questionMarkPosY);

        if (!recipe.getBlacklist().isEmpty() || !recipe.getWhitelist().isEmpty()) {
            ModGuiTextures.WARNING.render(graphics, questionMarkPosX + questionMarkRect.getWidth() - 4, questionMarkPosY - questionMarkRect.getHeight() + 4);
        }

        if (questionMarkRect.contains((int) mouseX, (int) mouseY)) {
            List<Component> components = new ArrayList<>();
            components.add(Component.translatable("create_mob_spawners.jei.spawning.question_mark"));
            if (!recipe.getBlacklist().isEmpty()) {
                components.add(Component.translatable("create_mob_spawners.jei.spawning.blacklist"));
                for (var entityId : recipe.getBlacklist()) {
                    var optEntityType = EntityType.byString(entityId.toString());
                    optEntityType.ifPresent(entityType -> components.add(Component.literal(" - " + entityType.getDescription().getString())));
                }
            } else if (!recipe.getWhitelist().isEmpty()) {
                components.add(Component.translatable("create_mob_spawners.jei.spawning.whitelist"));
                for (var entityId : recipe.getWhitelist()) {
                    var optEntityType = EntityType.byString(entityId.toString());
                    optEntityType.ifPresent(entityType -> components.add(Component.literal(" - " + entityType.getDescription().getString())));
                }
            }

            int longestComponent = 0;
            for (var component : components) {
                var width = font.width(component.getString());
                if (width > longestComponent) {
                    longestComponent = width;
                }
            }

            graphics.renderComponentTooltip(font, components, (int) mouseX - longestComponent / 2 - 12, (int) mouseY - 12);
        }

        spawner
                .withFluid(recipe.getFluidIngredient().getMatchingFluidStacks().get(0))
                .draw(graphics, getWidth() / 2 - 13, START_Y + 8);

        ModGuiTextures.renderFrameBorder(graphics, 4, 48, getWidth() - 8, 55);

        var textY = 54;

        graphics.drawString(font, Component.translatable("create_mob_spawners.jei.spawning.duration").getString(), 9, textY, 0xFFFFFF, false);
        textY += font.lineHeight;

        var rpms = new int[] {
                128,
                256
        };
        DrawStringUtils.drawTable(
                graphics,
                17,
                textY,
                0xFFFFFF,
                false,
                2,
                0,
                4,
                new DrawStringUtils.TableColumnDefinition(
                        DrawStringUtils.TableColumnDefinition.HorizontalAlignment.RIGHT,
                        row -> String.format(Locale.US, "%.1fs", 1.f / MechanicalSpawnerBlockEntity.getProgressForTick(rpms[row], recipe.getSpawnTicksAtMaxSpeed()) / 20.f)
                ),
                new DrawStringUtils.TableColumnDefinition(
                        DrawStringUtils.TableColumnDefinition.HorizontalAlignment.LEFT,
                        row -> Component.translatable("create_mob_spawners.jei.spawning.at").getString()
                ),
                new DrawStringUtils.TableColumnDefinition(
                        DrawStringUtils.TableColumnDefinition.HorizontalAlignment.RIGHT,
                        row -> String.valueOf(rpms[row])
                ),
                new DrawStringUtils.TableColumnDefinition(
                        DrawStringUtils.TableColumnDefinition.HorizontalAlignment.LEFT,
                        row -> Component.translatable("create_mob_spawners.jei.spawning.rpm").getString()
                )
        );
        textY += font.lineHeight * 3;

        graphics.drawString(font, Component.translatable("create_mob_spawners.jei.spawning.additional_spawn_attempts", recipe.getAdditionalSpawnAttempts()).getString(), 9, textY, 0xFFFFFF, false);
    }
}
