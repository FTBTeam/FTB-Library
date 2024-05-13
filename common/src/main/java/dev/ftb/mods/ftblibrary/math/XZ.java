package dev.ftb.mods.ftblibrary.math;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Vec3i;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;


public record XZ(int x, int z) {
	public static final Codec<XZ> CODEC = RecordCodecBuilder.create(builder -> builder.group(
		Codec.INT.fieldOf("x").forGetter(XZ::x),
		Codec.INT.fieldOf("z").forGetter(XZ::z)
	).apply(builder, XZ::new));

	public static StreamCodec<FriendlyByteBuf, XZ> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.INT, XZ::x,
			ByteBufCodecs.INT, XZ::z,
			XZ::new
	);

	public static XZ of(int x, int z) {
		return new XZ(x, z);
	}

	public static XZ of(long singleLong) {
		return of((int) singleLong, (int) (singleLong >> 32));
	}

	public static XZ of(ChunkPos pos) {
		return of(pos.x, pos.z);
	}

	public static XZ chunkFromBlock(int x, int z) {
		return of(x >> 4, z >> 4);
	}

	public static XZ chunkFromBlock(Vec3i pos) {
		return chunkFromBlock(pos.getX(), pos.getZ());
	}

	public static XZ regionFromChunk(int x, int z) {
		return of(x >> 5, z >> 5);
	}

	public static XZ regionFromChunk(ChunkPos p) {
		return of(p.x >> 5, p.z >> 5);
	}

	public static XZ regionFromBlock(int x, int z) {
		return of(x >> 9, z >> 9);
	}

	public static XZ regionFromBlock(Vec3i pos) {
		return regionFromBlock(pos.getX(), pos.getZ());
	}

	public ChunkDimPos dim(ResourceKey<Level> type) {
		return new ChunkDimPos(type, x, z);
	}

	public ChunkDimPos dim(Level world) {
		return dim(world.dimension());
	}

	public XZ offset(int ox, int oz) {
		return of(x + ox, z + oz);
	}

	public long toLong() {
		return ChunkPos.asLong(x, z);
	}

	public String toRegionString() {
		return String.format("%05X-%05X", x + 60000, z + 60000);
	}
}
