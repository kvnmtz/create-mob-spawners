package dev.kvnmtz.createmobspawners.event;

import dev.kvnmtz.createmobspawners.CreateMobSpawners;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CreateMobSpawners.MOD_ID)
public class EventHandler {

    @SubscribeEvent
    public static void preventInstantDespawning(MobSpawnEvent.AllowDespawn event) {
        var despawnImmunityTicks = CreateMobSpawners.SERVER_CONFIG.mechanicalSpawnerDespawnImmunityTicks.get();
        if (despawnImmunityTicks == 0) {
            return;
        }

        var mob = event.getEntity();
        if (!mob.getPersistentData().getBoolean("FromMechanicalSpawner")) {
            return;
        }

        if (mob.tickCount < despawnImmunityTicks) {
            event.setResult(Event.Result.DENY);
        } else {
            mob.getPersistentData().remove("FromMechanicalSpawner");
        }
    }
}
