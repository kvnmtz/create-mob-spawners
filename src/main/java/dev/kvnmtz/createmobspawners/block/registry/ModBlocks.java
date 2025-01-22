package dev.kvnmtz.createmobspawners.block.registry;

import com.simibubi.create.foundation.data.SharedProperties;
import com.tterrag.registrate.util.entry.BlockEntry;
import dev.kvnmtz.createmobspawners.block.custom.MechanicalSpawnerBlock;
import dev.kvnmtz.createmobspawners.item.registry.ModCreativeModeTabs;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.SoundType;

import static com.simibubi.create.foundation.data.TagGen.pickaxeOnly;
import static dev.kvnmtz.createmobspawners.CreateMobSpawners.REGISTRATE;

public class ModBlocks {
    static {
        REGISTRATE.setCreativeTab(ModCreativeModeTabs.CREATE_MOB_SPAWNERS_TAB);
    }

    public static final BlockEntry<MechanicalSpawnerBlock> MECHANICAL_SPAWNER = REGISTRATE.block("mechanical_spawner", MechanicalSpawnerBlock::new)
            .initialProperties(SharedProperties::netheriteMetal)
            .properties(p -> p.strength(10.f).sound(SoundType.METAL).noOcclusion())
            .transform(pickaxeOnly())
            .item((block, properties) -> new BlockItem(block, properties.rarity(Rarity.RARE)))
            .build()
            .register();

    public static void register() {
        // init class
    }
}
