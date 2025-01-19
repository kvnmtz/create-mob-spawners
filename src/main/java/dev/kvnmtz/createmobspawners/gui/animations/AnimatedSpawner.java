package dev.kvnmtz.createmobspawners.gui.animations;

import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.math.Axis;
import com.simibubi.create.compat.jei.category.animations.AnimatedKinetics;
import com.simibubi.create.foundation.fluid.FluidRenderer;
import com.simibubi.create.foundation.gui.UIRenderHelper;
import dev.kvnmtz.createmobspawners.block.custom.MechanicalSpawnerBlock;
import dev.kvnmtz.createmobspawners.block.registry.ModBlocks;
import dev.kvnmtz.createmobspawners.item.registry.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraftforge.fluids.FluidStack;

public class AnimatedSpawner extends AnimatedKinetics {
    private FluidStack fluid = null;

    public AnimatedSpawner withFluid(FluidStack fluid) {
        this.fluid = fluid;
        return this;
    }

    @Override
    public void draw(GuiGraphics graphics, int xOffset, int yOffset) {
        var matrixStack = graphics.pose();
        matrixStack.pushPose();
        matrixStack.translate(xOffset, yOffset, 100);
        matrixStack.mulPose(Axis.XP.rotationDegrees(-15.5f));
        matrixStack.mulPose(Axis.YP.rotationDegrees(22.5f));
        int scale = 20;

        blockElement(shaft(Direction.Axis.Y))
                .rotateBlock(0, getCurrentAngle(), 0)
                .scale(scale)
                .render(graphics);

        blockElement(ModBlocks.SPAWNER.get().defaultBlockState()
                .setValue(MechanicalSpawnerBlock.FACING, Direction.SOUTH))
                .scale(scale)
                .render(graphics);

        matrixStack.pushPose();
        var itemRenderer = Minecraft.getInstance().getItemRenderer();
        var itemStack = ModItems.SOUL_CATCHER.get().getDefaultInstance();
        var buffer = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
        UIRenderHelper.flipForGuiRender(matrixStack);
        matrixStack.translate(scale / 2.f, scale / 2.f, scale / 2.f);
        matrixStack.scale(scale, scale, scale);
        matrixStack.mulPose(Axis.YP.rotationDegrees(getCurrentAngle()));
        itemRenderer.renderStatic(itemStack, ItemDisplayContext.FIXED, LightTexture.FULL_BRIGHT, OverlayTexture.WHITE_OVERLAY_V, matrixStack, buffer, null, 1);
        buffer.endBatch();
        matrixStack.popPose();

        if (fluid != null) {
            buffer = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
            UIRenderHelper.flipForGuiRender(matrixStack);
            matrixStack.scale(scale, scale, scale);
            float from = 2/16f;
            float to = 1f - from;
            FluidRenderer.renderFluidBox(fluid, from, from, from, to, to, to, buffer, matrixStack, LightTexture.FULL_BRIGHT, false);
            buffer.endBatch();
        }

        matrixStack.popPose();
    }
}
