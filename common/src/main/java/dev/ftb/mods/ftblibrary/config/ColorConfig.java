package dev.ftb.mods.ftblibrary.config;

import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.icon.Icon;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class ColorConfig extends ConfigFromString<Color4I> {
	public ColorConfig() {
		defaultValue = Icon.empty();
		value = Icon.empty();
	}

	@Override
	public Color4I getColor(Color4I v) {
		return v;
	}

	@Override
	public boolean parse(@Nullable Consumer<Color4I> callback, String string) {
		try {
			if (string.indexOf(',') != -1) {
				if (string.length() < 5) {
					return false;
				}

				var s = string.split(",");

				if (s.length == 3 || s.length == 4) {
					var c = new int[4];
					c[3] = 255;

					for (var i = 0; i < s.length; i++) {
						c[i] = Integer.parseInt(s[i]);
					}

					if (callback != null) {
						callback.accept(Color4I.rgba(c[0], c[1], c[2], c[3]));
					}

					return true;
				}
			} else {
				if (string.length() < 6) {
					return false;
				} else if (string.startsWith("#")) {
					string = string.substring(1);
				}

				var hex = Integer.parseInt(string, 16);

				if (callback != null) {
					callback.accept(Color4I.rgba(0xFF000000 | hex));
				}

				return true;
			}
		} catch (Exception ex) {
		}

		return false;
	}
}
