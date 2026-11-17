package someassemblyrequired.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import java.util.function.Supplier;

import someassemblyrequired.SomeAssemblyRequired;
import someassemblyrequired.block.SandwichBlockEntity;

@SuppressWarnings("ConstantConditions")
public class ModBlockEntityTypes {

    public static final FabricRegister<BlockEntityType<?>> ENTITY_TYPES = FabricRegister.create(Registries.BLOCK_ENTITY_TYPE, SomeAssemblyRequired.MOD_ID);

    public static final Supplier<BlockEntityType<SandwichBlockEntity>> SANDWICH = ENTITY_TYPES.register(
            "sandwich",
            () -> BlockEntityType.Builder.of(SandwichBlockEntity::new,
                    ModBlocks.SANDWICH.get()
            ).build(null)
    );
}
