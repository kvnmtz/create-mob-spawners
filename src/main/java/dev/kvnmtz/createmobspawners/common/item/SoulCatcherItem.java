package dev.kvnmtz.createmobspawners.common.item;

import dev.kvnmtz.createmobspawners.CreateMobSpawnersMod;
import dev.kvnmtz.createmobspawners.common.config.ModServerConfig;
import dev.kvnmtz.createmobspawners.common.item.registry.ModItems;
import dev.kvnmtz.createmobspawners.common.network.packet.ClientboundCatchEntityPacket;
import dev.kvnmtz.createmobspawners.common.network.packet.ClientboundReleaseEntityPacket;
import dev.kvnmtz.createmobspawners.common.util.BoundingBoxUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

@EventBusSubscriber(modid = CreateMobSpawnersMod.MOD_ID)
public class SoulCatcherItem extends Item {

    public SoulCatcherItem(Properties properties) {
        super(properties);
    }

    private static final float CATCHING_LINE_DRAW_DELAY = 0.2f;

    private static final HashMap<LivingEntity, ShrinkingEntityData> shrinkingEntities = new HashMap<>();

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Pre event) {
        for (var shrinkingEntity : shrinkingEntities.keySet()) {
            var data = shrinkingEntities.get(shrinkingEntity);
            var player = data.player;
            var itemStack = data.itemStack;

            var currentItem = player.getMainHandItem();

            if (currentItem.getItem() != ModItems.EMPTY_SOUL_CATCHER.get() || !ItemStack.matches(currentItem, itemStack) || !isItemAbleToCatch(currentItem)) {
                cancelCatch(shrinkingEntity);
                continue;
            }
            if (!isEntityCatchable(player, shrinkingEntity, component -> player.displayClientMessage(component,
                    true))) {
                cancelCatch(shrinkingEntity);
                continue;
            }

            var currentTime = System.currentTimeMillis();
            var elapsedTime = (currentTime - data.startTime) / 1000.0f;

            if (elapsedTime >= data.nextLineAfterElapsedSeconds) {
                var position = player.getEyePosition();
                PacketDistributor.sendToPlayersNear((ServerLevel) player.level(), null, position.x, position.y,
                        position.z, 16, new ClientboundCatchEntityPacket(shrinkingEntity.getId(), player.getId(),
                                ClientboundCatchEntityPacket.EntityCatchState.IN_PROGRESS));
                data.nextLineAfterElapsedSeconds += CATCHING_LINE_DRAW_DELAY;
            }

            if (elapsedTime >= getCatchingDuration(shrinkingEntity)) {
                onShrinkComplete(shrinkingEntity);
            }
        }
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
            if (player != entry.getValue().player) {
                continue;
            }

            cancelCatch(entry.getKey());
        }
    }

    @SubscribeEvent
    protected static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        var player = event.getEntity();
        for (var entry : shrinkingEntities.entrySet()) {
            if (player != entry.getValue().player) {
                continue;
            }

            cancelCatch(entry.getKey());
        }
    }

    // instead of overriding interactLivingEntity, this event is necessary to block interactions with entities like
    // villagers, wolves, donkeys...
    @SubscribeEvent(priority = EventPriority.LOWEST)
    protected static void onRightClickEntity(PlayerInteractEvent.EntityInteract event) {
        if (event.getLevel().isClientSide) {
            return;
        }

        var itemStack = event.getItemStack();
        if (itemStack.getItem() != ModItems.EMPTY_SOUL_CATCHER.get()) {
            return;
        }

        event.setCancellationResult(InteractionResult.FAIL);
        event.setCanceled(true);

        var player = (ServerPlayer) event.getEntity();
        var targetEntity = event.getTarget();

        if (!(targetEntity instanceof LivingEntity target)) {
            return;
        }

        if (!isItemAbleToCatch(itemStack)) {
            return;
        }
        if (!isEntityCatchable(player, target, component -> player.displayClientMessage(component, true))) {
            return;
        }

        if (shrinkingEntities.containsKey(target)) {
            return;
        }
        for (var shrinkingEntityData : shrinkingEntities.values()) {
            if (shrinkingEntityData.player.equals(player)) {
                return;
            }
        }

        event.setCancellationResult(InteractionResult.SUCCESS);
        startCatchingEntity(target, player, itemStack);
    }

    private static void cancelCatch(LivingEntity entity) {
        var data = shrinkingEntities.get(entity);
        if (data.hadAi && entity instanceof Mob mob) {
            mob.setNoAi(false);
        }

        PacketDistributor.sendToAllPlayers(new ClientboundCatchEntityPacket(entity.getId(), data.player.getId(),
                ClientboundCatchEntityPacket.EntityCatchState.CANCELED));
        shrinkingEntities.remove(entity);
    }

    private static void onShrinkComplete(LivingEntity entity) {
        var data = shrinkingEntities.get(entity);
        if (data == null) {
            return;
        }

        PacketDistributor.sendToAllPlayers(new ClientboundCatchEntityPacket(entity.getId(), data.player.getId(),
                ClientboundCatchEntityPacket.EntityCatchState.FINISHED));

        var player = data.player;
        var currentItem = player.getMainHandItem();

        var newItemStack = catchEntity(currentItem, entity, data.hadAi);

        if (currentItem.isEmpty()) {
            player.setItemInHand(InteractionHand.MAIN_HAND, newItemStack);
        } else {
            if (!player.getInventory().add(newItemStack)) {
                player.drop(newItemStack, false);
            }
        }

        shrinkingEntities.remove(entity);
    }

    private static void startCatchingEntity(LivingEntity entity, ServerPlayer player, ItemStack itemStack) {
        var hadAi = false;
        if (entity instanceof Mob mob) {
            hadAi = !mob.isNoAi();
        }
        shrinkingEntities.put(entity, new ShrinkingEntityData(player, itemStack, hadAi));

        if (entity instanceof Mob mob) {
            mob.setNoAi(true);
        }

        PacketDistributor.sendToAllPlayers(new ClientboundCatchEntityPacket(entity.getId(), player.getId(),
                ClientboundCatchEntityPacket.EntityCatchState.STARTED));
    }

    @Override
    public @NotNull InteractionResult useOn(@NotNull UseOnContext context) {
        if (!(context.getLevel() instanceof ServerLevel serverLevel)) {
            return InteractionResult.FAIL;
        }

        if (context.getItemInHand().getItem() != ModItems.SOUL_CATCHER.get()) {
            return InteractionResult.FAIL;
        }

        var player = context.getPlayer();
        if (player == null) {
            return InteractionResult.FAIL;
        }

        var optEntity = releaseEntity(
                context.getLevel(),
                context.getItemInHand(),
                context.getClickedFace(),
                context.getClickedPos(),
                emptyCatcher -> player.setItemInHand(context.getHand(), emptyCatcher),
                component -> player.displayClientMessage(component, true)
        );
        optEntity.ifPresent(entity -> {
            var pos = entity.position();
            PacketDistributor.sendToPlayersNear(serverLevel, null, pos.x, pos.y, pos.z, 16,
                    new ClientboundReleaseEntityPacket(entity.getId(), player.getId()));
        });

        return InteractionResult.SUCCESS;
    }

    public static float getCatchingDuration(Entity entity) {
        var boundingBox = entity.getBoundingBox();
        var volume = BoundingBoxUtils.getBoundingBoxVolume(boundingBox);
        return (float) (1.3811 * Math.pow(volume, 0.5026));
    }

    public static int getCatchingDurationInTicks(AABB boundingBox) {
        var volume = BoundingBoxUtils.getBoundingBoxVolume(boundingBox);
        return Math.round((float) (1.3811 * Math.pow(volume, 0.5026)) * 20);
    }

    private static CapturableStatus getCapturableStatus(EntityType<? extends LivingEntity> type,
                                                        @Nullable Entity entity) {
        if (entity != null && isBoss(entity) && !ModServerConfig.CONFIG.soulCatcherAllowBosses.get()) {
            return CapturableStatus.BOSS;
        }

        if (!type.canSerialize()) {
            return CapturableStatus.INCOMPATIBLE;
        }

        if (ModServerConfig.CONFIG.soulCatcherEntityBlacklist.get().contains(EntityType.getKey(type).toString())) {
            return CapturableStatus.BLACKLISTED;
        }

        return CapturableStatus.CAPTURABLE;
    }

    private static boolean isBoss(Entity entity) {
        return entity.getType().is(Tags.EntityTypes.BOSSES);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private static boolean isItemAbleToCatch(ItemStack stack) {
        return !stack.has(DataComponents.ENTITY_DATA);
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

        /*
        Bosses cannot be weakened -> check needs to depend on their remaining health

        To make this more robust, instead of checking isBoss, a pseudo addEffect could be called -> bosses just
        return false here
        (if it returned true, the effect has to be removed again or restored to whatever the previous state was)

        As this is just for a very niche config option (allowing bosses to be captured), this should be enough for now
        */
        var isBoss = isBoss(entity);
        if (ModServerConfig.CONFIG.soulCatcherRequireWeakness.get() && !entity.hasEffect(MobEffects.WEAKNESS) && !isBoss) {
            displayCallback.accept(Component.translatable(capturableStatusKeyPrefix + "no_weakness")
                    .withStyle(ChatFormatting.RED));
            return false;
        }

        if (!entity.isAlive()) {
            displayCallback.accept(Component.translatable(capturableStatusKeyPrefix + "dead")
                    .withStyle(ChatFormatting.RED));
            return false;
        }

        if (entity.distanceTo(player) > ModServerConfig.CONFIG.soulCatcherMaxDistance.get()) {
            displayCallback.accept(Component.translatable(capturableStatusKeyPrefix + "too_far")
                    .withStyle(ChatFormatting.RED));
            return false;
        }

        var maxHealthPercentage = isBoss ?
                ModServerConfig.CONFIG.soulCatcherBossMaxHealthPercentage.get().floatValue() :
                ModServerConfig.CONFIG.soulCatcherMaxHealthPercentage.get().floatValue();
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

    private static ItemStack catchEntity(ItemStack stack, LivingEntity entity, boolean hadAi) {
        var encodeId = entity.getEncodeId();
        if (entity.getEncodeId() == null) {
            CreateMobSpawnersMod.LOGGER.error("Tried to catch an entity without an encodeId");
            return stack;
        }

        if (entity instanceof Mob mob) {
            if (mob.getLeashHolder() != null) {
                mob.dropLeash(true, true);
            }
            if (hadAi) {
                mob.setNoAi(false);
            }
        }

        stack.shrink(1);
        var catcher = ModItems.SOUL_CATCHER.get().getDefaultInstance();

        var entityTag = entity.saveWithoutId(new CompoundTag());
        entityTag.putString("id", Objects.requireNonNull(encodeId));
        var data = CustomData.of(entityTag);
        catcher.set(DataComponents.ENTITY_DATA, data);

        entity.discard();

        return catcher;
    }

    private static Optional<Entity> releaseEntity(Level level, ItemStack catcher, Direction face, BlockPos pos,
                                                  Consumer<ItemStack> emptyCatcherSetter,
                                                  Consumer<Component> displayCallback) {
        Optional<Entity> optSpawnedEntity = Optional.empty();

        if (!catcher.has(DataComponents.ENTITY_DATA)) {
            return optSpawnedEntity;
        }

        var data = Objects.requireNonNull(catcher.get(DataComponents.ENTITY_DATA));
        var entityTag = data.copyTag();

        var optEntityType = EntityType.by(entityTag);
        if (optEntityType.isEmpty()) {
            return optSpawnedEntity;
        }

        var spawnX = pos.getX() + face.getStepX() + 0.5;
        var spawnY = pos.getY() + face.getStepY();
        var spawnZ = pos.getZ() + face.getStepZ() + 0.5;

        var rotation = Mth.wrapDegrees(level.getRandom().nextFloat() * 360.0f);

        var entityType = optEntityType.get();
        if (!level.noCollision(entityType.getSpawnAABB(spawnX, spawnY, spawnZ))) {
            displayCallback.accept(Component.translatable("item.create_mob_spawners.soul_catcher.no_space").withStyle(ChatFormatting.RED));
            return optSpawnedEntity;
        }

        optSpawnedEntity = EntityType.create(entityTag, level);
        if (optSpawnedEntity.isEmpty()) {
            return optSpawnedEntity;
        }

        var entity = optSpawnedEntity.get();
        entity.setPos(spawnX, spawnY, spawnZ);
        entity.setYRot(rotation);
        level.addFreshEntity(entity);
        emptyCatcherSetter.accept(ModItems.EMPTY_SOUL_CATCHER.get().getDefaultInstance());

        return optSpawnedEntity;
    }

    public enum CapturableStatus {
        CAPTURABLE(Component.empty()),
        BOSS
                (Component.translatable(
                        "item.create_mob_spawners.empty_soul_catcher.capturable_status.boss"
                ).withStyle(ChatFormatting.RED)),
        BLACKLISTED
                (Component.translatable(
                        "item.create_mob_spawners.empty_soul_catcher.capturable_status.blacklisted"
                ).withStyle(ChatFormatting.RED)),
        INCOMPATIBLE
                (Component.translatable(
                        "item.create_mob_spawners.empty_soul_catcher.capturable_status.incompatible"
                ).withStyle(ChatFormatting.RED));

        CapturableStatus(Component errorMessage) {
            this.errorMessage = errorMessage;
        }

        private final Component errorMessage;

        public Component errorMessage() {
            return errorMessage;
        }
    }

    private static class ShrinkingEntityData {
        private final ServerPlayer player;
        private final ItemStack itemStack;
        private final long startTime;
        private float nextLineAfterElapsedSeconds;
        private final boolean hadAi;

        public ShrinkingEntityData(ServerPlayer player, ItemStack itemStack, boolean hadAi) {
            this.player = player;
            this.itemStack = itemStack;
            this.startTime = System.currentTimeMillis();
            this.nextLineAfterElapsedSeconds = CATCHING_LINE_DRAW_DELAY;
            this.hadAi = hadAi;
        }
    }
}
