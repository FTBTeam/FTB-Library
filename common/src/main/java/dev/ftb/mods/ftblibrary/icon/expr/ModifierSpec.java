package dev.ftb.mods.ftblibrary.icon.expr;

import java.util.List;

/**
 * Describes the modifiers which FTB Library understands for a given layer kind, so that editors can offer the
 * right controls. Keys not listed here are still preserved, just shown as plain text.
 */
public record ModifierSpec(String key, Type type) {
    public enum Type {
        /** A color: {@code #RRGGBB}, {@code #AARRGGBB} or a named color */
        COLOR,
        /** An integer */
        INT,
        /** A decimal number */
        DOUBLE,
        /** A flag: present (bare) means on, absent means off */
        FLAG,
        /** Free text */
        TEXT
    }

    private static final List<ModifierSpec> COMMON = List.of(
            new ModifierSpec("padding", Type.INT),
            new ModifierSpec("color", Type.COLOR),
            new ModifierSpec("tint", Type.COLOR),
            new ModifierSpec("border", Type.COLOR),
            new ModifierSpec("border_round_edges", Type.FLAG)
    );

    private static final List<ModifierSpec> IMAGE = List.of(
            new ModifierSpec("tile_size", Type.DOUBLE),
            new ModifierSpec("u0", Type.DOUBLE),
            new ModifierSpec("v0", Type.DOUBLE),
            new ModifierSpec("u1", Type.DOUBLE),
            new ModifierSpec("v1", Type.DOUBLE)
    );

    private static final List<ModifierSpec> PART = List.of(
            // pos="x,y,width,height"
            new ModifierSpec("pos", Type.TEXT),
            new ModifierSpec("corner", Type.INT),
            new ModifierSpec("texture_w", Type.INT),
            new ModifierSpec("texture_h", Type.INT)
    );

    /** @return the modifiers applicable to a layer of this kind, kind-specific ones first */
    public static List<ModifierSpec> forKind(LayerKind kind) {
        List<ModifierSpec> list = new java.util.ArrayList<>();
        switch (kind) {
            case TEXTURE, FILE -> list.addAll(IMAGE);
            case PART -> list.addAll(PART);
            case HOLLOW_RECTANGLE -> list.add(new ModifierSpec("round_edges", Type.FLAG));
            case BULLET -> list.add(new ModifierSpec("inverse", Type.FLAG));
            default -> {
            }
        }
        if (kind != LayerKind.NONE && kind != LayerKind.BUILTIN) {
            list.addAll(COMMON);
        }
        return list;
    }
}
