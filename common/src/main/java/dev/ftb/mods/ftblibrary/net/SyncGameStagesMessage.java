package dev.ftb.mods.ftblibrary.net;

import dev.architectury.networking.NetworkManager;
import dev.ftb.mods.ftblibrary.FTBLibrary;
import dev.ftb.mods.ftblibrary.util.NetworkHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public record SyncGameStagesMessage(Collection<String> stages, Operation op) implements CustomPacketPayload {
    public static final Type<SyncGameStagesMessage> TYPE = new Type<>(FTBLibrary.rl("sync_game_stage"));
    public static final StreamCodec<FriendlyByteBuf, SyncGameStagesMessage> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.collection(ArrayList::new)), SyncGameStagesMessage::stages,
            NetworkHelper.enumStreamCodec(Operation.class), SyncGameStagesMessage::op,
            SyncGameStagesMessage::new
    );

    public static SyncGameStagesMessage add(String tag) {
        return new SyncGameStagesMessage(List.of(tag), Operation.ADD);
    }

    public static SyncGameStagesMessage add(Collection<String> tags) {
        return new SyncGameStagesMessage(tags, Operation.ADD);
    }

    public static SyncGameStagesMessage remove(String tag) {
        return new SyncGameStagesMessage(List.of(tag), Operation.REMOVE);
    }

    public static SyncGameStagesMessage fullSync(Player player) {
        return new SyncGameStagesMessage(player.getTags(), Operation.REPLACE);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncGameStagesMessage message, NetworkManager.PacketContext context) {
        context.queue(() -> {
            switch (message.op) {
                case ADD -> context.getPlayer().getTags().addAll(message.stages);
                case REMOVE -> context.getPlayer().getTags().removeAll(message.stages);
                case REPLACE -> {
                    context.getPlayer().getTags().clear();
                    context.getPlayer().getTags().addAll(message.stages);
                }
            }
        });
    }

    public enum Operation {
        ADD,
        REMOVE,
        REPLACE
    }
}
