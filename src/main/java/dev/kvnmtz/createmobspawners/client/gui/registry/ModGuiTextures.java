package dev.kvnmtz.createmobspawners.client.gui.registry;

import com.simibubi.create.Create;
import dev.kvnmtz.createmobspawners.CreateMobSpawnersMod;
import net.createmod.catnip.gui.element.ScreenElement;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public enum ModGuiTextures implements ScreenElement {

    JEI_INFO_FRAME_TILE_1(Create.ID, "jei/widgets", 0, 221, 2, 2),
    JEI_INFO_FRAME_TILE_2(Create.ID, "jei/widgets", 1, 221, 2, 2),
    SPAWNER(CreateMobSpawnersMod.MOD_ID, "spawner", 0, 0, 188, 79),
    WARNING(CreateMobSpawnersMod.MOD_ID, "icons", 0, 0, 16, 16),;

    public final ResourceLocation location;
    public final int width, height;
    public final int startX, startY;

    ModGuiTextures(String namespace, String location, int startX, int startY, int width, int height) {
        this.location = ResourceLocation.fromNamespaceAndPath(namespace, "textures/gui/" + location + ".png");
        this.width = width;
        this.height = height;
        this.startX = startX;
        this.startY = startY;
    }

    @Override
    public void render(GuiGraphics graphics, int x, int y) {
        graphics.blit(location, x, y, startX, startY, width, height);
    }

    public static void renderFrameBorder(GuiGraphics graphics, int x, int y, int width, int height) {
        for (int w = 0; w < width - 2; w += 2) {
            JEI_INFO_FRAME_TILE_1.render(graphics, x + w, y);
            JEI_INFO_FRAME_TILE_2.render(graphics, x + w, y + height - 2);
        }
        JEI_INFO_FRAME_TILE_2.render(graphics, x + width - 2, y);
        JEI_INFO_FRAME_TILE_1.render(graphics, x + width - 2, y + height - 2);

        for (int h = 2; h < height - 4; h += 2) {
            JEI_INFO_FRAME_TILE_1.render(graphics, x, y + h);
            JEI_INFO_FRAME_TILE_2.render(graphics, x + width - 2, y + h);
        }
        JEI_INFO_FRAME_TILE_1.render(graphics, x, y + height - 3);
        JEI_INFO_FRAME_TILE_2.render(graphics, x + width - 2, y + height - 3);
    }
}
