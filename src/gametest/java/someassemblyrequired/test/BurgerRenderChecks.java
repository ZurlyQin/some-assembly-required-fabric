package someassemblyrequired.test;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import someassemblyrequired.SomeAssemblyRequired;
import someassemblyrequired.block.SandwichBlockEntity;
import someassemblyrequired.block.SandwichBlockRenderer;
import someassemblyrequired.ingredient.Ingredients;
import someassemblyrequired.item.sandwich.SandwichItem;
import someassemblyrequired.registry.ModBlocks;
import someassemblyrequired.registry.ModItems;

import java.util.ArrayList;
import java.util.List;

/** Measures vertices emitted by the real block renderer, with the table surface at y=0. */
final class BurgerRenderChecks {
    static void run() {
        var fd = vectorwing.farmersdelight.common.registry.ModItems.BEEF_PATTY.get();
        for (var item : List.of(ModItems.BURGER_BUN_BOTTOM.get(), ModItems.BURGER_BUN_TOP.get(),
                ModItems.BURGER_BUN.get(), fd, Items.POTATO, Items.GOLD_BLOCK)) {
            ItemStack stack = new ItemStack(item);
            checkLayers(List.of(stack));
        }
        checkLayers(List.of(new ItemStack(ModItems.BURGER_BUN_BOTTOM.get()), new ItemStack(fd),
            new ItemStack(vectorwing.farmersdelight.common.registry.ModItems.CABBAGE_LEAF.get()),
            new ItemStack(ModItems.BURGER_BUN_TOP.get())));
        SomeAssemblyRequired.LOGGER.info("BURGER_RENDER_PASS: bun bottom/top/whole, patty, potato, gold block and stacked burger stay above the table without layer overlap");
    }

    private static void checkLayers(List<ItemStack> layers) {
        var block = new SandwichBlockEntity(BlockPos.ZERO, ModBlocks.SANDWICH.get().defaultBlockState());
        block.applyComponentsFromItemStack(SandwichItem.of(layers));
        List<Bounds> bounds = new ArrayList<>();
        new SandwichBlockRenderer(null).render(block, 0, new PoseStack(), type -> {
            Bounds b = new Bounds();
            bounds.add(b);
            return b;
        }, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
        if (bounds.size() != layers.size()) throw new IllegalStateException("Unexpected render buffer count: " + bounds.size());
        float bottom = 0;
        for (int i = 0; i < layers.size(); i++) {
            ItemStack layer = layers.get(i);
            Bounds b = bounds.get(i);
            float top = bottom + Ingredients.getHeight(layer) / 32F;
            // Flat sprites are inset slightly; volumetric ingredient models start at the layer base.
            if (b.vertices == 0 || b.minY < bottom - 0.00001F || b.maxY > top + 0.00001F)
                throw new IllegalStateException(layer + " renders at y=" + b.minY + ".." + b.maxY
                    + ", outside layer " + bottom + ".." + top + " (table surface y=0)");
            if (!Ingredients.shouldRenderAsItem(layer) && Math.abs(b.minY - bottom) > 0.00001F)
                throw new IllegalStateException(layer + " floats above its layer: " + b.minY + " vs " + bottom);
            SomeAssemblyRequired.LOGGER.info("BURGER_LAYER_BOUNDS: {} y={}..{} expected={}..{}", layer, b.minY, b.maxY, bottom, top);
            bottom = top;
        }
    }

    private static final class Bounds implements VertexConsumer {
        int vertices;
        float minY = Float.POSITIVE_INFINITY, maxY = Float.NEGATIVE_INFINITY;
        @Override public VertexConsumer addVertex(float x, float y, float z) {
            vertices++; minY = Math.min(minY, y); maxY = Math.max(maxY, y); return this;
        }
        @Override public VertexConsumer setColor(int r, int g, int b, int a) { return this; }
        @Override public VertexConsumer setUv(float u, float v) { return this; }
        @Override public VertexConsumer setUv1(int u, int v) { return this; }
        @Override public VertexConsumer setUv2(int u, int v) { return this; }
        @Override public VertexConsumer setNormal(float x, float y, float z) { return this; }
    }
}
