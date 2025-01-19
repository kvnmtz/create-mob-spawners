package dev.kvnmtz.createmobspawners.item.registry;

import dev.kvnmtz.createmobspawners.CreateMobSpawners;
import dev.kvnmtz.createmobspawners.item.custom.SoulCatcherItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, CreateMobSpawners.MOD_ID);

    public static final RegistryObject<Item> EMPTY_SOUL_CATCHER = ITEMS.register("empty_soul_catcher", SoulCatcherItem::new);
    public static final RegistryObject<Item> SOUL_CATCHER = ITEMS.register("soul_catcher", SoulCatcherItem::new);

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
