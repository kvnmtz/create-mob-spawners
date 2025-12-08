package dev.kvnmtz.createmobspawners.common.item.registry;

import dev.kvnmtz.createmobspawners.CreateMobSpawnersMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public abstract class ModCreativeModeTabs {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS
            = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CreateMobSpawnersMod.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> CREATE_MOB_SPAWNERS_TAB =
            CREATIVE_MODE_TABS.register("create_mob_spawners_tab", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup." + CreateMobSpawnersMod.MOD_ID + ".main"))
                    .icon(() -> ModItems.EMPTY_SOUL_CATCHER.get().getDefaultInstance())
                    .build()
            );

    public static void init(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
