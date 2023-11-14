package dev.ftb.mods.ftblibrary.ui;


import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import dev.ftb.mods.ftblibrary.util.client.PositionedIngredient;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;

import java.util.Optional;


public abstract class SimpleTextButton extends Button {
	public SimpleTextButton(Panel panel, Component txt, Icon icon) {
		super(panel, txt, icon);
		setWidth(panel.getGui().getTheme().getStringWidth(txt) + (hasIcon() ? 28 : 8));
		setHeight(20);
	}

	@Override
	public SimpleTextButton setTitle(Component txt) {
		super.setTitle(txt);
		setWidth(getGui().getTheme().getStringWidth(getTitle()) + (hasIcon() ? 28 : 8));
		return this;
	}

	public boolean renderTitleInCenter() {
		return false;
	}

	@Override
	public Optional<PositionedIngredient> getIngredientUnderMouse() {
		return PositionedIngredient.of(icon.getIngredient(), this);
	}

	public boolean hasIcon() {
		return !icon.isEmpty();
	}

	@Override
	public void addMouseOverText(TooltipList list) {
		if (getGui().getTheme().getStringWidth(getTitle()) + (hasIcon() ? 28 : 8) > width) {
			list.add(getTitle());
		}
	}

	@Override
	public void draw(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
		drawBackground(graphics, theme, x, y, w, h);
		var s = h >= 16 ? 16 : 8;
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
			drawIcon(graphics, theme, x + off, y + off, s, s);
			textX += off + s;
		}

		theme.drawString(graphics, title, textX, textY, theme.getContentColor(getWidgetType()), Theme.SHADOW);
	}
}
