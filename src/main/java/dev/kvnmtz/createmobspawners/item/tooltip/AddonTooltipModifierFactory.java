package dev.kvnmtz.createmobspawners.item.tooltip;

import com.simibubi.create.foundation.item.ItemDescription;
import com.simibubi.create.foundation.item.TooltipHelper;
import com.simibubi.create.foundation.item.TooltipModifier;
import net.createmod.catnip.lang.FontHelper;
import net.minecraft.world.item.Item;

public abstract class AddonTooltipModifierFactory {
    public static TooltipModifier factory(Item item) {
        return new ItemDescription.Modifier(item, FontHelper.Palette.PURPLE)
                .andThen(TooltipModifier.mapNull(CustomKineticStats.create(item)));
    }
}
