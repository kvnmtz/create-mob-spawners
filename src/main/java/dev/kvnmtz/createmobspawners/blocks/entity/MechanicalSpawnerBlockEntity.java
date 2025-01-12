package dev.kvnmtz.createmobspawners.blocks.entity;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import com.simibubi.create.foundation.utility.animation.LerpedFloat;
import dev.kvnmtz.createmobspawners.blocks.MechanicalSpawnerBlock;
import dev.kvnmtz.createmobspawners.blocks.entity.registry.ModBlockEntities;
import dev.kvnmtz.createmobspawners.capabilities.entitystorage.IEntityStorage;
import dev.kvnmtz.createmobspawners.capabilities.registry.ModCapabilities;
import dev.kvnmtz.createmobspawners.capabilities.entitystorage.StoredEntityData;
import dev.kvnmtz.createmobspawners.items.registry.ModItems;
import dev.kvnmtz.createmobspawners.network.ClientboundSpawnerEventPacket;
import dev.kvnmtz.createmobspawners.network.PacketHandler;
import dev.kvnmtz.createmobspawners.utils.DropUtils;
import dev.kvnmtz.createmobspawners.utils.ParticleUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
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
    }

    private SmartFluidTankBehaviour tank;
    private LerpedFloat fluidLevel;

    public LerpedFloat getFluidLevel() {
        return fluidLevel;
    }

    private float getFillState() {
        return (float) tank.getPrimaryHandler().getFluidAmount() / (float) tank.getPrimaryHandler().getCapacity();
    }

    public FluidStack getFluidStack() {
        return tank.getPrimaryHandler().getFluid();
    }

    @Override
    protected void write(CompoundTag compound, boolean clientPacket) {
        super.write(compound, clientPacket);
        compound.put("EntityStorage", storedEntityData.serializeNBT());
    }

    @Override
    protected void read(CompoundTag compound, boolean clientPacket) {
        super.read(compound, clientPacket);
        storedEntityData.deserializeNBT(compound.getCompound("EntityStorage"));

        if (clientPacket) {
            var fillState = this.getFillState();
            if (fluidLevel == null) {
                fluidLevel = LerpedFloat.linear().startWithValue(fillState);
            }
            fluidLevel.chase(fillState, 0.5f, LerpedFloat.Chaser.EXP);
        }
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        tank = SmartFluidTankBehaviour.single(this, 1000);
        tank.getPrimaryHandler().setValidator(fluidStack -> {
            var tag = fluidStack.getTag();
            if (tag == null) return false;
            var potionType = tag.getString("Potion");
            return potionType.equals("minecraft:regeneration")
                    || potionType.equals("minecraft:strong_regeneration")
                    || potionType.equals("minecraft:long_regeneration");
        });
        tank.whenFluidUpdates(() -> {
            if (!isVirtual()) return;
            if (fluidLevel == null) {
                fluidLevel = LerpedFloat.linear().startWithValue(this.getFillState());
            }
            fluidLevel.chase(this.getFillState(), 0.5f, LerpedFloat.Chaser.EXP);
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
            storedEntityData = StoredEntityData.empty();
        });
    }

    private int getNecessaryFluidAmountForSpawning() {
        var fluid = tank.getPrimaryHandler().getFluid();
        var potionType = fluid.getTag().getString("Potion");
        if (potionType.equals("minecraft:strong_regeneration") || potionType.equals("minecraft:long_regeneration")) {
            return 100;
        }

        return 200;
    }

    private static final int SPAWN_RANGE = 4;
    private static final int MAX_NEARBY_ENTITIES = 6;

    private float spawnProgress = 0;

    public int getSpawnProgressPercentage() {
        return Math.min(Math.round(spawnProgress * 100), 100);
    }

    private void addProgressForTick() {
        if (spawnProgress >= 1.f) return;

        final var BASE_TICKS_FOR_SPAWNING = 1200; // 1 minute at 1 RPM -> 4s at 256 RPM
        spawnProgress += (0.05f * Mth.abs(speed) + 0.95f) / (float) BASE_TICKS_FOR_SPAWNING;
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
        var fluid = tank.getPrimaryHandler().getFluid();
        fluid.setAmount(fluid.getAmount() - getNecessaryFluidAmountForSpawning());
    }

    private Optional<EntityType<?>> getEntityTypeFromStoredEntityData() {
        var optEntityTypeResourceLocation = storedEntityData.getEntityType();
        if (optEntityTypeResourceLocation.isEmpty()) return Optional.empty();

        var entityTypeResourceLocation = optEntityTypeResourceLocation.get();

        var tag = new CompoundTag();
        tag.putString("id", entityTypeResourceLocation.toString());

        return EntityType.by(tag);
    }

    private abstract static class EntitySpawnResult {
        private static class SuccessfulResult extends EntitySpawnResult {
            private final Entity entity;

            private SuccessfulResult(Entity entity) {
                this.entity = entity;
            }

            public Entity getEntity() {
                return entity;
            }
        }

        private static class DelayResult extends EntitySpawnResult {
            private final DelayReason reason;

            private DelayResult(DelayReason reason) {
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
        if (this.level == null) return new EntitySpawnResult.DelayResult(DelayReason.UNKNOWN);
        var level = (ServerLevel) this.level;

        var optEntityType = getEntityTypeFromStoredEntityData();
        if (optEntityType.isEmpty()) return new EntitySpawnResult.DelayResult(DelayReason.INVALID_ENTITY);

        var entityType = optEntityType.get();
        var blockPos = getBlockPos();

        var entity = entityType.create(level);
        if (entity == null) return new EntitySpawnResult.DelayResult(DelayReason.ENTITY_CREATION_ERROR);

        var nearbyEntities = level.getEntitiesOfClass(entity.getClass(), (new AABB(blockPos.getX(), blockPos.getY(), blockPos.getZ(), blockPos.getX() + 1, blockPos.getY() + 1, blockPos.getZ() + 1)).inflate(SPAWN_RANGE)).size();
        if (nearbyEntities >= MAX_NEARBY_ENTITIES)
            return new EntitySpawnResult.DelayResult(DelayReason.TOO_MANY_ENTITIES);

        var random = level.getRandom();

        var x = (double) blockPos.getX() + (random.nextDouble() - random.nextDouble()) * (double) SPAWN_RANGE + (double) 0.5F;
        var y = blockPos.getY() + random.nextInt(3) - 1;
        var z = (double) blockPos.getZ() + (random.nextDouble() - random.nextDouble()) * (double) SPAWN_RANGE + (double) 0.5F;

        if (!level.noCollision(entityType.getAABB(x, y, z)))
            return new EntitySpawnResult.DelayResult(DelayReason.SEARCHING_POSITION);

        var yaw = Mth.wrapDegrees(random.nextFloat() * 360.0f);
        entity.moveTo(x, y, z, yaw, 0);
        if (entity instanceof Mob mob) {
            if (!mob.checkSpawnObstruction(level))
                return new EntitySpawnResult.DelayResult(DelayReason.SEARCHING_POSITION);

            //noinspection deprecation,OverrideOnly
            mob.finalizeSpawn(level, level.getCurrentDifficultyAt(mob.blockPosition()), MobSpawnType.SPAWNER, null, null);
        }

        level.addFreshEntity(entity);

        level.gameEvent(entity, GameEvent.ENTITY_PLACE, BlockPos.containing(x, y, z));

        return new EntitySpawnResult.SuccessfulResult(entity);
    }

    private void trySpawnEntity() {
        if (level == null) return;

        var result = spawnEntity();
        if (result instanceof EntitySpawnResult.SuccessfulResult successfulResult) {
            useFluid();
            var center = getBlockPos().getCenter();
            PacketHandler.sendToNearbyPlayers(new ClientboundSpawnerEventPacket(getBlockPos(), successfulResult.getEntity().getId()), center, 16, level.dimension());
            spawnProgress = 0;
        } else if (result instanceof EntitySpawnResult.DelayResult delayResult) {
            delay(delayResult.getReason().getDelayTicks(), delayResult.getReason());
        }
    }

    private enum ReasonForNotProgressing {
        NO_SOUL,
        NO_REGENERATION_POTION_LIQUID,
        NOT_ENOUGH_REGENERATION_POTION_LIQUID,
        NO_ROTATIONAL_FORCE,
    }

    private Optional<ReasonForNotProgressing> getReasonForNotProgressing() {
        if (speed == 0) {
            return Optional.of(ReasonForNotProgressing.NO_ROTATIONAL_FORCE);
        }

        var fluid = tank.getPrimaryHandler().getFluid();
        if (fluid.getTag() == null) {
            return Optional.of(ReasonForNotProgressing.NO_REGENERATION_POTION_LIQUID);
        }
        if (fluid.getAmount() < getNecessaryFluidAmountForSpawning()) {
            if (fluid.getAmount() == 0) {
                return Optional.of(ReasonForNotProgressing.NO_REGENERATION_POTION_LIQUID);
            }
            return Optional.of(ReasonForNotProgressing.NOT_ENOUGH_REGENERATION_POTION_LIQUID);
        }

        if (storedEntityData.isEmpty()) {
            return Optional.of(ReasonForNotProgressing.NO_SOUL);
        }

        return Optional.empty();
    }

    public Optional<String> getReasonForNotProgressingTranslationKey() {
        var reason = getReasonForNotProgressing();
        return reason.map(reasonForNotProgressing -> reasonForNotProgressing.name().toLowerCase());
    }

    @Override
    public void tick() {
        super.tick();

        if (level == null) return;

        if (level.isClientSide && fluidLevel != null) {
            fluidLevel.tickChaser();
        }

        var unableToProgress = getReasonForNotProgressing().isPresent();
        if (unableToProgress) return;

        if (level.isClientSide) {
            if (level.random.nextInt(6) == 0) {
                ParticleUtils.drawParticles(ParticleTypes.ENTITY_EFFECT, level, getBlockPos().getCenter(), 3, 0.2, 0.2, 0.2, new Vec3(205 / 255.0, 92 / 255.0, 171 / 255.0));
            }
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
}
