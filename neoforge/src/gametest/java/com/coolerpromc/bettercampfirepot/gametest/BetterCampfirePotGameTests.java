package com.coolerpromc.bettercampfirepot.gametest;

import com.cobblemon.mod.common.block.campfirepot.CampfirePotColor;
import com.coolerpromc.bettercampfirepot.BetterCampfirePot;
import com.coolerpromc.bettercampfirepot.BetterCampfirePotConfig;
import com.coolerpromc.bettercampfirepot.block.BetterCampfireBlock;
import com.coolerpromc.bettercampfirepot.block.entity.BetterCampfireBlockEntity;
import com.coolerpromc.bettercampfirepot.menu.CookingPotMenu;
import com.coolerpromc.bettercampfirepot.network.CapabilityChangeSyncC2SPacket;
import com.coolerpromc.bettercampfirepot.network.ToggleLockSlotPacket;
import com.coolerpromc.bettercampfirepot.platform.util.PayloadContext;
import com.coolerpromc.bettercampfirepot.recipe.BetterCampfirePotRecipe;
import com.coolerpromc.bettercampfirepot.util.CampfirePotSlot;
import com.coolerpromc.bettercampfirepot.util.Side;
import com.coolerpromc.bettercampfirepot.util.Tiers;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.Optional;

@GameTestHolder(BetterCampfirePot.MODID)
@PrefixGameTestTemplate(false)
public class BetterCampfirePotGameTests {
    private static final BlockPos POT = new BlockPos(3, 1, 3);

    @GameTest(template = "empty", timeoutTicks = 200)
    public static void cooksARecipeAndStopsCooking(GameTestHelper helper) {
        BetterCampfirePotRecipe recipe = findSimpleRecipe(helper);
        BetterCampfireBlockEntity pot = placePot(helper, Tiers.NETHERITE);
        fillInputs(pot, recipe);

        helper.succeedWhen(() -> {
            helper.assertTrue(!pot.outputHandler.getItem(0).isEmpty(), "Pot did not produce an output");
            helper.assertTrue(pot.outputHandler.getItem(0).is(recipe.result().getItem()), "Output is not the recipe result");
            helper.assertTrue(pot.getIngredients().stream().allMatch(ItemStack::isEmpty), "Ingredients were not consumed");
            helper.assertTrue(!helper.getBlockState(POT).getValue(BetterCampfireBlock.COOKING), "Pot still shows as cooking after the ingredients ran out");
        });
    }

    @GameTest(template = "empty", timeoutTicks = 200)
    public static void recipeRebuildKeepsCooking(GameTestHelper helper) {
        BetterCampfirePotRecipe recipe = findSimpleRecipe(helper);
        BetterCampfireBlockEntity pot = placePot(helper, Tiers.NETHERITE);
        fillInputs(pot, recipe);

        // Same path as /reload: every pot's cached recipe match becomes stale
        BetterCampfirePotRecipe.onServerStarted(helper.getLevel().getServer());

        helper.succeedWhen(() -> helper.assertTrue(!pot.outputHandler.getItem(0).isEmpty(), "Pot did not cook after the recipe list was rebuilt"));
    }

    @GameTest(template = "empty", timeoutTicks = 200)
    public static void blockedOutputStopsShowingCooking(GameTestHelper helper) {
        BetterCampfirePotRecipe recipe = findSimpleRecipe(helper);
        BetterCampfireBlockEntity pot = placePot(helper, Tiers.COPPER);
        fillInputs(pot, recipe);

        helper.startSequence()
                .thenExecuteAfter(5, () -> {
                    helper.assertTrue(helper.getBlockState(POT).getValue(BetterCampfireBlock.COOKING), "Pot should be cooking");
                    pot.outputHandler.setItem(0, new ItemStack(Items.STICK));
                })
                .thenExecuteAfter(3, () -> {
                    helper.assertTrue(!helper.getBlockState(POT).getValue(BetterCampfireBlock.COOKING), "Pot still shows as cooking while its output is blocked");
                    helper.assertTrue(pot.getCookingProgress() == 0, "Progress should be reset while the output is blocked");
                    helper.assertTrue(pot.getIngredients().stream().anyMatch(stack -> !stack.isEmpty()), "Ingredients must not be consumed while blocked");
                })
                .thenSucceed();
    }

    @GameTest(template = "empty")
    public static void breakingDropsAndClearsContents(GameTestHelper helper) {
        BetterCampfireBlockEntity pot = placePot(helper, Tiers.IRON);
        pot.inputHandler.setItem(0, new ItemStack(Items.POTATO, 5));
        pot.outputHandler.setItem(0, new ItemStack(Items.APPLE, 3));

        helper.destroyBlock(POT);

        helper.assertItemEntityPresent(Items.POTATO, POT, 2.0);
        helper.assertItemEntityPresent(Items.APPLE, POT, 2.0);
        helper.assertTrue(pot.inputHandler.isEmpty() && pot.outputHandler.isEmpty(), "Contents were dropped but left in the pot, so an open menu could still take them");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void menuClosesWhenPotIsGone(GameTestHelper helper) {
        BetterCampfireBlockEntity pot = placePot(helper, Tiers.IRON);
        Player player = playerNextToPot(helper);
        CookingPotMenu menu = openMenu(player, pot);

        helper.assertTrue(menu.stillValid(player), "Menu should be valid next to the pot");
        helper.assertTrue(CookingPotMenu.getOpenBlockEntity(player, pot.getBlockPos()) == pot, "Open pot should be found");
        helper.assertTrue(CookingPotMenu.getOpenBlockEntity(player, pot.getBlockPos().above()) == null, "A different position must not match the open pot");

        helper.destroyBlock(POT);
        helper.assertTrue(!menu.stillValid(player), "Menu must become invalid once the pot is gone");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void packetsOnlyAffectTheOpenPot(GameTestHelper helper) {
        BetterCampfireBlockEntity pot = placePot(helper, Tiers.IRON);
        BlockPos pos = pot.getBlockPos();

        Player stranger = playerNextToPot(helper);
        new CapabilityChangeSyncC2SPacket(pos, Side.TOP, CampfirePotSlot.OUTPUT).handle(context(stranger));
        new ToggleLockSlotPacket(pos).handle(context(stranger));
        helper.assertTrue(pot.getCapabilityBySide(Side.TOP) == CampfirePotSlot.SEASONING, "A player without the menu open changed the side config");
        helper.assertTrue(pot.dataAccess.get(BetterCampfireBlockEntity.IS_SLOT_LOCKED_INDEX) == 0, "A player without the menu open toggled the slot lock");

        Player user = playerNextToPot(helper);
        openMenu(user, pot);
        new CapabilityChangeSyncC2SPacket(pos, Side.TOP, CampfirePotSlot.OUTPUT).handle(context(user));
        new ToggleLockSlotPacket(pos).handle(context(user));
        helper.assertTrue(pot.getCapabilityBySide(Side.TOP) == CampfirePotSlot.OUTPUT, "The player using the pot could not change the side config");
        helper.assertTrue(pot.dataAccess.get(BetterCampfireBlockEntity.IS_SLOT_LOCKED_INDEX) == 1, "The player using the pot could not toggle the slot lock");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 100)
    public static void autoOutputIsOffByDefaultAndPushesWhenEnabled(GameTestHelper helper) {
        BetterCampfireBlockEntity pot = placePot(helper, Tiers.IRON);
        Direction itemDirection = helper.getBlockState(POT).getValue(BetterCampfireBlock.ITEM_DIRECTION);
        BlockPos chestPos = POT.relative(itemDirection);
        helper.setBlock(chestPos, Blocks.CHEST);
        ChestBlockEntity chest = helper.getBlockEntity(chestPos);
        pot.outputHandler.setItem(0, new ItemStack(Items.APPLE, 4));

        boolean configured = BetterCampfirePotConfig.CONFIG.autoOutput.get();
        helper.startSequence()
                .thenExecuteAfter(20, () -> {
                    if (!configured) {
                        helper.assertTrue(pot.outputHandler.getItem(0).getCount() == 4 && chest.isEmpty(), "Output moved although auto output is off");
                    }
                    BetterCampfirePotConfig.CONFIG.autoOutput.set(true);
                })
                .thenExecuteAfter(20, () -> {
                    BetterCampfirePotConfig.CONFIG.autoOutput.set(configured);
                    helper.assertTrue(pot.outputHandler.isEmpty(), "Auto output did not empty the output slot");
                    helper.assertTrue(chest.countItem(Items.APPLE) == 4, "Auto output did not move the dish into the chest");
                })
                .thenSucceed();
    }

    @GameTest(template = "empty")
    public static void sidedItemHandlersMatchSideConfig(GameTestHelper helper) {
        BetterCampfireBlockEntity pot = placePot(helper, Tiers.IRON);
        Level level = helper.getLevel();
        BlockPos pos = pot.getBlockPos();

        helper.assertTrue(slots(level, pos, Direction.UP) == 3, "Top should expose the seasoning slots");
        helper.assertTrue(slots(level, pos, Direction.DOWN) == 1, "Bottom should expose the output slot");
        helper.assertTrue(slots(level, pos, Direction.NORTH) == 9, "Front should expose the input slots");
        helper.assertTrue(slots(level, pos, null) == 13, "No side should expose every slot");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void lockedSlotsFillLowestSlotFirst(GameTestHelper helper) {
        BetterCampfireBlockEntity pot = placePot(helper, Tiers.IRON);
        pot.inputHandler.setItem(0, new ItemStack(Items.POTATO, 10));
        pot.inputHandler.setItem(1, new ItemStack(Items.POTATO, 1));
        pot.inputHandler.setItem(2, new ItemStack(Items.POTATO, 5));
        pot.updateItemBySlot();
        pot.toggleLockSlot();

        IItemHandler input = helper.getLevel().getCapability(Capabilities.ItemHandler.BLOCK, pot.getBlockPos(), Direction.NORTH);
        helper.assertTrue(input != null, "Front item handler missing");
        ItemStack rest = input.insertItem(0, new ItemStack(Items.POTATO, 3), false);

        helper.assertTrue(rest.isEmpty(), "Insert was not fully accepted");
        helper.assertTrue(pot.inputHandler.getItem(1).getCount() == 4, "Locked insert should go to the lowest-filled valid slot first");
        helper.assertTrue(pot.inputHandler.getItem(0).getCount() == 10, "Fuller slot should not have been filled first");
        helper.assertTrue(input.insertItem(3, new ItemStack(Items.CARROT), true).getCount() == 1, "Locked slots must reject other items");
        helper.succeed();
    }

    private static BetterCampfireBlockEntity placePot(GameTestHelper helper, String tier) {
        BlockState state = BetterCampfirePot.BETTER_CAMPFIRE_BLOCK.get().defaultBlockState().setValue(BetterCampfireBlock.LID, true);
        helper.setBlock(POT, state);
        BetterCampfireBlockEntity pot = helper.getBlockEntity(POT);
        pot.setPotItem(new ItemStack(BetterCampfirePot.findBetterPotByTierAndColor(CampfirePotColor.RED, tier).orElseThrow()));
        return pot;
    }

    private static BetterCampfirePotRecipe findSimpleRecipe(GameTestHelper helper) {
        Optional<BetterCampfirePotRecipe> recipe = BetterCampfirePotRecipe.getRecipes().stream()
                .filter(r -> !r.result().isEmpty() && r.ingredients().size() <= 9)
                .filter(r -> r.ingredients().stream().allMatch(pair -> pair.getFirst().getItems().length > 0 && pair.getSecond() <= pair.getFirst().getItems()[0].getMaxStackSize()))
                .findFirst();
        if (recipe.isEmpty()) {
            helper.fail("No campfire pot recipes are loaded");
        }
        return recipe.get();
    }

    private static void fillInputs(BetterCampfireBlockEntity pot, BetterCampfirePotRecipe recipe) {
        int slot = 0;
        for (Pair<Ingredient, Integer> pair : recipe.ingredients()) {
            pot.inputHandler.setItem(slot++, pair.getFirst().getItems()[0].copyWithCount(pair.getSecond()));
        }
    }

    private static Player playerNextToPot(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        BlockPos pos = helper.absolutePos(POT);
        player.setPos(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 1.5);
        return player;
    }

    private static CookingPotMenu openMenu(Player player, BetterCampfireBlockEntity pot) {
        CookingPotMenu menu = new CookingPotMenu(1, player.getInventory(), pot, pot.dataAccess);
        player.containerMenu = menu;
        return menu;
    }

    private static int slots(Level level, BlockPos pos, Direction side) {
        IItemHandler handler = level.getCapability(Capabilities.ItemHandler.BLOCK, pos, side);
        return handler == null ? -1 : handler.getSlots();
    }

    private static PayloadContext context(Player player) {
        return new PayloadContext() {
            @Override
            public Player player() {
                return player;
            }

            @Override
            public Level level() {
                return player.level();
            }

            @Override
            public void execute(Runnable runnable) {
                runnable.run();
            }

            @Override
            public void disconnect(Component reason) {
            }
        };
    }
}
