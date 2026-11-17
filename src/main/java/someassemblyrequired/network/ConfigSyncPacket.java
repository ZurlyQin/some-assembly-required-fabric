package someassemblyrequired.network;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import someassemblyrequired.SomeAssemblyRequired;
public record ConfigSyncPacket(String json) implements CustomPacketPayload {
    public static final Type<ConfigSyncPacket> TYPE = new Type<>(SomeAssemblyRequired.id("server_config"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ConfigSyncPacket> CODEC = StreamCodec.of(
        (buf, packet) -> buf.writeUtf(packet.json, 32767), buf -> new ConfigSyncPacket(buf.readUtf(32767)));
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
