package someassemblyrequired.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import java.util.function.Supplier;

import someassemblyrequired.SomeAssemblyRequired;
import someassemblyrequired.loot.SandwichLootEnabledCondition;

public class ModLootConditions {

    public static final FabricRegister<LootItemConditionType> LOOT_CONDITION_TYPES = FabricRegister.create(Registries.LOOT_CONDITION_TYPE, SomeAssemblyRequired.MOD_ID);

    public static final Supplier<LootItemConditionType> SANDWICH_LOOT_ENABLED = LOOT_CONDITION_TYPES.register("sandwich_loot_enabled", () -> new LootItemConditionType(SandwichLootEnabledCondition.CODEC));

}
