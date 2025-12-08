package dev.kvnmtz.createmobspawners.client;

import com.simibubi.create.foundation.item.render.SimpleCustomRenderer;
import dev.kvnmtz.createmobspawners.CreateMobSpawnersMod;
import dev.kvnmtz.createmobspawners.client.item.renderer.SoulCatcherRenderer;
import dev.kvnmtz.createmobspawners.client.ponder.ModPonderPlugin;
import dev.kvnmtz.createmobspawners.common.item.registry.ModItems;
import net.createmod.ponder.foundation.PonderIndex;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(modid = CreateMobSpawnersMod.MOD_ID, value = Dist.CLIENT)
public abstract class CreateMobSpawnersModClient {

    @SubscribeEvent
    public static void onRegisterClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerItem(
                SimpleCustomRenderer.create(ModItems.SOUL_CATCHER.get(), new SoulCatcherRenderer()),
                ModItems.SOUL_CATCHER
        );
        event.registerItem(
                SimpleCustomRenderer.create(ModItems.EMPTY_SOUL_CATCHER.get(), new SoulCatcherRenderer()),
                ModItems.EMPTY_SOUL_CATCHER
        );
    }

    public static void init() {
        PonderIndex.addPlugin(new ModPonderPlugin());
    }
}
