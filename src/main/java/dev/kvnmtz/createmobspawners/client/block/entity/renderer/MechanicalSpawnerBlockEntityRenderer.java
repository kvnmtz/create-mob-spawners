package dev.kvnmtz.createmobspawners.client.block.entity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import dev.kvnmtz.createmobspawners.common.block.entity.MechanicalSpawnerBlockEntity;
import dev.kvnmtz.createmobspawners.common.item.registry.ModItems;
import net.createmod.catnip.platform.NeoForgeCatnipServices;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class MechanicalSpawnerBlockEntityRenderer extends KineticBlockEntityRenderer<MechanicalSpawnerBlockEntity> {

    public MechanicalSpawnerBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected BlockState getRenderedBlockState(MechanicalSpawnerBlockEntity be) {
        return KineticBlockEntityRenderer.shaft(KineticBlockEntityRenderer.getRotationAxisOf(be));
    }

    @Override
    protected void renderSafe(MechanicalSpawnerBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        if (be.getLevel() == null) {
            return;
        }

        var vb = buffer.getBuffer(RenderType.solid());
        KineticBlockEntityRenderer.renderRotatingKineticBlock(be, getRenderedBlockState(be), ms, vb, light);

        renderSoulCatcher(be, ms, buffer, light);
        renderContainedFluid(be, partialTicks, ms, buffer, light);
    }

    private void renderSoulCatcher(MechanicalSpawnerBlockEntity be, PoseStack ms, MultiBufferSource buffer, int light) {
        if (!be.hasStoredEntity()) {
            return;
        }

        var itemRenderer = Minecraft.getInstance().getItemRenderer();
        var itemStack = ModItems.SOUL_CATCHER.get().getDefaultInstance();
        ms.pushPose();
        ms.translate(0.5f, 0.5f, 0.5f);
        ms.scale(0.8f, 0.8f, 0.8f);
        ms.mulPose(Axis.YP.rotationDegrees(getAngleForBe(be, be.getBlockPos(), Direction.Axis.Y) * 180 / (float) Math.PI));
        itemRenderer.renderStatic(itemStack, ItemDisplayContext.FIXED, light, OverlayTexture.NO_OVERLAY, ms, buffer, be.getLevel(), 1);
        ms.popPose();
    }

    private void renderContainedFluid(MechanicalSpawnerBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light) {
        var fluidLevel = be.getFluidLevel();
        if (fluidLevel == null) {
            return;
        }

        var capHeight = 1.0F / 16.0F + 1.0F / 128.0F;
        var tankHullWidth = 1.75f / 16.0F;
        var minPuddleHeight = 1.0F / 16.0F;
        var height = 1.0F;
        var width = 1.0F;
        var totalHeight = height - 2 * capHeight - minPuddleHeight;
        var level = fluidLevel.getValue(partialTicks);
        if (level < 1.0F / (512 * totalHeight)) {
            return;
        }

        var clampedLevel = Mth.clamp(level * totalHeight, 0.0F, totalHeight);
        var fluidStack = be.getFluidStack();
        if (fluidStack.isEmpty()) {
            return;
        }
        
        var top = fluidStack.getFluid().getFluidType().isLighterThanAir();
        var xMax = tankHullWidth + width - 2 * tankHullWidth;
        var yMin = totalHeight + capHeight + minPuddleHeight - clampedLevel;
        var yMax = yMin + clampedLevel;
        if (top) {
            yMin += totalHeight - clampedLevel;
            yMax += totalHeight - clampedLevel;
        }

        var zMax = tankHullWidth + width - 2 * tankHullWidth;
        ms.pushPose();
        ms.translate(0.0F, clampedLevel - totalHeight, 0.0F);
        NeoForgeCatnipServices.FLUID_RENDERER.renderFluidBox(fluidStack, tankHullWidth, yMin, tankHullWidth, xMax, yMax, zMax, buffer, ms, light, false, true);
        ms.popPose();
    }
}
