package dev.kvnmtz.createmobspawners.gui.screens;

import com.simibubi.create.foundation.gui.AbstractSimiScreen;
import com.simibubi.create.foundation.gui.element.GuiGameElement;
import com.simibubi.create.foundation.gui.widget.Label;
import com.simibubi.create.foundation.gui.widget.ScrollInput;
import com.simibubi.create.foundation.utility.Components;
import dev.kvnmtz.createmobspawners.block.custom.entity.MechanicalSpawnerBlockEntity;
import dev.kvnmtz.createmobspawners.block.registry.ModBlocks;
import dev.kvnmtz.createmobspawners.gui.registry.ModGuiTextures;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class SpawnerScreen extends AbstractSimiScreen {
    private final ItemStack renderedItem = new ItemStack(ModBlocks.MECHANICAL_SPAWNER.get());

    private final ModGuiTextures background;
    private final MechanicalSpawnerBlockEntity blockEntity;

    public SpawnerScreen(MechanicalSpawnerBlockEntity be) {
        super(Component.literal("Test"));
        this.background = ModGuiTextures.SPAWNER;
        this.blockEntity = be;
    }

    @Override
    protected void renderWindow(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        int x = guiLeft;
        int y = guiTop;

        background.render(graphics, x, y);

        GuiGameElement.of(renderedItem).<GuiGameElement
                        .GuiRenderBuilder>at(x + background.width + 6, y + background.height - 56, -200)
                .scale(5)
                .render(graphics);
    }

    @Override
    protected void init() {
        setWindowSize(background.width, background.height);
        setWindowOffset(-20, 0);
        super.init();

        int x = guiLeft;
        int y = guiTop;

        String[] titles = { "Width and Length", "Height", "Height Offset from Center" };

        for (int i = 0; i < 3; i++) {
            var label = new Label(x + 82 + i * 20, y + 28, Components.immutableEmpty()).withShadow();

            int finalIdx = i;
            var input = new ScrollInput(x + 73 + i * 20, y + 23, 18, 18)
                    .withRange(5, 11)
                    .writingTo(label)
                    .titled(Component.literal(titles[i]))
                    .calling(value -> {
                        // apply value
                        label.setX(x + 82 + finalIdx * 20 - font.width(label.text) / 2);
                    });

            input.setState(5);
            input.onChanged();

            addRenderableWidget(label);
            addRenderableWidget(input);
        }
    }
}
