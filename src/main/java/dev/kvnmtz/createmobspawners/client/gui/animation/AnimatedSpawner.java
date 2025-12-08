package dev.kvnmtz.createmobspawners.client.gui.animation;

import com.mojang.math.Axis;
import com.simibubi.create.compat.jei.category.animations.AnimatedKinetics;
import dev.kvnmtz.createmobspawners.common.block.MechanicalSpawnerBlock;
import dev.kvnmtz.createmobspawners.common.block.registry.ModBlocks;
import dev.kvnmtz.createmobspawners.common.item.registry.ModItems;
import net.createmod.catnip.gui.UIRenderHelper;
import net.createmod.catnip.platform.NeoForgeCatnipServices;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.neoforged.neoforge.fluids.FluidStack;

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

        blockElement(ModBlocks.MECHANICAL_SPAWNER.get().defaultBlockState()
                .setValue(MechanicalSpawnerBlock.FACING, Direction.SOUTH))
                .scale(scale)
                .render(graphics);

        matrixStack.pushPose();
        var itemRenderer = Minecraft.getInstance().getItemRenderer();
        var itemStack = ModItems.SOUL_CATCHER.get().getDefaultInstance();
        UIRenderHelper.flipForGuiRender(matrixStack);
        matrixStack.translate(scale / 2.f, scale / 2.f, scale / 2.f);
        matrixStack.scale(scale, scale, scale);
        matrixStack.mulPose(Axis.YP.rotationDegrees(getCurrentAngle()));
        itemRenderer.renderStatic(itemStack, ItemDisplayContext.FIXED, LightTexture.FULL_BRIGHT, OverlayTexture.WHITE_OVERLAY_V, matrixStack, graphics.bufferSource(), null, 1);
        graphics.flush();
        matrixStack.popPose();

        if (fluid != null) {
            UIRenderHelper.flipForGuiRender(matrixStack);
            matrixStack.scale(scale, scale, scale);
            float from = 2/16f;
            float to = 1f - from;
            NeoForgeCatnipServices.FLUID_RENDERER.renderFluidBox(fluid, from, from, from, to, to, to, graphics.bufferSource(), matrixStack, LightTexture.FULL_BRIGHT, false, true);
            graphics.flush();
        }

        matrixStack.popPose();
    }
}
