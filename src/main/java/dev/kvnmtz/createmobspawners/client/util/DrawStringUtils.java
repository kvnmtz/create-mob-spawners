package dev.kvnmtz.createmobspawners.client.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

@OnlyIn(Dist.CLIENT)
public abstract class DrawStringUtils {

    public record TableColumnDefinition(HorizontalAlignment horizontalAlignment,
                                        Function<Integer, String> rowGenerator
    ) {

        public enum HorizontalAlignment {
            LEFT,
            RIGHT
        }
    }

    public static void drawTable(GuiGraphics graphics, int x, int y, int color, @Nullable Integer dropShadowColor,
                                 int rowCount, int rowSpacing, int columnSpacing, TableColumnDefinition... columns) {
        var font = Minecraft.getInstance().font;

        int currentX = x;
        int currentY = y;

        for (var column : columns) {
            var maxStringWidth = 0;

            for (var i = 0; i < rowCount; i++) {
                var text = column.rowGenerator().apply(i);

                var width = font.width(text);
                if (width > maxStringWidth) {
                    maxStringWidth = width;
                }
            }

            for (var i = 0; i < rowCount; i++) {
                var text = column.rowGenerator().apply(i);

                int drawX = switch (column.horizontalAlignment()) {
                    case LEFT -> currentX;
                    case RIGHT -> currentX + maxStringWidth - font.width(text);
                };

                drawString(graphics, text, drawX, currentY, color, dropShadowColor);
                currentY += font.lineHeight + rowSpacing;
            }

            currentY = y;
            currentX += maxStringWidth + columnSpacing;
        }
    }

    public static void drawString(GuiGraphics graphics, Component text, int x, int y, int color,
                                  @Nullable Integer dropShadowColor) {
        drawString(graphics, text.getString(), x, y, color, dropShadowColor);
    }

    public static void drawString(GuiGraphics graphics, String text, int x, int y, int color,
                                  @Nullable Integer dropShadowColor) {
        var font = Minecraft.getInstance().font;

        if (dropShadowColor != null) {
            graphics.drawString(font, text, x + 1, y + 1, dropShadowColor, false);
        }

        graphics.drawString(font, text, x, y, color, false);
    }
}
