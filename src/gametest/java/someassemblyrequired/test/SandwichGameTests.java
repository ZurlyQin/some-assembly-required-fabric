package someassemblyrequired.test;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.GameType;
import someassemblyrequired.SomeAssemblyRequired;
import someassemblyrequired.block.SandwichBlockEntity;
import someassemblyrequired.ingredient.Ingredients;
import someassemblyrequired.integration.farmersdelight.FarmersDelightCompat;
import someassemblyrequired.item.sandwich.SandwichContents;
import someassemblyrequired.item.sandwich.SandwichItem;
import someassemblyrequired.registry.*;
import vectorwing.farmersdelight.common.crafting.CuttingBoardRecipe;
import vectorwing.farmersdelight.common.crafting.CuttingBoardRecipeInput;

public class SandwichGameTests implements FabricGameTest {
    @GameTest(template = EMPTY_STRUCTURE)
    public void foodAndIngredientData(GameTestHelper test) {
        ItemStack cabbage = new ItemStack(vectorwing.farmersdelight.common.registry.ModItems.CABBAGE_LEAF.get());
        test.assertTrue(ModIngredients.get(cabbage) != null, "Farmer's Delight ingredient registry was not loaded");
        test.assertTrue(Ingredients.getDisplayItem(cabbage).is(ModItems.SPREAD.get()), "Cabbage layer model missing");
        ItemStack blt = FarmersDelightCompat.createBLT();
        FoodProperties food = blt.get(DataComponents.FOOD);
        test.assertTrue(food.nutrition() == SandwichContents.get(blt).nutrition(null) && food.nutrition() > 2, "Dynamic nutrition not exposed through FOOD component");
        test.assertTrue(blt.getOrDefault(DataComponents.FOOD, ModFoods.EMPTY).nutrition() == food.nutrition(), "FOOD getOrDefault mismatch");
        test.assertTrue(food.effects().stream().anyMatch(e -> e.effect().getEffect().equals(vectorwing.farmersdelight.common.registry.ModEffects.NOURISHMENT)), "Nourishment bonus missing");
        test.assertTrue(Float.isFinite(SandwichContents.EMPTY.createFoodProperties(null).saturation()), "Empty sandwich saturation must be finite");
        test.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void saveAndNetworkRoundTrip(GameTestHelper test) {
        ItemStack original = SandwichItem.makeSandwich(Potions.NIGHT_VISION);
        ItemStack restored = ItemStack.parse(test.getLevel().registryAccess(), original.save(test.getLevel().registryAccess())).orElseThrow();
        test.assertTrue(ItemStack.matches(original, restored), "Item save/load lost ingredients");
        RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), test.getLevel().registryAccess());
        try {
            ItemStack.STREAM_CODEC.encode(buffer, original);
            ItemStack received = ItemStack.STREAM_CODEC.decode(buffer);
            test.assertTrue(ItemStack.matches(original, received), "Network codec lost ingredients");
            test.assertTrue(!received.get(DataComponents.FOOD).effects().isEmpty(), "Potion effects disappeared");
        } finally { buffer.release(); }
        test.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void assembleRemoveAndContainer(GameTestHelper test) {
        BlockPos pos = new BlockPos(1, 2, 1);
        test.setBlock(pos.below(), ModBlocks.SANDWICHING_STATION.get());
        test.setBlock(pos, ModBlocks.SANDWICH.get());
        SandwichBlockEntity sandwich = (SandwichBlockEntity) test.getBlockEntity(pos);
        sandwich.applyComponentsFromItemStack(SandwichItem.of(new ItemStack(ModItems.BREAD_SLICE.get())));
        var player = test.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.POTION));
        sandwich.useItemOn(player.getMainHandItem(), player, InteractionHand.MAIN_HAND);
        test.assertTrue(sandwich.getContents().size() == 2, "Potion was not added");
        test.assertTrue(player.getMainHandItem().is(Items.GLASS_BOTTLE), "Potion container was not returned");
        sandwich.useWithoutItem(player);
        test.assertTrue(sandwich.getContents().size() == 1, "Top layer was not removed");
        sandwich.useWithoutItem(player);
        test.assertTrue(test.getBlockState(pos).isAir(), "Removing final layer must remove the block");
        test.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void farmersDelightCuttingRecipes(GameTestHelper test) {
        boolean moreDelight = net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded("moredelight");
        var manager = test.getLevel().getRecipeManager();
        ItemStack knife = new ItemStack(vectorwing.farmersdelight.common.registry.ModItems.IRON_KNIFE.get());
        var input = new CuttingBoardRecipeInput(new ItemStack(Items.BREAD), knife);
        var matches = manager.getAllRecipesFor(vectorwing.farmersdelight.common.registry.ModRecipeTypes.CUTTING.get())
            .stream().filter(holder -> holder.value().matches(input, test.getLevel())).toList();
        test.assertTrue(matches.size() == 1, "Bread + knife must match exactly one cutting recipe, got " + matches.size());
        ItemStack result = matches.getFirst().value().getResults().getFirst();
        var expected = moreDelight ? net.minecraft.core.registries.BuiltInRegistries.ITEM.get(
            net.minecraft.resources.ResourceLocation.parse("moredelight:bread_slice")) : ModItems.BREAD_SLICE.get();
        test.assertTrue(result.is(expected) && result.getCount() == 4, "Bread cutting output is wrong");
        test.assertTrue(manager.byKey(SomeAssemblyRequired.id("cutting/farmersdelight/bread")).isPresent() != moreDelight,
            "SAR bread recipe load condition is incorrect");
        test.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void villageChestLoot(GameTestHelper test) {
        var table = test.getLevel().getServer().reloadableRegistries().getLootTable(
            net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.LOOT_TABLE,
                net.minecraft.resources.ResourceLocation.withDefaultNamespace("chests/village/village_plains_house")));
        var params = new net.minecraft.world.level.storage.loot.LootParams.Builder(test.getLevel())
            .withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.ORIGIN, net.minecraft.world.phys.Vec3.ZERO)
            .create(net.minecraft.world.level.storage.loot.parameters.LootContextParamSets.CHEST);
        int sandwiches = 0;
        for (int seed = 1; seed <= 64; seed++) {
            for (ItemStack stack : table.getRandomItems(params, seed)) {
                if (stack.is(ModItems.SANDWICH.get())) {
                    sandwiches++;
                    test.assertTrue(SandwichContents.get(stack).hasTopAndBottomBread(), "Chest sandwich missing bread");
                    test.assertTrue(stack.get(DataComponents.FOOD).nutrition() > 0, "Chest sandwich is not edible");
                }
            }
        }
        test.assertTrue(sandwiches > 0, "Village chest injection did not generate sandwiches for fixed seeds");
        test.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void heightLimitAndBlockPersistence(GameTestHelper test) {
        BlockPos pos = new BlockPos(2, 2, 2);
        test.setBlock(pos.below(), ModBlocks.SANDWICHING_STATION.get());
        test.setBlock(pos, ModBlocks.SANDWICH.get());
        SandwichBlockEntity block = (SandwichBlockEntity) test.getBlockEntity(pos);
        java.util.List<ItemStack> layers = new java.util.ArrayList<>();
        for (int i = 0; i < 32; i++) layers.add(new ItemStack(ModItems.BREAD_SLICE.get()));
        block.applyComponentsFromItemStack(SandwichItem.of(layers));
        var player = test.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.CARROT));
        block.useItemOn(player.getMainHandItem(), player, InteractionHand.MAIN_HAND);
        test.assertTrue(block.getContents().size() == 32 && player.getMainHandItem().getCount() == 1, "Height limit consumed the held item");
        var restored = net.minecraft.world.level.block.entity.BlockEntity.loadStatic(block.getBlockPos(), block.getBlockState(),
            block.saveWithFullMetadata(test.getLevel().registryAccess()), test.getLevel().registryAccess());
        test.assertTrue(restored instanceof SandwichBlockEntity && ((SandwichBlockEntity) restored).getContents().equals(block.getContents()), "Block save/load lost layers");
        test.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void breadTagBridge(GameTestHelper test) {
        var legacy = net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM,
            net.minecraft.resources.ResourceLocation.parse("c:bread_slice"));
        test.assertTrue(new ItemStack(ModItems.BREAD_SLICE.get()).is(legacy), "SAR slices missing from More Delight's tag");
        test.assertTrue(new ItemStack(ModItems.TOASTED_BREAD_SLICE.get()).is(ModTags.TOASTED_BREAD_SLICES), "SAR toast tag missing");
        if (net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded("moredelight")) {
            for (String name : java.util.List.of("bread_slice", "toast")) {
                var item = net.minecraft.core.registries.BuiltInRegistries.ITEM.getOptional(
                    net.minecraft.resources.ResourceLocation.parse("moredelight:" + name)).orElseThrow();
                ItemStack stack = new ItemStack(item);
                test.assertTrue(stack.is(ModTags.BREAD_SLICES) && stack.is(ModTags.SANDWICH_BREAD), "More Delight " + name + " is not sandwich bread");
                test.assertTrue(name.equals("toast") == stack.is(ModTags.TOASTED_BREAD_SLICES), "Toasted bread classification is incorrect");
                var sandwich = SandwichItem.of(stack, new ItemStack(Items.COOKED_BEEF), stack);
                test.assertTrue(SandwichContents.get(sandwich).hasTopAndBottomBread(), "Foreign bread layers not recognized");
                test.assertTrue(sandwich.get(DataComponents.FOOD).nutrition() == 2 * stack.get(DataComponents.FOOD).nutrition()
                    + new ItemStack(Items.COOKED_BEEF).get(DataComponents.FOOD).nutrition(), "More Delight food values changed");
            }
        }
        test.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void startSandwichOnStation(GameTestHelper test) {
        boolean moreDelight = net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded("moredelight");
        java.util.List<net.minecraft.world.item.Item> breads = new java.util.ArrayList<>(java.util.List.of(ModItems.BREAD_SLICE.get(), ModItems.TOASTED_BREAD_SLICE.get()));
        if (moreDelight) for (String name : java.util.List.of("bread_slice", "toast")) breads.add(
            net.minecraft.core.registries.BuiltInRegistries.ITEM.getOptional(net.minecraft.resources.ResourceLocation.parse("moredelight:" + name)).orElseThrow());
        for (int i = 0; i < breads.size(); i++) {
            BlockPos pos = new BlockPos(i + 1, 2, 2);
            test.setBlock(pos, ModBlocks.SANDWICHING_STATION.get());
            var player = test.makeMockPlayer(GameType.SURVIVAL);
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(breads.get(i), 2));
            var absolute = test.absolutePos(pos);
            var hit = new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(absolute), net.minecraft.core.Direction.UP, absolute, false);
            net.fabricmc.fabric.api.event.player.UseBlockCallback.EVENT.invoker().interact(player, test.getLevel(), InteractionHand.MAIN_HAND, hit);
            var entity = test.getBlockEntity(pos.above());
            test.assertTrue(entity instanceof SandwichBlockEntity, "Bread could not start sandwich on table: " + breads.get(i));
            test.assertTrue(((SandwichBlockEntity) entity).getContents().getFirst().is(breads.get(i)), "Placement changed bread item ID");
            test.assertTrue(player.getMainHandItem().getCount() == 1, "Placing bottom bread consumed wrong count");
        }
        test.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void honeySandwichIngredientAndBonusChest(GameTestHelper test) {
        ItemStack honey = new ItemStack(Items.HONEY_BOTTLE);
        ItemStack display = Ingredients.getDisplayItem(honey);
        test.assertTrue(display.is(ModItems.SPREAD.get()), "Honey must render as a spread, not as a bottle");
        test.assertTrue(display.getOrDefault(ModDataComponents.SPREAD_COLOR.get(), 0) == 0xCCF0A90E, "Honey spread tint missing");
        test.assertTrue(Ingredients.shouldRenderAsItem(honey), "Honey spread should lie flat");
        test.assertTrue(Ingredients.getFood(honey, null).usingConvertsTo().orElse(ItemStack.EMPTY).is(Items.GLASS_BOTTLE), "Honey bottle remainder missing");
        var table = test.getLevel().getServer().reloadableRegistries().getLootTable(
            net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.LOOT_TABLE,
                net.minecraft.resources.ResourceLocation.withDefaultNamespace("chests/spawn_bonus_chest")));
        var params = new net.minecraft.world.level.storage.loot.LootParams.Builder(test.getLevel())
            .withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.ORIGIN, net.minecraft.world.phys.Vec3.ZERO)
            .create(net.minecraft.world.level.storage.loot.parameters.LootContextParamSets.CHEST);
        boolean found = false;
        for (int seed = 1; seed <= 64; seed++) {
            for (ItemStack stack : table.getRandomItems(params, seed)) {
                if (stack.is(ModItems.SANDWICH.get()) && SandwichContents.get(stack).stream().anyMatch(item -> item.is(Items.HONEY_BOTTLE))) {
                    found = true;
                    test.assertTrue(SandwichContents.get(stack).hasTopAndBottomBread(), "Bonus chest honey sandwich is incomplete");
                    test.assertTrue(stack.get(DataComponents.FOOD).nutrition() > 0, "Honey sandwich must remain edible");
                }
            }
        }
        test.assertTrue(found, "Expected honey sandwich in bonus chest loot");
        test.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void burgerPlacementStackingAndPersistence(GameTestHelper test) {
        BlockPos pos = new BlockPos(2, 2, 2);
        test.setBlock(pos, ModBlocks.SANDWICHING_STATION.get());
        var player = test.makeMockPlayer(GameType.SURVIVAL);
        var absolute = test.absolutePos(pos);
        var hit = new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(absolute),
            net.minecraft.core.Direction.UP, absolute, false);
        var layers = java.util.List.of(ModItems.BURGER_BUN_BOTTOM.get(),
            vectorwing.farmersdelight.common.registry.ModItems.BEEF_PATTY.get(),
            vectorwing.farmersdelight.common.registry.ModItems.CABBAGE_LEAF.get(), ModItems.BURGER_BUN_TOP.get());
        for (int i = 0; i < layers.size(); i++) {
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(layers.get(i), 2));
            var result = net.fabricmc.fabric.api.event.player.UseBlockCallback.EVENT.invoker()
                .interact(player, test.getLevel(), InteractionHand.MAIN_HAND, hit);
            test.assertTrue(result.consumesAction(), "Burger layer interaction was not handled");
            var entity = test.getBlockEntity(pos.above());
            test.assertTrue(entity instanceof SandwichBlockEntity, "Burger base disappeared after placement");
            var block = (SandwichBlockEntity) entity;
            test.assertTrue(block.getContents().size() == i + 1, "Burger layer was lost or duplicated");
            for (int j = 0; j <= i; j++)
                test.assertTrue(block.getContents().get(j).is(layers.get(j)), "Burger ingredient order changed");
            test.assertTrue(player.getMainHandItem().getCount() == 1, "Placing burger layer consumed wrong count");
        }
        var block = (SandwichBlockEntity) test.getBlockEntity(pos.above());
        test.assertTrue(block.getContents().hasTopAndBottomBread(), "Burger buns not recognized as bread");
        test.assertTrue(block.getContents().getTotalHeight() == 9, "Burger ingredient heights changed");
        var restored = net.minecraft.world.level.block.entity.BlockEntity.loadStatic(block.getBlockPos(), block.getBlockState(),
            block.saveWithFullMetadata(test.getLevel().registryAccess()), test.getLevel().registryAccess());
        test.assertTrue(restored instanceof SandwichBlockEntity && ((SandwichBlockEntity) restored).getContents().equals(block.getContents()),
            "Burger save/load lost layers");
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        for (int remaining = layers.size() - 1; remaining >= 0; remaining--) {
            net.fabricmc.fabric.api.event.player.UseBlockCallback.EVENT.invoker()
                .interact(player, test.getLevel(), InteractionHand.MAIN_HAND, hit);
            if (remaining > 0) test.assertTrue(block.getContents().size() == remaining, "Removing burger top lost other layers");
        }
        test.assertTrue(test.getBlockState(pos.above()).isAir(), "Removing last bun left a ghost block");
        test.succeed();
    }

}
