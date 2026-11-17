package someassemblyrequired.registry;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;
import someassemblyrequired.SomeAssemblyRequired;
import someassemblyrequired.ingredient.IngredientProperties;
import someassemblyrequired.ingredient.IngredientPropertiesBase;
import someassemblyrequired.ingredient.PotionProperties;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

// TODO simplify by using a data map
public class ModIngredients {

    public static final ResourceKey<Registry<IngredientProperties>> INGREDIENTS = ResourceKey.createRegistryKey(SomeAssemblyRequired.id("ingredients"));

    private static final IngredientPropertiesBase DEFAULT_PROPERTIES = new IngredientProperties(
            null, Optional.empty(), Optional.empty(), Optional.empty(),
            ItemStack.EMPTY, net.minecraft.core.registries.BuiltInRegistries.SOUND_EVENT.wrapAsHolder(ModSoundEvents.ADD_ITEM.get()), 1, true, false
    );

    private static final Map<Item, IngredientPropertiesBase> PROPERTIES = new HashMap<>();

    public static void refresh(HolderLookup.RegistryLookup<IngredientProperties> registry) {
        PROPERTIES.clear();
        registry.listElements().forEachOrdered(ingredient -> {
            Item item = ingredient.value().item().value();
            if (PROPERTIES.containsKey(item)) {
                SomeAssemblyRequired.LOGGER.error("Multiple ingredients found for item {}", BuiltInRegistries.ITEM.getKey(item));
            }
            PROPERTIES.put(item, ingredient.value());
        });
        PROPERTIES.put(Items.POTION, new PotionProperties());
    }

    @Nullable
    public static IngredientPropertiesBase get(ItemStack item) {
        return PROPERTIES.get(item.getItem());
    }

    public static IngredientPropertiesBase getOrDefault(ItemStack item) {
        if (get(item) != null) {
            return get(item);
        }
        return DEFAULT_PROPERTIES;
    }
}
