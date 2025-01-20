package dev.kvnmtz.createmobspawners.item.registry;

import dev.kvnmtz.createmobspawners.CreateMobSpawners;
import dev.kvnmtz.createmobspawners.block.registry.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CreateMobSpawners.MOD_ID);

    public static final RegistryObject<CreativeModeTab> CREATE_MOB_SPAWNERS_TAB =
            CREATIVE_MODE_TABS.register("create_mob_spawners_tab", () -> CreativeModeTab.builder()
                    .icon(() -> ModItems.EMPTY_SOUL_CATCHER.get().getDefaultInstance())
                    .title(Component.translatable("creativetab.create_mob_spawners_tab"))
                    .displayItems((itemDisplayParameters, output) -> {
                        output.accept(ModItems.EMPTY_SOUL_CATCHER.get());
                        output.accept(ModBlocks.MECHANICAL_SPAWNER.get());
                    }).build()
            );

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
