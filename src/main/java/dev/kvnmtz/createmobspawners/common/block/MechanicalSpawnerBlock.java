package dev.kvnmtz.createmobspawners.common.block;

import com.simibubi.create.AllItems;
import com.simibubi.create.content.kinetics.base.KineticBlock;
import com.simibubi.create.foundation.block.IBE;
import dev.kvnmtz.createmobspawners.client.block.MechanicalSpawnerBlockClient;
import dev.kvnmtz.createmobspawners.common.block.entity.MechanicalSpawnerBlockEntity;
import dev.kvnmtz.createmobspawners.common.block.registry.ModBlockEntities;
import dev.kvnmtz.createmobspawners.common.config.ModServerConfig;
import dev.kvnmtz.createmobspawners.common.item.registry.ModItems;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MechanicalSpawnerBlock extends KineticBlock implements IBE<MechanicalSpawnerBlockEntity> {

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public MechanicalSpawnerBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
                                               BlockHitResult hitResult) {
        var isSneaking = player.isShiftKeyDown();
        if (isSneaking) {
            if (level.isClientSide) {
                return InteractionResult.SUCCESS;
            }

            var blockEntity = getBlockEntity(level, pos);
            if (blockEntity == null) {
                return InteractionResult.FAIL;
            }

            if (!blockEntity.hasStoredEntity()) {
                return InteractionResult.PASS;
            }

            blockEntity.ejectSoulCatcher((ServerPlayer) player);
        } else {
            if (!level.isClientSide) {
                return InteractionResult.SUCCESS;
            }

            var blockEntity = getBlockEntity(level, pos);
            if (blockEntity == null) {
                return InteractionResult.FAIL;
            }

            MechanicalSpawnerBlockClient.openConfigGuiIfAllowed(blockEntity);
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hitResult) {
        var isSneaking = player.isShiftKeyDown();
        if (isSneaking) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (stack.is(ModItems.SOUL_CATCHER)) {
            if (level.isClientSide) {
                return ItemInteractionResult.SUCCESS;
            }

            var blockEntity = getBlockEntity(level, pos);
            if (blockEntity == null) {
                return ItemInteractionResult.FAIL;
            }

            if (blockEntity.hasStoredEntity()) {
                return ItemInteractionResult.FAIL;
            }

            var data = stack.get(DataComponents.ENTITY_DATA);
            if (data == null) {
                return ItemInteractionResult.FAIL;
            }

            var entityTag = data.copyTag();
            blockEntity.setStoredEntity(entityTag);

            stack.shrink(1);
        } else if (stack.is(AllItems.WRENCH)) {
            if (!level.isClientSide) {
                return ItemInteractionResult.SUCCESS;
            }

            var blockEntity = getBlockEntity(level, pos);
            if (blockEntity == null) {
                return ItemInteractionResult.FAIL;
            }

            MechanicalSpawnerBlockClient.openConfigGuiIfAllowed(blockEntity);
        } else {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        return ItemInteractionResult.SUCCESS;
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        var drops = super.getDrops(state, params);

        var blockEntity = params.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (blockEntity instanceof MechanicalSpawnerBlockEntity spawner && spawner.hasStoredEntity()) {
            var stack = ModItems.SOUL_CATCHER.asStack();
            stack.set(DataComponents.ENTITY_DATA, CustomData.of(spawner.getStoredEntity()));

            drops.add(stack);
            spawner.clearStoredEntity();
        }

        return drops;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public Class<MechanicalSpawnerBlockEntity> getBlockEntityClass() {
        return MechanicalSpawnerBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends MechanicalSpawnerBlockEntity> getBlockEntityType() {
        return ModBlockEntities.MECHANICAL_SPAWNER.get();
    }

    @Override
    public @Nullable MechanicalSpawnerBlockEntity getBlockEntity(BlockGetter worldIn, BlockPos pos) {
        return (MechanicalSpawnerBlockEntity) worldIn.getBlockEntity(pos);
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState blockState) {
        return Direction.Axis.Y;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
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
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, getPlacementDirection(context));
    }

    @Override
    public SpeedLevel getMinimumRequiredSpeedLevel() {
        return SpeedLevel.of(ModServerConfig.CONFIG.mechanicalSpawnerMinRpm.get().floatValue());
    }
}
