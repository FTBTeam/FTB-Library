package dev.ftb.mods.ftblibrary.util;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author LatvianModder
 * This annotation combines method and parameter nonnullability
 */
@Documented
@NotNull
@Retention(RetentionPolicy.RUNTIME)
public @interface NonnullByDefault {
}