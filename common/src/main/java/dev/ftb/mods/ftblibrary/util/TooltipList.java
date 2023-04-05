package dev.ftb.mods.ftblibrary.util;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LatvianModder
 */
public class TooltipList {
	private final List<Component> lines = new ArrayList<>();
	public int zOffset = 950;
	public int zOffsetItemTooltip = 0;
	public int backgroundColor = 0xF0100010;
	public int borderColorStart = 0x505000FF;
	public int borderColorEnd = (borderColorStart & 0xFEFEFE) >> 1 | borderColorStart & 0xFF000000;
	public int maxWidth = 0;
	public int xOffset = 0;
	public int yOffset = 0;

	public boolean shouldRender() {
		return !lines.isEmpty();
	}

	public void reset() {
		lines.clear();
		zOffset = 950;
		zOffsetItemTooltip = 0;
		backgroundColor = 0xF0100010;
		borderColorStart = 0x505000FF;
		borderColorEnd = (borderColorStart & 0xFEFEFE) >> 1 | borderColorStart & 0xFF000000;
		maxWidth = 0;
		xOffset = 0;
		yOffset = 0;
	}

	public void add(Component component) {
		lines.add(component);
	}

	public void blankLine() {
		add(Component.empty());
	}

	public void styledString(String text, Style style) {
		add(Component.literal(text).withStyle(style));
	}

	public void styledString(String text, ChatFormatting color) {
		add(Component.literal(text).withStyle(color));
	}

	public void styledTranslate(String key, Style style, Object... objects) {
		add(Component.translatable(key, objects).withStyle(style));
	}

	public void string(String text) {
		styledString(text, Style.EMPTY);
	}

	public void translate(String key, Object... objects) {
		styledTranslate(key, Style.EMPTY, objects);
	}

	@Environment(EnvType.CLIENT)
	public void render(PoseStack mStack, int mouseX, int mouseY, int screenWidth, int screenHeight, Font font) {
		mouseX += xOffset;
		mouseY += yOffset;

		List<FormattedCharSequence> textLines = new ArrayList<>(lines.size());

		for (var component : lines) {
			textLines.add(component.getVisualOrderText());
		}

		RenderSystem.disableDepthTest();
		var tooltipTextWidth = 0;

		for (var textLine : textLines) {
			var textLineWidth = font.width(textLine);
			if (textLineWidth > tooltipTextWidth) {
				tooltipTextWidth = textLineWidth;
			}
		}

		var needsWrap = false;

		var titleLinesCount = 1;
		var tooltipX = mouseX + 12;
		if (tooltipX + tooltipTextWidth + 4 > screenWidth) {
			tooltipX = mouseX - 16 - tooltipTextWidth;
			if (tooltipX < 4) // if the tooltip doesn't fit on the screen
			{
				if (mouseX > screenWidth / 2) {
					tooltipTextWidth = mouseX - 12 - 8;
				} else {
					tooltipTextWidth = screenWidth - 16 - mouseX;
				}
				needsWrap = true;
			}
		}

		if (maxWidth > 0 && tooltipTextWidth > maxWidth) {
			tooltipTextWidth = maxWidth;
			needsWrap = true;
		}

		if (needsWrap) {
			var wrappedTooltipWidth = 0;
			List<FormattedCharSequence> wrappedTextLines = new ArrayList<>();
			for (var i = 0; i < lines.size(); i++) {
				var textLine = lines.get(i);
				var wrappedLine = font.split(textLine, tooltipTextWidth);
				if (i == 0) {
					titleLinesCount = wrappedLine.size();
				}

				for (var line : wrappedLine) {
					var lineWidth = font.width(line);
					if (lineWidth > wrappedTooltipWidth) {
						wrappedTooltipWidth = lineWidth;
					}
					wrappedTextLines.add(line);
				}
			}
			tooltipTextWidth = wrappedTooltipWidth;
			textLines = wrappedTextLines;

			if (mouseX > screenWidth / 2) {
				tooltipX = mouseX - 16 - tooltipTextWidth;
			} else {
				tooltipX = mouseX + 12;
			}
		}

		var tooltipY = mouseY - 12;
		var tooltipHeight = 8;

		if (textLines.size() > 1) {
			tooltipHeight += (textLines.size() - 1) * 10;
			if (textLines.size() > titleLinesCount) {
				tooltipHeight += 2; // gap between title lines and next lines
			}
		}

		if (tooltipY < 4) {
			tooltipY = 4;
		} else if (tooltipY + tooltipHeight + 4 > screenHeight) {
			tooltipY = screenHeight - tooltipHeight - 4;
		}

		mStack.pushPose();
		mStack.translate(0, 0, zOffset);
		var mat = mStack.last().pose();
		RenderSystem.enableDepthTest();
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();

		var tesselator = Tesselator.getInstance();
		var buffer = tesselator.getBuilder();
		RenderSystem.setShader(GameRenderer::getPositionColorShader);
		buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
		drawGradientRect(mat, buffer, tooltipX - 3, tooltipY - 4, tooltipX + tooltipTextWidth + 3, tooltipY - 3, backgroundColor, backgroundColor);
		drawGradientRect(mat, buffer, tooltipX - 3, tooltipY + tooltipHeight + 3, tooltipX + tooltipTextWidth + 3, tooltipY + tooltipHeight + 4, backgroundColor, backgroundColor);
		drawGradientRect(mat, buffer, tooltipX - 3, tooltipY - 3, tooltipX + tooltipTextWidth + 3, tooltipY + tooltipHeight + 3, backgroundColor, backgroundColor);
		drawGradientRect(mat, buffer, tooltipX - 4, tooltipY - 3, tooltipX - 3, tooltipY + tooltipHeight + 3, backgroundColor, backgroundColor);
		drawGradientRect(mat, buffer, tooltipX + tooltipTextWidth + 3, tooltipY - 3, tooltipX + tooltipTextWidth + 4, tooltipY + tooltipHeight + 3, backgroundColor, backgroundColor);
		drawGradientRect(mat, buffer, tooltipX - 3, tooltipY - 3 + 1, tooltipX - 3 + 1, tooltipY + tooltipHeight + 3 - 1, borderColorStart, borderColorEnd);
		drawGradientRect(mat, buffer, tooltipX + tooltipTextWidth + 2, tooltipY - 3 + 1, tooltipX + tooltipTextWidth + 3, tooltipY + tooltipHeight + 3 - 1, borderColorStart, borderColorEnd);
		drawGradientRect(mat, buffer, tooltipX - 3, tooltipY - 3, tooltipX + tooltipTextWidth + 3, tooltipY - 3 + 1, borderColorStart, borderColorStart);
		drawGradientRect(mat, buffer, tooltipX - 3, tooltipY + tooltipHeight + 2, tooltipX + tooltipTextWidth + 3, tooltipY + tooltipHeight + 3, borderColorEnd, borderColorEnd);
		tesselator.end();

		RenderSystem.disableBlend();
		var renderType = MultiBufferSource.immediate(buffer);

		for (var lineNumber = 0; lineNumber < textLines.size(); ++lineNumber) {
			var line = textLines.get(lineNumber);
			if (line != null) {
				font.drawInBatch(line, (float) tooltipX, (float) tooltipY, -1, true, mat, renderType, Font.DisplayMode.NORMAL, 0, 15728880);
			}

			if (lineNumber + 1 == titleLinesCount) {
				tooltipY += 2;
			}

			tooltipY += 10;
		}

		renderType.endBatch();
		mStack.popPose();

		RenderSystem.enableDepthTest();
	}

	@Environment(EnvType.CLIENT)
	private static void drawGradientRect(Matrix4f mat, BufferBuilder buffer, int left, int top, int right, int bottom, int startColor, int endColor) {
		var startAlpha = (startColor >> 24) & 255;
		var startRed = (startColor >> 16) & 255;
		var startGreen = (startColor >> 8) & 255;
		var startBlue = startColor & 255;
		var endAlpha = (endColor >> 24) & 255;
		var endRed = (endColor >> 16) & 255;
		var endGreen = (endColor >> 8) & 255;
		var endBlue = endColor & 255;

		buffer.vertex(mat, right, top, 0).color(startRed, startGreen, startBlue, startAlpha).endVertex();
		buffer.vertex(mat, left, top, 0).color(startRed, startGreen, startBlue, startAlpha).endVertex();
		buffer.vertex(mat, left, bottom, 0).color(endRed, endGreen, endBlue, endAlpha).endVertex();
		buffer.vertex(mat, right, bottom, 0).color(endRed, endGreen, endBlue, endAlpha).endVertex();
	}
}
