package dev.ftb.mods.ftblibrary.icon.expr;

import java.util.Locale;

/**
 * The kind of source an icon layer draws from, determined from the layer's base string using the same rules (and
 * the same order) as FTB Library's icon parser.
 */
public enum LayerKind {
    /** Empty / "none" */
    NONE,
    /** The special theme value "builtin" */
    BUILTIN,
    /** A whole-layer reference to another theme property, e.g. {@code {{button}}} */
    VARIABLE,
    /** A literal color, e.g. {@code #AARRGGBB}, or a named color */
    COLOR,
    /** {@code color:<color>} */
    COLOR_PREFIXED("color:"),
    /** {@code item:<item id>} */
    ITEM("item:"),
    /** {@code bullet:<color>} */
    BULLET("bullet:"),
    /** {@code hollow_rectangle:<color>} */
    HOLLOW_RECTANGLE("hollow_rectangle:"),
    /** {@code part:<texture>}; a sub-area (optionally nine-sliced) of a texture */
    PART("part:"),
    /** {@code file:<uri>} */
    FILE("file:"),
    /** A .png/.jpg/.webp resource location */
    TEXTURE,
    /** Anything else: an atlas sprite id */
    SPRITE;

    private final String prefix;

    LayerKind() {
        this("");
    }

    LayerKind(String prefix) {
        this.prefix = prefix;
    }

    /** @return the text which precedes the argument in the base string, or empty if there is none */
    public String prefix() {
        return prefix;
    }

    public boolean hasArgument() {
        return !prefix.isEmpty();
    }

    /** Detect the kind of a layer base string */
    public static LayerKind detect(String base) {
        if (base.isEmpty() || base.equals("none")) {
            return NONE;
        }
        if (base.equals("builtin")) {
            return BUILTIN;
        }
        if (base.startsWith("{{") && base.endsWith("}}") && base.indexOf("}}") == base.length() - 2) {
            return VARIABLE;
        }
        if (isLiteralColor(base)) {
            return COLOR;
        }
        for (LayerKind k : values()) {
            if (k.hasArgument() && base.startsWith(k.prefix)) {
                return k;
            }
        }
        String lower = base.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".png") || lower.endsWith(".jpg") || lower.endsWith(".webp")) {
            return TEXTURE;
        }
        return SPRITE;
    }

    /** Mirrors the formats accepted by FTB Library's {@code Color4I.parse} */
    public static boolean isLiteralColor(String s) {
        if ((s.length() == 7 || s.length() == 9) && s.charAt(0) == '#') {
            for (int i = 1; i < s.length(); i++) {
                if (Character.digit(s.charAt(i), 16) < 0) {
                    return false;
                }
            }
            return true;
        }
        return switch (s.toLowerCase(Locale.ROOT)) {
            case "transparent", "black", "dark_gray", "gray", "white", "red", "green", "blue",
                 "light_red", "light_green", "light_blue" -> true;
            default -> false;
        };
    }
}
