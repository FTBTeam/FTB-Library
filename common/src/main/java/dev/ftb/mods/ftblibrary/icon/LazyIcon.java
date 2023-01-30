package dev.ftb.mods.ftblibrary.icon;

import com.google.gson.JsonElement;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.ftb.mods.ftblibrary.math.PixelBuffer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class LazyIcon extends Icon {
	public final Supplier<Icon> iconSupplier;
	private Icon cachedIcon;

	public LazyIcon(Supplier<Icon> s) {
		iconSupplier = s;
	}

	public Icon getIcon() {
		if (cachedIcon == null) {
			cachedIcon = iconSupplier.get();

			if (cachedIcon == null || cachedIcon.isEmpty()) {
				cachedIcon = Icon.empty();
			}
		}

		return cachedIcon;
	}

	@Override
	public boolean isEmpty() {
		return getIcon().isEmpty();
	}

	@Override
	public Icon copy() {
		return new LazyIcon(() -> getIcon().copy());
	}

	@Override
	public JsonElement getJson() {
		return getIcon().getJson();
	}

	@Override
	public Icon withColor(Color4I color) {
		return getIcon().withColor(color);
	}

	@Override
	public Icon withTint(Color4I color) {
		return getIcon().withTint(color);
	}

	@Override
	public Icon withUV(float u0, float v0, float u1, float v1) {
		return getIcon().withUV(u0, v0, u1, v1);
	}

	public int hashCode() {
		return getJson().hashCode();
	}

	@Override
	public boolean hasPixelBuffer() {
		return getIcon().hasPixelBuffer();
	}

	@Override
	@Nullable
	public PixelBuffer createPixelBuffer() {
		return getIcon().createPixelBuffer();
	}

	@Override
	@Nullable
	public Object getIngredient() {
		return getIcon().getIngredient();
	}

	@Override
	protected void setProperties(IconProperties properties) {
		getIcon().setProperties(properties);
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void draw(PoseStack matrixStack, int x, int y, int w, int h) {
		getIcon().draw(matrixStack, x, y, w, h);
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void drawStatic(PoseStack matrixStack, int x, int y, int w, int h) {
		getIcon().drawStatic(matrixStack, x, y, w, h);
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void draw3D(PoseStack matrixStack) {
		getIcon().draw3D(matrixStack);
	}

	@Override
	public String toString() {
		return getIcon().toString();
	}
}
