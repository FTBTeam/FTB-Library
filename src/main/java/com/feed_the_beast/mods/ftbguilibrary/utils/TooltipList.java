package com.feed_the_beast.mods.ftbguilibrary.utils;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LatvianModder
 */
public class TooltipList
{
	private final List<ITextComponent> lines = new ArrayList<>();
	public int zOffset = 950;
	public int backgroundColor = 0xF0100010;
	public int borderColorStart = 0x505000FF;
	public int borderColorEnd = (borderColorStart & 0xFEFEFE) >> 1 | borderColorStart & 0xFF000000;

	public boolean shouldRender()
	{
		return !lines.isEmpty();
	}

	public void reset()
	{
		lines.clear();
		zOffset = 950;
		backgroundColor = 0xF0100010;
		borderColorStart = 0x505000FF;
		borderColorEnd = (borderColorStart & 0xFEFEFE) >> 1 | borderColorStart & 0xFF000000;
	}

	public void add(ITextComponent component)
	{
		lines.add(component);
	}

	public void blankLine()
	{
		add(StringTextComponent.EMPTY);
	}

	public void styledString(String text, Style style)
	{
		add(new StringTextComponent(text).mergeStyle(style));
	}

	public void styledString(String text, TextFormatting color)
	{
		add(new StringTextComponent(text).mergeStyle(color));
	}

	public void styledTranslate(String key, Style style, Object... objects)
	{
		add(new TranslationTextComponent(key, objects).mergeStyle(style));
	}

	public void string(String text)
	{
		styledString(text, Style.EMPTY);
	}

	public void translate(String key, Object... objects)
	{
		styledTranslate(key, Style.EMPTY, objects);
	}

	public void render(MatrixStack mStack, int mouseX, int mouseY, int screenWidth, int screenHeight, int maxTextWidth, FontRenderer font)
	{
		List<IReorderingProcessor> textLines = new ArrayList<>(lines.size());

		for (ITextComponent component : lines)
		{
			textLines.add(component.func_241878_f());
		}

		RenderSystem.disableRescaleNormal();
		RenderSystem.disableDepthTest();
		int tooltipTextWidth = 0;

		for (IReorderingProcessor textLine : textLines)
		{
			int textLineWidth = font.func_243245_a(textLine);
			if (textLineWidth > tooltipTextWidth)
			{
				tooltipTextWidth = textLineWidth;
			}
		}

		boolean needsWrap = false;

		int titleLinesCount = 1;
		int tooltipX = mouseX + 12;
		if (tooltipX + tooltipTextWidth + 4 > screenWidth)
		{
			tooltipX = mouseX - 16 - tooltipTextWidth;
			if (tooltipX < 4) // if the tooltip doesn't fit on the screen
			{
				if (mouseX > screenWidth / 2)
				{
					tooltipTextWidth = mouseX - 12 - 8;
				}
				else
				{
					tooltipTextWidth = screenWidth - 16 - mouseX;
				}
				needsWrap = true;
			}
		}

		if (maxTextWidth > 0 && tooltipTextWidth > maxTextWidth)
		{
			tooltipTextWidth = maxTextWidth;
			needsWrap = true;
		}

		if (needsWrap)
		{
			int wrappedTooltipWidth = 0;
			List<IReorderingProcessor> wrappedTextLines = new ArrayList<>();
			for (int i = 0; i < lines.size(); i++)
			{
				ITextComponent textLine = lines.get(i);
				List<IReorderingProcessor> wrappedLine = font.trimStringToWidth(textLine, tooltipTextWidth);
				if (i == 0)
				{
					titleLinesCount = wrappedLine.size();
				}

				for (IReorderingProcessor line : wrappedLine)
				{
					int lineWidth = font.func_243245_a(line);
					if (lineWidth > wrappedTooltipWidth)
					{
						wrappedTooltipWidth = lineWidth;
					}
					wrappedTextLines.add(line);
				}
			}
			tooltipTextWidth = wrappedTooltipWidth;
			textLines = wrappedTextLines;

			if (mouseX > screenWidth / 2)
			{
				tooltipX = mouseX - 16 - tooltipTextWidth;
			}
			else
			{
				tooltipX = mouseX + 12;
			}
		}

		int tooltipY = mouseY - 12;
		int tooltipHeight = 8;

		if (textLines.size() > 1)
		{
			tooltipHeight += (textLines.size() - 1) * 10;
			if (textLines.size() > titleLinesCount)
			{
				tooltipHeight += 2; // gap between title lines and next lines
			}
		}

		if (tooltipY < 4)
		{
			tooltipY = 4;
		}
		else if (tooltipY + tooltipHeight + 4 > screenHeight)
		{
			tooltipY = screenHeight - tooltipHeight - 4;
		}

		mStack.push();
		mStack.translate(0, 0, zOffset);
		Matrix4f mat = mStack.getLast().getMatrix();
		RenderSystem.enableDepthTest();
		RenderSystem.disableTexture();
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.shadeModel(GL11.GL_SMOOTH);

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();
		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
		drawGradientRect(mat, buffer, tooltipX - 3, tooltipY - 4, tooltipX + tooltipTextWidth + 3, tooltipY - 3, backgroundColor, backgroundColor);
		drawGradientRect(mat, buffer, tooltipX - 3, tooltipY + tooltipHeight + 3, tooltipX + tooltipTextWidth + 3, tooltipY + tooltipHeight + 4, backgroundColor, backgroundColor);
		drawGradientRect(mat, buffer, tooltipX - 3, tooltipY - 3, tooltipX + tooltipTextWidth + 3, tooltipY + tooltipHeight + 3, backgroundColor, backgroundColor);
		drawGradientRect(mat, buffer, tooltipX - 4, tooltipY - 3, tooltipX - 3, tooltipY + tooltipHeight + 3, backgroundColor, backgroundColor);
		drawGradientRect(mat, buffer, tooltipX + tooltipTextWidth + 3, tooltipY - 3, tooltipX + tooltipTextWidth + 4, tooltipY + tooltipHeight + 3, backgroundColor, backgroundColor);
		drawGradientRect(mat, buffer, tooltipX - 3, tooltipY - 3 + 1, tooltipX - 3 + 1, tooltipY + tooltipHeight + 3 - 1, borderColorStart, borderColorEnd);
		drawGradientRect(mat, buffer, tooltipX + tooltipTextWidth + 2, tooltipY - 3 + 1, tooltipX + tooltipTextWidth + 3, tooltipY + tooltipHeight + 3 - 1, borderColorStart, borderColorEnd);
		drawGradientRect(mat, buffer, tooltipX - 3, tooltipY - 3, tooltipX + tooltipTextWidth + 3, tooltipY - 3 + 1, borderColorStart, borderColorStart);
		drawGradientRect(mat, buffer, tooltipX - 3, tooltipY + tooltipHeight + 2, tooltipX + tooltipTextWidth + 3, tooltipY + tooltipHeight + 3, borderColorEnd, borderColorEnd);
		tessellator.draw();

		RenderSystem.shadeModel(GL11.GL_FLAT);
		RenderSystem.disableBlend();
		RenderSystem.enableTexture();
		IRenderTypeBuffer.Impl renderType = IRenderTypeBuffer.getImpl(buffer);

		for (int lineNumber = 0; lineNumber < textLines.size(); ++lineNumber)
		{
			IReorderingProcessor line = textLines.get(lineNumber);
			if (line != null)
			{
				font.func_238416_a_(line, (float) tooltipX, (float) tooltipY, -1, true, mat, renderType, false, 0, 15728880);
			}

			if (lineNumber + 1 == titleLinesCount)
			{
				tooltipY += 2;
			}

			tooltipY += 10;
		}

		renderType.finish();
		mStack.pop();

		RenderSystem.enableDepthTest();
		RenderSystem.enableRescaleNormal();
	}

	private static void drawGradientRect(Matrix4f mat, BufferBuilder buffer, int left, int top, int right, int bottom, int startColor, int endColor)
	{
		int startAlpha = (startColor >> 24) & 255;
		int startRed = (startColor >> 16) & 255;
		int startGreen = (startColor >> 8) & 255;
		int startBlue = startColor & 255;
		int endAlpha = (endColor >> 24) & 255;
		int endRed = (endColor >> 16) & 255;
		int endGreen = (endColor >> 8) & 255;
		int endBlue = endColor & 255;

		buffer.pos(mat, right, top, 0).color(startRed, startGreen, startBlue, startAlpha).endVertex();
		buffer.pos(mat, left, top, 0).color(startRed, startGreen, startBlue, startAlpha).endVertex();
		buffer.pos(mat, left, bottom, 0).color(endRed, endGreen, endBlue, endAlpha).endVertex();
		buffer.pos(mat, right, bottom, 0).color(endRed, endGreen, endBlue, endAlpha).endVertex();
	}
}