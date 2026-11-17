package someassemblyrequired.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import java.util.function.Supplier;

import someassemblyrequired.SomeAssemblyRequired;
import someassemblyrequired.loot.SetSandwichContentsFunction;
import someassemblyrequired.loot.SmeltMatchingItemFunction;

public class ModLootFunctions {

    public static final FabricRegister<LootItemFunctionType<?>> LOOT_FUNCTION_TYPES = FabricRegister.create(Registries.LOOT_FUNCTION_TYPE, SomeAssemblyRequired.MOD_ID);

    public static final Supplier<LootItemFunctionType<SetSandwichContentsFunction>> SET_SANDWICH_CONTENTS = LOOT_FUNCTION_TYPES.register("set_sandwich_contents", () -> new LootItemFunctionType<>(SetSandwichContentsFunction.CODEC));
    public static final Supplier<LootItemFunctionType<SmeltMatchingItemFunction>> SMELT_MATCHING_ITEM = LOOT_FUNCTION_TYPES.register("smelt_matching_item", () -> new LootItemFunctionType<>(SmeltMatchingItemFunction.CODEC));

}
