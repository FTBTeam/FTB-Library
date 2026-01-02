package dev.ftb.mods.ftblibrary.icon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureContents;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class EntityImageIcon extends Icon {
    @Nullable
    private final Slice mainSlice;
    private final List<ChildIconData> children;
    private final Icon mainIcon;
    private final List<Icon> childIcons;
    @Nullable
    private final EntityIconLoader.WidthHeight defaultImageSize;

    public EntityImageIcon(Identifier mainTexture, @Nullable Slice mainSlice, List<ChildIconData> children, @Nullable EntityIconLoader.WidthHeight defaultImageSize) {
        this.mainSlice = mainSlice;
        this.children = children;
        this.defaultImageSize = defaultImageSize;

        mainIcon = createIcon(mainTexture, mainSlice);
        childIcons = children.stream().map(childIconData -> createIcon(childIconData.texture.orElse(mainTexture), childIconData.slice)).toList();
    }

    @Override
    public void draw(GuiGraphics graphics, int x, int y, int width, int height) {
        var pose = graphics.pose();
        pose.pushMatrix();
        pose.translate(x, y);

        float drawWidth = mainSlice == null ? width : mainSlice.width;
        float drawHeight = mainSlice == null ? height : mainSlice.height;

        float scaleX = width / drawWidth;
        float scaleY = height / drawHeight;

        pose.scale(scaleX, scaleY);

        mainIcon.draw(graphics, 0, 0, (int) drawWidth, (int) drawHeight);

        for (int i = 0; i < children.size(); i++) {
            ChildIconData child = children.get(i);
            Icon icon = childIcons.get(i);

            pose.pushMatrix();
            child.offset.ifPresent(offset -> pose.translate(offset.x, offset.y));
            icon.draw(graphics, 0, 0, child.slice.width, child.slice.height);
            pose.popMatrix();
        }

        pose.popMatrix();
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

    private Icon createIcon(Identifier texture, @Nullable Slice slice) {
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
