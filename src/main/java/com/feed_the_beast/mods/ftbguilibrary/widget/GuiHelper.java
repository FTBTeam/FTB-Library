package com.feed_the_beast.mods.ftbguilibrary.widget;

import com.feed_the_beast.mods.ftbguilibrary.icon.Color4I;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Stack;

/**
 * @author LatvianModder
 */
public class GuiHelper
{
	private static class Scissor
	{
		private final int x, y, w, h;

		private Scissor(int _x, int _y, int _w, int _h)
		{
			x = _x;
			y = _y;
			w = Math.max(0, _w);
			h = Math.max(0, _h);
		}

		public Scissor crop(int sx, int sy, int sw, int sh)
		{
			int x0 = Math.max(x, sx);
			int y0 = Math.max(y, sy);
			int x1 = Math.min(x + w, sx + sw);
			int y1 = Math.min(y + h, sy + sh);
			return new Scissor(x0, y0, x1 - x0, y1 - y0);
		}

		public void scissor(MainWindow screen)
		{
			double scale = screen.getGuiScaleFactor();
			int sx = (int) (x * scale);
			int sy = (int) ((screen.getScaledHeight() - (y + h)) * scale);
			int sw = (int) (w * scale);
			int sh = (int) (h * scale);
			GL11.glScissor(sx, sy, sw, sh);
		}
	}

	private static final Stack<Scissor> SCISSOR = new Stack<>();

	public static final GuiBase BLANK_GUI = new GuiBase()
	{
		@Override
		public void addWidgets()
		{
		}

		@Override
		public void alignWidgets()
		{
		}
	};

	public static void setupDrawing()
	{
		RenderSystem.color4f(1F, 1F, 1F, 1F);
		RenderSystem.disableLighting();
		RenderSystem.enableBlend();
		RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
	}

	public static void playSound(SoundEvent event, float pitch)
	{
		Minecraft.getInstance().getSoundHandler().play(SimpleSound.master(event, pitch));
	}

	public static void drawTexturedRect(int x, int y, int w, int h, Color4I col, float u0, float v0, float u1, float v1)
	{
		if (u0 == u1 || v0 == v1)
		{
			Tessellator tessellator = Tessellator.getInstance();
			BufferBuilder buffer = tessellator.getBuffer();
			buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
			addRectToBuffer(buffer, x, y, w, h, col);
			tessellator.draw();
		}
		else
		{
			Tessellator tessellator = Tessellator.getInstance();
			BufferBuilder buffer = tessellator.getBuffer();
			buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR_TEXTURE);
			addRectToBufferWithUV(buffer, x, y, w, h, col, u0, v0, u1, v1);
			tessellator.draw();
		}
	}

	public static void addRectToBuffer(BufferBuilder buffer, int x, int y, int w, int h, Color4I col)
	{
		int r = col.redi();
		int g = col.greeni();
		int b = col.bluei();
		int a = col.alphai();
		buffer.vertex(x, y + h, 0D).color(r, g, b, a).endVertex();
		buffer.vertex(x + w, y + h, 0D).color(r, g, b, a).endVertex();
		buffer.vertex(x + w, y, 0D).color(r, g, b, a).endVertex();
		buffer.vertex(x, y, 0D).color(r, g, b, a).endVertex();
	}

	public static void addRectToBufferWithUV(BufferBuilder buffer, int x, int y, int w, int h, Color4I col, float u0, float v0, float u1, float v1)
	{
		int r = col.redi();
		int g = col.greeni();
		int b = col.bluei();
		int a = col.alphai();
		buffer.vertex(x, y + h, 0D).color(r, g, b, a).texture(u0, v1).endVertex();
		buffer.vertex(x + w, y + h, 0D).color(r, g, b, a).texture(u1, v1).endVertex();
		buffer.vertex(x + w, y, 0D).color(r, g, b, a).texture(u1, v0).endVertex();
		buffer.vertex(x, y, 0D).color(r, g, b, a).texture(u0, v0).endVertex();
	}

	public static void drawHollowRect(int x, int y, int w, int h, Color4I col, boolean roundEdges)
	{
		if (w <= 1 || h <= 1 || col.isEmpty())
		{
			col.draw(x, y, w, h);
			return;
		}

		RenderSystem.disableTexture();
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();
		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

		addRectToBuffer(buffer, x, y + 1, 1, h - 2, col);
		addRectToBuffer(buffer, x + w - 1, y + 1, 1, h - 2, col);

		if (roundEdges)
		{
			addRectToBuffer(buffer, x + 1, y, w - 2, 1, col);
			addRectToBuffer(buffer, x + 1, y + h - 1, w - 2, 1, col);
		}
		else
		{
			addRectToBuffer(buffer, x, y, w, 1, col);
			addRectToBuffer(buffer, x, y + h - 1, w, 1, col);
		}

		tessellator.draw();
		RenderSystem.enableTexture();
	}

	public static void drawRectWithShade(int x, int y, int w, int h, Color4I col, int intensity)
	{
		RenderSystem.disableTexture();
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();
		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
		addRectToBuffer(buffer, x, y, w - 1, 1, col);
		addRectToBuffer(buffer, x, y + 1, 1, h - 1, col);
		col = col.mutable().addBrightness(-intensity);
		addRectToBuffer(buffer, x + w - 1, y, 1, 1, col);
		addRectToBuffer(buffer, x, y + h - 1, 1, 1, col);
		col = col.mutable().addBrightness(-intensity);
		addRectToBuffer(buffer, x + w - 1, y + 1, 1, h - 2, col);
		addRectToBuffer(buffer, x + 1, y + h - 1, w - 1, 1, col);
		tessellator.draw();
		RenderSystem.enableTexture();
	}

	public static boolean drawItem(ItemStack stack, double x, double y, double scaleX, double scaleY, boolean renderOverlay)
	{
		if (stack.isEmpty())
		{
			return false;
		}
		
		/*

		boolean result = true;

		ItemRenderer renderItem = Minecraft.getInstance().getItemRenderer();
		renderItem.zLevel = 180F;
		RenderSystem.pushMatrix();
		RenderSystem.translated(x, y, 32D);

		if (scaleX != 1D || scaleY != 1D)
		{
			RenderSystem.scaled(scaleX, scaleY, 1D);
		}

		RenderHelper.disableGuiDepthLighting();
		ClientUtils.pushMaxBrightness();
		RenderSystem.enableTexture();

		try
		{
			renderItem.renderItemAndEffectIntoGUI(stack, 0, 0);

			if (renderOverlay)
			{
				FontRenderer font = stack.getItem().getFontRenderer(stack);

				if (font == null)
				{
					font = Minecraft.getInstance().fontRenderer;
				}

				renderItem.renderItemOverlayIntoGUI(font, stack, 0, 0, null);
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			result = false;
		}

		ClientUtils.popBrightness();
		RenderSystem.popMatrix();
		renderItem.zLevel = 0F;
		return result;
		 */

		RenderSystem.pushMatrix();
		RenderSystem.translated(x, y, 0D);
		RenderSystem.scaled(scaleX, scaleY, 1D);
		Minecraft.getInstance().getItemRenderer().renderItemAndEffectIntoGUI(stack, 0, 0);

		if (renderOverlay)
		{
			FontRenderer font = stack.getItem().getFontRenderer(stack);

			if (font == null)
			{
				font = Minecraft.getInstance().fontRenderer;
			}

			Minecraft.getInstance().getItemRenderer().renderItemOverlayIntoGUI(font, stack, 0, 0, null);
		}

		RenderSystem.popMatrix();
		return true;
	}

	public static boolean drawItem(ItemStack stack, double x, double y, boolean renderOverlay)
	{
		return drawItem(stack, x, y, 1D, 1D, renderOverlay);
	}

	public static void pushScissor(MainWindow screen, int x, int y, int w, int h)
	{
		if (SCISSOR.isEmpty())
		{
			GL11.glEnable(GL11.GL_SCISSOR_TEST);
		}

		Scissor scissor = SCISSOR.isEmpty() ? new Scissor(x, y, w, h) : SCISSOR.lastElement().crop(x, y, w, h);
		SCISSOR.push(scissor);
		scissor.scissor(screen);
	}

	public static void popScissor(MainWindow screen)
	{
		SCISSOR.pop();

		if (SCISSOR.isEmpty())
		{
			GL11.glDisable(GL11.GL_SCISSOR_TEST);
		}
		else
		{
			SCISSOR.lastElement().scissor(screen);
		}
	}

	public static void setFixUnicode(boolean enabled)
	{
		/*
		TextureManager textureManager = Minecraft.getInstance().getTextureManager();
		int mode = enabled ? GL11.GL_LINEAR : GL11.GL_NEAREST;

		for (int i = 0; i < 256; i++)
		{
			ResourceLocation loc = ClientATHelper.getFontUnicodePage(i);

			if (loc != null)
			{
				textureManager.bindTexture(loc);
				GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, mode);
				GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, mode);
			}
		}
		*/
	}

	public static String clickEventToString(@Nullable ClickEvent event)
	{
		if (event == null)
		{
			return "";
		}

		switch (event.getAction())
		{
			case OPEN_URL:
			case CHANGE_PAGE:
				return event.getValue();
			case OPEN_FILE:
				return "file:" + event.getValue();
			case RUN_COMMAND:
				return "command:" + event.getValue();
			case SUGGEST_COMMAND:
				return "suggest_command:" + event.getValue();
			default:
				return "";
		}
	}

	public static void addStackTooltip(ItemStack stack, List<ITextComponent> list)
	{
		addStackTooltip(stack, list, null);
	}

	public static void addStackTooltip(ItemStack stack, List<ITextComponent> list, @Nullable ITextComponent prefix)
	{
		List<ITextComponent> tooltip = stack.getTooltip(Minecraft.getInstance().player, Minecraft.getInstance().gameSettings.advancedItemTooltips ? ITooltipFlag.TooltipFlags.ADVANCED : ITooltipFlag.TooltipFlags.NORMAL);
		list.add(prefix == null ? tooltip.get(0).deepCopy().applyTextStyle(stack.getRarity().color) : prefix.deepCopy().appendSibling(tooltip.get(0)));

		for (int i = 1; i < tooltip.size(); i++)
		{
			list.add(new StringTextComponent("").applyTextStyle(TextFormatting.GRAY).appendSibling(tooltip.get(i)));
		}
	}
}