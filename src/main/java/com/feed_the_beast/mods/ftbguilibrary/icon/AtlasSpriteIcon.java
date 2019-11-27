package com.feed_the_beast.mods.ftbguilibrary.icon;

import com.feed_the_beast.mods.ftbguilibrary.utils.IPixelBuffer;
import com.feed_the_beast.mods.ftbguilibrary.utils.PixelBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;

/**
 * @author LatvianModder
 */
public class AtlasSpriteIcon extends Icon
{
	public final String name;
	public Color4I color;

	AtlasSpriteIcon(String n)
	{
		name = n;
		color = Color4I.WHITE;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void draw(int x, int y, int w, int h)
	{
		TextureManager textureManager = Minecraft.getInstance().getTextureManager();
		textureManager.bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
		textureManager.getTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE).setBlurMipmap(false, false);
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();
		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
		TextureAtlasSprite sprite = Minecraft.getInstance().getTextureMap().getAtlasSprite(name);
		int r = color.redi();
		int g = color.greeni();
		int b = color.bluei();
		int a = color.alphai();
		buffer.pos(x, y + h, 0D).tex(sprite.getMinU(), sprite.getMaxV()).color(r, g, b, a).endVertex();
		buffer.pos(x + w, y + h, 0D).tex(sprite.getMaxU(), sprite.getMaxV()).color(r, g, b, a).endVertex();
		buffer.pos(x + w, y, 0D).tex(sprite.getMaxU(), sprite.getMinV()).color(r, g, b, a).endVertex();
		buffer.pos(x, y, 0D).tex(sprite.getMinU(), sprite.getMinV()).color(r, g, b, a).endVertex();
		tessellator.draw();
		textureManager.getTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE).restoreLastBlurMipmap();
	}

	@Override
	public String toString()
	{
		return name;
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
			ResourceLocation rl = new ResourceLocation(name);
			return PixelBuffer.from(Minecraft.getInstance().getResourceManager().getResource(new ResourceLocation(rl.getNamespace(), "textures/" + rl.getPath() + ".png")).getInputStream());
		}
		catch (Exception ex)
		{
			return null;
		}
	}

	@Override
	public AtlasSpriteIcon copy()
	{
		return new AtlasSpriteIcon(name);
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