package someassemblyrequired.config;
import java.util.*;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
public final class ServerConfig {
    public final ModConfig.Value<Integer> maximumSandwichHeight = new ModConfig.Value<>(32);
    public final ModConfig.Value<Boolean> generateChestLoot = new ModConfig.Value<>(true);
    public final ModConfig.Value<String> sandwichBonusEffect = new ModConfig.Value<>("farmersdelight:nourishment");
    public final ModConfig.Value<String> burgerBonusEffect = new ModConfig.Value<>("farmersdelight:nourishment");
    public final ModConfig.Value<List<Integer>> sandwichEffectDurations = new ModConfig.Value<>(List.of(0,0,60,120,180,240,300));
    public final ModConfig.Value<List<Integer>> burgerEffectDurations = new ModConfig.Value<>(List.of(0,0,60,120,180,240,300));
    public Optional<Holder.Reference<MobEffect>> getSandwichBonusEffect(boolean burger) {
        ResourceLocation id = ResourceLocation.tryParse((burger ? burgerBonusEffect : sandwichBonusEffect).get());
        return id == null ? Optional.empty() : BuiltInRegistries.MOB_EFFECT.getHolder(id);
    }
}
