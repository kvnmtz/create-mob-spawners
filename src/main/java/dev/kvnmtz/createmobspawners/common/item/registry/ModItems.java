package dev.kvnmtz.createmobspawners.common.item.registry;

import com.simibubi.create.foundation.data.AssetLookup;
import com.tterrag.registrate.util.entry.ItemEntry;
import dev.kvnmtz.createmobspawners.common.item.SoulCatcherItem;
import net.minecraft.world.item.Rarity;

import static dev.kvnmtz.createmobspawners.CreateMobSpawnersMod.REGISTRATE;

public abstract class ModItems {

    public static final ItemEntry<SoulCatcherItem> EMPTY_SOUL_CATCHER =
            REGISTRATE.item("empty_soul_catcher", SoulCatcherItem::new)
                    .properties(
                            p -> p.stacksTo(1)
                                    .rarity(Rarity.UNCOMMON)
                    )
                    .model(AssetLookup.existingItemModel())
                    .register();

    public static final ItemEntry<SoulCatcherItem> SOUL_CATCHER =
            REGISTRATE.item("soul_catcher", SoulCatcherItem::new)
                    .properties(
                            p -> p.stacksTo(1)
                                    .rarity(Rarity.UNCOMMON)
                                    .craftRemainder(EMPTY_SOUL_CATCHER.asItem())
                    )
                    .model(AssetLookup.existingItemModel())
                    .removeTab(ModCreativeModeTabs.CREATE_MOB_SPAWNERS_TAB.getKey())
                    .register();

    public static void init() {
        // load class
    }
}
