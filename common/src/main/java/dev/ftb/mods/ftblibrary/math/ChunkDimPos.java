package dev.ftb.mods.ftblibrary.math;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

public record ChunkDimPos(ResourceKey<Level> dimension, ChunkPos chunkPos) implements Comparable<ChunkDimPos> {
    private static final StreamCodec<ByteBuf, ChunkPos> CHUNK_POS_STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, cp -> cp.x,
            ByteBufCodecs.INT, cp -> cp.z,
            ChunkPos::new
    );

    public static StreamCodec<FriendlyByteBuf, ChunkDimPos> STREAM_CODEC = StreamCodec.composite(
            ResourceKey.streamCodec(Registries.DIMENSION), ChunkDimPos::dimension,
            CHUNK_POS_STREAM_CODEC, ChunkDimPos::chunkPos,
            ChunkDimPos::new
    );

    public ChunkDimPos(ResourceKey<Level> dim, int x, int z) {
        this(dim, new ChunkPos(x, z));
    }

    public ChunkDimPos(Level world, BlockPos pos) {
        this(world.dimension(), pos.getX() >> 4, pos.getZ() >> 4);
    }

    public ChunkDimPos(Entity entity) {
        this(entity.level(), entity.blockPosition());
    }

    public int x() {
        return chunkPos.x;
    }

    public int z() {
        return chunkPos.z;
    }

    public ResourceKey<Level> dimension() {
        return dimension;
    }

    @Override
    public int compareTo(ChunkDimPos o) {
        var i = dimension.location().compareTo(o.dimension.location());
        return i == 0 ? Long.compare(chunkPos.toLong(), o.chunkPos.toLong()) : i;
    }

    public ChunkDimPos offset(int ox, int oz) {
        return new ChunkDimPos(dimension, x() + ox, z() + oz);
    }
}
