package someassemblyrequired.config;
import com.google.gson.*;
import java.nio.file.*;
import java.io.IOException;
import java.util.List;
import net.fabricmc.loader.api.FabricLoader;
import someassemblyrequired.SomeAssemblyRequired;
public final class ModConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static final ServerConfig server = new ServerConfig();
    public static final ClientConfig client = new ClientConfig();
    public static final Spec serverSpec = new Spec();
    public static final class Spec { public boolean isLoaded() { return true; } }
    public static final class Value<T> {
        private T value;
        public Value(T value) { this.value = value; }
        public T get() { return value; }
        public void set(T value) { this.value = value; }
    }
    public record Settings(int maximumSandwichHeight, boolean generateChestLoot,
            String sandwichEffect, String burgerEffect, List<Integer> sandwichEffectDurations,
            List<Integer> burgerEffectDurations, boolean listIngredientsInTooltip) { }
    public static Settings snapshot() {
        return new Settings(server.maximumSandwichHeight.get(), server.generateChestLoot.get(),
            server.sandwichBonusEffect.get(), server.burgerBonusEffect.get(),
            server.sandwichEffectDurations.get(), server.burgerEffectDurations.get(), client.listItemsInTooltip.get());
    }
    private static void validate(Settings s) {
        if (s == null || s.maximumSandwichHeight < 2 || s.maximumSandwichHeight > 256
                || s.sandwichEffect == null || s.burgerEffect == null
                || !validDurations(s.sandwichEffectDurations) || !validDurations(s.burgerEffectDurations))
            throw new IllegalArgumentException("Invalid Some Assembly Required configuration");
    }
    private static boolean validDurations(List<Integer> values) {
        return values != null && !values.isEmpty() && values.size() <= 257
                && values.stream().allMatch(i -> i != null && i >= 0 && i <= 1000000);
    }
    public static void applyServer(String json) { apply(GSON.fromJson(json, Settings.class), false); }
    private static void apply(Settings s, boolean local) {
        validate(s);
        server.maximumSandwichHeight.set(s.maximumSandwichHeight);
        server.generateChestLoot.set(s.generateChestLoot);
        server.sandwichBonusEffect.set(s.sandwichEffect);
        server.burgerBonusEffect.set(s.burgerEffect);
        server.sandwichEffectDurations.set(List.copyOf(s.sandwichEffectDurations));
        server.burgerEffectDurations.set(List.copyOf(s.burgerEffectDurations));
        if (local) client.listItemsInTooltip.set(s.listIngredientsInTooltip);
    }
    public static String toJson() { return GSON.toJson(snapshot()); }
    public static void load() {
        Path path = FabricLoader.getInstance().getConfigDir().resolve("someassemblyrequired.json");
        try {
            if (Files.exists(path)) apply(GSON.fromJson(Files.readString(path), Settings.class), true);
            else { Files.createDirectories(path.getParent()); Files.writeString(path, toJson()); }
        } catch (IOException | RuntimeException e) {
            SomeAssemblyRequired.LOGGER.error("Cannot load {}; keeping current/default configuration", path, e);
        }
    }
}
