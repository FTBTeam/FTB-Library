package dev.ftb.mods.ftblibrary.ui;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import net.minecraft.network.chat.TextComponent;

import java.util.List;

/**
 * @author LatvianModder
 */
public class ContextMenu extends Panel {
	private static final int MARGIN = 3;

	public final List<ContextMenuItem> items;
	public boolean hasIcons;
	private int nColumns;
	private int columnWidth;

	public ContextMenu(Panel panel, List<ContextMenuItem> i) {
		super(panel);
		items = i;

		hasIcons = items.stream().anyMatch(item -> !item.icon.isEmpty());
	}

	@Override
	public void addWidgets() {
		items.forEach(item -> add(item.createWidget(this)));
	}

	@Override
	public boolean mousePressed(MouseButton button) {
		// close context menu if clicked outside it
		var pressed = super.mousePressed(button);

		if (!pressed && !isMouseOver()) {
			closeContextMenu();
			return true;
		}

		return pressed;
	}

	@Override
	public void alignWidgets() {
		setWidth(0);

		int totalHeight = 0;
		int maxWidth = 0;
		for (var widget : widgets) {
			maxWidth = Math.max(maxWidth, widget.width);
			totalHeight += widget.height + 1;
		}
		totalHeight += MARGIN * 2;

		// if there are too many menu items to fit vertically on-screen, use a multi-column layout
		nColumns = parent.getScreen().getGuiScaledHeight() > 0 ? (totalHeight / parent.getScreen().getGuiScaledHeight()) + 1 : 1;
		int nRows = nColumns == 1 ? widgets.size() : (widgets.size() / nColumns) + 1;

		columnWidth = maxWidth + MARGIN * 2;
		setWidth(columnWidth * nColumns);

		int yPos = MARGIN;
		int prevCol = 0;
		int maxHeight = 0;
		for (int i = 0; i < widgets.size(); i++) {
			int col = i / nRows;
			if (prevCol != col) {
				yPos = MARGIN;
				prevCol = col;
			}
			Widget widget = widgets.get(i);
			widget.setPosAndSize(MARGIN + columnWidth * col, yPos, maxWidth, widget.height);
			maxHeight = Math.max(maxHeight, yPos + widget.height + 1);
			yPos += widget.height + 1;
		}

		setHeight(maxHeight + MARGIN - 1);
	}

	@Override
	public void drawBackground(PoseStack matrixStack, Theme theme, int x, int y, int w, int h) {
		theme.drawContextMenuBackground(matrixStack, x, y, w, h);
	}

	@Override
	public void draw(PoseStack matrixStack, Theme theme, int x, int y, int w, int h) {
		GuiHelper.setupDrawing();
		matrixStack.pushPose();
		matrixStack.translate(0, 0, 900);
		super.draw(matrixStack, theme, x, y, w, h);
		for (int i = 1; i < nColumns; i++) {
			// vertical separator line between columns (only in multi-column layouts)
			Color4I.WHITE.withAlpha(130).draw(matrixStack, x + columnWidth * i, y + MARGIN, 1, height - MARGIN * 2);
		}
		matrixStack.popPose();
	}

	public static class CButton extends Button {
		public final ContextMenu contextMenu;
		public final ContextMenuItem item;

		public CButton(ContextMenu panel, ContextMenuItem i) {
			super(panel, i.title, i.icon);
			contextMenu = panel;
			item = i;
			setSize(panel.getGui().getTheme().getStringWidth(item.title) + (contextMenu.hasIcons ? 14 : 4), 12);
		}

		@Override
		public void addMouseOverText(TooltipList list) {
			item.addMouseOverText(list);
		}

		@Override
		public WidgetType getWidgetType() {
			return item.enabled.getAsBoolean() ? super.getWidgetType() : WidgetType.DISABLED;
		}

		@Override
		public void drawIcon(PoseStack matrixStack, Theme theme, int x, int y, int w, int h) {
			item.drawIcon(matrixStack, theme, x, y, w, h);
		}

		@Override
		public void draw(PoseStack matrixStack, Theme theme, int x, int y, int w, int h) {
			GuiHelper.setupDrawing();

			if (contextMenu.hasIcons) {
				drawIcon(matrixStack, theme, x + 1, y + 2, 8, 8);
				theme.drawString(matrixStack, getTitle(), x + 11, y + 2, theme.getContentColor(getWidgetType()), Theme.SHADOW);
			} else {
				theme.drawString(matrixStack, getTitle(), x + 2, y + 2, theme.getContentColor(getWidgetType()), Theme.SHADOW);
			}
		}

		@Override
		public void onClicked(MouseButton button) {
			playClickSound();

			if (item.yesNoText.getString().isEmpty()) {
				item.onClicked(contextMenu, button);
			} else {
				getGui().openYesNo(item.yesNoText, new TextComponent(""), () -> item.onClicked(contextMenu, button));
			}
		}
	}

	public static class CSeperator extends Button {
		public CSeperator(Panel panel) {
			super(panel);
			setHeight(5);
		}

		@Override
		public void draw(PoseStack matrixStack, Theme theme, int x, int y, int w, int h) {
			Color4I.WHITE.withAlpha(130).draw(matrixStack, x + 2, y + 2, parent.width - 10, 1);
		}

		@Override
		public void onClicked(MouseButton button) {
		}
	}
}
