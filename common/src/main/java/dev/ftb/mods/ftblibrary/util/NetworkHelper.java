package dev.ftb.mods.ftblibrary.util;

import dev.architectury.networking.NetworkManager;
import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

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
}
