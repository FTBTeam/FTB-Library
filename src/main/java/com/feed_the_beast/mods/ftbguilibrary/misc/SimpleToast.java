package com.feed_the_beast.mods.ftbguilibrary.misc;

import com.feed_the_beast.mods.ftbguilibrary.icon.Icon;
import com.feed_the_beast.mods.ftbguilibrary.widget.GuiHelper;
import com.feed_the_beast.mods.ftbguilibrary.widget.GuiIcons;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.toasts.IToast;
import net.minecraft.client.gui.toasts.ToastGui;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.ITextProperties;
import net.minecraft.util.text.StringTextComponent;

import java.util.List;

/**
 * @author LatvianModder
 */
public class SimpleToast implements IToast
{
	private boolean hasPlayedSound = false;

	@Override
	public Visibility func_230444_a_(MatrixStack matrixStack, ToastGui gui, long delta)
	{
		GuiHelper.setupDrawing();
		Minecraft mc = gui.getMinecraft();
		mc.getTextureManager().bindTexture(TEXTURE_TOASTS);
		RenderSystem.color4f(1F, 1F, 1F, 1F);
		gui.blit(matrixStack, 0, 0, 0, 0, 160, 32);

		List<ITextProperties> list = mc.fontRenderer.func_238425_b_(getSubtitle(), 125);
		int i = isImportant() ? 16746751 : 16776960;

		if (list.size() == 1)
		{
			mc.fontRenderer.func_238407_a_(matrixStack, getTitle(), 30, 7, i | -16777216);
			mc.fontRenderer.func_238407_a_(matrixStack, list.get(0), 30, 18, -1);
		}
		else
		{
			if (delta < 1500L)
			{
				int k = MathHelper.floor(MathHelper.clamp((float) (1500L - delta) / 300F, 0F, 1F) * 255F) << 24 | 67108864;
				mc.fontRenderer.func_238407_a_(matrixStack, getTitle(), 30, 11, i | k);
			}
			else
			{
				int i1 = MathHelper.floor(MathHelper.clamp((float) (delta - 1500L) / 300F, 0F, 1F) * 252F) << 24 | 67108864;
				int l = 16 - list.size() * mc.fontRenderer.FONT_HEIGHT / 2;

				for (ITextProperties s : list)
				{
					mc.fontRenderer.func_238407_a_(matrixStack, s, 30, l, 16777215 | i1);
					l += mc.fontRenderer.FONT_HEIGHT;
				}
			}
		}

		if (!hasPlayedSound && delta > 0L)
		{
			hasPlayedSound = true;
			playSound(mc.getSoundHandler());
		}

		RenderHelper.enableStandardItemLighting();
		getIcon().draw(8, 8, 16, 16);
		return delta >= 5000L ? IToast.Visibility.HIDE : IToast.Visibility.SHOW;
	}

	public ITextComponent getTitle()
	{
		return new StringTextComponent("<error>");
	}

	public ITextComponent getSubtitle()
	{
		return StringTextComponent.EMPTY;
	}

	public boolean isImportant()
	{
		return false;
	}

	public Icon getIcon()
	{
		return GuiIcons.INFO;
	}

	public void playSound(SoundHandler handler)
	{
	}
}