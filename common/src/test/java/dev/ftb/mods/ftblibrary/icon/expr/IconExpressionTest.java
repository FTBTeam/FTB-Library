package dev.ftb.mods.ftblibrary.icon.expr;

import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.*;

class IconExpressionTest {
    @Test
    void parsesLayersAndModifiers() {
        IconExpression e = IconExpression.parse("hollow_rectangle:{{widget_border}}; round_edges + {{background}}; color=#B4B4B4; padding=1");
        assertEquals(2, e.layers().size());

        IconLayer a = e.layers().get(0);
        assertEquals(LayerKind.HOLLOW_RECTANGLE, a.kind());
        assertEquals("{{widget_border}}", a.argument());
        assertTrue(a.isFlagSet("round_edges"));
        assertNull(a.get("round_edges"));

        IconLayer b = e.layers().get(1);
        assertEquals(LayerKind.VARIABLE, b.kind());
        assertEquals("#B4B4B4", b.get("color"));
        assertEquals("1", b.get("padding"));
    }

    @Test
    void roundTripsExactly() {
        String[] samples = {
                "ftblibrary:textures/gui/background_squares.png; color=#DCFFFFFF; tile_size=64",
                "part:minecraft:textures/gui/container/enchanting_table.png; pos=0,185,108,19; corner=6",
                "{{button}} + {{widget_background}}; padding=1",
                "{{button}}; padding=-1",
                "hollow_rectangle:#1B1D1E; round_edges",
                "item:minecraft:stone",
                "#44000000",
                "builtin",
                "ftblibrary:icons/accept",
                "none",
        };
        for (String s : samples) {
            assertEquals(s, IconExpression.parse(s).toString(), s);
        }
    }

    @Test
    void emptyAndNone() {
        assertTrue(IconExpression.parse("").isEmpty());
        assertTrue(IconExpression.parse("none").isEmpty());
        assertEquals("none", new IconExpression().toString());
    }

    @Test
    void canonicalFormIsStable() {
        assertEquals("a.png; color=#FF0000 + b.png", IconExpression.parse("a.png; color=#FF0000 + b.png").toString());
    }

    @Test
    void kindDetection() {
        assertEquals(LayerKind.COLOR, LayerKind.detect("#FF00FF"));
        assertEquals(LayerKind.COLOR, LayerKind.detect("#80FF00FF"));
        assertEquals(LayerKind.COLOR, LayerKind.detect("white"));
        assertEquals(LayerKind.COLOR_PREFIXED, LayerKind.detect("color:#FF00FF"));
        assertEquals(LayerKind.TEXTURE, LayerKind.detect("ftbquests:textures/gui/pin.png"));
        assertEquals(LayerKind.SPRITE, LayerKind.detect("ftblibrary:icons/accept"));
        assertEquals(LayerKind.ITEM, LayerKind.detect("item:minecraft:stone"));
        assertEquals(LayerKind.PART, LayerKind.detect("part:minecraft:textures/x.png"));
        assertEquals(LayerKind.VARIABLE, LayerKind.detect("{{button}}"));
        assertEquals(LayerKind.BUILTIN, LayerKind.detect("builtin"));
        assertEquals(LayerKind.NONE, LayerKind.detect("none"));
    }

    @Test
    void modifierEditing() {
        IconLayer l = IconExpression.parse("a.png; color=#111111; padding=2").layers().getFirst();
        l.set("color", "#222222");
        l.set("tile_size", "64");
        l.remove("padding");
        l.setFlag("round_edges", true);
        assertEquals("a.png; color=#222222; tile_size=64; round_edges", l.toString());
        l.setFlag("round_edges", false);
        assertEquals("a.png; color=#222222; tile_size=64", l.toString());
    }

    @Test
    void duplicateKeysLastWins() {
        IconLayer l = IconExpression.parse("a.png; color=#111111; color=#222222").layers().getFirst();
        assertEquals("#222222", l.get("color"));
        l.set("color", "#333333");
        assertEquals("a.png; color=#111111; color=#333333", l.toString());
    }

    @Test
    void changingKindAndArgument() {
        IconLayer l = new IconLayer("#FF0000");
        l.setKind(LayerKind.HOLLOW_RECTANGLE, "#00FF00");
        assertEquals("hollow_rectangle:#00FF00", l.toString());
        l.setArgument("#0000FF");
        assertEquals("hollow_rectangle:#0000FF", l.toString());
    }
}
