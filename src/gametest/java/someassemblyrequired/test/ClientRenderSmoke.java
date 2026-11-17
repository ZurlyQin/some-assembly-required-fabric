package someassemblyrequired.test;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potions;
import someassemblyrequired.SomeAssemblyRequired;
import someassemblyrequired.ingredient.IngredientProperties;
import someassemblyrequired.ingredient.Ingredients;
import someassemblyrequired.item.sandwich.SandwichItem;
import someassemblyrequired.registry.*;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/** Local renderer regression check; test models are never included in the release JAR. */
public final class ClientRenderSmoke implements ClientModInitializer {
    private boolean opened;
    private static boolean simulate3dBottle;
    @Override public void onInitializeClient() {
        if (!Boolean.getBoolean("someassemblyrequired.clientSmoke")) return;
        // Simulate a resource pack which makes bottles three-dimensional.
        ModelLoadingPlugin.register(plugin -> plugin.modifyModelAfterBake().register((model, context) -> {
            var id = context.topLevelId();
            if (model != null && id != null && id.id().getNamespace().equals("minecraft")
                    && java.util.Set.of("honey_bottle", "milk_bucket", "potion").contains(id.id().getPath())) {
                return new ForwardingBakedModel(model) {
                    @Override public boolean isGui3d() { return simulate3dBottle || super.isGui3d(); }
                };
            }
            return model;
        }));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!opened && client.getOverlay() == null && client.screen != null) {
                opened = true;
                loadIngredientFixtures();
                client.setScreen(new SamplesScreen());
            }
        });
    }

    // Decode the actual bundled ingredient definitions, not hand-built mock properties.
    private static void loadIngredientFixtures() {
        var registry = new MappedRegistry<IngredientProperties>(ModIngredients.INGREDIENTS, Lifecycle.stable());
        var ops = RegistryOps.create(JsonOps.INSTANCE, RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY));
        for (String name : java.util.List.of("honey_bottle", "milk_bucket", "burger_bun_bottom", "burger_bun_top", "burger_bun",
                "potato", "gold_block", "farmersdelight/beef_patty", "farmersdelight/cabbage_leaf")) {
            String path = "/data/someassemblyrequired/someassemblyrequired/ingredients/" + name + ".json";
            try (var input = ClientRenderSmoke.class.getResourceAsStream(path)) {
                if (input == null) throw new IllegalStateException("Missing ingredient " + path);
                var json = JsonParser.parseReader(new InputStreamReader(input, StandardCharsets.UTF_8));
                var properties = IngredientProperties.CODEC.parse(ops, json).getOrThrow();
                registry.register(ResourceKey.create(ModIngredients.INGREDIENTS, SomeAssemblyRequired.id(name)), properties, RegistrationInfo.BUILT_IN);
            } catch (java.io.IOException e) { throw new RuntimeException(e); }
        }
        ModIngredients.refresh(registry.freeze().asLookup());
        if (!Ingredients.getDisplayItem(new ItemStack(Items.HONEY_BOTTLE)).is(ModItems.SPREAD.get()))
            throw new IllegalStateException("Honey did not resolve to the spread model");
    }

    private static final class SamplesScreen extends Screen {
        private final ItemStack[] samples = {SandwichItem.makeSandwich(Items.HONEY_BOTTLE), SandwichItem.makeSandwich(Items.HONEY_BOTTLE),
            SandwichItem.makeSandwich(Items.MILK_BUCKET), SandwichItem.makeSandwich(Potions.NIGHT_VISION)};
        private final String[] labels = {"Honey / vanilla", "Honey / 3D bottle", "Milk / 3D bucket", "Potion / 3D bottle"};
        private final ItemStack[] burgers = {
            SandwichItem.of(new ItemStack(ModItems.BURGER_BUN_BOTTOM.get())),
            SandwichItem.makeBurger(vectorwing.farmersdelight.common.registry.ModItems.BEEF_PATTY.get(),
                vectorwing.farmersdelight.common.registry.ModItems.CABBAGE_LEAF.get()),
            SandwichItem.makeBurger(vectorwing.farmersdelight.common.registry.ModItems.BEEF_PATTY.get(), Items.HONEY_BOTTLE),
            SandwichItem.of(new ItemStack(ModItems.BURGER_BUN.get()))};
        private final String[] burgerLabels = {"Bun bottom", "Patty + cabbage", "Patty + honey", "Whole bun"};
        private int frames;
        SamplesScreen() { super(Component.literal("Spread renderer regression test")); }
        @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
            graphics.fill(0, 0, width, height, 0xFF243343);
            graphics.drawCenteredString(font, "SAR / actual ingredient data / 3D source-model regression", width / 2, 20, 0xFFFFFF);
            for (int i = 0; i < samples.length; i++) {
                simulate3dBottle = i != 0;
                if (i == 1 && !Minecraft.getInstance().getItemRenderer()
                        .getModel(new ItemStack(Items.HONEY_BOTTLE), null, null, 0).isGui3d())
                    throw new IllegalStateException("3D bottle simulation was not applied");
                float x = width * (i + 0.5F) / samples.length;
                graphics.drawCenteredString(font, labels[i], (int) x, 55, 0xFFFFFF);
                graphics.pose().pushPose();
                graphics.pose().translate(x - 24, height / 2F - 20, 0);
                graphics.pose().scale(3, 3, 3);
                graphics.renderItem(samples[i], 0, 0);
                graphics.pose().popPose();
                graphics.flush();
            }
            simulate3dBottle = false;
            for (int i = 0; i < burgers.length; i++) {
                float x = width * (i + 0.5F) / burgers.length;
                graphics.drawCenteredString(font, burgerLabels[i], (int) x, (int)(height * 0.7F), 0xFFFFFF);
                graphics.pose().pushPose();
                graphics.pose().translate(x - 24, height * 0.78F, 0);
                graphics.pose().scale(3, 3, 3);
                graphics.renderItem(burgers[i], 0, 0);
                graphics.pose().popPose();
                graphics.flush();
            }
            if (++frames == 60) {
                try (var image = Screenshot.takeScreenshot(Minecraft.getInstance().getMainRenderTarget())) {
                    image.writeToFile(Minecraft.getInstance().gameDirectory.toPath().resolve("renderer-smoke.png"));
                    int cellWidth = image.getWidth() / samples.length;
                    if (image.getWidth() % samples.length != 0)
                        throw new IllegalStateException("Use the default smoke-test window size for pixel comparison");
                    int differentPixels = 0;
                    for (int y = image.getHeight() / 3; y < image.getHeight() * 2 / 3; y++) {
                        for (int x = 0; x < cellWidth; x++) {
                            if (image.getPixelRGBA(x, y) != image.getPixelRGBA(x + cellWidth, y)) differentPixels++;
                        }
                    }
                    // Separate GUI positions can differ by a few edge pixels from GPU rasterization.
                    // The original orientation bug changes thousands of pixels in this region.
                    if (differentPixels > 4)
                        throw new IllegalStateException("Source bottle model changed honey spread rendering: " + differentPixels + " pixels differ");
                    SomeAssemblyRequired.LOGGER.info("CLIENT_RENDER_SMOKE_PASS: honey vanilla/3D source pixel differences {} (tolerance 4); milk and potion rendered", differentPixels);
                } catch (java.io.IOException e) { throw new RuntimeException(e); }
                BurgerRenderChecks.run();
                Minecraft.getInstance().stop();
            }
        }
    }
}
