package com.coolerpromc.bettercampfirepot;

import com.coolerpromc.bettercampfirepot.block.entity.BetterCampfireBlockEntity;
import com.coolerpromc.bettercampfirepot.compat.StoveHeatSources;
import com.coolerpromc.bettercampfirepot.item.BetterCampfirePotItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(BetterCampfirePot.MODID)
@PrefixGameTestTemplate(false)
public class StoveCompatibilityTests {
    private static BlockState stove(String id) {
        return BuiltInRegistries.BLOCK.get(ResourceLocation.parse(id)).defaultBlockState();
    }

    @GameTest(template = "empty")
    public static void realStoveHeatStates(GameTestHelper helper) {
        for (String id : new String[]{"farmersdelight:stove", "kaleidoscope_cookery:stove"}) {
            BlockState state = stove(id);
            helper.assertTrue(StoveHeatSources.isStove(state), id + " must be in the production stove tag");
            helper.assertTrue(!StoveHeatSources.isHot(state.setValue(BlockStateProperties.LIT, false)), "Unlit stove must not heat: " + id);
            helper.assertTrue(StoveHeatSources.isHot(state.setValue(BlockStateProperties.LIT, true)), "Lit stove must heat: " + id);
        }
        BlockState cobble = stove("cobblefurnies:red_stove");
        helper.assertTrue(StoveHeatSources.isHot(cobble), "CobbleFurnies stove must heat without fuel");
        helper.assertTrue(!StoveHeatSources.isHot(cobble.setValue(BlockStateProperties.WATERLOGGED, true)), "Waterlogged stove must not heat");
        helper.assertTrue(!StoveHeatSources.isHot(Blocks.FURNACE.defaultBlockState()), "Test fixtures must not add vanilla furnaces to the stove tag");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void realStoveInteractions(GameTestHelper helper) {
        var level = helper.getLevel();
        var player = FakePlayerFactory.getMinecraft(level);
        player.setShiftKeyDown(false);
        BetterCampfirePotItem item = BetterCampfirePot.CAMPFIRE_POT_ITEMS.getFirst().get();
        for (String id : new String[]{"farmersdelight:stove", "cobblefurnies:red_stove", "kaleidoscope_cookery:stove"}) {
            BlockPos stovePos = helper.absolutePos(new BlockPos(1, 1, 1));
            BlockPos potPos = stovePos.above();
            level.setBlockAndUpdate(stovePos, stove(id));
            var stoveEntity = level.getBlockEntity(stovePos);
            if (stoveEntity instanceof Container inventory) inventory.setItem(0, new ItemStack(Items.RAW_IRON, 3));
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(item, 2));
            var hit = new BlockHitResult(stovePos.getCenter().add(0, 0.5, 0), Direction.UP, stovePos, false);
            // Use the actual interaction pipeline, so stove block handlers run unless our hook intercepts them.
            var result = player.gameMode.useItemOn(player, level, player.getMainHandItem(), InteractionHand.MAIN_HAND, hit);
            helper.assertTrue(result.consumesAction(), "Placement must succeed for " + id);
            helper.assertTrue(level.getBlockEntity(potPos) instanceof BetterCampfireBlockEntity, "Real interaction must create a cooking pot on " + id);
            helper.assertTrue(player.getMainHandItem().getCount() == 1, "Placement must consume one pot: " + id);
            helper.assertTrue(level.getBlockEntity(stovePos) == stoveEntity, "Stove entity must be preserved: " + id);
            if (stoveEntity instanceof Container inventory) helper.assertTrue(inventory.getItem(0).getCount() == 3, "Stove inventory must be preserved: " + id);
            player.gameMode.useItemOn(player, level, player.getMainHandItem(), InteractionHand.MAIN_HAND, hit);
            helper.assertTrue(player.getMainHandItem().getCount() == 1, "Blocked placement must not consume a pot: " + id);
            level.setBlockAndUpdate(stovePos, Blocks.AIR.defaultBlockState());
            helper.assertTrue(level.getBlockState(potPos).isAir(), "Removing stove must remove unsupported pot: " + id);
            var drops = level.getEntitiesOfClass(ItemEntity.class, new AABB(potPos).inflate(2));
            int pots = drops.stream().filter(entity -> entity.getItem().is(item)).mapToInt(entity -> entity.getItem().getCount()).sum();
            helper.assertTrue(pots == 1, "Removing stove must drop exactly one pot: " + id);
            drops.forEach(ItemEntity::discard);
        }
        helper.succeed();
    }
    @GameTest(template = "empty", timeoutTicks = 160)
    public static void realStovesCompleteRecipesAndReportHeat(GameTestHelper helper) {
        var level = helper.getLevel();
        String[] ids = {"farmersdelight:stove", "cobblefurnies:red_stove", "kaleidoscope_cookery:stove", "kaleidoscope_cookery:stove"};
        var pots = new java.util.ArrayList<BetterCampfireBlockEntity>();
        for (int i = 0; i < ids.length; i++) {
            BlockPos pos = helper.absolutePos(new BlockPos(i + 1, 1, 1));
            BlockState source = stove(ids[i]);
            if (i < 3 && source.hasProperty(BlockStateProperties.LIT)) source = source.setValue(BlockStateProperties.LIT, true);
            level.setBlockAndUpdate(pos, source);
            level.setBlockAndUpdate(pos.above(), BetterCampfirePot.STOVE_POT_BLOCK.get().defaultBlockState()
                    .setValue(com.coolerpromc.bettercampfirepot.block.BetterCampfireBlock.LID, true));
            var pot = (BetterCampfireBlockEntity) level.getBlockEntity(pos.above());
            pot.setPotItem(new ItemStack(BuiltInRegistries.ITEM.get(BetterCampfirePot.id("netherite_black_campfire_pot"))));
            pot.inputHandler.setItem(0, new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse("cobblemon:exp_candy_l")), 3));
            pots.add(pot);
        }
        helper.runAfterDelay(100, () -> {
            for (int i = 0; i < 3; i++) {
                var pot = pots.get(i);
                helper.assertTrue(pot.inputHandler.getItem(0).isEmpty(), "Recipe must consume inputs on " + ids[i]);
                helper.assertTrue(pot.outputHandler.getItem(0).is(BuiltInRegistries.ITEM.get(ResourceLocation.parse("cobblemon:exp_candy_xl")))
                        && pot.outputHandler.getItem(0).getCount() == 1, "Recipe must produce one XL candy on " + ids[i]);
                helper.assertTrue(pot.dataAccess.get(BetterCampfireBlockEntity.HAS_HEAT_INDEX) == 1, "Menu must report heat on " + ids[i]);
            }
            var cold = pots.get(3);
            helper.assertTrue(cold.inputHandler.getItem(0).getCount() == 3 && cold.outputHandler.getItem(0).isEmpty(), "Unlit stove must preserve ingredients without output");
            helper.assertTrue(cold.dataAccess.get(BetterCampfireBlockEntity.HAS_HEAT_INDEX) == 0, "Menu must explain missing heat");
            helper.succeed();
        });
    }

}
