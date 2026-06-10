package dev.ftb.mods.ftblibrary.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

/**
 * See {@link MinMax} for more details, this is just a float version of that class.
 */
public record MinMaxFloat(
        float min,
        float max
) {
    public static MinMaxFloat of(int min, int max) {
        return new MinMaxFloat(min, max);
    }

    public static MinMaxFloat exact(int value) {
        return new MinMaxFloat(value, value);
    }

    public static Codec<MinMaxFloat> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    Codec.FLOAT.fieldOf("min").forGetter(MinMaxFloat::min),
                    Codec.FLOAT.fieldOf("max").forGetter(MinMaxFloat::max)
            ).apply(instance, MinMaxFloat::new)
    );

    public static StreamCodec<ByteBuf, MinMaxFloat> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT,
            MinMaxFloat::min,
            ByteBufCodecs.FLOAT,
            MinMaxFloat::max,
            MinMaxFloat::new
    );

    public boolean isInRange(int value) {
        return value >= min && value <= max;
    }

    public boolean isExact() {
        return min == max;
    }

    public float randomInRange(RandomSource source) {
        return Mth.randomBetween(source, this.min, this.max);
    }
}
