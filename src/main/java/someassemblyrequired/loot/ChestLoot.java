package someassemblyrequired.loot;
import java.util.Set;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.NestedLootTable;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import someassemblyrequired.SomeAssemblyRequired;
public final class ChestLoot {
    private static final Set<String> CHESTS = Set.of(
        "chests/abandoned_mineshaft",
        "chests/ancient_city",
        "chests/buried_treasure",
        "chests/end_city_treasure",
        "chests/nether_bridge",
        "chests/shipwreck_supply",
        "chests/simple_dungeon",
        "chests/spawn_bonus_chest",
        "chests/underwater_ruin_big",
        "chests/underwater_ruin_small",
        "chests/village/village_desert_house",
        "chests/village/village_plains_house",
        "chests/village/village_savanna_house",
        "chests/village/village_snowy_house",
        "chests/village/village_taiga_house");
    public static void register() {
        LootTableEvents.MODIFY.register((key, builder, source, registries) -> {
            if (source.isBuiltin() && key.location().getNamespace().equals("minecraft") && CHESTS.contains(key.location().getPath())) {
                builder.withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1))
                    .when(SandwichLootEnabledCondition::new)
                    .add(NestedLootTable.lootTableReference(ResourceKey.create(Registries.LOOT_TABLE,
                        SomeAssemblyRequired.id("inject/" + (key.location().getPath().startsWith("chests/village/") ? "chests/village_house" : key.location().getPath()))))));
            }
        });
    }
}
