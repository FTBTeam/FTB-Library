package dev.ftb.mods.ftblibrary.snbt;

import net.minecraft.nbt.*;
import org.jspecify.annotations.Nullable;

import java.io.DataOutput;
import java.io.IOException;

public class SpecialTags {
    public static final Tag TRUE = ByteTag.valueOf(true);
    public static final Tag FALSE = ByteTag.valueOf(false);
    public static final Tag NAN_D = DoubleTag.valueOf(Double.NaN);
    public static final Tag POS_INFINITY_D = DoubleTag.valueOf(Double.POSITIVE_INFINITY);
    public static final Tag NEG_INFINITY_D = DoubleTag.valueOf(Double.NEGATIVE_INFINITY);
    public static final Tag NAN_F = FloatTag.valueOf(Float.NaN);
    public static final Tag POS_INFINITY_F = FloatTag.valueOf(Float.POSITIVE_INFINITY);
    public static final Tag NEG_INFINITY_F = FloatTag.valueOf(Float.NEGATIVE_INFINITY);
}
