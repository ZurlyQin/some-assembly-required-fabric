package someassemblyrequired.item.sandwich;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import someassemblyrequired.ingredient.Ingredients;
import someassemblyrequired.registry.ModTags;

import java.util.Random;

public class SandwichItemRenderer extends BlockEntityWithoutLevelRenderer {

    private static final ItemStack BARRIER = new ItemStack(Items.BARRIER);
    private static final Random random = new Random();

    public SandwichItemRenderer() {
        // noinspection ConstantConditions
        super(null, null);
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext itemDisplayContext, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int overlay) {
        SandwichContents sandwich = SandwichContents.get(stack);
        poseStack.pushPose();

        poseStack.translate(0.5, 0.5 + 1 / 32D, 0.5);

        int height = sandwich.getTotalHeight();
        if (itemDisplayContext == ItemDisplayContext.GUI) {
            if (height > 16) {
                float scale = 24 / (height + 8F);
                poseStack.scale(scale, scale, scale);
            }
        }

        if (itemDisplayContext.firstPerson() || itemDisplayContext == ItemDisplayContext.GUI) {
            poseStack.translate(0, -height / 32D, 0);
        }

        if (sandwich.isEmpty()) {
            poseStack.mulPose(Axis.XP.rotationDegrees(90));
            poseStack.mulPose(Axis.ZP.rotationDegrees(180));
            Minecraft.getInstance().getItemRenderer().renderStatic(BARRIER, ItemDisplayContext.FIXED, packedLight, overlay, poseStack, buffer, null, 0);
        } else {
            renderSandwich(sandwich, poseStack, buffer, packedLight, overlay, 0);
        }

        poseStack.popPose();
    }

    public static void renderSandwich(SandwichContents sandwich, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int overlay, long seed) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.XP.rotationDegrees(90));
        poseStack.mulPose(Axis.ZP.rotationDegrees(180));
        random.setSeed(seed);
        for (int slot = 0; slot < sandwich.size(); slot++) {
            poseStack.pushPose();
            ItemStack stack = sandwich.get(slot);
            if (!stack.is(ModTags.SANDWICH_BREAD)) {
                poseStack.mulPose(Axis.ZP.rotationDegrees(random.nextFloat() * 40 - 20));
            }
            ItemStack displayStack = Ingredients.getDisplayItem(stack);

            // Orient the model actually being rendered. A resource pack may make the source
            // bottle/bucket 3D while its replacement spread is still a flat ingredient layer.
            boolean isGui3d = Minecraft.getInstance().getItemRenderer().getModel(displayStack, null, null, (int) seed + slot).isGui3d();
            boolean renderAsItem = Ingredients.shouldRenderAsItem(stack);
            boolean customIngredientModel = !renderAsItem && !ItemStack.isSameItemSameComponents(stack, displayStack);
            if (!renderAsItem || isGui3d) {
                poseStack.mulPose(Axis.ZP.rotationDegrees(180));
                poseStack.mulPose(Axis.XP.rotationDegrees(-90));
                poseStack.translate(0, 0.5 - 1 / 32D, 0);
                // Custom ingredient geometry is authored from y=0. Only ordinary item
                // models need the centering correction (e.g. a gold block at FIXED scale).
                if (isGui3d && !customIngredientModel) {
                    poseStack.translate(0, -4 / 16D, 0);
                }
            }

            poseStack.scale(1, 1, 0.99F);
            Minecraft.getInstance().getItemRenderer().renderStatic(displayStack, ItemDisplayContext.FIXED, packedLight, overlay, poseStack, buffer, null, (int) seed + slot);
            poseStack.popPose();

            int height = Ingredients.getHeight(stack);
            poseStack.translate(0, 0, -height / 16D);
        }
        poseStack.popPose();
    }
}
