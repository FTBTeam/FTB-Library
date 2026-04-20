package dev.ftb.mods.ftblibrary.expression.provider;

/// Math context provider, registered under the `math.` key.
///
/// Exposes common mathematical functions to expressions.
/// TODO: This class raises a valid question if we should allow for classes to be generated
///       as providers?
public class MathContextProvider extends ContextProvider {
    public MathContextProvider() {
        super("math");
    }

    /// See [Math#floor]
    public long floor(double value) {
        return (long) Math.floor(value);
    }

    /// See [Math#ceil]
    public long ceil(double value) {
        return (long) Math.ceil(value);
    }

    /// See [Math#round]
    public long round(double value) {
        return Math.round(value);
    }

    /// See [Math#abs]
    public long abs(long value) {
        return Math.abs(value);
    }

    /// See [Math#abs]
    public double absDouble(double value) {
        return Math.abs(value);
    }

    /// See [Math#min]
    public long min(long a, long b) {
        return Math.min(a, b);
    }

    /// See [Math#min]
    public double minDouble(double a, double b) {
        return Math.min(a, b);
    }

    /// See [Math#max]
    public long max(long a, long b) {
        return Math.max(a, b);
    }

    /// See [Math#max]
    public double maxDouble(double a, double b) {
        return Math.max(a, b);
    }

    /// See [Math#pow]
    public double pow(double base, double exponent) {
        return Math.pow(base, exponent);
    }

    /// See [Math#sqrt]
    public double sqrt(double value) {
        return Math.sqrt(value);
    }

    /// See [Math#cbrt]
    public double cbrt(double value) {
        return Math.cbrt(value);
    }

    /// See [Math#log]
    public double log(double value) {
        return Math.log(value);
    }

    /// See [Math#log10]
    public double log10(double value) {
        return Math.log10(value);
    }

    /// See [Math#sin]
    public double sin(double radians) {
        return Math.sin(radians);
    }

    /// See [Math#cos]
    public double cos(double radians) {
        return Math.cos(radians);
    }

    /// See [Math#tan]
    public double tan(double radians) {
        return Math.tan(radians);
    }

    /// See [Math#toRadians]
    public double toRadians(double degrees) {
        return Math.toRadians(degrees);
    }

    /// See [Math#toDegrees]
    public double toDegrees(double radians) {
        return Math.toDegrees(radians);
    }

    /// See [Math#clamp]
    public int clamp(int value, int min, int max) {
        return Math.clamp(value, min, max);
    }

    /// See [Math#clamp]
    public long clampLong(long value, long min, long max) {
        return Math.clamp(value, min, max);
    }

    /// See [Math#clamp]
    public double clampDouble(double value, double min, double max) {
        return Math.clamp(value, min, max);
    }

    /// See [Math#clamp]
    public float clampFloat(float value, float min, float max) {
        return Math.clamp(value, min, max);
    }

    /// See [Math#PI]
    public double pi() {
        return Math.PI;
    }

    /// See [Math#E]
    public double e() {
        return Math.E;
    }
}
