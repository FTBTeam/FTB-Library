package dev.ftb.mods.ftblibrary.util;

import java.util.function.BooleanSupplier;

public class Lambdas {
    public static BooleanSupplier ALWAYS_TRUE = () -> true;
    public static BooleanSupplier ALWAYS_FALSE = () -> false;

    public static Runnable NO_OP = () -> {};
}
