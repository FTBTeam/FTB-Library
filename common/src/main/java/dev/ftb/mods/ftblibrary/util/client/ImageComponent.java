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
            AspectRatioHint.CODEC.optionalFieldOf("aspect_ratio_hint", AspectRatioHint.NONE).forGetter(ImageComponent::getAspectRatioHint),
            Codec.BOOL.optionalFieldOf("fit", false).forGetter(ImageComponent::isFit),
            Codec.STRING.optionalFieldOf("click_action", "").forGetter(ImageComponent::getClickAction)
    ).apply(instance, ImageComponent::create));

    private static final ComponentContents.Type<ImageComponent> TYPE = new ComponentContents.Type<>(CODEC, "image");

    private Icon image = Icon.empty();
    private int width = 100;
    private int height = 100;
    private ImageAlign align = ImageAlign.CENTER;
    private AspectRatioHint aspectRatioHint = AspectRatioHint.NONE;
    private boolean fit = false;
    private String clickAction = "";

    public ImageComponent() {
        super();
    }

    public static ImageComponent create(String id, int width, int height, ImageAlign align, AspectRatioHint aspectRatioHint, boolean fit, String clickAction) {
        ImageComponent c = new ImageComponent();
        c.image = Icon.getIcon(id);
        c.width = width;
        c.height = height;
        c.align = align;
        c.aspectRatioHint = aspectRatioHint;
        c.fit = fit;
        c.clickAction = clickAction;
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

    public AspectRatioHint getAspectRatioHint() {
        return aspectRatioHint;
    }

    public void setAspectRatioHint(AspectRatioHint aspectRatioHint) {
        this.aspectRatioHint = aspectRatioHint;
    }

    public boolean isFit() {
        return fit;
    }

    public void setFit(boolean fit) {
        this.fit = fit;
    }

    public String getClickAction() {
        return clickAction;
    }

    public void setClickAction(String clickAction) {
        this.clickAction = clickAction;
    }

    @Override
    public String toString() {
        var sb = new StringBuilder("{image:");
        sb.append(image);

        sb.append(" width:").append(width);
        sb.append(" height:").append(height);
        sb.append(" align:").append(align.getName());

        if (aspectRatioHint != AspectRatioHint.NONE) {
            sb.append(" aspect_ratio_hint:").append(aspectRatioHint.getId());
        }

        if (fit) {
            sb.append(" fit:true");
        }

        if (!clickAction.isEmpty()) {
            sb.append(" click_action:").append(clickAction.replace(" ", "%20"));
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

    public enum AspectRatioHint implements StringRepresentable {
        NONE("none"),
        ADJUST_WIDTH("adjust_width"),
        ADJUST_HEIGHT("adjust_height");

        public static final Codec<AspectRatioHint> CODEC = StringRepresentable.fromEnum(AspectRatioHint::values);
        public static final NameMap<AspectRatioHint> NAME_MAP = NameMap.of(NONE, values()).id(AspectRatioHint::getId).create();

        private final String id;

        AspectRatioHint(String id) {
            this.id = id;
        }

        public static AspectRatioHint byId(String id) {
            return NAME_MAP.get(id);
        }

        public String getId() {
            return id;
        }

        @Override
        public String getSerializedName() {
            return id;
        }
    }
}
