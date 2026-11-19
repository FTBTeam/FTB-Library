package dev.ftb.mods.ftblibrary.icon.expr;

import java.util.ArrayList;
import java.util.List;

/**
 * A parsed icon expression: an ordered list of layers, drawn bottom to top. Serialises to the syntax understood by
 * FTB Library's {@code Icon.getIcon(String)}:
 * <ul>
 *     <li>layers are separated by {@code " + "} (with spaces)</li>
 *     <li>within a layer, the base and each modifier are separated by {@code "; "}</li>
 *     <li>a modifier is {@code key} or {@code key=value}</li>
 * </ul>
 * Parsing is deliberately lenient and lossless: anything not understood is kept verbatim in a layer's base or
 * as an unrecognised modifier, so that opening and re-saving an expression never loses information.
 * {@code {{variable}}} references are not expanded; they are kept as opaque text.
 */
public final class IconExpression {
    private final List<IconLayer> layers = new ArrayList<>();

    public IconExpression() {
    }

    public IconExpression(List<IconLayer> layers) {
        this.layers.addAll(layers);
    }

    /**
     * Parse an icon expression, following the same splitting rules as FTB Library.
     */
    public static IconExpression parse(String text) {
        IconExpression expr = new IconExpression();
        String t = text.trim();
        if (t.isEmpty() || t.equals("none")) {
            return expr;
        }

        for (String layerText : t.split(" \\+ ")) {
            String[] parts = layerText.split("; ");
            IconLayer layer = new IconLayer(parts[0]);
            for (int i = 1; i < parts.length; i++) {
                String p = parts[i].trim();
                if (p.isEmpty()) {
                    continue;
                }
                String[] kv = p.split("=", 2);
                layer.modifiers().add(new IconLayer.Modifier(kv[0], kv.length == 1 ? null : kv[1]));
            }
            expr.layers.add(layer);
        }
        return expr;
    }

    public List<IconLayer> layers() {
        return layers;
    }

    public boolean isEmpty() {
        return layers.isEmpty();
    }

    public IconExpression copy() {
        IconExpression e = new IconExpression();
        layers.forEach(l -> e.layers.add(l.copy()));
        return e;
    }

    @Override
    public String toString() {
        if (layers.isEmpty()) {
            return "none";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < layers.size(); i++) {
            if (i > 0) {
                sb.append(" + ");
            }
            sb.append(layers.get(i));
        }
        return sb.toString();
    }
}
