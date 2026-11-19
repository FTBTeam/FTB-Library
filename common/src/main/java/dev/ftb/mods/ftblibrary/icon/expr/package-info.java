/**
 * An editable parse tree for the textual icon syntax of {@link dev.ftb.mods.ftblibrary.icon.Icon#getIcon(String)} (e.g. {@code "hollow_rectangle:#1B1D1E; round_edges + #44000000; padding=1"}).
 * <p>
 * {@code Icon.getIcon(String)} only goes from string to icon, discarding the structure, so editors
 * can't use it. This package parses to a structure which serialises back to equivalent text.
 * <p>
 * This package deliberately has no dependencies on Minecraft or on any other FTB code, so it is usable (and
 * unit-testable) anywhere.
 */
@NullMarked
package dev.ftb.mods.ftblibrary.icon.expr;

import org.jspecify.annotations.NullMarked;
