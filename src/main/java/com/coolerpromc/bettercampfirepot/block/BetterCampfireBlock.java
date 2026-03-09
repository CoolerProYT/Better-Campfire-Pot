package com.coolerpromc.bettercampfirepot.block;

import com.cobblemon.mod.common.CobblemonSounds;
import com.cobblemon.mod.common.util.WorldExtensionsKt;
import com.coolerpromc.bettercampfirepot.BetterCampfirePot;
import com.coolerpromc.bettercampfirepot.block.entity.BetterCampfireBlockEntity;
import com.coolerpromc.bettercampfirepot.client.BetterCampfireBEClient;
import com.coolerpromc.bettercampfirepot.item.BetterCampfirePotItem;
import com.coolerpromc.bettercampfirepot.network.ValidItemSyncPacket;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class BetterCampfireBlock extends BaseEntityBlock implements SimpleWaterloggedBlock {
    public static final MapCodec<BetterCampfireBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            propertiesCodec(),
            Codec.BOOL.fieldOf("isSoul").forGetter(BetterCampfireBlock::isSoul)
    ).apply(instance, BetterCampfireBlock::new));
    public static final DirectionProperty ITEM_DIRECTION = DirectionProperty.create("item_facing");
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final BooleanProperty COOKING = BooleanProperty.create("cooking");
    public static final BooleanProperty LID = BooleanProperty.create("lid");

    private static final VoxelShape CAMPFIRE_AABB = Shapes.box(0.0, 0.0, 0.0, 1.0, 0.4375, 1.0);
    private static final VoxelShape AABB = Shapes.or(
            CAMPFIRE_AABB,
            Shapes.box(0.1875, 0.5, 0.125, 0.875, 0.8125, 0.1875),
            Shapes.box(0.125, 0.4375, 0.125, 0.875, 0.5, 0.875),
            Shapes.box(0.8125, 0.5, 0.1875, 0.875, 0.8125, 0.875),
            Shapes.box(0.125, 0.5, 0.125, 0.1875, 0.8125, 0.8125),
            Shapes.box(0.125, 0.5, 0.8125, 0.8125, 0.8125, 0.875)
    );

    private final boolean isSoul;

    public BetterCampfireBlock(Properties properties, boolean isSoul) {
        super(properties);
        this.isSoul = isSoul;
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH)
                .setValue(ITEM_DIRECTION, Direction.NORTH)
                .setValue(POWERED, false)
                .setValue(COOKING, false)
                .setValue(LID, false));
    }

    public boolean isSoul() {
        return isSoul;
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext ctx) {
        BlockState blockState = this.defaultBlockState();
        LevelReader worldView = ctx.getLevel();
        BlockPos blockPos = ctx.getClickedPos();

        for (Direction direction : ctx.getNearestLookingDirections()) {
            if (direction.getAxis().isHorizontal()) {
                blockState = blockState.setValue(HorizontalDirectionalBlock.FACING, direction).setValue(ITEM_DIRECTION, direction);
                if (blockState.canSurvive(worldView, blockPos)) {
                    return blockState;
                }
            }
        }
        return null;
    }

    @Override
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return AABB;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return AABB;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof BetterCampfireBlockEntity campfireBlockEntity)) {
            return InteractionResult.PASS;
        }

        ItemStack potItemStack = campfireBlockEntity.getPotItem();
        if (potItemStack != null && potItemStack.getItem() instanceof BetterCampfirePotItem) {
            if (!level.isClientSide) {
                if (player.isCrouching()) {
                    removePotItem(campfireBlockEntity, state, level, pos, player, false);
                } else {
                    openContainer(level, pos, player);
                }
            }
            return InteractionResult.SUCCESS;
        } else if (player.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof BetterCampfirePotItem) {
            ItemStack heldItem = player.getItemInHand(InteractionHand.MAIN_HAND);
            campfireBlockEntity.setPotItem(heldItem.split(1));

            WorldExtensionsKt.playSoundServer(
                    level,
                    pos.getBottomCenter(),
                    CobblemonSounds.CAMPFIRE_POT_PLACE,
                    SoundSource.NEUTRAL,
                    1F,
                    1F
            );

            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    @Override
    public boolean placeLiquid(LevelAccessor level, BlockPos pos, BlockState state, FluidState fluidState) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (fluidState.getType() == Fluids.WATER && blockEntity instanceof BetterCampfireBlockEntity campfireBlockEntity) {
            if (!level.isClientSide()) {
                removePotItem(campfireBlockEntity, state, (Level) level, pos, null, true);
                WorldExtensionsKt.playSoundServer(
                        (Level) level,
                        pos.getCenter(),
                        SoundEvents.GENERIC_EXTINGUISH_FIRE,
                        SoundSource.BLOCKS,
                        1.0F,
                        1.0F
                );
            }

            if (level.isClientSide()) {
                for (int i = 0; i <= 19; i++) {
                    CampfireBlock.makeParticles((Level) level, pos, false, true);
                }
            }

            level.scheduleTick(pos, fluidState.getType(), fluidState.getType().getTickDelay(level));
            return true;
        } else {
            return false;
        }
    }

    private void removePotItem(BetterCampfireBlockEntity blockEntity, BlockState blockState, Level level, BlockPos blockPos, @Nullable Player player, boolean byWater) {
        if (!byWater && player != null && !player.getItemInHand(InteractionHand.MAIN_HAND).isEmpty()) {
            return;
        }

        ItemStack potItem = blockEntity.getPotItem();

        if (!byWater && player != null) {
            if (!player.isCreative() && potItem != null) {
                player.setItemInHand(InteractionHand.MAIN_HAND, potItem);
            }
        } else {
            Direction direction = blockState.getValue(HorizontalDirectionalBlock.FACING);
            float f = 0.25F * direction.getStepX();
            float g = 0.25F * direction.getStepZ();

            ItemEntity itemEntity = new ItemEntity(
                    level,
                    blockPos.getX() + 0.5 + f,
                    blockPos.getY() + 1,
                    blockPos.getZ() + 0.5 + g,
                    potItem
            );
            itemEntity.setDefaultPickUpDelay();
            level.addFreshEntity(itemEntity);
        }

        blockEntity.setPotItem(ItemStack.EMPTY);
        WorldExtensionsKt.playSoundServer(
                level,
                blockPos.getBottomCenter(),
                CobblemonSounds.CAMPFIRE_POT_RETRIEVE,
                SoundSource.NEUTRAL,
                1F,
                1F
        );

        Containers.dropContents(level, blockPos, blockEntity.dropContents());

        Direction facing = blockState.getValue(HorizontalDirectionalBlock.FACING);
        blockEntity.setRemoved();

        BlockState newBlockState = isSoul
                ? Blocks.SOUL_CAMPFIRE.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, facing)
                : Blocks.CAMPFIRE.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, facing);

        if (byWater) {
            newBlockState = newBlockState
                    .setValue(BlockStateProperties.WATERLOGGED, true)
                    .setValue(CampfireBlock.LIT, false);
        }

        level.setBlockAndUpdate(blockPos, newBlockState);
    }

    public void openContainer(Level level, BlockPos pos, Player player) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof BetterCampfireBlockEntity campfireBlockEntity) {
            player.openMenu(campfireBlockEntity);
            ServerPlayNetworking.send((ServerPlayer) player, new ValidItemSyncPacket(campfireBlockEntity.getValidInputItem(), campfireBlockEntity.getValidSeasoningItem()));
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HorizontalDirectionalBlock.FACING, ITEM_DIRECTION, POWERED, COOKING, LID);
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor world, BlockPos pos, BlockPos neighborPos) {
        if (direction == state.getValue(HorizontalDirectionalBlock.FACING) && !state.canSurvive(world, pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, direction, neighborState, world, pos, neighborPos);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return AbstractContainerMenu.getRedstoneSignalFromBlockEntity(level.getBlockEntity(pos));
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType type) {
        return false;
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return createCookingPotTicker(level, blockEntityType, BetterCampfirePot.BETTER_CAMPFIRE_BE);
    }

    @Nullable
    protected <T extends BlockEntity> BlockEntityTicker<T> createCookingPotTicker(Level level, BlockEntityType<T> serverType, BlockEntityType<? extends BetterCampfireBlockEntity> clientType) {
        if (level.isClientSide) {
            return createTickerHelper(serverType, clientType, BetterCampfireBEClient::clientTick);
        } else {
            return createTickerHelper(serverType, clientType, BetterCampfireBlockEntity::serverTick);
        }
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new BetterCampfireBlockEntity(blockPos, blockState);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(10) == 0) {
            level.playLocalSound(
                    pos.getX() + 0.5,
                    pos.getY() + 0.5,
                    pos.getZ() + 0.5,
                    SoundEvents.CAMPFIRE_CRACKLE,
                    SoundSource.BLOCKS,
                    0.5f + random.nextFloat(),
                    random.nextFloat() * 0.7f + 0.6f,
                    false
            );
        }
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
        return isSoul ? new ItemStack(Blocks.SOUL_CAMPFIRE) : new ItemStack(Blocks.CAMPFIRE);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
        boolean isPowered = level.hasNeighborSignal(pos);
        BlockEntity blockEntity = level.getBlockEntity(pos);

        if (!(blockEntity instanceof BetterCampfireBlockEntity campfireBlockEntity)) {
            return;
        }

        if (isPowered != state.getValue(POWERED)) {
            level.setBlock(pos, state.setValue(POWERED, isPowered), UPDATE_ALL);
            campfireBlockEntity.toggleLid(!isPowered);
        }
    }

    @Override
    protected boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof BetterCampfireBlockEntity campfireBlockEntity) {
                if (level instanceof ServerLevel) {
                    Containers.dropContents(level, pos, campfireBlockEntity.dropContents());
                    ItemStack potItem = campfireBlockEntity.getPotItem();

                    if (potItem == null) {
                        potItem = ItemStack.EMPTY;
                    }

                    if (!potItem.isEmpty()) {
                        Direction direction = state.getValue(HorizontalDirectionalBlock.FACING);
                        float f = 0.25F * direction.getStepX();
                        float g = 0.25F * direction.getStepZ();

                        ItemEntity itemEntity = new ItemEntity(
                                level,
                                pos.getX() + 0.5 + f,
                                pos.getY() + 1,
                                pos.getZ() + 0.5 + g,
                                potItem
                        );
                        itemEntity.setDefaultPickUpDelay();
                        level.addFreshEntity(itemEntity);
                    }
                }

                super.onRemove(state, level, pos, newState, movedByPiston);
                level.updateNeighbourForOutputSignal(pos, this);
            } else {
                super.onRemove(state, level, pos, newState, movedByPiston);
            }
        }
    }
}
