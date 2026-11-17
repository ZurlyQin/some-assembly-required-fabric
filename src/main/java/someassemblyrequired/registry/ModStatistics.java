package someassemblyrequired.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import java.util.function.Supplier;

import someassemblyrequired.SomeAssemblyRequired;

public class ModStatistics {

    public static final FabricRegister<ResourceLocation> CUSTOM_STATS = FabricRegister.create(Registries.CUSTOM_STAT, SomeAssemblyRequired.MOD_ID);

    public static final Supplier<ResourceLocation> SANDWICHES_EATEN = CUSTOM_STATS.register("sandwiches_eaten", () -> SomeAssemblyRequired.id("sandwiches_eaten"));
    public static final Supplier<ResourceLocation> BURGERS_EATEN = CUSTOM_STATS.register("burgers_eaten", () -> SomeAssemblyRequired.id("burgers_eaten"));
}
