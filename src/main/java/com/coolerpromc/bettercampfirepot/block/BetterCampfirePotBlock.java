package com.coolerpromc.bettercampfirepot.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;


public class BetterCampfirePotBlock extends HorizontalDirectionalBlock implements SimpleWaterloggedBlock{
    public static final MapCodec<BetterCampfirePotBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(propertiesCodec()).apply(instance, BetterCampfirePotBlock::new));

    public static final BooleanProperty OPEN = BlockStateProperties.OPEN;
    public static final BooleanProperty OCCUPIED = BlockStateProperties.OCCUPIED;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    private static final VoxelShape AABB_NS = Shapes.or(
            Shapes.box(0.125, 0.0, 0.125, 0.875, 0.375, 0.875),
            Shapes.box(0.0, 0.3125, 0.375, 0.125, 0.375, 0.625),
            Shapes.box(0.875, 0.3125, 0.375, 1.0, 0.375, 0.625),
            Shapes.box(0.4375, 0.375, 0.4375, 0.5625, 0.4375, 0.5625)
    );

    private static final VoxelShape AABB_WE = Shapes.or(
            Shapes.box(0.125, 0.0, 0.125, 0.875, 0.375, 0.875),
            Shapes.box(0.375, 0.3125, 0.0, 0.625, 0.375, 0.125),
            Shapes.box(0.375, 0.3125, 0.875, 0.625, 0.375, 1.0),
            Shapes.box(0.4375, 0.375, 0.4375, 0.5625, 0.4375, 0.5625)
    );

    private static final VoxelShape AABB_NS_OPEN = Shapes.or(
            Shapes.box(0.1875, 0.0625, 0.125, 0.875, 0.375, 0.1875),
            Shapes.box(0.125, 0.0, 0.125, 0.875, 0.0625, 0.875),
            Shapes.box(0.8125, 0.0625, 0.1875, 0.875, 0.375, 0.875),
            Shapes.box(0.125, 0.0625, 0.125, 0.1875, 0.375, 0.8125),
            Shapes.box(0.125, 0.0625, 0.8125, 0.8125, 0.375, 0.875),
            Shapes.box(0.0, 0.3125, 0.375, 0.125, 0.375, 0.625),
            Shapes.box(0.875, 0.3125, 0.375, 1.0, 0.375, 0.625)
    );

    private static final VoxelShape AABB_WE_OPEN = Shapes.or(
            Shapes.box(0.1875, 0.0625, 0.125, 0.875, 0.375, 0.1875),
            Shapes.box(0.125, 0.0, 0.125, 0.875, 0.0625, 0.875),
            Shapes.box(0.8125, 0.0625, 0.1875, 0.875, 0.375, 0.875),
            Shapes.box(0.125, 0.0625, 0.125, 0.1875, 0.375, 0.8125),
            Shapes.box(0.125, 0.0625, 0.8125, 0.8125, 0.375, 0.875),
            Shapes.box(0.375, 0.3125, 0.0, 0.625, 0.375, 0.125),
            Shapes.box(0.375, 0.3125, 0.875, 0.625, 0.375, 1.0)
    );

    public BetterCampfirePotBlock(Properties settings) {
        super(settings);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(OPEN, false)
                .setValue(OCCUPIED, false)
                .setValue(WATERLOGGED, false)
        );
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        Direction direction = state.getValue(FACING);
        boolean open = state.getValue(OPEN);

        if (open) {
            return (direction == Direction.NORTH || direction == Direction.SOUTH) ? AABB_NS_OPEN : AABB_WE_OPEN;
        } else {
            return (direction == Direction.NORTH || direction == Direction.SOUTH) ? AABB_NS : AABB_WE;
        }
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        BlockState blockState = this.defaultBlockState()
                .setValue(WATERLOGGED, ctx.getLevel().getFluidState(ctx.getClickedPos()).getType() == Fluids.WATER);

        LevelAccessor worldView = ctx.getLevel();
        BlockPos blockPos = ctx.getClickedPos();

        for (Direction direction : ctx.getNearestLookingDirections()) {
            if (direction.getAxis().isHorizontal()) {
                blockState = blockState.setValue(FACING, direction.getOpposite());
                if (blockState.canSurvive(worldView, blockPos)) {
                    return blockState;
                }
            }
        }

        return null;
    }

    @Override
    public MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                  LevelAccessor world, BlockPos pos, BlockPos neighborPos) {
        if (state.getValue(WATERLOGGED)) {
            world.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(world));
        }

        if (direction == state.getValue(FACING) && !state.canSurvive(world, pos)) {
            return Blocks.AIR.defaultBlockState();
        } else {
            return super.updateShape(state, direction, neighborState, world, pos, neighborPos);
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, OPEN, OCCUPIED, WATERLOGGED);
    }

    @Override
    public FluidState getFluidState(BlockState blockState) {
        return blockState.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(blockState);
    }

    @Override
    public MutableComponent getName() {
        return Component.literal(this.asItem().getName(this.asItem().getDefaultInstance()).getString());
    }
}
