package dev.kvnmtz.createmobspawners.item.tooltip;

import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.content.kinetics.steamEngine.SteamEngineBlock;
import com.simibubi.create.foundation.item.KineticStats;
import com.simibubi.create.foundation.item.TooltipHelper;
import com.simibubi.create.foundation.utility.Components;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.LangBuilder;
import dev.kvnmtz.createmobspawners.block.registry.ModBlocks;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static net.minecraft.ChatFormatting.GRAY;

public class CustomKineticStats extends KineticStats {
    public CustomKineticStats(Block block) {
        super(block);
    }

    public static @Nullable KineticStats create(Item item) {
        if (item instanceof BlockItem blockItem) {
            Block block = blockItem.getBlock();
            if (block instanceof IRotate || block instanceof SteamEngineBlock) {
                return new CustomKineticStats(block);
            }
        }

        return null;
    }

    @Override
    public void modify(ItemTooltipEvent context) {
        if (block == ModBlocks.MECHANICAL_SPAWNER.get()) {
            List<Component> list = new ArrayList<>();

            Lang.translate("tooltip.stressImpact")
                    .style(GRAY)
                    .addTo(list);

            LangBuilder builder = Lang.builder()
                    .add(Lang.text(TooltipHelper.makeProgressBar(3, 3))
                            .style(IRotate.StressImpact.HIGH.getAbsoluteColor()));

            builder.add(Lang.text(Component.translatable("block.create_mob_spawners.mechanical_spawner.tooltip.stress").getString())).addTo(list);

            List<Component> tooltip = context.getToolTip();
            tooltip.add(Components.immutableEmpty());
            tooltip.addAll(list);
        } else {
            super.modify(context);
        }
    }
}
