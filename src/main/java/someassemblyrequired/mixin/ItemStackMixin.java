package someassemblyrequired.mixin;

import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import someassemblyrequired.item.sandwich.SandwichContents;
import someassemblyrequired.item.sandwich.SandwichItem;

/** Vanilla 1.21 food lives on a component; derive it from the sandwich contents on both sides. */
@Mixin(DataComponentHolder.class)
public interface ItemStackMixin {
    @Inject(method = "get", at = @At("RETURN"), cancellable = true)
    private <T> void someassemblyrequired$food(DataComponentType<? extends T> type, CallbackInfoReturnable<T> cir) {
        if (type == DataComponents.FOOD && (Object) this instanceof ItemStack stack && stack.getItem() instanceof SandwichItem) {
            @SuppressWarnings("unchecked") T food = (T) SandwichContents.get(stack).createFoodProperties(null);
            cir.setReturnValue(food);
        }
    }

    @Inject(method = "getOrDefault", at = @At("RETURN"), cancellable = true)
    private <T> void someassemblyrequired$foodOrDefault(DataComponentType<? extends T> type, T fallback, CallbackInfoReturnable<T> cir) {
        if (type == DataComponents.FOOD && (Object) this instanceof ItemStack stack && stack.getItem() instanceof SandwichItem) {
            @SuppressWarnings("unchecked") T food = (T) SandwichContents.get(stack).createFoodProperties(null);
            cir.setReturnValue(food);
        }
    }
}
