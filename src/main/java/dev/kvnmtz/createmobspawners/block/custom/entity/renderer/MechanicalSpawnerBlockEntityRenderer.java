package dev.kvnmtz.createmobspawners.block.custom.entity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.foundation.fluid.FluidRenderer;
import dev.kvnmtz.createmobspawners.block.custom.entity.MechanicalSpawnerBlockEntity;
import dev.kvnmtz.createmobspawners.item.registry.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

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
        if (be.getLevel() == null) return;

        var vb = buffer.getBuffer(RenderType.solid());
        KineticBlockEntityRenderer.renderRotatingKineticBlock(be, getRenderedBlockState(be), ms, vb, light);

        renderSoulCatcher(be, ms, buffer, light);

        renderContainedFluid(be, partialTicks, ms, buffer, light);
    }

    private void renderSoulCatcher(MechanicalSpawnerBlockEntity be, PoseStack ms, MultiBufferSource buffer, int light) {
        if (!be.hasStoredEntity()) return;

        var itemRenderer = Minecraft.getInstance().getItemRenderer();
        var itemStack = ModItems.SOUL_CATCHER.get().getDefaultInstance();
        ms.pushPose();
        ms.translate(0.5f, 0.5f, 0.5f);
        ms.scale(0.8f, 0.8f, 0.8f);
        ms.mulPose(Axis.YP.rotationDegrees(getAngleForTe(be, be.getBlockPos(), Direction.Axis.Y) * 180 / (float) Math.PI));
        itemRenderer.renderStatic(itemStack, ItemDisplayContext.FIXED, light, OverlayTexture.NO_OVERLAY, ms, buffer, be.getLevel(), 1);
        ms.popPose();
    }

    private void renderContainedFluid(MechanicalSpawnerBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light) {
        var fluidLevel = be.getTank().getPrimaryTank().getFluidLevel();
        if (fluidLevel == null) return;

        var capHeight = 1.f / 16.f + 1.f / 128.f;
        var tankHullWidth = 1.75f / 16.f;
        var minPuddleHeight = 1.f / 16.f;
        var height = 1.f;
        var width = 1.f;
        var totalHeight = height - 2 * capHeight - minPuddleHeight;
        var level = fluidLevel.getValue(partialTicks);
        if (level < 1.0F / (512 * totalHeight)) return;

        var clampedLevel = Mth.clamp(level * totalHeight, 0.0F, totalHeight);
        var fluidStack = be.getFluidStack();
        if (!fluidStack.isEmpty()) {
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
            FluidRenderer.renderFluidBox(fluidStack, tankHullWidth, yMin, tankHullWidth, xMax, yMax, zMax, buffer, ms, light, false);
            ms.popPose();
        }
    }
}
