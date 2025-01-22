package dev.kvnmtz.createmobspawners.block.custom;

import com.simibubi.create.content.kinetics.base.KineticBlock;
import com.simibubi.create.foundation.block.IBE;
import dev.kvnmtz.createmobspawners.Config;
import dev.kvnmtz.createmobspawners.block.custom.entity.registry.ModBlockEntities;
import dev.kvnmtz.createmobspawners.block.custom.entity.MechanicalSpawnerBlockEntity;
import dev.kvnmtz.createmobspawners.item.registry.ModItems;
import dev.kvnmtz.createmobspawners.item.custom.SoulCatcherItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MechanicalSpawnerBlock extends KineticBlock implements IBE<MechanicalSpawnerBlockEntity> {
    public MechanicalSpawnerBlock(Properties properties) {
        super(properties);
        registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    public static final DirectionProperty FACING = DirectionProperty.create("facing", Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST);

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(FACING);
        super.createBlockStateDefinition(pBuilder);
    }

    private MechanicalSpawnerBlockEntity getBlockEntity(Level level, BlockPos pos) {
        return (MechanicalSpawnerBlockEntity) level.getBlockEntity(pos);
    }

    @SuppressWarnings("deprecation")
    @Override
    public @NotNull InteractionResult use(@NotNull BlockState pState, @NotNull Level pLevel, @NotNull BlockPos pPos, @NotNull Player pPlayer, @NotNull InteractionHand pHand, @NotNull BlockHitResult pHit) {
        var itemStack = pPlayer.getMainHandItem();
        var blockEntity = getBlockEntity(pLevel, pPos);
        var hasStoredEntity = blockEntity.hasStoredEntity();
        if (!hasStoredEntity && itemStack.getItem() == ModItems.SOUL_CATCHER.get()) {
            var entityData = SoulCatcherItem.getEntityData(itemStack);
            if (entityData.isEmpty()) return InteractionResult.FAIL;
            blockEntity.setStoredEntityData(entityData.get());
            itemStack.shrink(1);
        } else if (hasStoredEntity && itemStack.isEmpty() && pPlayer.isShiftKeyDown()) {
            blockEntity.ejectSoulCatcher();
        } else {
            return InteractionResult.PASS;
        }

        blockEntity.setChanged();
        blockEntity.sendData();

        return InteractionResult.SUCCESS;
    }

    @Override
    public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pMovedByPiston) {
        if (pState.getBlock() != pNewState.getBlock()) {
            var blockEntity = getBlockEntity(pLevel, pPos);
            blockEntity.ejectSoulCatcher();
        }

        super.onRemove(pState, pLevel, pPos, pNewState, pMovedByPiston);
    }

    @Override
    public Class<MechanicalSpawnerBlockEntity> getBlockEntityClass() {
        return MechanicalSpawnerBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends MechanicalSpawnerBlockEntity> getBlockEntityType() {
        return ModBlockEntities.MECHANICAL_SPAWNER.get();
    }

    @SuppressWarnings("deprecation")
    @Override
    public @NotNull RenderShape getRenderShape(@NotNull BlockState pState) {
        return RenderShape.MODEL;
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState blockState) {
        return Direction.Axis.Y;
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return face.getAxis() == Direction.Axis.Y;
    }

    private Direction getPlacementDirection(BlockPlaceContext context) {
        var player = context.getPlayer();
        var mirror = player != null && player.isShiftKeyDown();
        if (mirror) {
            return context.getHorizontalDirection();
        } else {
            return context.getHorizontalDirection().getOpposite();
        }
    }

    @Override
    public @Nullable BlockState getStateForPlacement(@NotNull BlockPlaceContext pContext) {
        return this.defaultBlockState().setValue(FACING, getPlacementDirection(pContext));
    }

    @Override
    public SpeedLevel getMinimumRequiredSpeedLevel() {
        return SpeedLevel.of(Config.mechanicalSpawnerMinRpm);
    }
}
