package com.feed_the_beast.mods.ftbguilibrary.icon;

import com.feed_the_beast.mods.ftbguilibrary.utils.IPixelBuffer;
import com.feed_the_beast.mods.ftbguilibrary.utils.PixelBuffer;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.HttpTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;

import javax.imageio.ImageIO;
import java.io.File;
import java.net.URI;

/**
 * @author LatvianModder
 */
public class URLImageIcon extends ImageIcon
{
	public final URI uri;
	private final String url;

	public URLImageIcon(ResourceLocation tex, URI _uri)
	{
		super(tex);
		uri = _uri;
		url = uri.toString();
	}

	public URLImageIcon(URI uri)
	{
		this(new ResourceLocation(uri.toString()), uri);
	}

	@Override
	public URLImageIcon copy()
	{
		URLImageIcon icon = new URLImageIcon(texture, uri);
		icon.minU = minU;
		icon.minV = minV;
		icon.maxU = maxU;
		icon.maxV = maxV;
		icon.tileSize = tileSize;
		return icon;
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void bindTexture()
	{
		TextureManager manager = Minecraft.getInstance().getTextureManager();
		AbstractTexture img = manager.getTexture(texture);

		if (img == null)
		{
			if (uri.getScheme().equals("http") || uri.getScheme().equals("https"))
			{
				img = new HttpTexture(null, url, MISSING_IMAGE, false, null);
			}
			else
			{
				File file = null;

				if (uri.getScheme().equals("file"))
				{
					try
					{
						file = new File(uri.getPath());
					}
					catch (Exception ex)
					{
						ex.printStackTrace();
					}
				}

				if (file == null)
				{
					file = new File(uri);
				}

				img = new HttpTexture(file, url, MISSING_IMAGE, false, null);
			}

			manager.register(texture, img);
		}

		RenderSystem.bindTexture(img.getId());
	}

	public String toString()
	{
		return url;
	}

	@Override
	public IPixelBuffer createPixelBuffer()
	{
		try
		{
			return PixelBuffer.from(ImageIO.read(uri.toURL().openConnection(Minecraft.getInstance().getProxy()).getInputStream()));
		}
		catch (Exception ex)
		{
			return null;
		}
	}
}