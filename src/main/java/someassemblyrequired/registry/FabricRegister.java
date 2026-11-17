package someassemblyrequired.registry;
import java.util.*;
import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
public final class FabricRegister<T> {
    private final Registry<T> registry;
    private final String namespace;
    private final List<Supplier<?>> entries = new ArrayList<>();
    @SuppressWarnings("unchecked")
    private FabricRegister(ResourceKey<? extends Registry<T>> key, String namespace) {
        this.registry = (Registry<T>) BuiltInRegistries.REGISTRY.get(key.location());
        this.namespace = namespace;
    }
    public static <T> FabricRegister<T> create(ResourceKey<? extends Registry<T>> key, String namespace) {
        return new FabricRegister<>(key, namespace);
    }
    public <V extends T> Supplier<V> register(String name, Supplier<V> factory) {
        Supplier<V> entry = new Supplier<>() {
            private V value;
            public V get() {
                if (value == null) value = Registry.register(registry, ResourceLocation.fromNamespaceAndPath(namespace, name), factory.get());
                return value;
            }
        };
        entries.add(entry);
        return entry;
    }
    public void registerAll() { entries.forEach(Supplier::get); }
}
