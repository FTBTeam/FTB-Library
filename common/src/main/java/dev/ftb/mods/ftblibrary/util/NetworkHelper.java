package dev.ftb.mods.ftblibrary.util;

import dev.architectury.injectables.annotations.ExpectPlatform;
import dev.architectury.networking.NetworkManager;
import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public class NetworkHelper {
    public static <T extends CustomPacketPayload> void registerC2S(CustomPacketPayload.Type<T> type, StreamCodec<? super RegistryFriendlyByteBuf, T> codec, NetworkManager.NetworkReceiver<T> handler) {
        NetworkManager.registerReceiver(NetworkManager.c2s(), type, codec, handler);
    }

    public static <T extends CustomPacketPayload> void registerS2C(CustomPacketPayload.Type<T> type, StreamCodec<? super RegistryFriendlyByteBuf, T> codec, NetworkManager.NetworkReceiver<T> handler) {
        if (Platform.getEnvironment() == Env.CLIENT) {
            NetworkManager.registerReceiver(NetworkManager.s2c(), type, codec, handler);
        } else {
            NetworkManager.registerS2CPayloadType(type, codec);
        }
    }

    @ExpectPlatform
    public static void sendToAll(CustomPacketPayload.Type<?> type, MinecraftServer server, Packet<?> packet) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void sendTo(CustomPacketPayload.Type<?> type, ServerPlayer player, Packet<?> packet) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static <T extends CustomPacketPayload> void sendToAll(MinecraftServer server, T packet) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static <T extends CustomPacketPayload> void sendTo(ServerPlayer player, T packet) {
        throw new AssertionError();
    }

    public static <B extends FriendlyByteBuf, V extends Enum<V>> StreamCodec<B, V> enumStreamCodec(Class<V> enumClass) {
        return new StreamCodec<>() {
            @Override
            public V decode(B buf) {
                return buf.readEnum(enumClass);
            }

            @Override
            public void encode(B buf, V value) {
                buf.writeEnum(value);
            }
        };
    }
}
