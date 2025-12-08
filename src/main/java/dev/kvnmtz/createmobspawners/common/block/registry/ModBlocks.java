package dev.kvnmtz.createmobspawners.common.block.registry;

import com.simibubi.create.foundation.data.SharedProperties;
import com.tterrag.registrate.util.entry.BlockEntry;
import dev.kvnmtz.createmobspawners.common.block.MechanicalSpawnerBlock;
import dev.kvnmtz.createmobspawners.common.item.registry.ModCreativeModeTabs;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.SoundType;

import static com.simibubi.create.foundation.data.TagGen.pickaxeOnly;
import static dev.kvnmtz.createmobspawners.CreateMobSpawnersMod.REGISTRATE;

public abstract class ModBlocks {

    public static final BlockEntry<MechanicalSpawnerBlock> MECHANICAL_SPAWNER =
            REGISTRATE.block("mechanical_spawner", MechanicalSpawnerBlock::new)
                    .initialProperties(SharedProperties::netheriteMetal)
                    .properties(p -> p.strength(10.f).sound(SoundType.METAL).noOcclusion())
                    .transform(pickaxeOnly())
                    .blockstate((context, provider) -> {}) // dont datagen defaults
                    .item((block, properties) -> new BlockItem(block, properties.rarity(Rarity.RARE)))
                        .model((context, provider) -> {}) // dont datagen defaults
                        .build()
                    .register();

    public static void init() {
        // load class
    }
}
