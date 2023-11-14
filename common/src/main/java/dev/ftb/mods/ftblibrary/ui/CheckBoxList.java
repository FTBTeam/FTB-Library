package dev.ftb.mods.ftblibrary.ui;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.List;


public class CheckBoxList extends Button {
	public static class CheckBoxEntry {
		private final String name;
		private final CheckBoxList checkBoxList;
		private int index = 0;

		public CheckBoxEntry(String name, CheckBoxList checkBoxList) {
			this.name = name;
			this.checkBoxList = checkBoxList;
		}

		public void onClicked(MouseButton button, int index) {
			select((this.index + 1) % checkBoxList.getValueCount());
			checkBoxList.playClickSound();
		}

		public void addMouseOverText(List<String> list) {
		}

		public CheckBoxEntry select(int index) {
			if (checkBoxList.radioButtonBehaviour) {
				if (index > 0) {
					for (var entry : checkBoxList.entries) {
						var old1 = entry.index > 0;
						entry.index = 0;

						if (old1) {
							entry.onValueChanged();
						}
					}
				} else {
					return this;
				}
			}

			var old = this.index;
			this.index = index;

			if (old != this.index) {
				onValueChanged();
			}

			return this;
		}

		public int getIndex() {
			return index;
		}

		public void onValueChanged() {
		}
	}

	private final boolean radioButtonBehaviour;
	private final List<CheckBoxEntry> entries;

	public CheckBoxList(BaseScreen gui, boolean radioButtonBehaviour) {
		super(gui);
		setSize(10, 2);
		this.radioButtonBehaviour = radioButtonBehaviour;
		entries = new ArrayList<>();
	}

	public int getValueCount() {
		return 2;
	}

	@Override
	public void drawBackground(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
	}

	public void drawCheckboxBackground(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
		theme.drawCheckboxBackground(graphics, x, y, w, h, radioButtonBehaviour);
	}

	public void getCheckboxIcon(GuiGraphics graphics, Theme theme, int x, int y, int w, int h, int index, int value) {
		theme.drawCheckbox(graphics, x, y, w, h, WidgetType.mouseOver(isMouseOver()), value != 0, radioButtonBehaviour);
	}

	public void addBox(CheckBoxEntry checkBox) {
		entries.add(checkBox);
		setWidth(Math.max(width, getGui().getTheme().getStringWidth(checkBox.name)));
		setHeight(height + 11);
	}

	public CheckBoxEntry addBox(String name) {
		var entry = new CheckBoxEntry(name, this);
		addBox(entry);
		return entry;
	}

	@Override
	public void onClicked(MouseButton button) {
		var y = getMouseY() - getY();

		if (y % 11 == 10) {
			return;
		}

		var i = y / 11;

		if (i >= 0 && i < entries.size()) {
			entries.get(i).onClicked(button, i);
		}
	}

	@Override
	public void addMouseOverText(TooltipList list) {
	}

	@Override
	public void draw(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
		drawBackground(graphics, theme, x, y, w, h);

		for (var i = 0; i < entries.size(); i++) {
			var entry = entries.get(i);
			var ey = y + i * 11 + 1;
			drawCheckboxBackground(graphics, theme, x, ey, 10, 10);
			getCheckboxIcon(graphics, theme, x + 1, ey + 1, 8, 8, i, entry.index);
			theme.drawString(graphics, entry.name, x + 12, ey + 1);
			RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
		}
	}
}
