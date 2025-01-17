package dev.kvnmtz.createmobspawners.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

import java.util.function.Function;

public class DrawStringUtils {
    public record TableColumnDefinition(HorizontalAlignment horizontalAlignment,
                                        Function<Integer, String> rowGenerator
    ) {
        public enum HorizontalAlignment {
            LEFT,
            RIGHT
        }
    }

    public static void drawTable(GuiGraphics graphics, int x, int y, int color, boolean dropShadow, int rowCount, int rowSpacing, int columnSpacing, TableColumnDefinition... columns) {
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

                graphics.drawString(font, text, drawX, currentY, color, dropShadow);
                currentY += font.lineHeight + rowSpacing;
            }

            currentY = y;
            currentX += maxStringWidth + columnSpacing;
        }
    }
}
