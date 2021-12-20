package dev.ftb.mods.ftblibrary.ui;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.ui.misc.NordColors;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;

public abstract class NordButton extends SimpleTextButton {
	public NordButton(Panel panel, Component txt, Icon icon) {
		super(panel, txt, icon);
		setHeight(16);
	}

	@Override
	public void addMouseOverText(TooltipList list) {
	}

	@Override
	public void drawBackground(PoseStack matrixStack, Theme theme, int x, int y, int w, int h) {
		(isMouseOver() ? NordColors.POLAR_NIGHT_4 : NordColors.POLAR_NIGHT_2).draw(matrixStack, x, y, w, h);
	}

	@Override
	public void draw(PoseStack matrixStack, Theme theme, int x, int y, int w, int h) {
		this.drawBackground(matrixStack, theme, x, y, w, h);
		var s = h >= 20 ? 16 : 8;
		var off = (h - s) / 2;
		FormattedText title = getTitle();
		var textX = x;
		var textY = y + (h - theme.getFontHeight() + 1) / 2;
		var sw = theme.getStringWidth(title);
		var mw = w - (hasIcon() ? off + s : 0) - 6;

		if (sw > mw) {
			sw = mw;
			title = theme.trimStringToWidth(title, mw);
		}

		if (renderTitleInCenter()) {
			textX += (mw - sw + 6) / 2;
		} else {
			textX += 4;
		}

		if (hasIcon()) {
			drawIcon(matrixStack, theme, x + off, y + off, s, s);
			textX += off + s;
		}

		theme.drawString(matrixStack, title, (float) textX, (float) textY, isMouseOver() ? NordColors.SNOW_STORM_3 : NordColors.SNOW_STORM_1, 0);
	}
}
