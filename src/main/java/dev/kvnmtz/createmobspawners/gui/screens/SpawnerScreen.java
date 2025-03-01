package dev.kvnmtz.createmobspawners.gui.screens;

import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.widget.IconButton;
import com.simibubi.create.foundation.gui.widget.Label;
import com.simibubi.create.foundation.gui.widget.ScrollInput;
import com.simibubi.create.foundation.gui.widget.TooltipArea;
import dev.kvnmtz.createmobspawners.CreateMobSpawners;
import dev.kvnmtz.createmobspawners.block.custom.entity.MechanicalSpawnerBlockEntity;
import dev.kvnmtz.createmobspawners.block.registry.ModBlocks;
import dev.kvnmtz.createmobspawners.gui.registry.ModGuiTextures;
import dev.kvnmtz.createmobspawners.network.PacketHandler;
import dev.kvnmtz.createmobspawners.network.packet.ServerboundConfigureSpawnerPacket;
import net.createmod.catnip.data.Pair;
import net.createmod.catnip.gui.AbstractSimiScreen;
import net.createmod.catnip.gui.element.GuiGameElement;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class SpawnerScreen extends AbstractSimiScreen {
    private final ItemStack renderedItem = new ItemStack(ModBlocks.MECHANICAL_SPAWNER.get());

    private final ModGuiTextures background;
    private final MechanicalSpawnerBlockEntity blockEntity;

    private final List<ScrollInput> spawnAreaInputs = new ArrayList<>();

    public SpawnerScreen(MechanicalSpawnerBlockEntity be) {
        super(Component.translatable("block.create_mob_spawners.mechanical_spawner"));
        this.background = ModGuiTextures.SPAWNER;
        this.blockEntity = be;
    }

    @Override
    protected void renderWindow(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        int x = guiLeft;
        int y = guiTop;

        background.render(graphics, x, y);

        graphics.drawString(font, title, x + (background.width - 8) / 2 - font.width(title) / 2, y + 4, 0x592424, false);

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

        blockEntity.stopLingerSpawnAreaHighlighting();

        int x = guiLeft;
        int y = guiTop;

        final String spawnerGuiKeyPrefix = "create_mob_spawners.gui.spawner.";

        var spawnAreaTooltip = new TooltipArea(x + 49, y + 23, 18, 18).withTooltip(List.of(Component.translatable(spawnerGuiKeyPrefix + "spawning_area")));
        addRenderableWidget(spawnAreaTooltip);

        var titles = List.of(Component.translatable(spawnerGuiKeyPrefix + "width"), Component.translatable(spawnerGuiKeyPrefix + "height"), Component.translatable(spawnerGuiKeyPrefix + "height_offset"));
        var ranges = List.of(
                Pair.of(CreateMobSpawners.SERVER_CONFIG.mechanicalSpawnerAreaMinWidth.get(), CreateMobSpawners.SERVER_CONFIG.mechanicalSpawnerAreaMaxWidth.get()),
                Pair.of(CreateMobSpawners.SERVER_CONFIG.mechanicalSpawnerAreaMinHeight.get(), CreateMobSpawners.SERVER_CONFIG.mechanicalSpawnerAreaMaxHeight.get()),
                Pair.of(CreateMobSpawners.SERVER_CONFIG.mechanicalSpawnerAreaMinHeightOffset.get(), CreateMobSpawners.SERVER_CONFIG.mechanicalSpawnerAreaMaxHeightOffset.get())
        );

        for (int i = 0; i < 3; i++) {
            var label = new Label(x + 82 + i * 20, y + 28, CommonComponents.EMPTY).withShadow();

            int finalIdx = i;
            var input = new ScrollInput(x + 73 + i * 20, y + 23, 18, 18)
                    .withRange(ranges.get(i).getFirst(), ranges.get(i).getSecond() + 1)
                    .writingTo(label)
                    .titled(titles.get(i))
                    .calling(value -> {
                        label.setX(x + 82 + finalIdx * 20 - font.width(label.text) / 2);
                    })
                    .withStepFunction(stepContext -> {
                        if (finalIdx == 2) return 1;
                        return 2;
                    });

            switch (finalIdx) {
                case 0:
                    input.setState(blockEntity.getSpawningAreaWidth());
                    break;
                case 1:
                    input.setState(blockEntity.getSpawningAreaHeight());
                    break;
                case 2:
                    input.setState(blockEntity.getSpawningAreaHeightOffset());
                    break;
            }
            input.onChanged();

            addRenderableWidget(label);
            addRenderableWidget(input);
            spawnAreaInputs.add(input);
        }

        var confirmButton = new IconButton(x + background.width - 33, y + background.height - 24, AllIcons.I_CONFIRM);
        confirmButton.withCallback(this::onClose);
        addRenderableWidget(confirmButton);
    }

    @Override
    public void tick() {
        super.tick();
        blockEntity.highlightSpawningArea(spawnAreaInputs.get(0).getState(), spawnAreaInputs.get(1).getState(), spawnAreaInputs.get(2).getState());
    }

    @Override
    public void removed() {
        super.removed();
        send();
        blockEntity.lingerSpawnAreaHighlighting(spawnAreaInputs.get(0).getState(), spawnAreaInputs.get(1).getState(), spawnAreaInputs.get(2).getState());
    }

    private void send() {
        PacketHandler.sendToServer(
                new ServerboundConfigureSpawnerPacket(
                        blockEntity.getBlockPos(),
                        spawnAreaInputs.get(0).getState(),
                        spawnAreaInputs.get(1).getState(),
                        spawnAreaInputs.get(2).getState()
                )
        );
    }
}
