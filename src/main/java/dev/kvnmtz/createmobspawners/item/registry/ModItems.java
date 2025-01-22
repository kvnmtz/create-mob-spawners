package dev.kvnmtz.createmobspawners.item.registry;

import com.simibubi.create.foundation.data.AssetLookup;
import com.tterrag.registrate.util.entry.ItemEntry;
import dev.kvnmtz.createmobspawners.item.custom.SoulCatcherItem;
import net.minecraft.world.item.Rarity;

import java.util.Objects;

import static dev.kvnmtz.createmobspawners.CreateMobSpawners.REGISTRATE;

public class ModItems {
    public static final ItemEntry<SoulCatcherItem> EMPTY_SOUL_CATCHER =
            REGISTRATE.item("empty_soul_catcher", SoulCatcherItem::new)
                    .properties(p -> p.stacksTo(1).rarity(Rarity.UNCOMMON))
                    .model(AssetLookup.existingItemModel())
                    .tab(Objects.requireNonNull(ModCreativeModeTabs.CREATE_MOB_SPAWNERS_TAB.getKey()))
                    .register();

    public static final ItemEntry<SoulCatcherItem> SOUL_CATCHER =
            REGISTRATE.item("soul_catcher", SoulCatcherItem::new)
                    .properties(p -> p.stacksTo(1).rarity(Rarity.UNCOMMON))
                    .model(AssetLookup.existingItemModel())
                    .register();

    public static void register() {
        // init class
    }
}
