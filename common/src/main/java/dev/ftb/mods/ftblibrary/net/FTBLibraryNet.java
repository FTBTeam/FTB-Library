package dev.ftb.mods.ftblibrary.net;

import dev.ftb.mods.ftblibrary.util.NetworkHelper;

public class FTBLibraryNet {
    public static void register() {
        NetworkHelper.registerS2C(EditConfigPacket.TYPE, EditConfigPacket.STREAM_CODEC, EditConfigPacket::handle);
        NetworkHelper.registerS2C(EditNBTPacket.TYPE, EditNBTPacket.STREAM_CODEC, EditNBTPacket::handle);
        NetworkHelper.registerS2C(SyncKnownServerRegistriesPacket.TYPE, SyncKnownServerRegistriesPacket.STREAM_CODEC, SyncKnownServerRegistriesPacket::handle);

        NetworkHelper.registerC2S(EditNBTResponsePacket.TYPE, EditNBTResponsePacket.STREAM_CODEC, EditNBTResponsePacket::handle);
    }
}
