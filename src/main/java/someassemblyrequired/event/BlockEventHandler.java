package someassemblyrequired.event;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import someassemblyrequired.SomeAssemblyRequired;
import someassemblyrequired.block.SandwichBlockEntity;
import someassemblyrequired.ingredient.Ingredients;
import someassemblyrequired.item.sandwich.SandwichItem;
import someassemblyrequired.registry.ModItems;
import someassemblyrequired.registry.ModTags;

public class BlockEventHandler {

    public static void register() {
        net.fabricmc.fabric.api.event.player.UseBlockCallback.EVENT.register(BlockEventHandler::onRightClickBlock);
    }
    private static InteractionResult onRightClickBlock(Player player, Level level, InteractionHand hand, BlockHitResult hitResult) {
        if (player.isSpectator() || !level.mayInteract(player, hitResult.getBlockPos())) return InteractionResult.PASS;
        BlockPos pos = hitResult.getBlockPos();
        BlockState state = level.getBlockState(pos);
        if (player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }

        if (state.is(ModTags.SANDWICHING_STATIONS)) {
            InteractionResult result;
            if (level.getBlockEntity(pos.above()) instanceof SandwichBlockEntity blockEntity) {
                if (player.getItemInHand(InteractionHand.MAIN_HAND).isEmpty() && player.getItemInHand(InteractionHand.OFF_HAND).isEmpty()) {
                    result = blockEntity.useWithoutItem(player);
                } else if (!player.getItemInHand(hand).isEmpty()) {
                    result = blockEntity.useItemOn(player.getItemInHand(hand), player, hand).result();
                } else {
                    result = InteractionResult.PASS;
                }
            } else {
                result = tryPlaceSandwich(player, hand, hitResult);
            }

            if (result != InteractionResult.PASS) {
                return result;
            }
        }
        return InteractionResult.PASS;
    }

    private static InteractionResult tryPlaceSandwich(Player player, InteractionHand hand, BlockHitResult hitResult) {
        ItemStack heldItem = player.getItemInHand(hand).copy();
        BlockPos pos = hitResult.getBlockPos();
        heldItem.setCount(1);

        if (!Ingredients.canAddToSandwich(heldItem)) {
            return InteractionResult.PASS;
        } else if (!heldItem.is(ModItems.SANDWICH.get()) && !heldItem.is(ModTags.SANDWICH_BREAD)) {
            player.displayClientMessage(SomeAssemblyRequired.translate("message.bottom_bread"), true);
            return InteractionResult.SUCCESS;
        }

        ItemStack sandwich = SandwichItem.of(heldItem);
        UseOnContext useOnContext = new UseOnContext(player, hand, hitResult);
        InteractionResult placeResult = ModItems.SANDWICH.get().place(useOnContext, pos.above(), sandwich);

        if (sandwich.isEmpty()) {
            player.getItemInHand(hand).shrink(1);
        }

        return placeResult;
    }
}
