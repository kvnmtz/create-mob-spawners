package dev.kvnmtz.createmobspawners.item.custom;

import com.simibubi.create.foundation.item.render.SimpleCustomRenderer;
import dev.kvnmtz.createmobspawners.CreateMobSpawners;
import dev.kvnmtz.createmobspawners.capabilities.entitystorage.EntityStorageItemStackCapabilityProvider;
import dev.kvnmtz.createmobspawners.capabilities.registry.ModCapabilities;
import dev.kvnmtz.createmobspawners.capabilities.entitystorage.IEntityStorage;
import dev.kvnmtz.createmobspawners.capabilities.entitystorage.StoredEntityData;
import dev.kvnmtz.createmobspawners.item.registry.ModItems;
import dev.kvnmtz.createmobspawners.item.renderer.SoulCatcherRenderer;
import dev.kvnmtz.createmobspawners.network.packet.ClientboundEntityReleasePacket;
import dev.kvnmtz.createmobspawners.network.packet.ClientboundEntityCatchPacket;
import dev.kvnmtz.createmobspawners.network.PacketHandler;
import dev.kvnmtz.createmobspawners.utils.BoundingBoxUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.extensions.IForgeItem;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

@Mod.EventBusSubscriber(modid = CreateMobSpawners.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class SoulCatcherItem extends Item implements IForgeItem {
    public SoulCatcherItem(Properties properties) {
        super(properties);
    }

    private static class ShrinkingEntityData {
        private final Player player;
        private final ItemStack itemStack;
        private final long startTime;
        private float nextLineAfterElapsedSeconds;
        private final boolean hadAi;

        public ShrinkingEntityData(Player player, ItemStack itemStack, boolean hadAi) {
            this.player = player;
            this.itemStack = itemStack;
            this.startTime = System.currentTimeMillis();
            this.nextLineAfterElapsedSeconds = CATCHING_LINE_DRAW_DELAY;
            this.hadAi = hadAi;
        }
    }

    /// use on server
    private static final HashMap<LivingEntity, ShrinkingEntityData> shrinkingEntities = new HashMap<>();

    /// use on client
    private static final HashMap<Entity, Long> shrinkingEntitiesToStartTimeMap = new HashMap<>();

    public static void addShrinkingEntity(Entity entity) {
        shrinkingEntitiesToStartTimeMap.put(entity, System.currentTimeMillis());
    }

    public static void removeShrinkingEntity(Entity entity) {
        shrinkingEntitiesToStartTimeMap.remove(entity);
    }

    private static final float CATCHING_LINE_DRAW_DELAY = 0.2f;

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    protected static void onRenderEntity(RenderLivingEvent.Pre<?, ?> event) {
        var entity = event.getEntity();
        if (!shrinkingEntitiesToStartTimeMap.containsKey(entity)) return;

        var startTime = shrinkingEntitiesToStartTimeMap.get(entity);

        var currentTime = System.currentTimeMillis();
        var elapsedTime = (currentTime - startTime) / 1000.0f;

        var scaleFactor = 1.f - (elapsedTime / getCatchingDuration(entity));
        scaleFactor = Math.max(scaleFactor, 0);

        var translationHeight = entity.getBbHeight() / 2;
        event.getPoseStack().translate(0, translationHeight - scaleFactor * translationHeight, 0);
        event.getPoseStack().scale(scaleFactor, scaleFactor, scaleFactor);
    }

    @SubscribeEvent
    protected static void onServerTick(TickEvent.ServerTickEvent event) {
        for (var shrinkingEntity : shrinkingEntities.keySet()) {
            var data = shrinkingEntities.get(shrinkingEntity);
            var player = data.player;
            var itemStack = data.itemStack;

            var currentItem = player.getMainHandItem();

            if (!currentItem.equals(itemStack, true) || !isItemAbleToCatch(currentItem)) {
                cancelCatch(shrinkingEntity);
                continue;
            }
            if (!isEntityCatchable(player, shrinkingEntity, component -> player.displayClientMessage(component, true))) {
                cancelCatch(shrinkingEntity);
                continue;
            }

            var currentTime = System.currentTimeMillis();
            var elapsedTime = (currentTime - data.startTime) / 1000.0f;

            if (elapsedTime >= data.nextLineAfterElapsedSeconds) {
                PacketHandler.sendToNearbyPlayers(new ClientboundEntityCatchPacket(shrinkingEntity.getId(), player.getId(), ClientboundEntityCatchPacket.EntityCatchState.IN_PROGRESS), player.getEyePosition(), 16, player.level().dimension());
                data.nextLineAfterElapsedSeconds += CATCHING_LINE_DRAW_DELAY;
            }

            if (elapsedTime >= getCatchingDuration(shrinkingEntity)) {
                onShrinkComplete(shrinkingEntity);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    protected static void addMobTooltip(ItemTooltipEvent event) {
        var itemStack = event.getItemStack();
        if (itemStack.getItem() != ModItems.SOUL_CATCHER.get()) return;

        var hasEntityData = getEntityData(itemStack).isPresent();
        if (!hasEntityData) return;

        var displayName = getEntityData(itemStack).get().getEntityDisplayName();
        if (displayName.isEmpty()) return;

        event.getToolTip().add(1, Component.literal(displayName.get()).withStyle(ChatFormatting.GRAY));
    }

    @SubscribeEvent
    protected static void onServerClose(ServerStoppingEvent event) {
        for (var shrinkingEntity : shrinkingEntities.keySet()) {
            cancelCatch(shrinkingEntity);
        }
    }

    @SubscribeEvent
    protected static void onPlayerLeave(PlayerEvent.PlayerLoggedOutEvent event) {
        var player = event.getEntity();
        for (var entry : shrinkingEntities.entrySet()) {
            if (player != entry.getValue().player) continue;
            cancelCatch(entry.getKey());
        }
    }

    @SubscribeEvent
    protected static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        var player = event.getEntity();
        for (var entry : shrinkingEntities.entrySet()) {
            if (player != entry.getValue().player) continue;
            cancelCatch(entry.getKey());
        }
    }

    // instead of overriding interactLivingEntity, this event is necessary to block interactions with entities like villagers, wolves, donkeys...
    @SubscribeEvent(priority = EventPriority.LOWEST)
    protected static void onRightClickEntity(PlayerInteractEvent.EntityInteract event) {
        if (event.getLevel().isClientSide) return;

        var itemStack = event.getItemStack();
        if (itemStack.getItem() != ModItems.EMPTY_SOUL_CATCHER.get()) return;

        event.setCancellationResult(InteractionResult.FAIL);
        event.setCanceled(true);

        var player = event.getEntity();
        var targetEntity = event.getTarget();

        if (!(targetEntity instanceof LivingEntity target)) return;

        if (!isItemAbleToCatch(itemStack)) return;
        if (!isEntityCatchable(player, target, component -> player.displayClientMessage(component, true))) return;

        if (shrinkingEntities.containsKey(target)) return;
        for (var shrinkingEntityData : shrinkingEntities.values()) {
            if (shrinkingEntityData.player.equals(player)) {
                return;
            }
        }

        event.setCancellationResult(InteractionResult.SUCCESS);
        startCatchingEntity(target, player, itemStack);
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    protected static void onEntityRemoved(EntityLeaveLevelEvent event) {
        var entity = event.getEntity();
        shrinkingEntitiesToStartTimeMap.remove(entity);
    }

    private static void cancelCatch(LivingEntity entity) {
        var data = shrinkingEntities.get(entity);
        if (data.hadAi && entity instanceof Mob mob) {
            mob.setNoAi(false);
        }

        PacketHandler.sendToAllPlayers(new ClientboundEntityCatchPacket(entity.getId(), data.player.getId(), ClientboundEntityCatchPacket.EntityCatchState.CANCELED));
        shrinkingEntities.remove(entity);
    }

    private static float getCatchingDuration(Entity entity) {
        var boundingBox = entity.getBoundingBox();
        var volume = BoundingBoxUtils.getBoundingBoxVolume(boundingBox);
        return (float) (1.3811 * Math.pow(volume, 0.5026));
    }

    public static int getCatchingDurationInTicks(AABB boundingBox) {
        var volume = BoundingBoxUtils.getBoundingBoxVolume(boundingBox);
        return Math.round((float) (1.3811 * Math.pow(volume, 0.5026)) * 20);
    }

    private static void onShrinkComplete(LivingEntity entity) {
        var data = shrinkingEntities.get(entity);
        if (data == null) return;

        PacketHandler.sendToAllPlayers(new ClientboundEntityCatchPacket(entity.getId(), data.player.getId(), ClientboundEntityCatchPacket.EntityCatchState.FINISHED));

        var newItemStack = catchEntity(data.itemStack, entity, data.hadAi);

        var player = data.player;
        player.setItemInHand(InteractionHand.MAIN_HAND, newItemStack);

        shrinkingEntities.remove(entity);
    }

    private static void startCatchingEntity(LivingEntity entity, Player player, ItemStack itemStack) {
        var hadAi = false;
        if (entity instanceof Mob mob) {
            hadAi = !mob.isNoAi();
        }
        shrinkingEntities.put(entity, new ShrinkingEntityData(player, itemStack, hadAi));

        if (entity instanceof Mob mob) {
            mob.setNoAi(true);
        }

        PacketHandler.sendToAllPlayers(new ClientboundEntityCatchPacket(entity.getId(), player.getId(), ClientboundEntityCatchPacket.EntityCatchState.STARTED));
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        if (context.getLevel().isClientSide) {
            return InteractionResult.FAIL;
        }

        if (context.getItemInHand().getItem() != ModItems.SOUL_CATCHER.get()) return InteractionResult.FAIL;

        var player = context.getPlayer();
        if (player == null) {
            return InteractionResult.FAIL;
        }

        var optEntity = releaseEntity(context.getLevel(), context.getItemInHand(), context.getClickedFace(), context.getClickedPos(), emptyCatcher -> player.setItemInHand(context.getHand(), emptyCatcher), component -> player.displayClientMessage(component, true));
        optEntity.ifPresent(entity -> PacketHandler.sendToNearbyPlayers(new ClientboundEntityReleasePacket(entity.getId(), player.getId()), entity.position(), 16, entity.level().dimension()));

        return InteractionResult.SUCCESS;
    }

    public enum CapturableStatus {
        CAPTURABLE(Component.empty()),
        BOSS(Component.translatable("item.create_mob_spawners.empty_soul_catcher.capturable_status.boss").withStyle(ChatFormatting.RED)),
        BLACKLISTED(Component.translatable("item.create_mob_spawners.empty_soul_catcher.capturable_status.blacklisted").withStyle(ChatFormatting.RED)),
        INCOMPATIBLE(Component.translatable("item.create_mob_spawners.empty_soul_catcher.capturable_status.incompatible").withStyle(ChatFormatting.RED));

        CapturableStatus(Component errorMessage) {
            this.errorMessage = errorMessage;
        }

        private final Component errorMessage;

        public Component errorMessage() {
            return errorMessage;
        }
    }

    private static CapturableStatus getCapturableStatus(EntityType<? extends LivingEntity> type, @Nullable Entity entity) {
        if (entity != null && isBlacklistedBoss(entity)) {
            return CapturableStatus.BOSS;
        }

        if (!type.canSerialize()) {
            return CapturableStatus.INCOMPATIBLE;
        }

        if (CreateMobSpawners.SERVER_CONFIG.soulCatcherEntityBlacklist.get().contains(EntityType.getKey(type).toString())) {
            return CapturableStatus.BLACKLISTED;
        }

        return CapturableStatus.CAPTURABLE;
    }

    public static boolean isBlacklistedBoss(Entity entity) {
        return entity.getType().is(Tags.EntityTypes.BOSSES);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private static boolean isItemAbleToCatch(ItemStack soulCatcher) {
        var entityData = getEntityData(soulCatcher);
        return entityData.isEmpty() || entityData.get().getEntityTypeResourceLocation().isEmpty();
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private static boolean isEntityCatchable(Player player, LivingEntity entity, Consumer<Component> displayCallback) {
        final var capturableStatusKeyPrefix = "item.create_mob_spawners.empty_soul_catcher.capturable_status.";

        if (entity instanceof Player) {
            displayCallback.accept(Component.translatable(capturableStatusKeyPrefix + "player").withStyle(ChatFormatting.RED));
            return false;
        }

        //noinspection unchecked
        var status = getCapturableStatus((EntityType<? extends LivingEntity>) entity.getType(), entity);
        if (status != CapturableStatus.CAPTURABLE) {
            displayCallback.accept(status.errorMessage());
            return false;
        }

        if (!entity.hasEffect(MobEffects.WEAKNESS)) {
            displayCallback.accept(Component.translatable(capturableStatusKeyPrefix + "no_weakness").withStyle(ChatFormatting.RED));
            return false;
        }

        if (!entity.isAlive()) {
            displayCallback.accept(Component.translatable(capturableStatusKeyPrefix + "dead").withStyle(ChatFormatting.RED));
            return false;
        }

        if (entity.distanceTo(player) > CreateMobSpawners.SERVER_CONFIG.soulCatcherMaxDistance.get()) {
            displayCallback.accept(Component.translatable(capturableStatusKeyPrefix + "too_far").withStyle(ChatFormatting.RED));
            return false;
        }

        var maxHealthPercentage = CreateMobSpawners.SERVER_CONFIG.soulCatcherMaxHealthPercentage.get().floatValue();
        if (maxHealthPercentage != 1.f) {
            var health = entity.getHealth();
            if (maxHealthPercentage == 0.f) {
                var hasHalfHeartLeft = health == 1.f;
                if (!hasHalfHeartLeft) {
                    displayCallback.accept(Component.translatable(capturableStatusKeyPrefix + "too_much_health").withStyle(ChatFormatting.RED));
                    return false;
                }
            } else {
                var percentageLeft = health / entity.getMaxHealth();
                if (percentageLeft > maxHealthPercentage) {
                    displayCallback.accept(Component.translatable(capturableStatusKeyPrefix + "too_much_health").withStyle(ChatFormatting.RED));
                    return false;
                }
            }
        }

        return true;
    }

    private static ItemStack catchEntity(ItemStack itemStack, LivingEntity entity, boolean hadAi) {
        if (entity instanceof Mob mob) {
            if (mob.getLeashHolder() != null) {
                mob.dropLeash(true, true);
            }
            if (hadAi) {
                mob.setNoAi(false);
            }
        }

        itemStack.shrink(1);
        var catcher = ModItems.SOUL_CATCHER.get().getDefaultInstance();
        setEntityData(catcher, entity);

        entity.discard();

        return catcher;
    }

    private static Optional<Entity> releaseEntity(Level level, ItemStack catcher, Direction face, BlockPos pos, Consumer<ItemStack> emptyCatcherSetter, Consumer<Component> displayCallback) {
        var spawnedEntity = new AtomicReference<Optional<Entity>>(Optional.empty());

        catcher.getCapability(ModCapabilities.ENTITY_STORAGE).ifPresent(entityStorage -> {
            if (entityStorage.hasStoredEntity()) {
                var entityData = entityStorage.getStoredEntityData();

                var spawnX = pos.getX() + face.getStepX() + 0.5;
                var spawnY = pos.getY() + face.getStepY();
                var spawnZ = pos.getZ() + face.getStepZ() + 0.5;

                var rotation = Mth.wrapDegrees(level.getRandom().nextFloat() * 360.0f);

                var optEntityType = entityData.getEntityType();
                if (optEntityType.isEmpty()) return;

                var entityType = optEntityType.get();
                if (!level.noCollision(entityType.getAABB(spawnX, spawnY, spawnZ))) {
                    displayCallback.accept(Component.translatable("item.create_mob_spawners.soul_catcher.no_space").withStyle(ChatFormatting.RED));
                    return;
                }

                var optEntity = EntityType.create(entityData.getEntityTag(), level);
                optEntity.ifPresent(ent -> {
                    ent.setPos(spawnX, spawnY, spawnZ);
                    ent.setYRot(rotation);
                    level.addFreshEntity(ent);
                    emptyCatcherSetter.accept(ModItems.EMPTY_SOUL_CATCHER.get().getDefaultInstance());
                    spawnedEntity.set(optEntity);
                });
            }
        });

        return spawnedEntity.get();
    }

    private static void setEntityData(ItemStack stack, LivingEntity entity) {
        stack.getCapability(ModCapabilities.ENTITY_STORAGE).ifPresent(storage -> storage.setStoredEntityData(StoredEntityData.of(entity)));
    }

    public static Optional<StoredEntityData> getEntityData(ItemStack stack) {
        return stack.getCapability(ModCapabilities.ENTITY_STORAGE).map(IEntityStorage::getStoredEntityData);
    }

    @Override
    public @Nullable ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new EntityStorageItemStackCapabilityProvider(stack);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(SimpleCustomRenderer.create(this, new SoulCatcherRenderer()));
    }
}
