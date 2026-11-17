package someassemblyrequired;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import someassemblyrequired.block.SandwichBlockRenderer;
import someassemblyrequired.item.sandwich.SandwichItemRenderer;
import someassemblyrequired.registry.*;
public class SomeAssemblyRequiredClient implements ClientModInitializer {
    @Override public void onInitializeClient() {
        net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.registerGlobalReceiver(someassemblyrequired.network.ConfigSyncPacket.TYPE,
            (packet, context) -> someassemblyrequired.config.ModConfig.applyServer(packet.json()));
        net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> someassemblyrequired.config.ModConfig.load());
        BlockEntityRendererRegistry.register(ModBlockEntityTypes.SANDWICH.get(), SandwichBlockRenderer::new);
        ColorProviderRegistry.ITEM.register((stack, index) -> index == 0 ? stack.getOrDefault(ModDataComponents.SPREAD_COLOR.get(), 0xFFFFFFFF) : 0xFFFFFFFF, ModItems.SPREAD.get());
        BuiltinItemRendererRegistry.INSTANCE.register(ModItems.SANDWICH.get(), new SandwichItemRenderer()::renderByItem);
    }
}
