package dev.kvnmtz.createmobspawners.common.item.tooltip;

import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.content.kinetics.steamEngine.SteamEngineBlock;
import com.simibubi.create.foundation.item.KineticStats;
import com.simibubi.create.foundation.item.TooltipHelper;
import com.simibubi.create.foundation.utility.CreateLang;
import dev.kvnmtz.createmobspawners.common.block.registry.ModBlocks;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static net.minecraft.ChatFormatting.GRAY;

public class ModKineticStats extends KineticStats {

    public ModKineticStats(Block block) {
        super(block);
    }

    public static @Nullable KineticStats create(Item item) {
        if (item instanceof BlockItem blockItem) {
            var block = blockItem.getBlock();
            if (block instanceof IRotate || block instanceof SteamEngineBlock) {
                return new ModKineticStats(block);
            }
        }

        return null;
    }

    @Override
    public void modify(ItemTooltipEvent context) {
        if (block != ModBlocks.MECHANICAL_SPAWNER.get()) {
            super.modify(context);
            return;
        }

        List<Component> list = new ArrayList<>();

        CreateLang.translate("tooltip.stressImpact")
                .style(GRAY)
                .addTo(list);

        var builder = CreateLang.builder()
                .add(CreateLang.text(TooltipHelper.makeProgressBar(3, 3))
                        .style(IRotate.StressImpact.HIGH.getAbsoluteColor()));

        builder.add(CreateLang.text(Component.translatable("block.create_mob_spawners.mechanical_spawner.tooltip.stress").getString())).addTo(list);

        var tooltip = context.getToolTip();
        tooltip.add(CommonComponents.EMPTY);
        tooltip.addAll(list);
    }
}
