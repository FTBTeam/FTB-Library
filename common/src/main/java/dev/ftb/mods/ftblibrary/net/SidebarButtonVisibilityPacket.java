package dev.ftb.mods.ftblibrary.net;

import dev.ftb.mods.ftblibrary.FTBLibrary;
import dev.ftb.mods.ftblibrary.platform.network.PacketContext;
import dev.ftb.mods.ftblibrary.sidebar.SidebarButtonManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record SidebarButtonVisibilityPacket(Identifier id, boolean visible) implements CustomPacketPayload {
    public static final Type<SidebarButtonVisibilityPacket> TYPE = new Type<>(FTBLibrary.rl("sidebar_button_visibility"));

    public static final StreamCodec<FriendlyByteBuf, SidebarButtonVisibilityPacket> STREAM_CODEC = StreamCodec.composite(
            Identifier.STREAM_CODEC, SidebarButtonVisibilityPacket::id,
            ByteBufCodecs.BOOL, SidebarButtonVisibilityPacket::visible,
            SidebarButtonVisibilityPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SidebarButtonVisibilityPacket packet, PacketContext ignoredPacketContext) {
        SidebarButtonManager.INSTANCE.getButton(packet.id).ifPresent(button -> button.setForceHidden(!packet.visible));
    }
}
