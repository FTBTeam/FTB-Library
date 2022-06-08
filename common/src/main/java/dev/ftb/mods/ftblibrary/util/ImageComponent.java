package dev.ftb.mods.ftblibrary.util;

import dev.ftb.mods.ftblibrary.icon.Icon;
import net.minecraft.network.chat.ComponentContents;

// TODO: Fix me
public class ImageComponent implements ComponentContents {
	public Icon image = Icon.EMPTY;
	public int width = 100;
	public int height = 100;
	public int align = 1;
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
		sb.append(" align:").append(align);

		if (fit) {
			sb.append(" fit:true");
		}

		sb.append('}');
		return sb.toString();
	}
}
