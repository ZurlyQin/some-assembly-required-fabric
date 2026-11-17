package someassemblyrequired.registry;

import net.minecraft.advancements.critereon.ItemSubPredicate;
import net.minecraft.core.registries.Registries;
import java.util.function.Supplier;

import someassemblyrequired.SomeAssemblyRequired;
import someassemblyrequired.predicate.DoubleDeckerPredicate;
import someassemblyrequired.predicate.SandwichContentsPredicate;

public class ModItemSubPredicateTypes {

    public static final FabricRegister<ItemSubPredicate.Type<?>> ITEM_SUB_PREDICATE_TYPES = FabricRegister.create(Registries.ITEM_SUB_PREDICATE_TYPE, SomeAssemblyRequired.MOD_ID);

    public static final Supplier<ItemSubPredicate.Type<SandwichContentsPredicate>> SANDWICH_CONTENTS
            = ITEM_SUB_PREDICATE_TYPES.register("sandwich_contents", () -> new ItemSubPredicate.Type<>(SandwichContentsPredicate.CODEC));
    public static final Supplier<ItemSubPredicate.Type<DoubleDeckerPredicate>> IS_DOUBLE_DECKER
            = ITEM_SUB_PREDICATE_TYPES.register("is_double_decker", () -> new ItemSubPredicate.Type<>(DoubleDeckerPredicate.CODEC));
}
