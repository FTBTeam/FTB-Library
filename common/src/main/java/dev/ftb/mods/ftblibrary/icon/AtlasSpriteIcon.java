package dev.ftb.mods.ftblibrary.icon;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.ftb.mods.ftblibrary.math.PixelBuffer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

/**
 * @author LatvianModder
 */
public class AtlasSpriteIcon extends Icon {
	public final ResourceLocation id;
	public Color4I color;

	AtlasSpriteIcon(ResourceLocation n) {
		id = n;
		color = Color4I.WHITE;
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void draw(PoseStack matrixStack, int x, int y, int w, int h) {
		var sprite = Minecraft.getInstance().getModelManager().getAtlas(TextureAtlas.LOCATION_BLOCKS).getSprite(id);

		if (sprite == null) {
			return;
		}

		var m = matrixStack.last().pose();

		var r = color.redi();
		var g = color.greeni();
		var b = color.bluei();
		var a = color.alphai();

		var minU = sprite.getU0();
		var minV = sprite.getV0();
		var maxU = sprite.getU1();
		var maxV = sprite.getV1();

		RenderSystem.setShader(GameRenderer::getPositionColorTexShader);
		RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
		RenderSystem.setShaderTexture(0, sprite.atlas().getId());
		var buffer = Tesselator.getInstance().getBuilder();
		buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);
		buffer.vertex(m, x, y, 0F).color(r, g, b, a).uv(minU, minV).endVertex();
		buffer.vertex(m, x, y + h, 0F).color(r, g, b, a).uv(minU, maxV).endVertex();
		buffer.vertex(m, x + w, y + h, 0F).color(r, g, b, a).uv(maxU, maxV).endVertex();
		buffer.vertex(m, x + w, y, 0F).color(r, g, b, a).uv(maxU, minV).endVertex();
		buffer.end();
		BufferUploader.end(buffer);
	}

	@Override
	public String toString() {
		return id.toString();
	}

	@Override
	public boolean hasPixelBuffer() {
		return true;
	}

	@Override
	@Nullable
	public PixelBuffer createPixelBuffer() {
		try {
			return PixelBuffer.from(Minecraft.getInstance().getResourceManager().getResource(new ResourceLocation(id.getNamespace(), "textures/" + id.getPath() + ".png")).getInputStream());
		} catch (Exception ex) {
			return null;
		}
	}

	@Override
	public AtlasSpriteIcon copy() {
		return new AtlasSpriteIcon(id);
	}

	@Override
	public AtlasSpriteIcon withColor(Color4I color) {
		var icon = copy();
		icon.color = color;
		return icon;
	}

	@Override
	public AtlasSpriteIcon withTint(Color4I c) {
		return withColor(color.withTint(c));
	}
}