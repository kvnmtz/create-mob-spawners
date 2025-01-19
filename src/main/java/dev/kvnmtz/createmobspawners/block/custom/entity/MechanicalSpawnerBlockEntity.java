package dev.kvnmtz.createmobspawners.block.custom.entity;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import dev.kvnmtz.createmobspawners.Config;
import dev.kvnmtz.createmobspawners.block.custom.MechanicalSpawnerBlock;
import dev.kvnmtz.createmobspawners.block.custom.entity.registry.ModBlockEntities;
import dev.kvnmtz.createmobspawners.capabilities.entitystorage.IEntityStorage;
import dev.kvnmtz.createmobspawners.capabilities.registry.ModCapabilities;
import dev.kvnmtz.createmobspawners.capabilities.entitystorage.StoredEntityData;
import dev.kvnmtz.createmobspawners.item.registry.ModItems;
import dev.kvnmtz.createmobspawners.network.packet.ClientboundSpawnerEventPacket;
import dev.kvnmtz.createmobspawners.network.PacketHandler;
import dev.kvnmtz.createmobspawners.recipe.registry.ModRecipes;
import dev.kvnmtz.createmobspawners.recipe.custom.SpawningRecipe;
import dev.kvnmtz.createmobspawners.utils.DropUtils;
import dev.kvnmtz.createmobspawners.utils.ParticleUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class MechanicalSpawnerBlockEntity extends KineticBlockEntity implements IEntityStorage {
    public MechanicalSpawnerBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntities.SPAWNER_BE.get(), pPos, pBlockState);
    }

    private StoredEntityData storedEntityData = StoredEntityData.empty();

    @Override
    public StoredEntityData getStoredEntityData() {
        return storedEntityData;
    }

    @Override
    public void setStoredEntityData(StoredEntityData entityData) {
        storedEntityData = entityData;
        onStoredEntityDataChanged();
    }

    private float stressImpact = 8.f;

    private void onStoredEntityDataChanged() {
        stressImpact = calculateStressImpactForContainedSoulCatcher();
        spawnProgress = 0.f;
    }

    private float calculateStressImpactForContainedSoulCatcher() {
        if (level == null) return 0.f;

        var optEntityType = storedEntityData.getEntityType();
        if (optEntityType.isEmpty()) return 0.f;

        var entityType = (EntityType<? extends LivingEntity>) optEntityType.get();
        var entity = entityType.create(level);
        if (entity == null) return 0.f;

        var health = entity.getMaxHealth();
        entity.discard();
        var rawImpact = (float) (0.0009652871 * Math.pow(health, 3) - 0.0548339331 * Math.pow(health, 2) + 1.5872319688 * health + 2.4666366772);
        return Mth.clamp(rawImpact, 4, Config.mechanicalSpawnerMaxStressImpact);
    }

    private SmartFluidTankBehaviour tank;

    public SmartFluidTankBehaviour getTank() {
        return tank;
    }

    public FluidStack getFluidStack() {
        return tank.getPrimaryHandler().getFluid();
    }

    @Override
    protected void write(CompoundTag compound, boolean clientPacket) {
        compound.put("EntityStorage", storedEntityData.serializeNBT());
        super.write(compound, clientPacket);
    }

    @Override
    protected void read(CompoundTag compound, boolean clientPacket) {
        storedEntityData.deserializeNBT(compound.getCompound("EntityStorage"));
        onStoredEntityDataChanged();
        super.read(compound, clientPacket);
    }

    Optional<SpawningRecipe> getCurrentRecipe() {
        if (level == null) return Optional.empty();

        var potentialRecipes = level.getRecipeManager().getAllRecipesFor(ModRecipes.SPAWNING.get());
        for (var recipe : potentialRecipes) {
            if (recipe.getFluidIngredient().getMatchingFluidStacks().contains(getFluidStack())) {
                return Optional.of(recipe);
            }
        }

        return Optional.empty();
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        tank = SmartFluidTankBehaviour.single(this, 1000);
        tank.getPrimaryHandler().setValidator(fluidStack -> {
            if (level == null) return false;

            var potentialRecipes = level.getRecipeManager().getAllRecipesFor(ModRecipes.SPAWNING.get());
            for (var recipe : potentialRecipes) {
                if (recipe.getFluidIngredient().getMatchingFluidStacks().contains(fluidStack)) {
                    return true;
                }
            }

            return false;
        });
        behaviours.add(tank);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.FLUID_HANDLER && side == getBlockState().getValue(MechanicalSpawnerBlock.FACING).getOpposite())
            return tank.getCapability().cast();

        return super.getCapability(cap, side);
    }

    public void ejectSoulCatcher() {
        if (level == null) return;
        if (storedEntityData.isEmpty()) return;
        var itemStack = ModItems.SOUL_CATCHER.get().getDefaultInstance();
        itemStack.getCapability(ModCapabilities.ENTITY_STORAGE).ifPresent(entityStorage -> {
            entityStorage.setStoredEntityData(storedEntityData);
            var droppedItems = DropUtils.dropItemStack(level, worldPosition.getX(), worldPosition.getY() + 1, worldPosition.getZ(), itemStack);
            droppedItems.stream().findFirst().ifPresent(itemEntity -> itemEntity.setGlowingTag(true));
            setStoredEntityData(StoredEntityData.empty());
        });
    }

    private static final int SPAWN_RANGE = 4;
    private static final int MAX_NEARBY_ENTITIES = 6;

    private float spawnProgress = 0;

    public int getSpawnProgressPercentage() {
        return Math.min(Math.round(spawnProgress * 100), 100);
    }

    public static float getProgressForTick(float speed, int ticksAtMaxSpeed) {
        return (float) (0.0625 * Math.pow(1.0108892861, Math.abs(speed))) / (float) ticksAtMaxSpeed;
    }

    private void addProgressForTick() {
        if (spawnProgress >= 1.f) return;

        var optRecipe = getCurrentRecipe();
        if (optRecipe.isEmpty()) return;

        var recipe = optRecipe.get();
        spawnProgress += getProgressForTick(speed, recipe.getSpawnTicksAtMaxSpeed());
    }

    private int delayTicks = -1;
    private DelayReason delayReason;

    private void delay(int ticks, DelayReason reason) {
        delayTicks = ticks;
        delayReason = reason;
    }

    public boolean isDelayed() {
        return delayTicks >= 0;
    }

    public String getDelayReasonTranslationKey() {
        return "create_mob_spawners.waila.spawner_delay_reason." + delayReason.name().toLowerCase();
    }

    private void useFluid() {
        var optRecipe = getCurrentRecipe();
        if (optRecipe.isEmpty()) return;
        var recipe = optRecipe.get();

        var fluid = tank.getPrimaryHandler().getFluid();
        fluid.setAmount(fluid.getAmount() - recipe.getFluidIngredient().getRequiredAmount());
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

    private EntitySpawnResult spawnEntity() {
        if (this.level == null) return new EntitySpawnResult.Delay(DelayReason.UNKNOWN);
        var level = (ServerLevel) this.level;

        var optEntityType = storedEntityData.getEntityType();
        if (optEntityType.isEmpty()) return new EntitySpawnResult.Delay(DelayReason.INVALID_ENTITY);

        var entityType = optEntityType.get();
        var blockPos = getBlockPos();

        var entity = entityType.create(level);
        if (entity == null) return new EntitySpawnResult.Delay(DelayReason.ENTITY_CREATION_ERROR);

        var nearbyEntities = level.getEntitiesOfClass(entity.getClass(), (new AABB(blockPos.getX(), blockPos.getY(), blockPos.getZ(), blockPos.getX() + 1, blockPos.getY() + 1, blockPos.getZ() + 1)).inflate(SPAWN_RANGE)).size();
        if (nearbyEntities >= MAX_NEARBY_ENTITIES)
            return new EntitySpawnResult.Delay(DelayReason.TOO_MANY_ENTITIES);

        var random = level.getRandom();

        var x = (double) blockPos.getX() + (random.nextDouble() - random.nextDouble()) * (double) SPAWN_RANGE + (double) 0.5F;
        var y = blockPos.getY() + random.nextInt(3) - 1;
        var z = (double) blockPos.getZ() + (random.nextDouble() - random.nextDouble()) * (double) SPAWN_RANGE + (double) 0.5F;

        if (!level.noCollision(entityType.getAABB(x, y, z)))
            return new EntitySpawnResult.Delay(DelayReason.SEARCHING_POSITION);

        var yaw = Mth.wrapDegrees(random.nextFloat() * 360.0f);
        entity.moveTo(x, y, z, yaw, 0);
        if (entity instanceof Mob mob) {
            if (!mob.checkSpawnObstruction(level))
                return new EntitySpawnResult.Delay(DelayReason.SEARCHING_POSITION);

            //noinspection deprecation,OverrideOnly
            mob.finalizeSpawn(level, level.getCurrentDifficultyAt(mob.blockPosition()), MobSpawnType.SPAWNER, null, null);
        }

        level.addFreshEntity(entity);

        level.gameEvent(entity, GameEvent.ENTITY_PLACE, BlockPos.containing(x, y, z));

        return new EntitySpawnResult.Success(entity);
    }

    private void trySpawnEntity() {
        if (level == null) return;

        var optRecipe = getCurrentRecipe();
        if (optRecipe.isEmpty()) return;

        var recipe = optRecipe.get();
        var additionalSpawnAttempts = recipe.getAdditionalSpawnAttempts();

        var result = spawnEntity();
        if (result instanceof EntitySpawnResult.Success successfulResult) {
            useFluid();
            var center = getBlockPos().getCenter();
            PacketHandler.sendToNearbyPlayers(new ClientboundSpawnerEventPacket(getBlockPos(), successfulResult.getEntity().getId()), center, 16, level.dimension());
            spawnProgress = 0;

            for (var i = 0; i < additionalSpawnAttempts; i++) {
                var subsequentResult = spawnEntity();
                if (subsequentResult instanceof EntitySpawnResult.Success subsequentSuccessfulResult) {
                    PacketHandler.sendToNearbyPlayers(new ClientboundSpawnerEventPacket(getBlockPos(), subsequentSuccessfulResult.getEntity().getId()), center, 16, level.dimension());
                }
            }
        } else if (result instanceof EntitySpawnResult.Delay delayResult) {
            delay(delayResult.getReason().getDelayTicks(), delayResult.getReason());
        }
    }

    private enum StallingReason {
        NO_SOUL,
        NOT_ENOUGH_FLUID,
        NO_ROTATIONAL_FORCE,
        ROTATION_SPEED_TOO_LOW,
    }

    private Optional<StallingReason> getStallingReason() {
        if (speed == 0) {
            return Optional.of(StallingReason.NO_ROTATIONAL_FORCE);
        }

        if (Mth.abs(speed) < Config.mechanicalSpawnerMinRpm) {
            return Optional.of(StallingReason.ROTATION_SPEED_TOO_LOW);
        }

        if (storedEntityData.isEmpty()) {
            return Optional.of(StallingReason.NO_SOUL);
        }

        var optRecipe = getCurrentRecipe();
        if (optRecipe.isEmpty()) {
            return Optional.of(StallingReason.NOT_ENOUGH_FLUID);
        }

        var recipe = optRecipe.get();
        var fluid = tank.getPrimaryHandler().getFluid();

        if (fluid.getAmount() < recipe.getFluidIngredient().getRequiredAmount()) {
            return Optional.of(StallingReason.NOT_ENOUGH_FLUID);
        }

        return Optional.empty();
    }

    public Optional<String> getStallingReasonTranslationKey() {
        var reason = getStallingReason();
        return reason.map(stallingReason -> stallingReason.name().toLowerCase());
    }

    @Override
    public void tick() {
        super.tick();

        if (level == null) return;

        var unableToProgress = getStallingReason().isPresent();
        if (unableToProgress) return;

        if (level.isClientSide) {
            ParticleUtils.drawPotionEffectParticles(level, getRenderBoundingBox(), getBlockPos().getCenter().subtract(0, 0.5, 0), PotionUtils.getColor(Potions.REGENERATION), 1);
            return;
        }

        if (isVirtual()) return;

        if (delayTicks != -1) {
            delayTicks--;
            if (delayTicks == 0) {
                delayTicks = -1;
                trySpawnEntity();
            }
        } else {
            addProgressForTick();
            if (spawnProgress >= 1.f) {
                trySpawnEntity();
            }
        }
    }

    @Override
    public float calculateStressApplied() {
        lastStressApplied = stressImpact;
        return stressImpact;
    }
}
