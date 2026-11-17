package someassemblyrequired.registry;

import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.advancements.critereon.PlayerTrigger;
import net.minecraft.core.registries.Registries;
import java.util.function.Supplier;

import someassemblyrequired.SomeAssemblyRequired;

public class ModAdvancementTriggers {

    public static final FabricRegister<CriterionTrigger<?>> CRITERIA_TRIGGERS = FabricRegister.create(Registries.TRIGGER_TYPE, SomeAssemblyRequired.MOD_ID);

    public static final Supplier<PlayerTrigger> CONSUME_1000_SANDWICHES = CRITERIA_TRIGGERS.register("consume_1000_sandwiches", PlayerTrigger::new);
    public static final Supplier<PlayerTrigger> CONSUME_1000_BURGERS = CRITERIA_TRIGGERS.register("consume_1000_burgers", PlayerTrigger::new);
}
