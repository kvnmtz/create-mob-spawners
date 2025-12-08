package dev.kvnmtz.createmobspawners.client.item;

import dev.kvnmtz.createmobspawners.CreateMobSpawnersMod;
import dev.kvnmtz.createmobspawners.common.item.SoulCatcherItem;
import dev.kvnmtz.createmobspawners.common.item.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLivingEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.util.HashMap;
import java.util.Objects;

@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(modid = CreateMobSpawnersMod.MOD_ID, value = Dist.CLIENT)
public abstract class SoulCatcherItemClient {

    private static final HashMap<Entity, Long> shrinkingEntitiesToStartTimeMap = new HashMap<>();

    public static void addShrinkingEntity(Entity entity) {
        shrinkingEntitiesToStartTimeMap.put(entity, System.currentTimeMillis());
    }

    public static void removeShrinkingEntity(Entity entity) {
        shrinkingEntitiesToStartTimeMap.remove(entity);
    }

    @SubscribeEvent
    public static void onRenderEntity(RenderLivingEvent.Pre<?, ?> event) {
        var entity = event.getEntity();
        if (!shrinkingEntitiesToStartTimeMap.containsKey(entity)) return;

        var startTime = shrinkingEntitiesToStartTimeMap.get(entity);

        var currentTime = System.currentTimeMillis();
        var elapsedTime = (currentTime - startTime) / 1000.0f;

        var scaleFactor = 1.f - (elapsedTime / SoulCatcherItem.getCatchingDuration(entity));
        scaleFactor = Math.max(scaleFactor, 0);

        var translationHeight = entity.getBbHeight() / 2;
        event.getPoseStack().translate(0, translationHeight - scaleFactor * translationHeight, 0);
        event.getPoseStack().scale(scaleFactor, scaleFactor, scaleFactor);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void addMobTooltip(ItemTooltipEvent event) {
        var stack = event.getItemStack();
        if (!stack.is(ModItems.SOUL_CATCHER.get())) {
            return;
        }

        if (!stack.has(DataComponents.ENTITY_DATA)) {
            return;
        }

        var data = Objects.requireNonNull(stack.get(DataComponents.ENTITY_DATA));
        var entityTag = data.copyTag();

        var optEntityType = EntityType.by(entityTag);
        if (optEntityType.isEmpty()) {
            return;
        }

        var entityType = optEntityType.get();
        var displayName = entityType.getDescription().getString();

        event.getToolTip().add(1, Component.literal(displayName).withStyle(ChatFormatting.GRAY));
    }

    @SubscribeEvent
    public static void onEntityRemoved(EntityLeaveLevelEvent event) {
        var entity = event.getEntity();
        shrinkingEntitiesToStartTimeMap.remove(entity);
    }
}
