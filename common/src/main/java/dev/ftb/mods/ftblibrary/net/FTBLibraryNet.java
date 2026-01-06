package dev.ftb.mods.ftblibrary.net;

import dev.ftb.mods.ftblibrary.util.NetworkHelper;

public class FTBLibraryNet {
    public static void register() {
        NetworkHelper.registerS2C(EditConfigPacket.TYPE, EditConfigPacket.STREAM_CODEC, EditConfigPacket::handle);
        NetworkHelper.registerS2C(EditConfigChoicePacket.TYPE, EditConfigChoicePacket.STREAM_CODEC, EditConfigChoicePacket::handle);
        NetworkHelper.registerS2C(EditNBTPacket.TYPE, EditNBTPacket.STREAM_CODEC, EditNBTPacket::handle);
        NetworkHelper.registerS2C(SyncKnownServerRegistriesPacket.TYPE, SyncKnownServerRegistriesPacket.STREAM_CODEC, SyncKnownServerRegistriesPacket::handle);
        NetworkHelper.registerS2C(SyncConfigFromServerPacket.TYPE, SyncConfigFromServerPacket.STREAM_CODEC, SyncConfigFromServerPacket::handle);
        NetworkHelper.registerS2C(SidebarButtonVisibilityPacket.TYPE, SidebarButtonVisibilityPacket.STREAM_CODEC, SidebarButtonVisibilityPacket::handle);
        NetworkHelper.registerS2C(SyncGameStagesMessage.TYPE, SyncGameStagesMessage.STREAM_CODEC, SyncGameStagesMessage::handle);
        NetworkHelper.registerS2C(OpenTestScreenPacket.TYPE, OpenTestScreenPacket.STREAM_CODEC, OpenTestScreenPacket::handle);

        NetworkHelper.registerC2S(EditNBTResponsePacket.TYPE, EditNBTResponsePacket.STREAM_CODEC, EditNBTResponsePacket::handle);
        NetworkHelper.registerC2S(SyncConfigToServerPacket.TYPE, SyncConfigToServerPacket.STREAM_CODEC, SyncConfigToServerPacket::handle);
    }
}
