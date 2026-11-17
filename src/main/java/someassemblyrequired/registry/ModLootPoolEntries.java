package someassemblyrequired.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import java.util.function.Supplier;

import someassemblyrequired.SomeAssemblyRequired;
import someassemblyrequired.loot.OptionalLootItem;

public class ModLootPoolEntries {

    public static final FabricRegister<LootPoolEntryType> LOOT_POOL_ENTRY_TYPES = FabricRegister.create(Registries.LOOT_POOL_ENTRY_TYPE, SomeAssemblyRequired.MOD_ID);

    public static final Supplier<LootPoolEntryType> OPTIONAL_ITEM = LOOT_POOL_ENTRY_TYPES.register("optional_item", () -> new LootPoolEntryType(OptionalLootItem.CODEC));
}
