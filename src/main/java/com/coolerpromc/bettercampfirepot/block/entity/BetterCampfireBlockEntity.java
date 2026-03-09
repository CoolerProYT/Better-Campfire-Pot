package com.coolerpromc.bettercampfirepot.block.entity;

import com.cobblemon.mod.common.CobblemonSounds;
import com.cobblemon.mod.common.client.particle.BedrockParticleOptionsRepository;
import com.cobblemon.mod.common.client.particle.ParticleStorm;
import com.cobblemon.mod.common.client.render.MatrixWrapper;
import com.cobblemon.mod.common.client.sound.BlockEntitySoundTracker;
import com.cobblemon.mod.common.item.components.PotComponent;
import com.cobblemon.mod.common.util.WorldExtensionsKt;
import com.coolerpromc.bettercampfirepot.BetterCampfirePot;
import com.coolerpromc.bettercampfirepot.BetterCampfirePotConfig;
import com.coolerpromc.bettercampfirepot.block.BetterCampfireBlock;
import com.coolerpromc.bettercampfirepot.item.BetterCampfirePotItem;
import com.coolerpromc.bettercampfirepot.menu.CookingPotMenu;
import com.coolerpromc.bettercampfirepot.recipe.BetterCampfirePotRecipe;
import com.coolerpromc.bettercampfirepot.util.CampfirePotSlot;
import com.coolerpromc.bettercampfirepot.util.KotlinHelper;
import com.coolerpromc.bettercampfirepot.util.Side;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.FastColor;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector4f;

import java.util.*;

public class BetterCampfireBlockEntity extends BlockEntity implements ExtendedScreenHandlerFactory<BlockPos> {
    public static final int COOKING_TOTAL_TIME = 200;

    public static final int COOKING_PROGRESS_INDEX = 0;
    public static final int COOKING_PROGRESS_TOTAL_TIME_INDEX = 1;
    public static final int IS_LID_OPEN_INDEX = 2;
    public static final int COOKING_POT_COLOR_INDEX = 3;
    public static final int IS_SLOT_LOCKED_INDEX = 4;

    public static final int BASE_BROTH_COLOR = 0xFDFACF;
    public static final int BASE_BROTH_BUBBLE_COLOR = 0xFFFEFDE4;

    public final SoundEvent runningSound = CobblemonSounds.CAMPFIRE_POT_ACTIVE;
    public final SoundEvent ambientSound = CobblemonSounds.CAMPFIRE_POT_AMBIENT;
    private int cookingProgress = 0;
    private int cookingTotalTime = COOKING_TOTAL_TIME;
    private PotComponent potComponent = null;
    public int particleCooldown = 0;
    public int brothColor = BASE_BROTH_COLOR;
    public int bubbleColor = BASE_BROTH_BUBBLE_COLOR;
    public int time = 0;
    public BetterCampfirePotRecipe currentRecipe;
    public int progressPerTick = 2;
    private Map<Side, CampfirePotSlot> capabilityBySide = new HashMap<>();
    private final NonNullList<Item> validInputItem = NonNullList.withSize(9, Items.AIR);
    private final NonNullList<Item> validSeasoningItem = NonNullList.withSize(3, Items.AIR);
    private boolean lockSlot = false;

    public List<ItemStack> lastSeasoningStacks = new ArrayList<>();

    public final SimpleContainer inputHandler = new SimpleContainer(9){
        @Override
        public void setChanged() {
            super.setChanged();
            if (level != null){
                onItemUpdate(level);
            }
        }

        @Override
        public boolean canPlaceItem(int slot, ItemStack stack) {
            if (!lockSlot){
                return true;
            }
            return stack.is(BetterCampfireBlockEntity.this.validInputItem.get(slot));
        }
    };
    public final SimpleContainer seasoningHandler = new SimpleContainer(3){
        @Override
        public void setChanged() {
            super.setChanged();
            if (level != null){
                onItemUpdate(level);
            }
        }

        @Override
        public boolean canPlaceItem(int slot, ItemStack stack) {
            if (currentRecipe == null) return false;
            boolean isSeasoningItem = stack.is(currentRecipe.seasoningTag());
            if (!lockSlot && isSeasoningItem){
                return true;
            }
            return isSeasoningItem && stack.is(BetterCampfireBlockEntity.this.validSeasoningItem.get(slot));
        }
    };
    public SimpleContainer outputHandler = new SimpleContainer(1){
        @Override
        public void setChanged() {
            super.setChanged();
            if (level != null){
                onItemUpdate(level);
            }
        }

        @Override
        public boolean canPlaceItem(int i, ItemStack itemStack) {
            return false;
        }
    };

    public final InventoryStorage inputStorage = InventoryStorage.of(inputHandler, null);
    public final InventoryStorage seasoningStorage = InventoryStorage.of(seasoningHandler, null);
    public final InventoryStorage outputStorage = InventoryStorage.of(outputHandler, null);

    public final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case COOKING_PROGRESS_INDEX -> BetterCampfireBlockEntity.this.cookingProgress;
                case COOKING_PROGRESS_TOTAL_TIME_INDEX -> BetterCampfireBlockEntity.this.cookingTotalTime;
                case IS_LID_OPEN_INDEX -> BetterCampfireBlockEntity.this.getBlockState().getValue(BetterCampfireBlock.LID) ? 0 : 1;
                case COOKING_POT_COLOR_INDEX -> {
                    ItemStack potItem = BetterCampfireBlockEntity.this.getPotItem();
                    if (potItem != null && potItem.getItem() instanceof BetterCampfirePotItem campfirePotItem) {
                        yield campfirePotItem.color.ordinal();
                    }
                    yield 0;
                }
                case IS_SLOT_LOCKED_INDEX -> BetterCampfireBlockEntity.this.lockSlot ? 1 : 0;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case COOKING_PROGRESS_INDEX -> BetterCampfireBlockEntity.this.cookingProgress = value;
                case COOKING_PROGRESS_TOTAL_TIME_INDEX -> BetterCampfireBlockEntity.this.cookingTotalTime = value;
                case IS_LID_OPEN_INDEX -> BetterCampfireBlockEntity.this.toggleLid(value == 1);
                case COOKING_POT_COLOR_INDEX -> {}
                case IS_SLOT_LOCKED_INDEX -> BetterCampfireBlockEntity.this.lockSlot = value == 1;
            }
        }

        @Override
        public int getCount() {
            return 5;
        }
    };

    public BetterCampfireBlockEntity(BlockPos pos, BlockState state) {
        super(BetterCampfirePot.BETTER_CAMPFIRE_BE, pos, state);
        initCapabilityBySide();
    }

    private void initCapabilityBySide(){
        capabilityBySide.putIfAbsent(Side.TOP, CampfirePotSlot.SEASONING);
        capabilityBySide.putIfAbsent(Side.BOTTOM, CampfirePotSlot.OUTPUT);
        capabilityBySide.putIfAbsent(Side.FRONT, CampfirePotSlot.INPUT);
        capabilityBySide.putIfAbsent(Side.BACK, CampfirePotSlot.INPUT);
        capabilityBySide.putIfAbsent(Side.LEFT, CampfirePotSlot.INPUT);
        capabilityBySide.putIfAbsent(Side.RIGHT, CampfirePotSlot.INPUT);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BetterCampfireBlockEntity campfireBlockEntity) {
        if (level.isClientSide) return;

        boolean hasChanged = false;
        boolean recipeChanged = false;

        if (campfireBlockEntity.getPotItem() != null && campfireBlockEntity.getPotItem().getItem() instanceof BetterCampfirePotItem potItem){
            campfireBlockEntity.progressPerTick = BetterCampfirePotConfig.CONFIG.getTickByTier(potItem.tier);
        }

        boolean isCookingBefore = campfireBlockEntity.cookingProgress > 0;
        BetterCampfirePotRecipe recipeBefore = campfireBlockEntity.currentRecipe;

        List<ItemStack> inputs = new ArrayList<>();

        for (int i = 0; i < campfireBlockEntity.inputHandler.getContainerSize(); i++) {
            ItemStack item = campfireBlockEntity.inputHandler.getItem(i);
            if (!item.isEmpty()) {
                inputs.add(item);
            }
        }

        Optional<BetterCampfirePotRecipe> optionalRecipe = fetchRecipe(inputs);

        if (optionalRecipe.isEmpty()) {
            if (campfireBlockEntity.currentRecipe != null) {
                recipeChanged = true;
            }
            campfireBlockEntity.cookingProgress = 0;
            campfireBlockEntity.currentRecipe = null;
        } else {
            BetterCampfirePotRecipe recipe = optionalRecipe.get();
            if (recipe != recipeBefore){
                recipeChanged = true;
            }
            campfireBlockEntity.currentRecipe = recipe;
            ItemStack cookedItem = recipe.result().copy();
            ItemStack resultSlotItem = campfireBlockEntity.outputHandler.getItem(0);

            List<ItemStack> filteredSeasonings = campfireBlockEntity.getSeasonings().stream().filter(s -> s.is(recipe.seasoningTag())).toList();
            recipe.applySeasoning(cookedItem, filteredSeasonings);

            if (!campfireBlockEntity.getBlockState().getValue(BetterCampfireBlock.LID)) {
                campfireBlockEntity.cookingProgress = 0;
            } else {
                if (!resultSlotItem.isEmpty()) {
                    if (!ItemStack.isSameItemSameComponents(resultSlotItem, cookedItem) || resultSlotItem.getCount() + cookedItem.getCount() > resultSlotItem.getMaxStackSize()) {
                        campfireBlockEntity.cookingProgress = 0;
                        return;
                    }
                }

                campfireBlockEntity.cookingProgress += campfireBlockEntity.progressPerTick;
                campfireBlockEntity.cookingProgress = Math.min(campfireBlockEntity.cookingProgress, campfireBlockEntity.cookingTotalTime);
                if (campfireBlockEntity.cookingProgress >= campfireBlockEntity.cookingTotalTime) {
                    campfireBlockEntity.cookingProgress = 0;

                    if (!cookedItem.isEmpty()) {
                        if (resultSlotItem.isEmpty()) {
                            campfireBlockEntity.outputHandler.setItem(0, cookedItem);
                        } else {
                            resultSlotItem.grow(cookedItem.getCount());
                        }

                        campfireBlockEntity.consumeCraftingIngredients(recipe, level, pos, state, campfireBlockEntity);

                        WorldExtensionsKt.playSoundServer(
                                level,
                                pos.getBottomCenter(),
                                CobblemonSounds.CAMPFIRE_POT_COOK,
                                SoundSource.NEUTRAL,
                                1F,
                                1F
                        );

                        hasChanged = true;
                    }
                }
            }
        }

        boolean isCookingNow = campfireBlockEntity.cookingProgress > 0;

        if (isCookingBefore != isCookingNow) {
            hasChanged = true;
            level.setBlock(pos, state.setValue(BetterCampfireBlock.COOKING, isCookingNow), 3);
        }

        if (recipeChanged){
            hasChanged = true;
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }

        if (hasChanged) {
            setChanged(level, pos, state);
        }
    }

    private static Optional<BetterCampfirePotRecipe> fetchRecipe(List<ItemStack> inputs) {
        return BetterCampfirePotRecipe.getRecipeFor(inputs);
    }

    public ParticleStorm particleEntityHandler(Vec3 position, Level level, ResourceLocation particle) {
        MatrixWrapper wrapper = new MatrixWrapper();
        PoseStack matrix = new PoseStack();
        wrapper.updateMatrix(matrix.last().pose());
        wrapper.updatePosition(position);

        var effect = BedrockParticleOptionsRepository.INSTANCE.getEffect(particle);
        if (effect == null) {
            throw new IllegalStateException("Particle with resource location " + particle + " not found");
        }

        float red = FastColor.ARGB32.red(bubbleColor) / 255F;
        float green = FastColor.ARGB32.green(bubbleColor) / 255F;
        float blue = FastColor.ARGB32.blue(bubbleColor) / 255F;
        float alpha = FastColor.ARGB32.alpha(bubbleColor) / 255F;
        Vector4f vector4f = new Vector4f(red, green, blue, alpha);

        ParticleStorm particleStorm = KotlinHelper.Companion.particleStorm(getBlockState(), effect, wrapper, (ClientLevel) level, vector4f);
        particleStorm.spawn();
        return particleStorm;
    }

    public void consumeCraftingIngredients(BetterCampfirePotRecipe recipe, Level level, BlockPos pos, BlockState state, BetterCampfireBlockEntity campfireBlockEntity) {
        Map<Item, Integer> remainderItems = new HashMap<>();

        consumeItem(recipe, remainderItems, campfireBlockEntity.inputHandler);

        for (int i = 0; i < campfireBlockEntity.seasoningHandler.getContainerSize(); i++) {
            ItemStack item = campfireBlockEntity.seasoningHandler.getItem(i);
            if (item.is(recipe.seasoningTag()) && recipe.seasoningProcessors().stream().anyMatch(p -> p.consumesItem(item))) {
                item.shrink(1);
                if (item.isEmpty()) {
                    campfireBlockEntity.seasoningHandler.setItem(i, ItemStack.EMPTY);
                }
                if (!item.getRecipeRemainder().isEmpty()) {
                    Item remainder = item.getRecipeRemainder().getItem();
                    remainderItems.put(remainder, remainderItems.getOrDefault(remainder, 0) + 1);
                }
            }
        }

        Direction direction = state.getValue(BetterCampfireBlock.ITEM_DIRECTION);
        Storage<ItemVariant> container = ItemStorage.SIDED.find(level, pos.relative(direction), direction.getOpposite());

        for (Map.Entry<Item, Integer> remainder : remainderItems.entrySet()) {
            ItemStack remainderItem = new ItemStack(remainder.getKey(), remainder.getValue());

            if (container != null) {
                try (Transaction transaction = Transaction.openOuter()) {
                    ItemVariant variant = ItemVariant.of(remainderItem);

                    long inserted = container.insert(variant, remainderItem.getCount(), transaction);

                    if (inserted > 0) {
                        remainderItem.shrink((int) inserted);
                        transaction.commit();
                    }
                }
            }

            if (!remainderItem.isEmpty()) {
                Vec3 spawnPos = Vec3.atCenterOf(pos).relative(direction, 0.7);
                ItemEntity itemEntity = new ItemEntity(level, spawnPos.x, spawnPos.y, spawnPos.z, remainderItem);
                itemEntity.setDeltaMovement(direction.getStepX() * 0.05, 0.0, direction.getStepZ() * 0.05);
                level.addFreshEntity(itemEntity);
            }
        }
    }

    private void consumeItem(BetterCampfirePotRecipe recipe, Map<Item, Integer> remainderItems, SimpleContainer inputHandler) {
        for (Pair<Ingredient, Integer> pair : recipe.ingredients()) {
            int neededCount = pair.getSecond();

            for (int slot = 0; slot < inputHandler.getContainerSize() && neededCount > 0; slot++) {
                ItemStack stack = inputHandler.getItem(slot);
                if (stack.isEmpty()) continue;

                if (pair.getFirst().test(stack)) {
                    int toConsume = Math.min(neededCount, stack.getCount());

                    if (stack.getItem().hasCraftingRemainingItem()) {
                        Item remainder = stack.getItem().getCraftingRemainingItem();
                        remainderItems.put(remainder, remainderItems.getOrDefault(remainder, 0) + toConsume);
                    }

                    stack.shrink(toConsume);
                    neededCount -= toConsume;

                    if (stack.isEmpty()) {
                        inputHandler.setItem(slot, ItemStack.EMPTY);
                    }
                }
            }
        }
    }

    public List<ItemStack> getSeasonings() {
        List<ItemStack> items = new ArrayList<>();
        for (int i = 0; i < seasoningHandler.getContainerSize(); i++) {
            items.add(seasoningHandler.getItem(i));
        }
        return items;
    }

    public List<ItemStack> getIngredients() {
        List<ItemStack> items = new ArrayList<>();
        for (int i = 0; i < inputHandler.getContainerSize(); i++) {
            items.add(inputHandler.getItem(i));
        }
        return items;
    }

    @Nullable
    public ItemStack getPotItem() {
        return potComponent != null ? potComponent.getPotItem() : null;
    }

    public void setPotItem(@Nullable ItemStack stack) {
        this.potComponent = new PotComponent(stack != null ? stack : ItemStack.EMPTY);
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("CookingProgress", this.cookingProgress);
        tag.put("inputHandler", ContainerHelper.saveAllItems(new CompoundTag(), inputHandler.items, registries));
        tag.put("seasoningHandler", ContainerHelper.saveAllItems(new CompoundTag(), seasoningHandler.items, registries));
        tag.put("outputHandler", ContainerHelper.saveAllItems(new CompoundTag(), outputHandler.items, registries));
        tag.putInt("progressPerTick", progressPerTick);
        tag.putBoolean("lockSlot", lockSlot);
        if (capabilityBySide.isEmpty()){
            this.initCapabilityBySide();
        }
        tag.put("capabilityBySide", ExtraCodecs.strictUnboundedMap(Side.CODEC, CampfirePotSlot.CODEC).encodeStart(NbtOps.INSTANCE, capabilityBySide).getOrThrow());

        if (potComponent != null) {
            PotComponent.Companion.getCODEC().encodeStart(NbtOps.INSTANCE, potComponent).result().ifPresent(encoded -> tag.put("PotComponent", encoded));
        }

        if (currentRecipe != null){
            BetterCampfirePotRecipe.CODEC.encodeStart(RegistryOps.create(NbtOps.INSTANCE, registries), currentRecipe).ifError(d -> System.out.println(d.message())).ifSuccess(encoded -> tag.put("currentRecipe", encoded));
        }
        BuiltInRegistries.ITEM.byNameCodec().listOf().encodeStart(RegistryOps.create(NbtOps.INSTANCE, registries), validInputItem).ifError(e -> System.out.println(e.message())).ifSuccess(encoded ->  tag.put("validInputItem", encoded));
        BuiltInRegistries.ITEM.byNameCodec().listOf().encodeStart(RegistryOps.create(NbtOps.INSTANCE, registries), validSeasoningItem).ifError(e -> System.out.println(e.message())).ifSuccess(encoded ->  tag.put("validSeasoningItem", encoded));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.cookingProgress = tag.getInt("CookingProgress");
        ContainerHelper.loadAllItems(tag.getCompound("inputHandler"), inputHandler.items, registries);
        ContainerHelper.loadAllItems(tag.getCompound("seasoningHandler"), seasoningHandler.items, registries);
        ContainerHelper.loadAllItems(tag.getCompound("outputHandler"), outputHandler.items, registries);
        this.progressPerTick = tag.getInt("progressPerTick");
        this.lockSlot = tag.getBoolean("lockSlot");
        this.capabilityBySide = new HashMap<>(ExtraCodecs.strictUnboundedMap(Side.CODEC, CampfirePotSlot.CODEC).parse(NbtOps.INSTANCE, tag.getCompound("capabilityBySide")).getOrThrow());
        if(capabilityBySide.isEmpty()){
            this.initCapabilityBySide();
        }

        if (tag.contains("PotComponent")) {
            PotComponent.Companion.getCODEC().parse(NbtOps.INSTANCE, tag.getCompound("PotComponent")).result().ifPresent(component -> this.potComponent = component);
        }

        if (tag.contains("currentRecipe")) {
            BetterCampfirePotRecipe.CODEC.parse(RegistryOps.create(NbtOps.INSTANCE, registries), tag.getCompound("currentRecipe")).result().ifPresent(recipe -> this.currentRecipe = recipe);
        }
        else {
            this.currentRecipe = null;
        }
        BuiltInRegistries.ITEM.byNameCodec().listOf().parse(RegistryOps.create(NbtOps.INSTANCE, registries), tag.get("validInputItem")).ifError(System.out::println).result().ifPresent(items -> {
            this.validInputItem.clear();
            for (int i = 0; i < items.size(); i++) {
                this.validInputItem.set(i, items.get(i));
            }
        });
        BuiltInRegistries.ITEM.byNameCodec().listOf().parse(RegistryOps.create(NbtOps.INSTANCE, registries), tag.get("validSeasoningItem")).result().ifPresent(items ->{
            this.validSeasoningItem.clear();
            for (int i = 0; i < items.size(); i++) {
                this.validSeasoningItem.set(i, items.get(i));
            }
        });
    }

    @Override
    @Nullable
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registryLookup) {
        return saveWithoutMetadata(registryLookup);
    }

    public void onItemUpdate(Level level) {
        BlockState oldState = level.getBlockState(getBlockPos());
        level.sendBlockUpdated(getBlockPos(), oldState, level.getBlockState(getBlockPos()), Block.UPDATE_ALL);
        level.updateNeighbourForOutputSignal(getBlockPos(), level.getBlockState(getBlockPos()).getBlock());
        setChanged();
        level.sendBlockUpdated(getBlockPos(), oldState, level.getBlockState(getBlockPos()), Block.UPDATE_ALL);
    }

    @Override
    public void setRemoved() {
        cookingProgress = 0;
        super.setRemoved();

        if (level != null && level.isClientSide) {
            BlockEntitySoundTracker.INSTANCE.stop(getBlockPos(), runningSound.getLocation());
            BlockEntitySoundTracker.INSTANCE.stop(getBlockPos(), ambientSound.getLocation());
        }
    }

    public void toggleLid(boolean isOpen) {
        if (level != null) {
            WorldExtensionsKt.playSoundServer(
                    level,
                    getBlockPos().getCenter(),
                    isOpen ? CobblemonSounds.CAMPFIRE_POT_OPEN : CobblemonSounds.CAMPFIRE_POT_CLOSE,
                    SoundSource.NEUTRAL,
                    1F,
                    1F
            );
            level.gameEvent(null, isOpen ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, getBlockPos());
            level.setBlock(getBlockPos(), getBlockState().setValue(BetterCampfireBlock.LID, !isOpen), 3);
        }
        setChanged();
    }

    public SimpleContainer dropContents(){
        SimpleContainer container = new SimpleContainer();
        for (int i = 0; i < inputHandler.getContainerSize(); i++) {
            container.addItem(inputHandler.getItem(i));
        }
        for (int i = 0; i < seasoningHandler.getContainerSize(); i++) {
            container.addItem(seasoningHandler.getItem(i));
        }
        for (int i = 0; i < outputHandler.getContainerSize(); i++) {
            container.addItem(outputHandler.getItem(i));
        }
        return container;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("tier.bettercampfirepot." + ((BetterCampfirePotItem) getPotItem().getItem()).tier).append(" ").append(Component.translatable("cobblemon.container.campfire_pot"));
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return new CookingPotMenu(i, inventory, this, dataAccess);
    }

    @Override
    public BlockPos getScreenOpeningData(ServerPlayer player) {
        return getBlockPos();
    }

    public @Nullable Storage<ItemVariant> getCapability(@Nullable Direction direction) {
        if (direction == null) return null;

        Direction facing = getBlockState().getValue(HorizontalDirectionalBlock.FACING);
        Side side = Side.fromDirection(direction, facing);

        CampfirePotSlot slot = getCapabilityBySide(side);
        if (slot == null) return null;

        return getItemHandlerForSlot(slot);
    }

    private Storage<ItemVariant> getItemHandlerForSlot(CampfirePotSlot slot){
        return switch (slot){
            case INPUT -> inputStorage;
            case SEASONING -> seasoningStorage;
            case OUTPUT -> outputStorage;
        };
    }

    public CampfirePotSlot getCapabilityBySide(Side side){
        return capabilityBySide.get(side);
    }

    public void setCapabilityBySide(Side side, CampfirePotSlot slot) {
        this.capabilityBySide.put(side, slot);
        setChanged();
        level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
    }

    public void updateItemBySlot(){
        for (int i = 0; i < inputHandler.getContainerSize(); i++) {
            this.validInputItem.set(i, inputHandler.getItem(i).getItem());
        }
        for (int i = 0; i < seasoningHandler.getContainerSize(); i++) {
            this.validSeasoningItem.set(i, seasoningHandler.getItem(i).getItem());
        }
        setChanged();
        level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
    }

    public void toggleLockSlot(){
        this.lockSlot = !this.lockSlot;
        setChanged();
        level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
    }

    public NonNullList<Item> getValidInputItem() {
        return validInputItem;
    }

    public NonNullList<Item> getValidSeasoningItem() {
        return validSeasoningItem;
    }
}
