package dev.kvnmtz.createmobspawners.client.compat.jei.category;

import com.simibubi.create.compat.jei.category.CreateRecipeCategory;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import dev.kvnmtz.createmobspawners.CreateMobSpawnersMod;
import dev.kvnmtz.createmobspawners.client.gui.animation.AnimatedSpawner;
import dev.kvnmtz.createmobspawners.client.gui.registry.ModGuiTextures;
import dev.kvnmtz.createmobspawners.client.util.DrawStringUtils;
import dev.kvnmtz.createmobspawners.common.block.entity.MechanicalSpawnerBlockEntity;
import dev.kvnmtz.createmobspawners.common.block.registry.ModBlocks;
import dev.kvnmtz.createmobspawners.common.item.registry.ModItems;
import dev.kvnmtz.createmobspawners.common.recipe.SpawningRecipe;
import mezz.jei.api.constants.VanillaTypes;
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
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class SpawningCategory implements IRecipeCategory<RecipeHolder<SpawningRecipe>> {

    public static final ResourceLocation UID = CreateMobSpawnersMod.asResource("spawning");
    public static final RecipeType<RecipeHolder<SpawningRecipe>> RECIPE_TYPE = RecipeType.createRecipeHolderType(UID);

    private static final int START_Y = 23;

    private final IDrawable icon;
    private final AnimatedSpawner spawner = new AnimatedSpawner();

    public SpawningCategory(IGuiHelper helper) {
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, ModBlocks.MECHANICAL_SPAWNER.asStack());
    }

    @Override
    public RecipeType<RecipeHolder<SpawningRecipe>> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("create_mob_spawners.jei.spawning.title");
    }

    @Override
    public @Nullable IDrawable getIcon() {
        return icon;
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
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<SpawningRecipe> recipeHolder,
                          IFocusGroup iFocusGroup) {
        var recipe = recipeHolder.value();

        builder
                .addSlot(RecipeIngredientRole.INPUT,
                        getWidth() / 2 - AllGuiTextures.JEI_LONG_ARROW.getWidth() / 2 - 16 - 16, START_Y - 8 - 10)
                .setBackground(CreateRecipeCategory.getRenderedSlot(), -1, -1)
                .addIngredient(VanillaTypes.ITEM_STACK, ModItems.SOUL_CATCHER.asStack());

        CreateRecipeCategory.addFluidSlot(builder,
                getWidth() / 2 - AllGuiTextures.JEI_LONG_ARROW.getWidth() / 2 - 16 - 16, START_Y - 8 + 10,
                recipe.fluid().getFluids()[0]);
    }

    @Override
    public void draw(RecipeHolder<SpawningRecipe> recipeHolder, IRecipeSlotsView recipeSlotsView,
                     GuiGraphics graphics, double mouseX, double mouseY) {
        var recipe = recipeHolder.value();
        var font = Minecraft.getInstance().font;

        AllGuiTextures.JEI_SHADOW.render(graphics, getWidth() / 2 - AllGuiTextures.JEI_SHADOW.getWidth() / 2,
                START_Y - AllGuiTextures.JEI_SHADOW.getHeight() / 2 + 9);
        AllGuiTextures.JEI_LONG_ARROW.render(graphics, getWidth() / 2 - AllGuiTextures.JEI_LONG_ARROW.getWidth() / 2,
                START_Y - AllGuiTextures.JEI_LONG_ARROW.getHeight() / 2);

        var questionMarkPosX = getWidth() / 2 + AllGuiTextures.JEI_LONG_ARROW.getWidth() / 2 + 16;
        var questionMarkPosY = START_Y - AllGuiTextures.JEI_QUESTION_MARK.getHeight() / 2;
        var questionMarkRect = new Rect2i(questionMarkPosX, questionMarkPosY,
                AllGuiTextures.JEI_QUESTION_MARK.getWidth(), AllGuiTextures.JEI_QUESTION_MARK.getHeight());
        AllGuiTextures.JEI_QUESTION_MARK.render(graphics, questionMarkPosX, questionMarkPosY);

        var hasBlacklist = recipe.blacklist().isPresent() && !recipe.blacklist().get().isEmpty();
        var hasWhitelist = recipe.whitelist().isPresent() && !recipe.whitelist().get().isEmpty();
        if (hasBlacklist || hasWhitelist) {
            ModGuiTextures.WARNING.render(graphics, questionMarkPosX + questionMarkRect.getWidth() - 4,
                    questionMarkPosY - questionMarkRect.getHeight() + 4);
        }

        if (questionMarkRect.contains((int) mouseX, (int) mouseY)) {
            List<Component> components = new ArrayList<>();
            components.add(Component.translatable("create_mob_spawners.jei.spawning.question_mark"));
            if (hasBlacklist) {
                components.add(Component.translatable("create_mob_spawners.jei.spawning.blacklist"));
                for (var entityId : recipe.blacklist().get()) {
                    var optEntityType = EntityType.byString(entityId.toString());
                    optEntityType.ifPresent(entityType -> components.add(Component.literal(" - " + entityType.getDescription().getString())));
                }
            } else if (hasWhitelist) {
                components.add(Component.translatable("create_mob_spawners.jei.spawning.whitelist"));
                for (var entityId : recipe.whitelist().get()) {
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

            graphics.renderComponentTooltip(font, components, (int) mouseX - longestComponent / 2 - 12,
                    (int) mouseY - 12);
        }

        spawner
                .withFluid(recipe.fluid().getFluids()[0])
                .draw(graphics, getWidth() / 2 - 13, START_Y + 8);

        ModGuiTextures.renderFrameBorder(graphics, 4, 48, getWidth() - 8, 55);

        var textY = 54;
        var color = 0xFFFFFF;
        var dropShadowColor = FastColor.ARGB32.color(128, 0);

        DrawStringUtils.drawString(graphics,
                Component.translatable("create_mob_spawners.jei.spawning.duration").getString(), 9, textY, color,
                dropShadowColor);
        textY += font.lineHeight;

        var rpms = new int[]{
                128,
                256
        };
        DrawStringUtils.drawTable(
                graphics,
                17,
                textY,
                color,
                dropShadowColor,
                2,
                0,
                4,
                new DrawStringUtils.TableColumnDefinition(
                        DrawStringUtils.TableColumnDefinition.HorizontalAlignment.RIGHT,
                        row -> String.format(Locale.US, "%.1fs",
                                1.0F / MechanicalSpawnerBlockEntity.getProgressForTick(rpms[row],
                                        recipe.spawnTicksAtMaxSpeed()) / 20.0F)
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

        DrawStringUtils.drawString(graphics, Component.translatable("create_mob_spawners.jei.spawning" +
                ".additional_spawn_attempts", recipe.additionalSpawnAttempts()), 9, textY, color, dropShadowColor);
    }
}
