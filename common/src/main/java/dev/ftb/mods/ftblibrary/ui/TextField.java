package dev.ftb.mods.ftblibrary.ui;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.math.Bits;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.util.Mth;

/**
 * @author LatvianModder
 */
public class TextField extends Widget {
	public Component component = TextComponent.EMPTY;
	private FormattedText[] formattedText = new FormattedText[0];
	public int textFlags = 0;
	public int minWidth = 0;
	public int maxWidth = 5000;
	public int textSpacing = 10;
	public float scale = 1.0F;
	public Color4I textColor = Icon.EMPTY;
	public boolean trim = false;

	public TextField(Panel panel) {
		super(panel);
	}

	public TextField addFlags(int flags) {
		textFlags |= flags;
		return this;
	}

	public TextField setMinWidth(int width) {
		minWidth = width;
		return this;
	}

	public TextField setMaxWidth(int width) {
		maxWidth = width;
		return this;
	}

	public TextField setColor(Color4I color) {
		textColor = color;
		return this;
	}

	public TextField setScale(float s) {
		scale = s;
		return this;
	}

	public TextField setSpacing(int s) {
		textSpacing = s;
		return this;
	}

	public TextField setTrim() {
		trim = true;
		return this;
	}

	public TextField setText(Component txt) {
		Theme theme = getGui().getTheme();

		if (trim) {
			formattedText = new FormattedText[]{theme.trimStringToWidth(new TextComponent("").append(txt), maxWidth)};
		} else {
			formattedText = theme.listFormattedStringToWidth(new TextComponent("").append(txt), maxWidth).toArray(new FormattedText[0]);
		}

		return resize(theme);
	}

	public TextField setText(String txt) {
		return setText(new TextComponent(txt));
	}

	public TextField resize(Theme theme) {
		setWidth(0);

		for (FormattedText s : formattedText) {
			setWidth(Math.max(width, (int) ((float) theme.getStringWidth(s) * scale)));
		}

		setWidth(Mth.clamp(width, minWidth, maxWidth));
		setHeight((int) ((float) (Math.max(1, formattedText.length) * textSpacing - (textSpacing - theme.getFontHeight() + 1)) * scale));
		return this;
	}

	@Override
	public void addMouseOverText(TooltipList list) {
	}

	public void drawBackground(PoseStack matrixStack, Theme theme, int x, int y, int w, int h) {
	}

	@Override
	public void draw(PoseStack matrixStack, Theme theme, int x, int y, int w, int h) {
		drawBackground(matrixStack, theme, x, y, w, h);

		if (formattedText.length != 0) {
			boolean centered = Bits.getFlag(textFlags, 4);
			boolean centeredV = Bits.getFlag(textFlags, 32);
			Color4I col = textColor;
			if (col.isEmpty()) {
				col = theme.getContentColor(WidgetType.mouseOver(Bits.getFlag(textFlags, 16)));
			}

			int tx = x + (centered ? w / 2 : 0);
			int ty = y + (centeredV ? (h - theme.getFontHeight()) / 2 : 0);
			int i;
			if (scale == 1.0F) {
				for (i = 0; i < formattedText.length; ++i) {
					theme.drawString(matrixStack, formattedText[i], (float) tx, (float) (ty + i * textSpacing), col, textFlags);
				}
			} else {
				matrixStack.pushPose();
				matrixStack.translate(tx, ty, 0.0D);
				matrixStack.scale(scale, scale, 1.0F);

				for (i = 0; i < formattedText.length; ++i) {
					theme.drawString(matrixStack, formattedText[i], 0.0F, (float) (i * textSpacing), col, textFlags);
				}

				matrixStack.popPose();
			}
		}
	}
}
