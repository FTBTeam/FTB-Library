package dev.ftb.mods.ftblibrary.sidebar;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.mods.ftblibrary.ui.GuiHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
		graphics.pose().translate(0,0,5000);
		currentMouseX = mx;
		currentMouseY = my;

		mouseOver = null;

		GridLocation gridLocation = getGridLocation();


		List<SidebarGuiButton> enabledButtonList = SidebarButtonManager.INSTANCE.getEnabledButtonList();
		for (SidebarGuiButton button : enabledButtonList) {
			if(button.getGridX() == gridLocation.x() && button.getGridY() == gridLocation.y()) {
				mouseOver = button;
			}
		}

		maxGridWith = enabledButtonList
				.stream()
				.max(Comparator.comparingInt(SidebarGuiButton::getGridX))
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
				drawGrid(graphics, ( maxGridWith + 1) * BUTTON_SPACING, 0, 1, 1, BUTTON_SPACING, BUTTON_SPACING, Color4I.GRAY, Color4I.BLACK);
				Icons.ADD.draw(graphics, (maxGridWith + 1) * BUTTON_SPACING + 1, 1, 16, 16);

				if(addBoxOpen) {


					int maxWidth = 0;
					for (SidebarGuiButton button : disabledButtonList) {
						String s = I18n.get(button.getSidebarButton().getLangKey());
						int width = Minecraft.getInstance().font.width(s);
						maxWidth = Math.max(maxWidth, width);
					}


//					drawHoveredGrid(graphics, ( maxGridWith + 2) * BUTTON_SPACING, 0, disabledButtonList.size(),  1, BUTTON_SPACING, Color4I.GRAY, Color4I.BLACK, mx, my);
//					Color4I.GRAY.draw(graphics, ( maxGridWith + 1) * BUTTON_SPACING, BUTTON_SPACING,  1 + BUTTON_SPACING * 5, BUTTON_SPACING * disabledButtonList.size());
					//draw black border
					drawGrid(graphics, ( maxGridWith + 1) * BUTTON_SPACING, BUTTON_SPACING, 1, disabledButtonList.size(), maxWidth + BUTTON_SPACING + 4, BUTTON_SPACING, Color4I.GRAY, Color4I.BLACK);



					for (int i = 0; i < disabledButtonList.size(); i++) {
						SidebarGuiButton button = disabledButtonList.get(i);
						if(selectedButton != null && selectedButton == button) {
							continue;
						}
						button.x = ( maxGridWith + 1) * BUTTON_SPACING;
						button.y = BUTTON_SPACING + BUTTON_SPACING * i;
						GuiHelper.setupDrawing();
						button.getSidebarButton().getData().icon().draw(graphics, button.x + 1, button.y + 1, 16, 16);
						graphics.drawString(Minecraft.getInstance().font, I18n.get(button.getSidebarButton().getLangKey()), button.x + 20, button.y + 4, 0xFFFFFFFF);

						if(mx >= button.x && mx < button.x + 16 && my >= button.y && my < button.y + 16) {
							mouseOver = button;
						}
					}

				}
			}

		}


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
			button.getSidebarButton().getData().icon().draw(graphics, button.x, button.y, 16, 16);

			if(isEditMode && button != selectedButton && SidebarButtonManager.INSTANCE.getEnabledButtonList().size() > 1) {
				//if mouse if over cancle
				if (mx >= button.x + 12 && my <= button.y + 4 && mx < button.x + 16 && my >= button.y) {
					Icons.CANCEL.draw(graphics, button.x + 12, button.y, 6, 6);
				}else {
					Icons.CANCEL.draw(graphics, button.x + 12, button.y, 4, 4);
				}
			}

			if (button == mouseOver) {
				Color4I.WHITE.withAlpha(33).draw(graphics, button.x, button.y, 16, 16);
			}

			if (button.getSidebarButton().getCustomTextHandler() != null) {
				var text = button.getSidebarButton().getCustomTextHandler().get();

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
			list.add(I18n.get(mouseOver.getSidebarButton().getLangKey()));

			if (mouseOver.getSidebarButton().getTooltipHandler() != null) {
				mouseOver.getSidebarButton().getTooltipHandler().accept(list);
			}

			var tw = 0;

			for (var s : list) {
				tw = Math.max(tw, font.width(s));
			}

			Color4I.DARK_GRAY.draw(graphics, mx1 - 3, my1 - 2, tw + 6, 2 + list.size() * 10);

			for (var i = 0; i < list.size(); i++) {
				graphics.drawString(font, list.get(i), mx1, my1 + i * 10, 0xFFFFFFFF);
			}
		}

//		GuiHelper.setupDrawing();
		//zLevel = 0F;


		graphics.pose().popPose();

	}

	@Override
	public void onRelease(double d, double e) {
		super.onRelease(d, e);
		isMouseDown = false;
		if (!isEditMode && mouseOver != null) {
			mouseOver.getSidebarButton().clickButton(Screen.hasShiftDown());
		} else {
			if (selectedButton != null) {
				GridLocation gLocation = getGridLocation();
				//Checks if the icon is placed in the same location picked up from, if so do nothing
                if (!gLocation.isOutOfBounds())
                    if (!gLocation.equals(selectedLocation)) {
                        //Checks for any icon at the place location and to left of that and moves them over one
                        List<SidebarGuiButton> buttonList = SidebarButtonManager.INSTANCE.getButtonList();
                        for (SidebarGuiButton button : buttonList) {
                            if (!selectedButton.equals(button)) {
                                if (button.getGridY() == gLocation.y() && button.getGridX() >= gLocation.x()) {
                                    button.setGrid(button.getGridX() + 1, button.getGridY());
                                }
                            }
                        }
                        selectedButton.setGrid(gLocation.x(), gLocation.y());
                        if (!selectedButton.isEnabled()) {
                            selectedButton.setEnabled(true);
                            SidebarButtonManager.INSTANCE.saveConfigFromButtonList();
							if (SidebarButtonManager.INSTANCE.getDisabledButtonList().isEmpty()) {
								addBoxOpen = false;
							}
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
		updateWidgetSize();

	}

	private void updateWidgetSize() {
		setX(0);
		setY(0);
		// Important: JEI doesn't like negative X/Y values and will silently clamp them,
		// leading it to think the values have changed every frame, and do unnecessary updating
		// of its GUI areas, including resetting the filter textfield's selection
		// https://github.com/FTBTeam/FTB-Mods-Issues/issues/262
		// https://github.com/mezz/JustEnoughItems/issues/2938
		var maxGirdX = 1;
		var maxGirdY = 1;
		for (SidebarGuiButton b : SidebarButtonManager.INSTANCE.getEnabledButtonList()) {
			maxGirdX = Math.max(maxGirdX, b.getGridX() + 1);
			maxGirdY = Math.max(maxGirdY, b.getGridY() + 1);
		}

		int disabledList = SidebarButtonManager.INSTANCE.getDisabledButtonList().size();
		if(isEditMode && addBoxOpen) {
			maxGirdX += 4;
			int size = disabledList;
			maxGirdY = Math.max(maxGirdY, size);
		}
		if(isEditMode) {
			maxGirdX += 3;
			maxGirdY += 1;
		}


		width = (maxGirdX) * BUTTON_SPACING;
		height = (maxGirdY) * BUTTON_SPACING;

		lastDrawnArea = new Rect2i(getX(), getY(), width, height);
	}

	private GridLocation getGridLocation() {
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
			//check if mouse clicked in same spot as the plus button
			//				drawGrid(graphics, ( maxGridWith + 1) * BUTTON_SPACING, 0, 1, 1, BUTTON_SPACING, BUTTON_SPACING, Color4I.GRAY, Color4I.BLACK);
			if(currentMouseX >= ( maxGridWith + 1) * BUTTON_SPACING && currentMouseY <= BUTTON_SPACING) {
				addBoxOpen = !addBoxOpen;
				updateWidgetSize();
				return;
			}


//			GridLocation gridLocation = getGridLocation(currentMouseX, currentMouseY);
//			if(gridLocation.x() == maxGridWith - 1 && gridLocation.y() == maxGridHeight - 1) {
//				addBoxOpen = !addBoxOpen;
//				updateWidgetSize();
//				return;
//			}
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


	@Override
	protected boolean isValidClickButton(int i) {
		//Todo don't just allow edit mode try and be more specific
        return super.isValidClickButton(i) && (isEditMode || !getGridLocation().isOutOfBounds());
	}

	public void tick() {
		if(isMouseDown) {
			mouseDownTime++;
			if(mouseDownTime > 20) {
				isEditMode = true;
				mouseOver = null;
				updateWidgetSize();
			}
		}
	}

	private static void drawGrid(GuiGraphics graphics, int x, int y, int width, int height, int spacingWidth, int spacingHeight, Color4I backgroundColor, Color4I gridColor) {
		backgroundColor.draw(graphics, x, y, width * spacingWidth, height * spacingHeight);
		for (var i = 0; i < width + 1; i++) {
			gridColor.draw(graphics, x + i * spacingWidth, y, 1, height * spacingHeight);
		}
		for (var i = 0; i < height + 1; i++) {
			gridColor.draw(graphics, x, y + i * spacingHeight, width * spacingWidth, 1);
		}
	}

	public static void drawGrid(GuiGraphics graphics, int x, int y, int width, int height, int spacing, Color4I backgroundColor, Color4I gridColor) {
		drawGrid(graphics, x, y, width, height, spacing, spacing, backgroundColor, gridColor);
//		backgroundColor.draw(graphics, x, y, width * spacing, height * spacing);
//		for (var i = 0; i < width + 1; i++) {
//			gridColor.draw(graphics, x + i * spacing, y, 1, height * spacing);
//		}
//		for (var i = 0; i < height + 1; i++) {
//			gridColor.draw(graphics, x, y + i * spacing, width * spacing, 1);
//		}
	}

	public static void drawHoveredGrid(GuiGraphics graphics, int x, int y, int width, int height, int spacing, Color4I backgroundColor, Color4I gridColor, int mx, int my) {
		drawGrid(graphics, x, y, width, height, spacing, backgroundColor, gridColor);
		if (mx >= x && my >= y && mx < x + width * spacing && my < y + height * spacing) {
			Color4I.WHITE.draw(graphics, (mx / spacing) * spacing + 1, (my / spacing) * spacing + 1, spacing - 1, spacing - 1);
		}
	}
}
