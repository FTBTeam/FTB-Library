package dev.ftb.mods.ftblibrary.icon.expr;

import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * One layer of an icon expression: a base string plus an ordered list of {@code key[=value]} modifiers.
 * <p>
 * Modifier order is preserved, as are bare keys (a key with no value, which FTB Library treats as {@code 1}).
 * If a key appears more than once the last occurrence wins, as it does in FTB Library.
 */
public final class IconLayer {
    /** A single modifier; {@code value} is null for a bare key */
    public record Modifier(String key, @Nullable String value) {
        @Override
        public String toString() {
            return value == null ? key : key + "=" + value;
        }
    }

    private String base;
    private final List<Modifier> modifiers = new ArrayList<>();

    public IconLayer(String base) {
        this.base = base.trim();
    }

    public IconLayer(String base, List<Modifier> modifiers) {
        this(base);
        this.modifiers.addAll(modifiers);
    }

    public IconLayer copy() {
        return new IconLayer(base, modifiers);
    }

    /** @return the raw base string, including any prefix, e.g. {@code hollow_rectangle:#FF0000} */
    public String base() {
        return base;
    }

    public void setBase(String base) {
        this.base = base.trim();
    }

    public LayerKind kind() {
        return LayerKind.detect(base);
    }

    /** @return the part of the base after the kind's prefix (or the whole base if the kind has no prefix) */
    public String argument() {
        LayerKind k = kind();
        return k.hasArgument() ? base.substring(k.prefix().length()) : base;
    }

    /** Replace the argument, keeping the current kind's prefix */
    public void setArgument(String arg) {
        LayerKind k = kind();
        base = (k.hasArgument() ? k.prefix() : "") + arg.trim();
    }

    /** Change the layer to a new kind, using the given argument (the prefix is added as needed) */
    public void setKind(LayerKind kind, String argument) {
        base = kind == LayerKind.NONE ? "none" : kind.prefix() + argument.trim();
    }

    public List<Modifier> modifiers() {
        return modifiers;
    }

    /** @return the effective value of a modifier (the last occurrence), or null if absent or bare */
    public @Nullable String get(String key) {
        for (int i = modifiers.size() - 1; i >= 0; i--) {
            if (modifiers.get(i).key().equals(key)) {
                return modifiers.get(i).value();
            }
        }
        return null;
    }

    public boolean has(String key) {
        return modifiers.stream().anyMatch(m -> m.key().equals(key));
    }

    /** @return true if the key is present and, as FTB Library evaluates booleans, equals "1" (a bare key counts) */
    public boolean isFlagSet(String key) {
        for (int i = modifiers.size() - 1; i >= 0; i--) {
            if (modifiers.get(i).key().equals(key)) {
                String v = modifiers.get(i).value();
                return v == null || v.equals("1");
            }
        }
        return false;
    }

    /** Set a modifier's value: replaces the last occurrence in place, or appends */
    public void set(String key, @Nullable String value) {
        for (int i = modifiers.size() - 1; i >= 0; i--) {
            if (modifiers.get(i).key().equals(key)) {
                modifiers.set(i, new Modifier(key, value));
                return;
            }
        }
        modifiers.add(new Modifier(key, value));
    }

    /** Turn a boolean flag on (as a bare key) or off (removed) */
    public void setFlag(String key, boolean on) {
        if (on) {
            if (!isFlagSet(key)) {
                remove(key);
                modifiers.add(new Modifier(key, null));
            }
        } else {
            remove(key);
        }
    }

    /** Remove all occurrences of a key */
    public void remove(String key) {
        modifiers.removeIf(m -> m.key().equals(key));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(base);
        for (Modifier m : modifiers) {
            sb.append("; ").append(m);
        }
        return sb.toString();
    }
}
