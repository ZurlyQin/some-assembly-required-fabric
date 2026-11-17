package someassemblyrequired.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntry;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;

import someassemblyrequired.registry.ModLootPoolEntries;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class OptionalLootItem extends LootPoolSingletonContainer {

    private final ResourceLocation item;
    private final List<ResourceCondition> loadingConditions;

    public static final MapCodec<OptionalLootItem> CODEC = RecordCodecBuilder.mapCodec(instance ->
            singletonFields(instance)
                    .and(ResourceLocation.CODEC.fieldOf("item").forGetter(OptionalLootItem::item))
                    .and(ResourceCondition.CODEC.listOf().fieldOf("loading_conditions").forGetter(OptionalLootItem::loadingConditions))
                    .apply(instance, OptionalLootItem::new));

    public OptionalLootItem(int weight, int quality, List<LootItemCondition> conditions, List<LootItemFunction> functions, ResourceLocation item, List<ResourceCondition> loadingConditions) {
        super(weight, quality, conditions, functions);
        this.item = item;
        this.loadingConditions = loadingConditions;
    }

    private ResourceLocation item() {
        return item;
    }

    private List<ResourceCondition> loadingConditions() {
        return loadingConditions;
    }

    public LootPoolEntryType getType() {
        return ModLootPoolEntries.OPTIONAL_ITEM.get();
    }

    @Override
    public boolean expand(LootContext context, Consumer<LootPoolEntry> entries) {
        if (!canRun(context) || !testConditions(loadingConditions)) {
            return false;
        }
        entries.accept(new LootPoolSingletonContainer.EntryBase() {
            public void createItemStack(Consumer<ItemStack> items, LootContext context1) {
                items.accept(new ItemStack(BuiltInRegistries.ITEM.get(item)));
            }
        });
        return true;
    }

    public static boolean testConditions(List<ResourceCondition> conditions) {
        for (ResourceCondition condition : conditions) {
            if (!condition.test(null)) {
                return false;
            }
        }
        return true;
    }

    public void createItemStack(Consumer<ItemStack> pStackConsumer, LootContext pLootContext) {
        pStackConsumer.accept(new ItemStack(BuiltInRegistries.ITEM.get(item)));
    }

    public static LootPoolSingletonContainer.Builder<?> whenLoaded(Item item) {
        return whenLoaded(BuiltInRegistries.ITEM.getKey(item));
    }

    public static LootPoolSingletonContainer.Builder<?> whenLoaded(ResourceLocation item) {
        return optionalLootItem(item, ResourceConditions.allModsLoaded(item.getNamespace()));
    }

    public static LootPoolSingletonContainer.Builder<?> optionalLootItem(ResourceLocation item, ResourceCondition... loadingConditions) {
        return simpleBuilder((weight, quality, conditions, functions) -> new OptionalLootItem(weight, quality, conditions, functions, item, Arrays.asList(loadingConditions)));
    }
}
