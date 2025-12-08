package dev.kvnmtz.createmobspawners.common.block.entity;

import com.simibubi.create.content.fluids.potion.PotionFluid;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import dev.kvnmtz.createmobspawners.CreateMobSpawnersMod;
import dev.kvnmtz.createmobspawners.common.block.MechanicalSpawnerBlock;
import dev.kvnmtz.createmobspawners.common.block.registry.ModBlockEntities;
import dev.kvnmtz.createmobspawners.common.config.ModServerConfig;
import dev.kvnmtz.createmobspawners.common.item.registry.ModItems;
import dev.kvnmtz.createmobspawners.common.network.packet.ClientboundEntityGlowPacket;
import dev.kvnmtz.createmobspawners.common.network.packet.ClientboundSpawnerEventPacket;
import dev.kvnmtz.createmobspawners.common.recipe.SpawningRecipe;
import dev.kvnmtz.createmobspawners.common.recipe.registry.ModRecipes;
import dev.kvnmtz.createmobspawners.common.util.DropUtils;
import dev.kvnmtz.createmobspawners.common.util.ParticleUtils;
import net.createmod.catnip.animation.LerpedFloat;
import net.createmod.catnip.outliner.Outliner;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Optional;

@ParametersAreNonnullByDefault
@EventBusSubscriber(modid = CreateMobSpawnersMod.MOD_ID)
public class MechanicalSpawnerBlockEntity extends KineticBlockEntity {

    private @NotNull CompoundTag storedEntity = new CompoundTag();
    private SmartFluidTankBehaviour tank;
    private float spawnProgress = 0.0F;
    private float stressImpact = 8.0F;
    private int delayTicks = -1;
    private DelayReason delayReason;
    private int spawningAreaWidth;
    private int spawningAreaHeight;
    private int spawningAreaHeightOffset;
    private int spawnAreaHighlightingTicks = 0;
    private int lingerWidth;
    private int lingerHeight;
    private int lingerHeightOffset;
    private final Object spawnAreaOutlineSlot = new Object();

    public MechanicalSpawnerBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
        spawningAreaWidth = ModServerConfig.CONFIG.mechanicalSpawnerAreaDefaultWidth.get();
        spawningAreaHeight = ModServerConfig.CONFIG.mechanicalSpawnerAreaDefaultHeight.get();
        spawningAreaHeightOffset = ModServerConfig.CONFIG.mechanicalSpawnerAreaDefaultHeightOffset.get();
    }

    @Override
    public void tick() {
        super.tick();

        if (level == null) {
            return;
        }

        if (level.isClientSide && spawnAreaHighlightingTicks > 0) {
            highlightSpawningArea();
            spawnAreaHighlightingTicks--;
        }

        var unableToProgress = getPotentialStallingReason().isPresent();
        if (unableToProgress) {
            return;
        }

        if (level.isClientSide) {
            var optColor = getParticleColor();
            if (optColor.isPresent()) {
                var color = optColor.get();
                ParticleUtils.drawPotionEffectParticles(level, getRenderBoundingBox().inflate(0.25), color, 1);
            }
            return;
        }

        if (isVirtual()) {
            return;
        }

        if (delayTicks != -1) {
            delayTicks--;
            if (delayTicks == 0) {
                delayTicks = -1;
                handleSpawning();
            }
        } else {
            addProgressForTick();
            if (spawnProgress >= 1.f) {
                handleSpawning();
            }
        }
    }

    private void setStoredEntity(CompoundTag storedEntity, boolean needsSave) {
        if (this.storedEntity == storedEntity) {
            return;
        }

        this.storedEntity = storedEntity;

        stressImpact = calculateStressImpactForContainedSoulCatcher();
        spawnProgress = 0.0F;

        if (needsSave) {
            notifyUpdate();
        }
    }

    public void setStoredEntity(CompoundTag storedEntity) {
        setStoredEntity(storedEntity, true);
    }

    private void clearStoredEntity() {
        setStoredEntity(new CompoundTag());
    }

    public void ejectSoulCatcher() {
        ejectSoulCatcher(null);
    }

    public void ejectSoulCatcher(@Nullable ServerPlayer player) {
        if (storedEntity.isEmpty()) {
            return;
        }

        if (level == null || (level.isClientSide && !isVirtual())) {
            return;
        }

        var stack = ModItems.SOUL_CATCHER.asStack();
        var data = CustomData.of(storedEntity);
        stack.set(DataComponents.ENTITY_DATA, data);

        var droppedItems = DropUtils.dropItemStack(level, worldPosition.getX(), worldPosition.getY() + 1,
                worldPosition.getZ(), stack);

        if (player != null) {
            droppedItems.forEach(itemEntity -> PacketDistributor.sendToPlayer(player,
                    new ClientboundEntityGlowPacket(itemEntity.getId())));
        } else {
            droppedItems.forEach(itemEntity -> itemEntity.setGlowingTag(true));
        }

        clearStoredEntity();
    }

    private void addProgressForTick() {
        if (spawnProgress >= 1.f) {
            return;
        }

        var optRecipe = getCurrentRecipe();
        if (optRecipe.isEmpty()) {
            return;
        }

        var recipe = optRecipe.get();
        spawnProgress += getProgressForTick(speed, recipe.spawnTicksAtMaxSpeed());
    }

    private void delay(int ticks, DelayReason reason) {
        delayTicks = ticks;
        delayReason = reason;
    }

    private void useFluid() {
        var optRecipe = getCurrentRecipe();
        if (optRecipe.isEmpty()) {
            return;
        }

        var recipe = optRecipe.get();
        tank.getPrimaryHandler().drain(recipe.fluid().getFluids()[0], IFluidHandler.FluidAction.EXECUTE);
        notifyUpdate();
    }

    private void handleSpawning() {
        if (level == null || !(level instanceof ServerLevel serverLevel)) {
            return;
        }

        var optRecipe = getCurrentRecipe();
        if (optRecipe.isEmpty()) {
            return;
        }

        var recipe = optRecipe.get();
        var additionalSpawnAttempts = recipe.additionalSpawnAttempts();

        var result = trySpawnEntity();
        if (result instanceof EntitySpawnResult.Success successfulResult) {
            useFluid();
            spawnProgress = 0;

            var center = getBlockPos().getCenter();
            PacketDistributor.sendToPlayersNear(serverLevel, null, center.x, center.y, center.z, 16,
                    new ClientboundSpawnerEventPacket(getBlockPos(), successfulResult.getEntity().getId()));

            for (var i = 0; i < additionalSpawnAttempts; i++) {
                var subsequentResult = trySpawnEntity();
                if (subsequentResult instanceof EntitySpawnResult.Success subsequentSuccessfulResult) {
                    PacketDistributor.sendToPlayersNear(serverLevel, null, center.x, center.y, center.z, 16,
                            new ClientboundSpawnerEventPacket(getBlockPos(),
                                    subsequentSuccessfulResult.getEntity().getId()));
                }
            }
        } else if (result instanceof EntitySpawnResult.Delay delayResult) {
            delay(delayResult.getReason().getDelayTicks(), delayResult.getReason());
        }
    }

    private EntitySpawnResult trySpawnEntity() {
        if (level == null || !(level instanceof ServerLevel serverLevel)) {
            return new EntitySpawnResult.Delay(DelayReason.UNKNOWN);
        }

        var optEntityType = EntityType.by(storedEntity);
        if (optEntityType.isEmpty()) {
            return new EntitySpawnResult.Delay(DelayReason.INVALID_ENTITY);
        }

        var entityType = optEntityType.get();
        var blockPos = getBlockPos();

        var entity = entityType.create(serverLevel);
        if (entity == null) {
            return new EntitySpawnResult.Delay(DelayReason.ENTITY_CREATION_ERROR);
        }

        var nearbyEntities = serverLevel.getEntitiesOfClass(
                entity.getClass(),
                new AABB(
                        getBlockPos().getX(), getBlockPos().getY(), getBlockPos().getZ(),
                        getBlockPos().getX() + 1, getBlockPos().getY() + 1, getBlockPos().getZ() + 1
                )
                        .inflate(Math.max(spawningAreaWidth, spawningAreaHeight))
                        .move(new Vec3(0, spawningAreaHeightOffset, 0))
        ).size();
        if (nearbyEntities >= ModServerConfig.CONFIG.mechanicalSpawnerMaxNearbyEntities.get()) {
            return new EntitySpawnResult.Delay(DelayReason.TOO_MANY_ENTITIES);
        }

        var random = serverLevel.getRandom();

        var x = blockPos.getX() + (random.nextDouble() - random.nextDouble()) * (spawningAreaWidth - 1) / 2 + 0.5;
        var y = blockPos.getY() + (random.nextDouble() - random.nextDouble()) * (spawningAreaHeight - 1) / 2 + spawningAreaHeightOffset + 0.5;
        var z = blockPos.getZ() + (random.nextDouble() - random.nextDouble()) * (spawningAreaWidth - 1) / 2 + 0.5;

        if (!serverLevel.noCollision(entityType.getSpawnAABB(x, y, z))) {
            return new EntitySpawnResult.Delay(DelayReason.SEARCHING_POSITION);
        }

        var yaw = Mth.wrapDegrees(random.nextFloat() * 360.0f);
        entity.moveTo(x, y, z, yaw, 0);
        if (entity instanceof Mob mob) {
            if (!mob.checkSpawnObstruction(serverLevel)) {
                return new EntitySpawnResult.Delay(DelayReason.SEARCHING_POSITION);
            }

            //noinspection deprecation,OverrideOnly
            mob.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(mob.blockPosition()),
                    MobSpawnType.SPAWNER, null);
        }

        serverLevel.addFreshEntity(entity);
        serverLevel.gameEvent(entity, GameEvent.ENTITY_PLACE, BlockPos.containing(x, y, z));

        return new EntitySpawnResult.Success(entity);
    }

    public void lingerSpawnAreaHighlighting(int width, int height, int yOffset) {
        spawnAreaHighlightingTicks = 20 * 5;
        lingerWidth = width;
        lingerHeight = height;
        lingerHeightOffset = yOffset;
    }

    public void stopLingerSpawnAreaHighlighting() {
        spawnAreaHighlightingTicks = 0;
    }

    public void highlightSpawningArea(int width, int height, int yOffset) {
        var spawningArea = AABB.ofSize(
                getBlockPos().getCenter().add(0, yOffset, 0),
                width,
                height,
                width
        );

        Outliner.getInstance().chaseAABB(spawnAreaOutlineSlot, spawningArea)
                .colored(0x9b59b6)
                .lineWidth(1 / 16f);
    }

    private void highlightSpawningArea() {
        highlightSpawningArea(lingerWidth, lingerHeight, lingerHeightOffset);
    }

    @Override
    public float calculateStressApplied() {
        lastStressApplied = stressImpact;
        return stressImpact;
    }

    private Optional<StallingReason> getPotentialStallingReason() {
        if (speed == 0) {
            return Optional.of(StallingReason.NO_ROTATIONAL_FORCE);
        }

        if (Mth.abs(speed) < ModServerConfig.CONFIG.mechanicalSpawnerMinRpm.get().floatValue()) {
            return Optional.of(StallingReason.ROTATION_SPEED_TOO_LOW);
        }

        if (!hasStoredEntity()) {
            return Optional.of(StallingReason.NO_SOUL);
        }

        var optRecipe = getCurrentRecipe();
        if (optRecipe.isEmpty()) {
            return Optional.of(StallingReason.NOT_ENOUGH_FLUID);
        }

        var recipe = optRecipe.get();
        var fluid = tank.getPrimaryHandler().getFluid();
        if (fluid.getAmount() < recipe.fluid().amount()) {
            return Optional.of(StallingReason.NOT_ENOUGH_FLUID);
        }

        var storedEntityId = ResourceLocation.parse(storedEntity.getString("id"));

        if (recipe.blacklist().isPresent()) {
            var isEntityBlacklistedByRecipe = recipe.blacklist().get().contains(storedEntityId);
            if (isEntityBlacklistedByRecipe) {
                return Optional.of(StallingReason.RECIPE_CANT_SPAWN_ENTITY);
            }
        }

        if (recipe.whitelist().isPresent()) {
            var isEntityWhitelistedByRecipe = recipe.whitelist().get().contains(storedEntityId);
            if (!isEntityWhitelistedByRecipe) {
                return Optional.of(StallingReason.RECIPE_CANT_SPAWN_ENTITY);
            }
        }

        return Optional.empty();
    }

    public static float getProgressForTick(float speed, int ticksAtMaxSpeed) {
        return (float) (0.0625 * Math.pow(1.0108892861, Math.abs(speed))) / (float) ticksAtMaxSpeed;
    }

    private float calculateStressImpactForContainedSoulCatcher() {
        if (level == null || storedEntity.isEmpty()) {
            return 0.0F;
        }

        var optEntityType = EntityType.by(storedEntity);
        if (optEntityType.isEmpty()) {
            return 0.0F;
        }

        var entityType = optEntityType.get();
        var entity = (LivingEntity) entityType.create(level);
        if (entity == null) {
            return 0.0F;
        }

        var health = entity.getMaxHealth();
        entity.discard();

        var rawImpact =
                (float) (0.0009652871 * Math.pow(health, 3) - 0.0548339331 * Math.pow(health, 2) + 1.5872319688 * health + 2.4666366772);
        return Mth.clamp(rawImpact, 4, ModServerConfig.CONFIG.mechanicalSpawnerMaxStressImpact.get().floatValue());
    }

    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                ModBlockEntities.MECHANICAL_SPAWNER.get(),
                (blockEntity, direction) -> {
                    var facing = blockEntity.getBlockState().getValue(MechanicalSpawnerBlock.FACING);
                    if (direction == null || direction == facing.getOpposite()) {
                        return blockEntity.tank.getCapability();
                    }

                    return null;
                }
        );
    }

    @Override
    public void invalidate() {
        super.invalidate();
        invalidateCapabilities();
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        tank = SmartFluidTankBehaviour.single(this, 1000);
        tank.getPrimaryHandler().setValidator(fluidStack -> {
            if (level == null) {
                return false;
            }

            var potentialRecipes = level.getRecipeManager().getAllRecipesFor(ModRecipes.SPAWNING.get());
            for (var recipeHolder : potentialRecipes) {
                var recipe = recipeHolder.value();
                var nonSizedIngredient = recipe.fluid().ingredient();
                if (nonSizedIngredient.test(fluidStack)) {
                    return true;
                }
            }

            return false;
        });
        behaviours.add(tank);
    }

    public boolean isDelayed() {
        return delayTicks >= 0;
    }

    public FluidStack getFluidStack() {
        return tank.getPrimaryHandler().getFluid();
    }

    public LerpedFloat getFluidLevel() {
        return tank.getPrimaryTank().getFluidLevel();
    }

    public boolean hasStoredEntity() {
        return storedEntity.contains("id", CompoundTag.TAG_STRING);
    }

    public @NotNull CompoundTag getStoredEntity() {
        return storedEntity;
    }

    private Optional<SpawningRecipe> getCurrentRecipe() {
        if (level == null) {
            return Optional.empty();
        }

        var potentialRecipes = level.getRecipeManager().getAllRecipesFor(ModRecipes.SPAWNING.get());
        for (var recipeHolder : potentialRecipes) {
            var recipe = recipeHolder.value();
            var nonSizedIngredient = recipe.fluid().ingredient();
            if (nonSizedIngredient.test(getFluidStack())) {
                return Optional.of(recipe);
            }
        }

        return Optional.empty();
    }

    public Optional<Integer> getParticleColor() {
        var color = new PotionContents(Potions.REGENERATION).getColor();

        if (getCurrentRecipe().isEmpty()) {
            return Optional.empty();
        }

        var recipe = getCurrentRecipe().get();

        if (recipe.particleColor().isPresent()) {
            color = recipe.particleColor().get();
        } else {
            var fluidStack = getFluidStack();
            if (getFluidStack().getFluid() instanceof PotionFluid) {
                color = fluidStack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).getColor();
            }
        }

        return Optional.of(color);
    }

    @Override
    protected void read(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(compound, registries, clientPacket);

        setStoredEntity(compound.getCompound("StoredEntity"), false);

        spawningAreaWidth = compound.contains("SpawnAreaWidth")
                ? compound.getInt("SpawnAreaWidth")
                : ModServerConfig.CONFIG.mechanicalSpawnerAreaDefaultWidth.get();

        spawningAreaHeight = compound.contains("SpawnAreaHeight")
                ? compound.getInt("SpawnAreaHeight")
                : ModServerConfig.CONFIG.mechanicalSpawnerAreaDefaultHeight.get();

        spawningAreaHeightOffset = compound.contains("SpawnAreaHeightOffset")
                ? compound.getInt("SpawnAreaHeightOffset")
                : ModServerConfig.CONFIG.mechanicalSpawnerAreaDefaultHeightOffset.get();
    }

    @Override
    protected void write(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(compound, registries, clientPacket);

        compound.put("StoredEntity", storedEntity);

        compound.putInt("SpawnAreaWidth", spawningAreaWidth);
        compound.putInt("SpawnAreaHeight", spawningAreaHeight);
        compound.putInt("SpawnAreaHeightOffset", spawningAreaHeightOffset);
    }

    public int getSpawningAreaWidth() {
        return spawningAreaWidth;
    }

    public void setSpawningAreaWidth(int spawningAreaWidth) {
        this.spawningAreaWidth = spawningAreaWidth;
    }

    public int getSpawningAreaHeight() {
        return spawningAreaHeight;
    }

    public void setSpawningAreaHeight(int spawningAreaHeight) {
        this.spawningAreaHeight = spawningAreaHeight;
    }

    public int getSpawningAreaHeightOffset() {
        return spawningAreaHeightOffset;
    }

    public void setSpawningAreaHeightOffset(int spawningAreaHeightOffset) {
        this.spawningAreaHeightOffset = spawningAreaHeightOffset;
    }

    public int getSpawnProgressPercentage() {
        return Math.min(Math.round(spawnProgress * 100), 100);
    }

    public String getDelayReasonTranslationKey() {
        return "create_mob_spawners.waila.spawner_delay_reason." + delayReason.name().toLowerCase();
    }

    public Optional<String> getStallingReasonTranslationKey() {
        var reason = getPotentialStallingReason();
        return reason.map(stallingReason -> stallingReason.name().toLowerCase());
    }

    private abstract static class EntitySpawnResult {
        private static class Success extends EntitySpawnResult {
            private final Entity entity;

            private Success(Entity entity) {
                this.entity = entity;
            }

            public Entity getEntity() {
                return entity;
            }
        }

        private static class Delay extends EntitySpawnResult {
            private final DelayReason reason;

            private Delay(DelayReason reason) {
                this.reason = reason;
            }

            public DelayReason getReason() {
                return reason;
            }
        }
    }

    private enum DelayReason {
        UNKNOWN(20),
        INVALID_ENTITY(20),
        SEARCHING_POSITION(5),
        ENTITY_CREATION_ERROR(20),
        TOO_MANY_ENTITIES(10);

        private final int delayTicks;

        DelayReason(int delayTicks) {
            this.delayTicks = delayTicks;
        }

        public int getDelayTicks() {
            return delayTicks;
        }
    }

    private enum StallingReason {
        NO_SOUL,
        NOT_ENOUGH_FLUID,
        NO_ROTATIONAL_FORCE,
        ROTATION_SPEED_TOO_LOW,
        RECIPE_CANT_SPAWN_ENTITY,
    }
}
