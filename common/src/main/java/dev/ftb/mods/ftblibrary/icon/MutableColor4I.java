package dev.ftb.mods.ftblibrary.icon;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import dev.ftb.mods.ftblibrary.math.PixelBuffer;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

/**
 * @author LatvianModder
 */
public class MutableColor4I extends Color4I {
	public static final Color4I TEMP = new MutableColor4I(255, 255, 255, 255);

	static class None extends MutableColor4I {
		private boolean hasColor = false;

		None() {
			super(255, 255, 255, 255);
		}

		@Override
		public Color4I set(int r, int g, int b, int a) {
			hasColor = true;
			return super.set(r, g, b, a);
		}

		@Override
		public boolean isEmpty() {
			return !hasColor;
		}

		@Override
		@Nullable
		public PixelBuffer createPixelBuffer() {
			return null;
		}

		public int hashCode() {
			return 0;
		}

		public boolean equals(Object o) {
			return o == this;
		}
	}

	MutableColor4I(int r, int g, int b, int a) {
		super(r, g, b, a);
	}

	@Override
	public MutableColor4I copy() {
		return new MutableColor4I(red, green, blue, alpha);
	}

	@Override
	public boolean isMutable() {
		return true;
	}

	@Override
	public MutableColor4I mutable() {
		return this;
	}

	@Override
	public JsonElement getJson() {
		if (isEmpty()) {
			return JsonNull.INSTANCE;
		}

		JsonObject json = new JsonObject();
		json.addProperty("red", red);
		json.addProperty("green", green);
		json.addProperty("blue", blue);

		if (alpha < 255) {
			json.addProperty("alpha", alpha);
		}

		json.addProperty("mutable", true);
		return json;
	}

	public Color4I set(int r, int g, int b, int a) {
		red = r & 0xFF;
		green = g & 0xFF;
		blue = b & 0xFF;
		alpha = a & 0xFF;
		return this;
	}

	public Color4I set(Color4I col, int a) {
		return set(col.red, col.green, col.blue, a);
	}

	public Color4I set(Color4I col) {
		return set(col, col.alpha);
	}

	public Color4I set(int col, int a) {
		return set(col >> 16, col >> 8, col, a);
	}

	public Color4I set(int col) {
		return set(col, col >> 24);
	}

	public Color4I setAlpha(int a) {
		alpha = a;
		return this;
	}

	public Color4I addBrightness(int b) {
		return set(Mth.clamp(red + b, 0, 255), Mth.clamp(green + b, 0, 255), Mth.clamp(blue + b, 0, 255), alpha);
	}

	private int toint(float f) {
		return (int) (f * 255F + 0.5F);
	}

	public Color4I setFromHSB(float h, float s, float b) {
		red = green = blue = 0;
		if (s <= 0 || b <= 0) {
			red = green = blue = toint(b);
			return this;
		}

		if (s > 1F) {
			s = 1F;
		}

		if (b > 1F) {
			b = 1F;
		}

		float h6 = (h - Mth.floor(h)) * 6F;
		float f = h6 - Mth.floor(h6);
		float p = b * (1F - s);
		float q = b * (1F - s * f);
		float t = b * (1F - (s * (1F - f)));
		switch ((int) h6) {
			case 0 -> {
				red = toint(b);
				green = toint(t);
				blue = toint(p);
				return this;
			}
			case 1 -> {
				red = toint(q);
				green = toint(b);
				blue = toint(p);
				return this;
			}
			case 2 -> {
				red = toint(p);
				green = toint(b);
				blue = toint(t);
				return this;
			}
			case 3 -> {
				red = toint(p);
				green = toint(q);
				blue = toint(b);
				return this;
			}
			case 4 -> {
				red = toint(t);
				green = toint(p);
				blue = toint(b);
				return this;
			}
			default -> {
				red = toint(b);
				green = toint(p);
				blue = toint(q);
				return this;
			}
		}
	}
}