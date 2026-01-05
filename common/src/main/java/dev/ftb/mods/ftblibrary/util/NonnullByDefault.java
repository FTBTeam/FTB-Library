package dev.ftb.mods.ftblibrary.util;

import org.jspecify.annotations.NonNull;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * This annotation combines method and parameter nonnullability
 */
@Documented
@NonNull
@Retention(RetentionPolicy.RUNTIME)
public @interface NonnullByDefault {
}
