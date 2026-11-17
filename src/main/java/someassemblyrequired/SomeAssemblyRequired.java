package someassemblyrequired;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.CommonLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import someassemblyrequired.config.ModConfig;
import someassemblyrequired.event.BlockEventHandler;
import someassemblyrequired.ingredient.IngredientProperties;
import someassemblyrequired.ingredient.Ingredients;
import someassemblyrequired.integration.farmersdelight.FarmersDelightCompat;
import someassemblyrequired.registry.*;
public class SomeAssemblyRequired implements ModInitializer {
    public static final String MOD_ID = "someassemblyrequired";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
    @Override public void onInitialize() {
        ModConfig.load();
        for (FabricRegister<?> registry : new FabricRegister<?>[] {
            ModSoundEvents.SOUND_EVENTS, ModBlocks.BLOCKS, ModDataComponents.DATA_COMPONENT_TYPES,
            ModItems.ITEMS, ModItems.CREATIVE_MODE_TABS, ModStatistics.CUSTOM_STATS,
            ModBlockEntityTypes.ENTITY_TYPES, ModLootFunctions.LOOT_FUNCTION_TYPES,
            ModLootConditions.LOOT_CONDITION_TYPES, ModLootPoolEntries.LOOT_POOL_ENTRY_TYPES,
            ModAdvancementTriggers.CRITERIA_TRIGGERS, ModItemSubPredicateTypes.ITEM_SUB_PREDICATE_TYPES
        }) registry.registerAll();
        DynamicRegistries.registerSynced(ModIngredients.INGREDIENTS, IngredientProperties.CODEC);
        CommonLifecycleEvents.TAGS_LOADED.register((registries, client) ->
            registries.lookup(ModIngredients.INGREDIENTS).ifPresent(ModIngredients::refresh));
        ServerLifecycleEvents.SERVER_STARTED.register(server -> FarmersDelightCompat.setup());
        net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry.playS2C().register(someassemblyrequired.network.ConfigSyncPacket.TYPE, someassemblyrequired.network.ConfigSyncPacket.CODEC);
        net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
            sender.sendPacket(new someassemblyrequired.network.ConfigSyncPacket(ModConfig.toJson())));
        net.fabricmc.fabric.api.registry.CompostingChanceRegistry composting = net.fabricmc.fabric.api.registry.CompostingChanceRegistry.INSTANCE;
        for (var item : java.util.List.of(ModItems.APPLE_SLICES, ModItems.BREAD_SLICE, ModItems.BURGER_BUN_BOTTOM,
                ModItems.BURGER_BUN_TOP, ModItems.CHOPPED_BEETROOT, ModItems.CHOPPED_CARROT,
                ModItems.SLICED_ONION, ModItems.TOASTED_BREAD_SLICE, ModItems.TOMATO_SLICES)) composting.add(item.get(), 0.3F);
        composting.add(ModItems.BURGER_BUN.get(), 0.5F);
        Ingredients.addBehaviors();
        BlockEventHandler.register();
        someassemblyrequired.loot.ChestLoot.register();
    }
    public static ResourceLocation id(String path) { return ResourceLocation.fromNamespaceAndPath(MOD_ID, path); }
    public static <T> ResourceKey<T> key(ResourceKey<? extends Registry<T>> registry, String path) { return ResourceKey.create(registry, id(path)); }
    public static MutableComponent translate(String key, Object... args) { return Component.translatable(MOD_ID + "." + key, args); }
}
