package com.feed_the_beast.mods.ftbguilibrary.icon;

import com.feed_the_beast.mods.ftbguilibrary.utils.IPixelBuffer;
import com.feed_the_beast.mods.ftbguilibrary.utils.PixelBuffer;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.math.Matrix4f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;

/**
 * @author LatvianModder
 */
public class AtlasSpriteIcon extends Icon
{
	public final ResourceLocation id;
	public Color4I color;

	AtlasSpriteIcon(ResourceLocation n)
	{
		id = n;
		color = Color4I.WHITE;
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void draw(PoseStack matrixStack, int x, int y, int w, int h)
	{
		TextureAtlasSprite sprite = Minecraft.getInstance().getModelManager().getAtlas(TextureAtlas.LOCATION_BLOCKS).getSprite(id);

		if (sprite == null)
		{
			return;
		}

		Matrix4f m = matrixStack.last().pose();

		int r = color.redi();
		int g = color.greeni();
		int b = color.bluei();
		int a = color.alphai();

		float minU = sprite.getU0();
		float minV = sprite.getV0();
		float maxU = sprite.getU1();
		float maxV = sprite.getV1();

		sprite.atlas().bind();
		BufferBuilder buffer = Tesselator.getInstance().getBuilder();
		buffer.begin(GL11.GL_QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);
		buffer.vertex(m, x, y, 0F).color(r, g, b, a).uv(minU, minV).endVertex();
		buffer.vertex(m, x, y + h, 0F).color(r, g, b, a).uv(minU, maxV).endVertex();
		buffer.vertex(m, x + w, y + h, 0F).color(r, g, b, a).uv(maxU, maxV).endVertex();
		buffer.vertex(m, x + w, y, 0F).color(r, g, b, a).uv(maxU, minV).endVertex();
		buffer.end();
		BufferUploader.end(buffer);
	}

	@Override
	public String toString()
	{
		return id.toString();
	}

	@Override
	public boolean hasPixelBuffer()
	{
		return true;
	}

	@Override
	@Nullable
	public IPixelBuffer createPixelBuffer()
	{
		try
		{
			return PixelBuffer.from(Minecraft.getInstance().getResourceManager().getResource(new ResourceLocation(id.getNamespace(), "textures/" + id.getPath() + ".png")).getInputStream());
		}
		catch (Exception ex)
		{
			return null;
		}
	}

	@Override
	public AtlasSpriteIcon copy()
	{
		return new AtlasSpriteIcon(id);
	}

	@Override
	public AtlasSpriteIcon withColor(Color4I color)
	{
		AtlasSpriteIcon icon = copy();
		icon.color = color;
		return icon;
	}

	@Override
	public AtlasSpriteIcon withTint(Color4I c)
	{
		return withColor(color.withTint(c));
	}
}