package dev.ftb.mods.ftblibrary.sidebar;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Axis;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.ui.Button;
import dev.ftb.mods.ftblibrary.ui.GuiHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.joml.Quaternionf;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class SidebarGroupGuiButton extends AbstractButton {
	public static Rect2i lastDrawnArea = new Rect2i(0, 0, 0, 0);
	private static final int BUTTON_SPACING = 17;

	private final List<SidebarGuiButton> buttons = new ArrayList<>();

	private SidebarGuiButton mouseOver;
	private SidebarGuiButton selectedButton;
	private GridLocation selectedLocation;
	private boolean isMouseDown;
	private int mouseDownTime;
	private boolean isEditMode;
	private int currentMouseX;
	private int currentMouseY;

	private int mouseOffsetX;
	private int mouseOffsetY;


	public SidebarGroupGuiButton() {
		super(0, 0, 0, 0, Component.empty());
		int rx, ry = 0;
		boolean addedAny;

		for (Map.Entry<SidebarButtonGroup, List<SidebarButton>> buttonEntry : SidebarButtonManager.INSTANCE.getButtonGroups().entrySet()) {
			rx = 0;
			addedAny = false;

			SidebarButtonGroup group = buttonEntry.getKey();
			for (SidebarButton sidebarButton : buttonEntry.getValue()) {
				if(sidebarButton.isVisible()) {
					SidebarGuiButton e = new SidebarGuiButton(rx, ry, sidebarButton);
					buttons.add(e);
					rx++;
					addedAny = true;
				}
			}

			if (addedAny) {
				ry++;
			}
		}

		isMouseDown = false;
	}

	@Override
	public void renderWidget(GuiGraphics graphics, int mx, int my, float partialTicks) {
		currentMouseX = mx;
		currentMouseY = my;

		mouseOver = null;

        setX(Integer.MAX_VALUE);
		setY(Integer.MAX_VALUE);
		var maxX = Integer.MIN_VALUE;
		var maxY = Integer.MIN_VALUE;

		for (var b : buttons) {
			if (b.x >= 0 && b.y >= 0) {
				setX(Math.min(getX(), b.x));
				setY(Math.min(getY(), b.y));
				maxX = Math.max(maxX, b.x + 16);
				maxY = Math.max(maxY, b.y + 16);
			}

			if (mx >= b.x && my >= b.y && mx < b.x + 16 && my < b.y + 16) {
				mouseOver = b;
			}
		}

		// Important: JEI doesn't like negative X/Y values and will silently clamp them,
		// leading it to think the values have changed every frame, and do unnecessary updating
		// of its GUI areas, including resetting the filter textfield's selection
		// https://github.com/FTBTeam/FTB-Mods-Issues/issues/262
		// https://github.com/mezz/JustEnoughItems/issues/2938
		setX(Math.max(0, getX() - 2));
		setY(Math.max(0, getY() - 2));
		maxX += 2;
		maxY += 2;

		width = maxX - getX();
		height = maxY - getY();
		//zLevel = 0F;

		if (isEditMode) {
			//draw box with one extra spot of 16x16 on each side with current
			int maxWidth = buttons
					.stream().max(Comparator.comparingInt(SidebarGuiButton::getGridX))
					.map(SidebarGuiButton::getGridX)
					.orElse(1) + 2;
			int maxHeight = buttons
					.stream()
					.max(Comparator.comparingInt(SidebarGuiButton::getGridY))
					.map(SidebarGuiButton::getGridY)
					.orElse(1) + 2;

			//Don't show extra row on bottom if everything is vertically aligned
			if(maxHeight == buttons.size() + 1) {
				maxHeight--;
			}
			//Don't show extra column on right if everything is horizontally aligned
			if(maxWidth == buttons.size() + 1) {
				maxWidth--;
			}

			Color4I.GRAY.draw(graphics, 0, 0, maxWidth * BUTTON_SPACING, maxHeight * BUTTON_SPACING);
			//draw black grid lines
			for (var i = 0; i < maxWidth + 1; i++) {
				Color4I.BLACK.draw(graphics, i * BUTTON_SPACING, 0, 1, maxHeight * BUTTON_SPACING);
			}
			for (var i = 0; i < maxHeight + 1; i++) {
				Color4I.BLACK.draw(graphics, 0, i * BUTTON_SPACING, maxWidth * BUTTON_SPACING, 1);
			}

			if(selectedButton != null) {
				GridLocation gridLocation = getGridLocation(mx, my);
				gridLocation = new GridLocation(Math.min(maxWidth - 1, Math.max(0, gridLocation.x)), Math.min(maxHeight - 1, Math.max(0, gridLocation.y)));
				Color4I.WHITE.draw(graphics, gridLocation.x * BUTTON_SPACING + 1, gridLocation.y * BUTTON_SPACING + 1, 16, 16);
			}
		}

		graphics.pose().pushPose();
		graphics.pose().translate(0, 0, 500);

		var font = Minecraft.getInstance().font;

		//Todo better way?
		List<SidebarGuiButton> sortedButtons = new ArrayList<>(buttons);
		if(selectedButton != null) {
			sortedButtons.remove(selectedButton);
			sortedButtons.addLast(selectedButton);
		}

		for (SidebarGuiButton button : sortedButtons) {
			if (isEditMode && button == selectedButton) {
				button.x = mx - mouseOffsetX;
				button.y = my - mouseOffsetY;
			}else {
				button.x = 1 + button.getGridX() * BUTTON_SPACING;
				button.y = 1 + button.getGridY() * BUTTON_SPACING;
			}
			GuiHelper.setupDrawing();
			button.button.icon().draw(graphics, button.x, button.y, 16, 16);

			if (button == mouseOver) {
				Color4I.WHITE.withAlpha(33).draw(graphics, button.x, button.y, 16, 16);
			}

			if (button.button.getCustomTextHandler() != null) {
				var text = button.button.getCustomTextHandler().get();

				if (!text.isEmpty()) {
					var nw = font.width(text);
					var width = 16;
					Color4I.LIGHT_RED.draw(graphics, button.x + width - nw, button.y - 1, nw + 1, 9);
					graphics.drawString(font, text, button.x + width - nw + 1, button.y, 0xFFFFFFFF);
					RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
				}
			}
		}

		if (!isEditMode && mouseOver != null) {
			GuiHelper.setupDrawing();
			var mx1 = mx + 10;
			var my1 = Math.max(3, my - 9);

			List<String> list = new ArrayList<>();
			list.add(I18n.get(mouseOver.button.getLangKey()));

			if (mouseOver.button.getTooltipHandler() != null) {
				mouseOver.button.getTooltipHandler().accept(list);
			}

			var tw = 0;

			for (var s : list) {
				tw = Math.max(tw, font.width(s));
			}

			graphics.pose().translate(0, 0, 500);

			Color4I.DARK_GRAY.draw(graphics, mx1 - 3, my1 - 2, tw + 6, 2 + list.size() * 10);

			for (var i = 0; i < list.size(); i++) {
				graphics.drawString(font, list.get(i), mx1, my1 + i * 10, 0xFFFFFFFF);
			}
		}

		GuiHelper.setupDrawing();
		//zLevel = 0F;

		lastDrawnArea = new Rect2i(getX(), getY(), width, height);
		graphics.pose().popPose();

	}

	@Override
	public void onRelease(double d, double e) {
		super.onRelease(d, e);
		isMouseDown = false;
		if (!isEditMode && mouseOver != null) {
			mouseOver.button.onClicked(Screen.hasShiftDown());
		} else {
			if (selectedButton != null) {
				GridLocation gLocation = getGridLocation(currentMouseX, currentMouseY);
				//Checks if the icon is placed in the same location picked up from, if so do nothing
                if (!gLocation.equals(selectedLocation)) {
					//Checks for any icon at the place location and to left of that and moves them over one
					for (SidebarGuiButton button : buttons) {
						if(!selectedButton.equals(button)) {
							if (button.getGridY() == gLocation.y && button.getGridX() >= gLocation.x) {
								button.setGrid(button.getGridX() + 1, button.getGridY());
							}
						}
					}
					selectedButton.setGrid(gLocation.x, gLocation.y);
				}
				selectedButton = null;

				//Makes sure everything on the grid and far left and top that it can be
				Map<Integer, List<SidebarGuiButton>> gridMap = new HashMap<>();
				for (SidebarGuiButton button : buttons) {
					if(!gridMap.containsKey(button.getGridY())) {
						gridMap.put(button.getGridY(), new ArrayList<>());
					}
					gridMap.get(button.getGridY()).add(button);
				}
				int y = 0;
				for (Map.Entry<Integer, List<SidebarGuiButton>> entry : gridMap.entrySet()) {
					List<SidebarGuiButton> sorted = entry.getValue().stream().sorted(Comparator.comparingInt(SidebarGuiButton::getGridX)).toList();
					for (int i = 0; i < sorted.size(); i++) {
						sorted.get(i).setGrid(i, y);
					}
					y++;
				}
			}
		}
	}

	private GridLocation getGridLocation(int x, int y) {
		int gridX = (currentMouseX - 1) / BUTTON_SPACING;
		int gridY = (currentMouseY - 1) / BUTTON_SPACING;
		return new GridLocation(gridX, gridY);
	}

	@Override
	public void onPress() {
		if(mouseOver != null) {
			isMouseDown = true;
			mouseOffsetX = currentMouseX - mouseOver.x;
			mouseOffsetY = currentMouseY - mouseOver.y;

			if(isEditMode) {
				selectedButton = mouseOver;
				selectedLocation = new GridLocation(selectedButton.getGridX(), selectedButton.getGridY());
			}
		}
	}

	@Override
	public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
		defaultButtonNarrationText(narrationElementOutput);
	}

	public record GridLocation(int x, int y) {}


	public void tick() {
		if(isMouseDown) {
			mouseDownTime++;
			if(mouseDownTime > 20) {
				isEditMode = true;
				mouseOver = null;
			}
		} else {
			//Todo
//			if(mouseDownTime <= 0) {
//				isEditMode = false;
//			}else {
//				mouseDownTime--;
//			}
		}
	}
}
