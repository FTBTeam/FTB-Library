package dev.ftb.mods.ftblibrary.math;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import java.util.Objects;

/**
 * @author LatvianModder
 */
public class ChunkDimPos implements Comparable<ChunkDimPos> {
	private final ResourceKey<Level> dimension;
	private final ChunkPos chunkPos;
	private int hash;

	public ChunkDimPos(ResourceKey<Level> dim, int x, int z) {
		dimension = dim;
		chunkPos = new ChunkPos(x, z);

		int h = Objects.hash(dimension.location(), chunkPos);
		hash = h == 0 ? 1 : h;
	}

	public ChunkDimPos(ResourceKey<Level> dim, ChunkPos pos) {
		this(dim, pos.x, pos.z);
	}

	public ChunkDimPos(Level world, BlockPos pos) {
		this(world.dimension(), pos.getX() >> 4, pos.getZ() >> 4);
	}

	public ChunkDimPos(Entity entity) {
		this(entity.level(), entity.blockPosition());
	}

	public ChunkPos getChunkPos() {
		return chunkPos;
	}

	public int x() {
		return chunkPos.x;
	}

	public int z() {
		return chunkPos.z;
	}

	@Override
	public String toString() {
		return "[" + dimension.location() + ":" + x() + ":" + z() + "]";
	}

	@Override
	public int hashCode() {
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		} else if (obj instanceof ChunkDimPos p) {
			return dimension == p.dimension && chunkPos.equals(p.chunkPos);
		}

		return false;
	}

	@Override
	public int compareTo(ChunkDimPos o) {
		var i = dimension.location().compareTo(o.dimension.location());
		return i == 0 ? Long.compare(getChunkPos().toLong(), o.getChunkPos().toLong()) : i;
	}

	public ChunkDimPos offset(int ox, int oz) {
		return new ChunkDimPos(dimension, x() + ox, z() + oz);
	}
}