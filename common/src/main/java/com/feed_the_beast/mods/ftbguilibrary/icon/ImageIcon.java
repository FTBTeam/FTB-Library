package com.feed_the_beast.mods.ftbguilibrary.icon;

import com.feed_the_beast.mods.ftbguilibrary.FTBGUILibrary;
import com.feed_the_beast.mods.ftbguilibrary.utils.IPixelBuffer;
import com.feed_the_beast.mods.ftbguilibrary.utils.PixelBuffer;
import com.feed_the_beast.mods.ftbguilibrary.widget.GuiHelper;
import com.google.common.base.Objects;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.math.Matrix4f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;

/**
 * @author LatvianModder
 */
public class ImageIcon extends Icon
{
	public static final ResourceLocation MISSING_IMAGE = new ResourceLocation(FTBGUILibrary.MOD_ID, "textures/gui/missing_image.png");

	public final ResourceLocation texture;
	public float minU, minV, maxU, maxV;
	public double tileSize;
	public Color4I color;

	public ImageIcon(ResourceLocation tex)
	{
		texture = tex;
		minU = 0;
		minV = 0;
		maxU = 1;
		maxV = 1;
		tileSize = 0;
		color = Color4I.WHITE;
	}

	@Override
	public ImageIcon copy()
	{
		ImageIcon icon = new ImageIcon(texture);
		icon.minU = minU;
		icon.minV = minV;
		icon.maxU = maxU;
		icon.maxV = maxV;
		icon.tileSize = tileSize;
		return icon;
	}

	@Override
	protected void setProperties(IconProperties properties)
	{
		super.setProperties(properties);
		minU = (float) properties.getDouble("u0", minU);
		minV = (float) properties.getDouble("v0", minV);
		maxU = (float) properties.getDouble("u1", maxU);
		maxV = (float) properties.getDouble("v1", maxV);
		tileSize = properties.getDouble("tile_size", tileSize);
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void bindTexture()
	{
		TextureManager manager = Minecraft.getInstance().getTextureManager();
		AbstractTexture tex = manager.getTexture(texture);

		if (tex == null)
		{
			tex = new SimpleTexture(texture);
			manager.register(texture, tex);
		}

		GlStateManager._bindTexture(tex.getId());
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void draw(PoseStack matrixStack, int x, int y, int w, int h)
	{
		bindTexture();

		if (tileSize <= 0D)
		{
			GuiHelper.drawTexturedRect(matrixStack, x, y, w, h, color, minU, minV, maxU, maxV);
		}
		else
		{
			int r = color.redi();
			int g = color.greeni();
			int b = color.bluei();
			int a = color.alphai();

			Matrix4f m = matrixStack.last().pose();
			Tesselator tessellator = Tesselator.getInstance();
			BufferBuilder buffer = tessellator.getBuilder();
			buffer.begin(GL11.GL_QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);
			buffer.vertex(m, x, y + h, 0).color(r, g, b, a).uv((float) (x / tileSize), (float) ((y + h) / tileSize)).endVertex();
			buffer.vertex(m, x + w, y + h, 0).color(r, g, b, a).uv((float) ((x + w) / tileSize), (float) ((y + h) / tileSize)).endVertex();
			buffer.vertex(m, x + w, y, 0).color(r, g, b, a).uv((float) ((x + w) / tileSize), (float) (y / tileSize)).endVertex();
			buffer.vertex(m, x, y, 0).color(r, g, b, a).uv((float) (x / tileSize), (float) (y / tileSize)).endVertex();
			tessellator.end();
		}
	}

	@Override
	public int hashCode()
	{
		return Objects.hashCode(texture, minU, minV, maxU, maxV);
	}

	@Override
	public boolean equals(Object o)
	{
		if (o == this)
		{
			return true;
		}
		else if (o instanceof ImageIcon)
		{
			ImageIcon img = (ImageIcon) o;
			return texture.equals(img.texture) && minU == img.minU && minV == img.minV && maxU == img.maxU && maxV == img.maxV;
		}
		return false;
	}

	@Override
	public String toString()
	{
		return texture.toString();
	}

	@Override
	public ImageIcon withColor(Color4I color)
	{
		ImageIcon icon = copy();
		icon.color = color;
		return icon;
	}

	@Override
	public ImageIcon withTint(Color4I c)
	{
		return withColor(color.withTint(c));
	}

	@Override
	public ImageIcon withUV(float u0, float v0, float u1, float v1)
	{
		ImageIcon icon = copy();
		icon.minU = u0;
		icon.minV = v0;
		icon.maxU = u1;
		icon.maxV = v1;
		return icon;
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
			return PixelBuffer.from(Minecraft.getInstance().getResourceManager().getResource(texture).getInputStream());
		}
		catch (Exception ex)
		{
			return null;
		}
	}
}