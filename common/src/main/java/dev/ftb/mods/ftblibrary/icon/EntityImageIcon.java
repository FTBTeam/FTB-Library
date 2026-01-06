package dev.ftb.mods.ftblibrary.icon;

import com.google.common.collect.Streams;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.ftb.mods.ftblibrary.client.icon.EntityImageIconRenderer;
import dev.ftb.mods.ftblibrary.client.icon.IconRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureContents;
import net.minecraft.resources.Identifier;
import org.apache.commons.lang3.tuple.Pair;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EntityImageIcon extends Icon<EntityImageIcon> {
    @Nullable
    private final Slice mainSlice;
    private final List<ChildIconData> children;
    private final Icon<?> mainIcon;
    private final List<Icon<?>> childIcons;
    private final EntityIconLoader.@Nullable WidthHeight defaultImageSize;

    public EntityImageIcon(Identifier mainTexture, @Nullable Slice mainSlice, List<ChildIconData> children, EntityIconLoader.@Nullable WidthHeight defaultImageSize) {
        this.mainSlice = mainSlice;
        this.children = children;
        this.defaultImageSize = defaultImageSize;

        mainIcon = createIcon(mainTexture, mainSlice);
        childIcons = children.stream().map(c -> createIcon(c.texture.orElse(mainTexture), c.slice)).collect(Collectors.toList());
    }

    public int getDrawWidth(int defWidth) {
        return mainSlice == null ? defWidth : mainSlice.width;
    }

    public int getDrawHeight(int defHeight) {
        return mainSlice == null ? defHeight : mainSlice.height;
    }

    public Icon<?> getMainIcon() {
        return mainIcon;
    }

    public List<Icon<?>> getChildIcons() {
        return childIcons;
    }

    public Stream<Pair<Icon<?>, ChildIconData>> children() {
        return Streams.zip(childIcons.stream(), children.stream(), Pair::of);
    }

    @Override
    public IconRenderer<EntityImageIcon> getRenderer() {
        return EntityImageIconRenderer.INSTANCE;
    }

    public record Offset(int x, int y) {
        public static final Codec<Offset> CODEC = RecordCodecBuilder.create(builder -> builder.group(
                Codec.INT.fieldOf("x").forGetter(o -> o.x),
                Codec.INT.fieldOf("y").forGetter(o -> o.y)
        ).apply(builder, Offset::new));
    }

    public record Slice(int x, int y, int width, int height) {
        public static final Codec<Slice> CODEC = RecordCodecBuilder.create(builder -> builder.group(
                Codec.INT.fieldOf("x").forGetter(o -> o.x),
                Codec.INT.fieldOf("y").forGetter(o -> o.y),
                Codec.INT.fieldOf("width").forGetter(s -> s.width),
                Codec.INT.fieldOf("height").forGetter(s -> s.height)
        ).apply(builder, Slice::new));
    }

    public record ChildIconData(Optional<Identifier> texture, Slice slice, Optional<Offset> offset) {
        public static final Codec<ChildIconData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Identifier.CODEC.optionalFieldOf("texture").forGetter(entityImageIcon -> entityImageIcon.texture),
                Slice.CODEC.fieldOf("slice").forGetter(ChildIconData::slice),
                Offset.CODEC.optionalFieldOf("offset").forGetter(ChildIconData::offset)
        ).apply(instance, ChildIconData::new));
    }

    private Icon<?> createIcon(Identifier texture, @Nullable Slice slice) {
        try (SimpleTexture tex = new SimpleTexture(texture)) {
            TextureContents load = tex.loadContents(Minecraft.getInstance().getResourceManager());
            ImageIcon imageIcon = new ImageIcon(texture);
            if (slice != null) {
                int textureWidth = load.image().getWidth();
                int textureHeight = load.image().getHeight();
                if (defaultImageSize != null) {
                    int defaultTextureWidth = defaultImageSize.width();
                    int defaultTextureHeight = defaultImageSize.height();

                    float scaleX = (float) textureWidth / defaultTextureWidth;
                    float scaleY = (float) textureHeight / defaultTextureHeight;
                    return imageIcon.withUV(slice.x * scaleX, slice.y * scaleY, slice.width * scaleX, slice.height * scaleY, textureWidth, textureHeight);
                } else {
                    return imageIcon.withUV(slice.x, slice.y, slice.width, slice.height, textureWidth, textureHeight);
                }
            }
            return imageIcon;
        } catch (Exception e) {
            throw new RuntimeException("Failed to load image: " + texture, e);
        }
    }
}
