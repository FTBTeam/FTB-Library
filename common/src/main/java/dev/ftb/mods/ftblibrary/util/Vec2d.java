package dev.ftb.mods.ftblibrary.util;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import net.minecraft.Util;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.List;

public record Vec2d(double x, double y) {
    public static final Vec2d ZERO = new Vec2d(0.0, 0.0);
    public static final Vec2d ONE = new Vec2d(1.0F, 1.0);
    public static final Vec2d UNIT_X = new Vec2d(1.0, 0.0);
    public static final Vec2d NEG_UNIT_X = new Vec2d(-1.0, 0.0);
    public static final Vec2d UNIT_Y = new Vec2d(0.0, 1.0);
    public static final Vec2d NEG_UNIT_Y = new Vec2d(0.0, -1.0);
    public static final Vec2d MAX = new Vec2d(Double.MAX_VALUE, Double.MAX_VALUE);
    public static final Vec2d MIN = new Vec2d(Double.MIN_VALUE, Double.MIN_VALUE);

    public static final Codec<Vec2d> CODEC = Codec.DOUBLE.listOf()
            .comapFlatMap(
                    input -> Util.fixedSize(input, 2)
                            .map(doubles -> new Vec2d(doubles.getFirst(), doubles.getLast())),
                    vec -> List.of(vec.x, vec.y)
            );
    public static final Codec<Pair<Vec2d,Vec2d>> PAIR_CODEC = Codec.DOUBLE.listOf()
            .comapFlatMap(
                    input -> Util.fixedSize(input, 4)
                            .map(doubles -> Pair.of(new Vec2d(doubles.get(0), doubles.get(1)), new Vec2d(doubles.get(2), doubles.get(3)))),
                    pair -> List.of(pair.getFirst().x, pair.getFirst().y, pair.getSecond().x, pair.getSecond().y)
            );
    public static final StreamCodec<FriendlyByteBuf, Vec2d> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.DOUBLE, v -> v.x,
            ByteBufCodecs.DOUBLE, v -> v.y,
            Vec2d::new
    );

    public Vec2d scale(final double s) {
        return new Vec2d(this.x * s, this.y * s);
    }

    public double dot(final Vec2d v) {
        return this.x * v.x + this.y * v.y;
    }

    public Vec2d add(final Vec2d rhs) {
        return new Vec2d(this.x + rhs.x, this.y + rhs.y);
    }

    public Vec2d sub(final Vec2d rhs) {
        return new Vec2d(this.x - rhs.x, this.y - rhs.y);
    }

    public Vec2d add(final double v) {
        return new Vec2d(this.x + v, this.y + v);
    }

    public boolean equals(final Vec2d rhs) {
        return this.x == rhs.x && this.y == rhs.y;
    }

    public Vec2d normalized() {
        double dist = Math.sqrt(this.x * this.x + this.y * this.y);
        return dist < 1.0E-4F ? ZERO : new Vec2d(this.x / dist, this.y / dist);
    }

    public double length() {
        return Math.sqrt(this.x * this.x + this.y * this.y);
    }

    public double lengthSquared() {
        return this.x * this.x + this.y * this.y;
    }

    public double distanceToSqr(final Vec2d p) {
        double xd = p.x - this.x;
        double yd = p.y - this.y;
        return xd * xd + yd * yd;
    }

    public Vec2d negated() {
        return new Vec2d(-this.x, -this.y);
    }
}
