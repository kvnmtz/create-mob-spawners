package dev.kvnmtz.createmobspawners.client.gui.screen;

import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.widget.IconButton;
import com.simibubi.create.foundation.gui.widget.Label;
import com.simibubi.create.foundation.gui.widget.ScrollInput;
import com.simibubi.create.foundation.gui.widget.TooltipArea;
import dev.kvnmtz.createmobspawners.CreateMobSpawnersMod;
import dev.kvnmtz.createmobspawners.client.gui.registry.ModGuiTextures;
import dev.kvnmtz.createmobspawners.common.block.entity.MechanicalSpawnerBlockEntity;
import dev.kvnmtz.createmobspawners.common.block.registry.ModBlocks;
import dev.kvnmtz.createmobspawners.common.config.ModServerConfig;
import dev.kvnmtz.createmobspawners.common.network.packet.ServerboundConfigureSpawnerPacket;
import net.createmod.catnip.data.Pair;
import net.createmod.catnip.gui.AbstractSimiScreen;
import net.createmod.catnip.gui.element.GuiGameElement;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
public class SpawnerScreen extends AbstractSimiScreen {

    private final ModGuiTextures background;
    private final MechanicalSpawnerBlockEntity blockEntity;

    private final ItemStack renderedItem = new ItemStack(ModBlocks.MECHANICAL_SPAWNER.get());
    private final List<ScrollInput> spawnAreaInputs = new ArrayList<>();

    public SpawnerScreen(MechanicalSpawnerBlockEntity be) {
        super(Component.translatable("block." + CreateMobSpawnersMod.MOD_ID + ".mechanical_spawner"));
        this.background = ModGuiTextures.SPAWNER;
        this.blockEntity = be;
    }

    @Override
    protected void renderWindow(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        var x = guiLeft;
        var y = guiTop;

        background.render(graphics, x, y);

        graphics.drawString(font, title, x + (background.width - 8) / 2 - font.width(title) / 2, y + 4, 0x592424,
                false);

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

        var x = guiLeft;
        var y = guiTop;

        var spawnerGuiKeyPrefix = CreateMobSpawnersMod.MOD_ID + ".gui.spawner.";

        var spawnAreaTooltip =
                new TooltipArea(x + 49, y + 23, 18, 18).withTooltip(List.of(Component.translatable(spawnerGuiKeyPrefix + "spawning_area")));
        addRenderableWidget(spawnAreaTooltip);

        var titles = List.of(Component.translatable(spawnerGuiKeyPrefix + "width"),
                Component.translatable(spawnerGuiKeyPrefix + "height"), Component.translatable(spawnerGuiKeyPrefix +
                        "height_offset"));
        var ranges = List.of(
                Pair.of(ModServerConfig.CONFIG.mechanicalSpawnerAreaMinWidth.get(),
                        ModServerConfig.CONFIG.mechanicalSpawnerAreaMaxWidth.get()),
                Pair.of(ModServerConfig.CONFIG.mechanicalSpawnerAreaMinHeight.get(),
                        ModServerConfig.CONFIG.mechanicalSpawnerAreaMaxHeight.get()),
                Pair.of(ModServerConfig.CONFIG.mechanicalSpawnerAreaMinHeightOffset.get(),
                        ModServerConfig.CONFIG.mechanicalSpawnerAreaMaxHeightOffset.get())
        );

        for (var i = 0; i < 3; i++) {
            var label = new Label(x + 82 + i * 20, y + 28, CommonComponents.EMPTY).withShadow();

            final var finalIdx = i;
            var input = new ScrollInput(x + 73 + i * 20, y + 23, 18, 18)
                    .withRange(ranges.get(i).getFirst(), ranges.get(i).getSecond() + 1)
                    .writingTo(label)
                    .titled(titles.get(i))
                    .calling(value -> label.setX(x + 82 + finalIdx * 20 - font.width(label.text) / 2))
                    .withStepFunction(stepContext -> {
                        if (finalIdx == 2) return 1;
                        return 2;
                    });

            switch (i) {
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
        blockEntity.highlightSpawningArea(getWidthInput(), getHeightInput(), getHeightOffsetInput());
    }

    @Override
    public void removed() {
        super.removed();
        send();
        blockEntity.lingerSpawnAreaHighlighting(getWidthInput(), getHeightInput(), getHeightOffsetInput());
    }

    private void send() {
        PacketDistributor.sendToServer(new ServerboundConfigureSpawnerPacket(
                blockEntity.getBlockPos(),
                getWidthInput(),
                getHeightInput(),
                getHeightOffsetInput()
        ));
    }
    
    private int getWidthInput() {
        return spawnAreaInputs.getFirst().getState();
    }
    
    private int getHeightInput() {
        return spawnAreaInputs.get(1).getState();
    }

    private int getHeightOffsetInput() {
        return spawnAreaInputs.get(2).getState();
    }
}
