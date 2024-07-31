package dev.ftb.mods.ftblibrary.sidebar;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Axis;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.Icons;
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

	private int maxGridWith = 1;
	private int maxGridHeight = 1;

	private boolean addBoxOpen;

	public SidebarGroupGuiButton() {
		super(0, 0, 0, 0, Component.empty());

		ensureGridAlignment();
		isMouseDown = false;
	}

	@Override
	public void renderWidget(GuiGraphics graphics, int mx, int my, float partialTicks) {
		currentMouseX = mx;
		currentMouseY = my;

		mouseOver = null;

		GridLocation gridLocation = getGridLocation(mx, my);


		List<SidebarGuiButton> enabledButtonList = SidebarButtonManager.INSTANCE.getEnabledButtonList();
		for (SidebarGuiButton button : enabledButtonList) {
			if(button.getGridX() == gridLocation.x && button.getGridY() == gridLocation.y) {
				mouseOver = button;
			}
		}

		maxGridWith = enabledButtonList
				.stream().max(Comparator.comparingInt(SidebarGuiButton::getGridX))
				.map(SidebarGuiButton::getGridX)
				.orElse(1) + 2;
		maxGridHeight = enabledButtonList
				.stream()
				.max(Comparator.comparingInt(SidebarGuiButton::getGridY))
				.map(SidebarGuiButton::getGridY)
				.orElse(1) + 2;


		if (isEditMode) {

			drawHoveredGrid(graphics, 0, 0, maxGridWith, maxGridHeight, BUTTON_SPACING, Color4I.GRAY, Color4I.BLACK, mx, my);

			List<SidebarGuiButton> disabledButtonList = SidebarButtonManager.INSTANCE.getDisabledButtonList();
			if(!disabledButtonList.isEmpty()) {
				Icons.ADD.draw(graphics, (maxGridWith - 1) * BUTTON_SPACING + 1, (maxGridHeight - 1) * BUTTON_SPACING + 1, 16, 16);

				if(addBoxOpen) {
					drawHoveredGrid(graphics, ( maxGridWith - 0) * BUTTON_SPACING, (maxGridHeight - 1) * BUTTON_SPACING, disabledButtonList.size(),  1, BUTTON_SPACING, Color4I.GRAY, Color4I.BLACK, mx, my);
					for (int i = 0; i < disabledButtonList.size(); i++) {
						SidebarGuiButton button = disabledButtonList.get(i);
						if(selectedButton != null && selectedButton == button) {
							continue;
						}
						button.x = (maxGridWith - 0) * BUTTON_SPACING + i * BUTTON_SPACING;
						button.y = (maxGridHeight - 1) * BUTTON_SPACING;
						GuiHelper.setupDrawing();
						button.button.icon().draw(graphics, button.x + 1, button.y + 1, 16, 16);

						if(mx >= button.x && mx < button.x + 16 && my >= button.y && my < button.y + 16) {
							mouseOver = button;
						}
					}

				}
			}

		}



		graphics.pose().pushPose();
		graphics.pose().translate(0, 0, 500);

		var font = Minecraft.getInstance().font;

		//Todo better way?
		List<SidebarGuiButton> sortedButtons = new ArrayList<>(enabledButtonList);
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
			if(isEditMode && button != selectedButton && SidebarButtonManager.INSTANCE.getEnabledButtonList().size() > 1) {
				Icons.CANCEL.draw(graphics, button.x + 12, button.y, 4, 4);
			}

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
                if (!gLocation.isOutOfBounds())
                    if (!gLocation.equals(selectedLocation)) {
                        //Checks for any icon at the place location and to left of that and moves them over one
                        List<SidebarGuiButton> buttonList = SidebarButtonManager.INSTANCE.getButtonList();
                        for (SidebarGuiButton button : buttonList) {
                            if (!selectedButton.equals(button)) {
                                if (button.getGridY() == gLocation.y && button.getGridX() >= gLocation.x) {
                                    button.setGrid(button.getGridX() + 1, button.getGridY());
                                }
                            }
                        }
                        selectedButton.setGrid(gLocation.x, gLocation.y);
                        if (!selectedButton.isEnabled()) {
                            selectedButton.setEnabled(true);
                            SidebarButtonManager.INSTANCE.saveConfigFromButtonList();
                        }
                    }
				selectedButton = null;
				ensureGridAlignment();

			}
		}
	}

	private void ensureGridAlignment() {
		//Makes sure everything on the grid and far left and top that it can be
		Map<Integer, List<SidebarGuiButton>> gridMap = new HashMap<>();
		for (SidebarGuiButton button : SidebarButtonManager.INSTANCE.getEnabledButtonList()) {
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





		SidebarButtonManager.INSTANCE.saveConfigFromButtonList();

		setX(0);
		setY(0);
		// Important: JEI doesn't like negative X/Y values and will silently clamp them,
		// leading it to think the values have changed every frame, and do unnecessary updating
		// of its GUI areas, including resetting the filter textfield's selection
		// https://github.com/FTBTeam/FTB-Mods-Issues/issues/262
		// https://github.com/mezz/JustEnoughItems/issues/2938
		var maxGirdX = 2;
		var maxGirdY = 2;
		for (SidebarGuiButton b : SidebarButtonManager.INSTANCE.getEnabledButtonList()) {
			maxGirdX = Math.max(maxGirdX, b.getGridX() + 1);
			maxGirdY = Math.max(maxGirdY, b.getGridY() + 1);
		}

		if(isEditMode && addBoxOpen) {
            for (SidebarGuiButton button : SidebarButtonManager.INSTANCE.getDisabledButtonList()) {
                maxGirdX++;
            }
        }

		width = (maxGirdX + 1) * BUTTON_SPACING;
		height = (maxGirdY + 1) * BUTTON_SPACING;
	}

	private GridLocation getGridLocation(int x, int y) {
		int gridX = (currentMouseX - 1) / BUTTON_SPACING;
		int gridY = (currentMouseY - 1) / BUTTON_SPACING;
		if(gridX >= maxGridWith || gridY >= maxGridHeight) {
			return new GridLocation(- 1, - 1);
		}
		return new GridLocation(gridX, gridY);
	}

	@Override
	public void onPress() {
		if(isEditMode) {
			//if clicked the bottom right grid spot
			GridLocation gridLocation = getGridLocation(currentMouseX, currentMouseY);
			if(gridLocation.x == maxGridWith - 1 && gridLocation.y == maxGridHeight - 1) {
				addBoxOpen = !addBoxOpen;
				return;
			}
		}

		if(mouseOver != null) {
			isMouseDown = true;
			mouseOffsetX = currentMouseX - mouseOver.x;
			mouseOffsetY = currentMouseY - mouseOver.y;

			if(isEditMode) {




				//Check if clicked the remove button
				if(SidebarButtonManager.INSTANCE.getEnabledButtonList().size() > 1 && currentMouseX >= mouseOver.x + 12 && currentMouseY <= mouseOver.y + 4) {
					mouseOver.setEnabled(false);
					mouseOver = null;
					SidebarButtonManager.INSTANCE.saveConfigFromButtonList();
					ensureGridAlignment();
					return;
				}




				selectedButton = mouseOver;
				if(!selectedButton.isEnabled()) {
					selectedLocation = new GridLocation(selectedButton.getGridX(), selectedButton.getGridY());
				}else {
					selectedLocation = GridLocation.OUT_OF_BOUNDS;
				}
			}
		}
	}

	@Override
	public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
		defaultButtonNarrationText(narrationElementOutput);
	}

	public record GridLocation(int x, int y) {

		public static final GridLocation OUT_OF_BOUNDS = new GridLocation(-1, -1);

		public boolean isOutOfBounds() {
			return x < 0 || y < 0;
		}
	}


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

	public static void drawGrid(GuiGraphics graphics, int x, int y, int width, int height, int spacing, Color4I backgroundColor, Color4I gridColor) {
		backgroundColor.draw(graphics, x, y, width * spacing, height * spacing);
		for (var i = 0; i < width + 1; i++) {
			gridColor.draw(graphics, x + i * spacing, y, 1, height * spacing);
		}
		for (var i = 0; i < height + 1; i++) {
			gridColor.draw(graphics, x, y + i * spacing, width * spacing, 1);
		}
	}

	public static void drawHoveredGrid(GuiGraphics graphics, int x, int y, int width, int height, int spacing, Color4I backgroundColor, Color4I gridColor, int mx, int my) {
		drawGrid(graphics, x, y, width, height, spacing, backgroundColor, gridColor);
		if (mx >= x && my >= y && mx < x + width * spacing && my < y + height * spacing) {
			Color4I.WHITE.draw(graphics, (mx / spacing) * spacing + 1, (my / spacing) * spacing + 1, spacing - 1, spacing - 1);
		}
	}
}
