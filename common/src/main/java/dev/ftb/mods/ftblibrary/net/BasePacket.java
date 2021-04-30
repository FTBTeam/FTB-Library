package dev.ftb.mods.ftblibrary.net;

import io.netty.buffer.Unpooled;
import me.shedaniel.architectury.networking.NetworkManager;
import me.shedaniel.architectury.utils.GameInstance;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.Objects;

public abstract class BasePacket {
	public abstract ResourceLocation getId();

	public abstract void write(FriendlyByteBuf buffer);

	public abstract void handle(NetworkManager.PacketContext context);

	public final <T> Packet<?> toPacket(NetworkManager.Side side) {
		FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
		write(buf);
		return NetworkManager.toPacket(side, getId(), buf);
	}

	@Environment(EnvType.CLIENT)
	public final <T> void sendToServer() {
		if (Minecraft.getInstance().getConnection() != null) {
			Minecraft.getInstance().getConnection().send(toPacket(NetworkManager.c2s()));
		} else {
			throw new IllegalStateException("Unable to send packet to the server while not in game!");
		}
	}

	public final <T> void sendTo(ServerPlayer player) {
		Objects.requireNonNull(player, "Unable to send packet to a 'null' player!").connection.send(toPacket(NetworkManager.s2c()));
	}

	public final <T> void sendTo(Iterable<ServerPlayer> players) {
		Packet<?> packet = toPacket(NetworkManager.s2c());

		for (ServerPlayer player : players) {
			Objects.requireNonNull(player, "Unable to send packet to a 'null' player!").connection.send(packet);
		}
	}

	public final void sendToAll() {
		sendTo(GameInstance.getServer().getPlayerList().getPlayers());
	}

	public final void sendToDimension(ResourceKey<Level> dimension) {
		sendTo(GameInstance.getServer().getLevel(dimension).players());
	}

	public final void sendToChunkListeners(LevelChunk chunk) {
		Packet<?> packet = toPacket(NetworkManager.s2c());
		((ServerChunkCache) chunk.getLevel().getChunkSource()).chunkMap.getPlayers(chunk.getPos(), false).forEach(e -> e.connection.send(packet));
	}
}
