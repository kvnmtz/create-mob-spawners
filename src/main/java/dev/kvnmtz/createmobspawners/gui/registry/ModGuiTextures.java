package dev.kvnmtz.createmobspawners.gui.registry;

import net.createmod.catnip.gui.element.ScreenElement;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public enum ModGuiTextures implements ScreenElement {
    JEI_INFO_FRAME_TILE_1("create", "jei/widgets", 0, 221, 2, 2),
    JEI_INFO_FRAME_TILE_2("create", "jei/widgets", 1, 221, 2, 2),
    SPAWNER("create_mob_spawners", "spawner", 0, 0, 188, 79),
    WARNING("create_mob_spawners", "icons", 0, 0, 16, 16),;

    public final ResourceLocation location;
    public final int width, height;
    public final int startX, startY;

    ModGuiTextures(String namespace, String location, int startX, int startY, int width, int height) {
        this.location = new ResourceLocation(namespace, "textures/gui/" + location + ".png");
        this.width = width;
        this.height = height;
        this.startX = startX;
        this.startY = startY;
    }

    @OnlyIn(Dist.CLIENT)
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
