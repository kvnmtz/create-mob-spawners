package dev.kvnmtz.createmobspawners.items.renderer;

import com.jozufozu.flywheel.core.PartialModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollValueHandler;
import com.simibubi.create.foundation.item.render.CustomRenderedItemModel;
import com.simibubi.create.foundation.item.render.CustomRenderedItemModelRenderer;
import com.simibubi.create.foundation.item.render.PartialItemModelRenderer;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import dev.kvnmtz.createmobspawners.CreateMobSpawners;
import dev.kvnmtz.createmobspawners.items.registry.ModItems;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SoulCatcherRenderer extends CustomRenderedItemModelRenderer {
    protected static final PartialModel GEAR = new PartialModel(CreateMobSpawners.asResource("item/soul_catcher/gear"));
    protected static final PartialModel GEAR_EMPTY = new PartialModel(CreateMobSpawners.asResource("item/soul_catcher/gear_empty"));

    @Override
    protected void render(ItemStack itemStack, CustomRenderedItemModel model, PartialItemModelRenderer renderer, ItemDisplayContext itemDisplayContext, PoseStack ms, MultiBufferSource multiBufferSource, int light, int overlay) {
        var isEmpty = itemStack.getItem() == ModItems.EMPTY_SOUL_CATCHER.get();

        renderer.render(model.getOriginalModel(), light);

        if (isEmpty) {
            // X pivot is already 8
            var yOffset = -2.6515/16f;
            var zOffset = 1.6515/16f;

            // Y & Z pivots need to be centered (8) in order to rotate around X
            ms.translate(0, -yOffset, -zOffset);
            ms.mulPose(Axis.XP.rotationDegrees(45));
            ms.translate(0, yOffset, zOffset);

            // X & Y pivots need to be centered (8) in order to rotate around Z
            ms.translate(0, -yOffset, 0);
            ms.mulPose(Axis.ZP.rotationDegrees(ScrollValueHandler.getScroll(AnimationTickHolder.getPartialTicks()) * 15));
            ms.translate(0, yOffset, 0);

            renderer.render(GEAR_EMPTY.get(), light);
        } else {
            // X pivot is already 8
            var yOffset = 3.5/16f;

            // X & Y pivots need to be centered (8) in order to rotate around Z
            ms.translate(0, -yOffset, 0);
            ms.mulPose(Axis.ZP.rotationDegrees(ScrollValueHandler.getScroll(AnimationTickHolder.getPartialTicks()) * 15));
            ms.translate(0, yOffset, 0);

            renderer.render(GEAR.get(), light);
        }
    }
}
