package dev.ftb.mods.ftblibrary.util.client;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.ftb.mods.ftblibrary.config.NameMap;
import dev.ftb.mods.ftblibrary.icon.Icon;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.util.StringRepresentable;

// TODO: Fix me
public class ImageComponent implements ComponentContents {
    public static final MapCodec<ImageComponent> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.fieldOf("image_id").forGetter(ImageComponent::imageStr),
            Codec.INT.optionalFieldOf("width", 100).forGetter(ImageComponent::getWidth),
            Codec.INT.optionalFieldOf("height", 100).forGetter(ImageComponent::getHeight),
            ImageAlign.CODEC.optionalFieldOf("align", ImageAlign.CENTER).forGetter(ImageComponent::getAlign),
            Codec.BOOL.optionalFieldOf("fit", false).forGetter(ImageComponent::isFit)
    ).apply(instance, ImageComponent::create));

    private static final ComponentContents.Type<ImageComponent> TYPE = new ComponentContents.Type<>(CODEC, "image");

    private Icon image = Icon.empty();
    private int width = 100;
    private int height = 100;
    private ImageAlign align = ImageAlign.CENTER;
    private boolean fit = false;

    public ImageComponent() {
        super();
    }

    public static ImageComponent create(String id, int width, int height, ImageAlign align, boolean fit) {
        ImageComponent c = new ImageComponent();
        c.image = Icon.getIcon(id);
        c.width = width;
        c.height = height;
        c.align = align;
        c.fit = fit;
        return c;
    }

    public String imageStr() {
        return image.toString();
    }

    public Icon getImage() {
        return image;
    }

    public void setImage(Icon image) {
        this.image = image;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public ImageAlign getAlign() {
        return align;
    }

    public void setAlign(ImageAlign align) {
        this.align = align;
    }

    public boolean isFit() {
        return fit;
    }

    public void setFit(boolean fit) {
        this.fit = fit;
    }

    @Override
    public String toString() {
        var sb = new StringBuilder("{image:");
        sb.append(image);

        sb.append(" width:").append(width);
        sb.append(" height:").append(height);
        sb.append(" align:").append(align.getName());

        if (fit) {
            sb.append(" fit:true");
        }

        sb.append('}');
        return sb.toString();
    }

    @Override
    public Type<?> type() {
        return TYPE;
    }

    public enum ImageAlign implements StringRepresentable {
        LEFT("left"),
        CENTER("center"),
        RIGHT("right");

        public static final Codec<ImageAlign> CODEC = StringRepresentable.fromEnum(ImageAlign::values);
        public static final NameMap<ImageAlign> NAME_MAP = NameMap.of(CENTER, values()).id(v -> v.name).create();

        private final String name;

        ImageAlign(String name) {
            this.name = name;
        }

        public static ImageAlign byName(String name) {
            return NAME_MAP.get(name);
        }

        public String getName() {
            return name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }
}
