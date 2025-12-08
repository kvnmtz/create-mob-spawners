package dev.kvnmtz.createmobspawners.common.network.registry;

import dev.kvnmtz.createmobspawners.CreateMobSpawnersMod;
import dev.kvnmtz.createmobspawners.common.network.packet.ServerboundConfigureSpawnerPacket;
import net.createmod.catnip.net.base.BasePacketPayload;
import net.createmod.catnip.net.base.CatnipPacketRegistry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.Locale;

@SuppressWarnings({"unchecked", "rawtypes"})
public enum CreateAddonPackets implements BasePacketPayload.PacketTypeProvider {

    CONFIGURE_SPAWNER(ServerboundConfigureSpawnerPacket.class, ServerboundConfigureSpawnerPacket.STREAM_CODEC);

    private final CatnipPacketRegistry.PacketType<?> type;

    <T extends BasePacketPayload> CreateAddonPackets(Class<T> clazz,
                                                     StreamCodec<? super RegistryFriendlyByteBuf, T> codec) {
        var name = this.name().toLowerCase(Locale.ROOT);
        this.type =
                new CatnipPacketRegistry.PacketType(new CustomPacketPayload.Type(CreateMobSpawnersMod.asResource(name)), clazz, codec);
    }

    @Override
    public <T extends CustomPacketPayload> CustomPacketPayload.Type<T> getType() {
        return (CustomPacketPayload.Type<T>) type.type();
    }

    public static void init() {
        var packetRegistry = new CatnipPacketRegistry(CreateMobSpawnersMod.MOD_ID, "1");

        for (var packet : values()) {
            packetRegistry.registerPacket(packet.type);
        }

        packetRegistry.registerAllPackets();
    }
}
