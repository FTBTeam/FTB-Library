package dev.ftb.mods.ftblibrary.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

/**
 * Represents a range of integers with a minimum and maximum value.
 *
 * @param min the minimum value of the range
 * @param max the maximum value of the range
 */
public record MinMax(
        int min,
        int max
) {
    /**
     * Creates a new MinMax instance with the given minimum and maximum values.
     */
    public static MinMax of(int min, int max) {
        return new MinMax(min, max);
    }

    /**
     * Support for when a range isn't required but is optional. This is common when this value is configurable by the user
     *
     * @param value the exact value to use for both min and max
     * @return a new MinMax instance with the given value as both the minimum and maximum
     */
    public static MinMax exact(int value) {
        return new MinMax(value, value);
    }

    public static Codec<MinMax> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    Codec.INT.fieldOf("min").forGetter(MinMax::min),
                    Codec.INT.fieldOf("max").forGetter(MinMax::max)
            ).apply(instance, MinMax::new)
    );

    public static StreamCodec<ByteBuf, MinMax> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            MinMax::min,
            ByteBufCodecs.INT,
            MinMax::max,
            MinMax::new
    );

    /**
     * Checks if the given value is within the range defined by min and max
     *
     * @param value the value to check
     * @return true if the value is within the range, false otherwise
     */
    public boolean isInRange(int value) {
        return value >= min && value <= max;
    }

    /**
     * Checks if the range is exact, meaning that min and max are equal
     * @return true if min and max are equal, false otherwise
     */
    public boolean isExact() {
        return min == max;
    }

    /**
     * Returns a random value between min and max (inclusive)
     *
     * @param source the random source to use for generating the random value
     * @return a random value between min and max
     */
    public int randomInRange(RandomSource source) {
        return Mth.randomBetweenInclusive(source, this.min, this.max);
    }
}
