package dev.ftb.mods.ftblibrary.icon;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import dev.ftb.mods.ftblibrary.client.icon.IconRenderer;
import dev.ftb.mods.ftblibrary.config.ImageResourceConfig;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Util;
import org.jspecify.annotations.Nullable;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Represents any drawable object
 */
public abstract class Icon<T extends Icon<T>> {
    public static final Codec<Icon<?>> CODEC = ExtraCodecs.JSON.xmap(Icon::getIcon, Icon::getJson);
    public static final Codec<Icon<?>> STRING_CODEC = Codec.STRING.comapFlatMap(
            s -> {
                Icon<?> res = Icon.getIcon(s);
                return res.isEmpty() ? DataResult.error(() -> "Invalid icon spec: " + s) : DataResult.success(res);
            },
            Icon::toString
    );
    public static final StreamCodec<FriendlyByteBuf, Icon<?>> STREAM_CODEC = StreamCodec.of(
            (buf, icon) -> buf.writeUtf(icon.toString()),
            buf -> Icon.getIcon(buf.readUtf())
    );

    public static Color4I empty() {
        return Color4I.EMPTY_ICON;
    }

    public static Icon<?> getIcon(@Nullable JsonElement json) {
        if (json == null || json.isJsonNull()) {
            return empty();
        } else if (json.isJsonObject()) {
            var o = json.getAsJsonObject();

            if (o.has("id")) {
                switch (o.get("id").getAsString()) {
                    case "color": {
                        var color = Color4I.fromJson(o.get("color"));
                        return (o.has("mutable") && o.get("mutable").getAsBoolean()) ? color.mutable() : color;
                    }
                    case "padding":
                        return getIcon(o.get("parent")).withPadding(o.has("padding") ? o.get("padding").getAsInt() : 0);
                    case "tint":
                        return getIcon(o.get("parent")).withTint(Color4I.fromJson(o.get("color")));
                    case "animation": {
                        List<Icon<?>> icons = new ArrayList<>();

                        for (var e : o.get("icons").getAsJsonArray()) {
                            icons.add(getIcon(e));
                        }

                        return AnimatedIcon.fromList(icons, true);
                    }
                    case "border": {
                        Icon<?> icon = empty();
                        var outline = empty();
                        var roundEdges = false;

                        if (o.has("icon")) {
                            icon = getIcon(o.get("icon"));
                        }

                        if (o.has("color")) {
                            outline = Color4I.fromJson(o.get("color"));
                        }

                        if (o.has("round_edges")) {
                            roundEdges = o.get("round_edges").getAsBoolean();
                        }

                        return icon.withBorder(outline, roundEdges);
                    }
                    case "bullet": {
                        return new BulletIcon().withColor(o.has("color") ? Color4I.fromJson(o.get("color")) : empty());
                    }
                    case "part": {
                        var partIcon = new PartIcon(getIcon(o.get("parent")));
                        partIcon.textureU = o.get("x").getAsInt();
                        partIcon.textureV = o.get("y").getAsInt();
                        partIcon.subWidth = o.get("width").getAsInt();
                        partIcon.subHeight = o.get("height").getAsInt();
                        partIcon.corner = o.get("corner").getAsInt();
                        partIcon.textureWidth = o.get("texture_width").getAsInt();
                        partIcon.textureHeight = o.get("texture_height").getAsInt();
                        return partIcon;
                    }
                }
            }
        } else if (json.isJsonArray()) {
            List<Icon<?>> list = Util.make(new ArrayList<>(), l -> json.getAsJsonArray().forEach(el -> l.add(getIcon(el))));
            return CombinedIcon.getCombined(list);
        }

        var str = json.getAsString();
        if (isNone(str)) {
            return empty();
        }

        var icon = IconPresets.MAP.get(str);
        return icon == null ? getIcon(str) : icon;
    }

    public static Icon<?> getIcon(@Nullable Identifier id) {
        return id == null ? empty() : getIcon(id.toString());
    }

    public static Icon<?> getIcon(String id) {
        if (isNone(id)) {
            return empty();
        }

        var comb = id.split(" \\+ ");
        if (comb.length > 1) {
            var list = new ArrayList<Icon<?>>(comb.length);
            for (var s : comb) {
                list.add(getIcon(s));
            }
            return CombinedIcon.getCombined(list);
        }

        var ids = id.split("; ");
        for (var i = 0; i < ids.length; i++) {
            ids[i] = ids[i].trim();
        }

        var icon = getIconInternal(ids[0]);

        if (ids.length > 1 && !icon.isEmpty()) {
            var properties = new IconProperties();

            for (var i = 1; i < ids.length; i++) {
                var p = ids[i].split("=", 2);
                properties.set(p[0], p.length == 1 ? "1" : p[1]);
            }

            icon.setProperties(properties);

            var padding = properties.getInt("padding", 0);
            if (padding != 0) {
                icon = icon.withPadding(padding);
            }

            var border = properties.getColor("border");
            if (border != null) {
                icon = icon.withBorder(border, properties.getBoolean("border_round_edges", false));
            }

            var color = properties.getColor("color");
            if (color != null) {
                icon = icon.withColor(color);
            }

            var tint = properties.getColor("tint");
            if (tint != null) {
                icon = icon.withTint(tint);
            }
        }

        return icon;
    }

    private static Icon<?> getIconInternal(String id) {
        if (isNone(id)) {
            return Icon.empty();
        }

        var col = Color4I.parse(id);
        if (!col.isEmpty()) {
            return col;
        }

        var ida = id.split(":", 2);
        if (ida.length == 2) {
            switch (ida[0]) {
                case "color":
                    return Color4I.parse(ida[1]);
                case "item":
                    return ItemIcon.parse(ida[1]);
                case "bullet":
                    return new BulletIcon().withColor(Color4I.parse(ida[1]));
                case "http":
                case "https":
                case "file":
                    try {
                        return new URLImageIcon(new URI(id));
                    } catch (Exception ex) {
                        return new ImageIcon(ImageIcon.MISSING_IMAGE);
                    }
                case "hollow_rectangle":
                    return new HollowRectangleIcon(Color4I.parse(ida[1]), false);
                case "part":
                    return new PartIcon(getIcon(ida[1]));
            }
        }

        return (id.endsWith(".png") || id.endsWith(".jpg")) ? new ImageIcon(Identifier.parse(id)) : new AtlasSpriteIcon(Identifier.parse(id));
    }

    private static boolean isNone(String id) {
        return id.isEmpty() || id.equals("none") || id.equals(ImageResourceConfig.NONE.toString());
    }

    public boolean isEmpty() {
        return false;
    }

    public Icon<T> copy() {
        return this;
    }

    public JsonElement getJson() {
        return new JsonPrimitive(toString());
    }

    public final Icon<?> combineWith(Icon<?> icon) {
        if (icon.isEmpty()) {
            return this;
        } else if (isEmpty()) {
            return icon;
        }

        return new CombinedIcon(this, icon);
    }

    public final Icon<?> combineWith(Icon<?>... icons) {
        if (icons.length == 0) {
            return this;
        } else if (icons.length == 1) {
            return combineWith(icons[0]);
        }

        List<Icon<?>> list = new ArrayList<>(icons.length + 1);
        list.add(this);
        list.addAll(Arrays.asList(icons));
        return CombinedIcon.getCombined(list);
    }

    public Icon<T> withColor(Color4I color) {
        return copy();
    }

    public final Icon<?> withBorder(Color4I color, boolean roundEdges) {
        if (color.isEmpty()) {
            return withPadding(1);
        }

        return new BorderedIcon(this, color, roundEdges);
    }

    public final Icon<?> withPadding(int padding) {
        return padding == 0 ? this : new PaddedIcon(this, padding);
    }

    public Icon<T> withTint(Color4I color) {
        return this;
    }

    public Icon<T> withUV(float u0, float v0, float u1, float v1) {
        return this;
    }

    public Icon<T> withUV(float x, float y, float w, float h, float tw, float th) {
        return withUV(x / tw, y / th, (x + w) / tw, (y + h) / th);
    }

    public int hashCode() {
        return getJson().hashCode();
    }

    public boolean equals(Object o) {
        return o == this || o instanceof Icon<?> && getJson().equals(((Icon<?>) o).getJson());
    }

//    /**
//     * @return false if this should be queued for rendering
//     */
//    public boolean hasPixelBuffer() {
//        return false;
//    }
//
//    /**
//     * @return null if this icon does not have a pixel buffer, or if it failed to load
//     */
//    @Nullable
//    public PixelBuffer createPixelBuffer() {
//        return null;
//    }
//
//    /**
//     * The number of animation frames in a pixel-buffer icon. Note: not to be confused with {@link IconAnimation},
//     * which is a collection of individual icons. This returns 1 for most icon types, but icons with animated
//     * textures (currently only atlas sprite icons) may have multiple frames.
//     *
//     * @return the number of frames
//     */
//    public int getPixelBufferFrameCount() {
//        return 1;
//    }
//
//    /**
//     * Get the aspect ratio of the icon, which is the width divided by height. For most icon types this is always 1.0,
//     * since icons do not in general know what size they are (they're scaled when drawn). However, for atlas sprite
//     * and image icons, the underlying image's aspect ratio is returned.
//     *
//     * @return the aspect ratio of the icon
//     */
//    public double aspectRatio() {
//        return 1.0;
//    }

    @Nullable
    public Object getIngredient() {
        return null;
    }

    protected void setProperties(IconProperties properties) {
    }

    public T self() {
        // this is safe to do because Icon<> is a self-referential generic
        //noinspection unchecked
        return (T) this;
    }

    public abstract IconRenderer<T> getRenderer();
}
