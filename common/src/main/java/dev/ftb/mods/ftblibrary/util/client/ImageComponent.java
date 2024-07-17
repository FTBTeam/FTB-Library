package dev.ftb.mods.ftblibrary.util.client;

import dev.ftb.mods.ftblibrary.config.NameMap;
import dev.ftb.mods.ftblibrary.icon.Icon;
import net.minecraft.network.chat.ComponentContents;

// TODO: Fix me
public class ImageComponent implements ComponentContents {
	public Icon image = Icon.empty();
	public int width = 100;
	public int height = 100;
	public ImageAlign align = ImageAlign.CENTER;
	public boolean fit = false;

	public ImageComponent() {
		super();
	}

	@Override
	public String toString() {
		var sb = new StringBuilder("{image:");
		sb.append(image);

		sb.append(" width:").append(width);
		sb.append(" height:").append(height);
		sb.append(" align:").append(align.name);

		if (fit) {
			sb.append(" fit:true");
		}

		sb.append('}');
		return sb.toString();
	}

	public enum ImageAlign {
		LEFT("left"),
		CENTER("center"),
		RIGHT("right");

		public static final NameMap<ImageAlign> NAME_MAP = NameMap.of(CENTER, values()).id(v -> v.name).create();

		private final String name;

		ImageAlign(String name) {
			this.name = name;
		}

		public static ImageAlign fromString(String str) {
			return switch (str.toLowerCase()) {
				case "left", "0" -> ImageAlign.LEFT;
                case "right", "2" -> ImageAlign.RIGHT;
				default -> CENTER;
			};
		}
	}
}
