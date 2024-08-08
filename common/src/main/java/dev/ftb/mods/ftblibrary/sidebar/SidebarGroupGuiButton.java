package dev.ftb.mods.ftblibrary.sidebar;

import dev.ftb.mods.ftblibrary.FTBLibraryClient;
import dev.ftb.mods.ftblibrary.api.sidebar.ButtonOverlayRender;
import dev.ftb.mods.ftblibrary.config.FTBLibraryClientConfig;
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
import net.minecraft.network.chat.MutableComponent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class SidebarGroupGuiButton extends AbstractButton {

    public static Rect2i lastDrawnArea = new Rect2i(0, 0, 0, 0);
    private static final int BUTTON_SPACING = 17;

    private static final List<Component> noButtonComponents = List.of(
            Component.translatable("sidebar_button.ftblibrary.config"),
            Component.translatable("sidebar_button.ftblibrary.config.enter_edit_mode"));

    private SidebarGuiButton mouseOver;
    private SidebarGuiButton selectedButton;
    private GridLocation selectedLocation;
    private int lastMouseClickButton = 0;
    private boolean isEditMode;

    private int currentMouseX;
    private int currentMouseY;

    private int mouseOffsetX;
    private int mouseOffsetY;

    private int currentGirdWidth = 1;
    private int currentGridHeight = 1;

    private boolean addBoxOpen;

    boolean gridStartBottom = false;
    boolean gridStartRight = false;

    int yRenderStart;
    int xRenderStart;

    private boolean isMouseOverAdd;
    private boolean mouseOverSettingsIcon;

    private final Map<SidebarGuiButton, GridLocation> realLocationMap = new HashMap<>();

    public SidebarGroupGuiButton() {
        super(0, 0, 0, 0, Component.empty());
        ensureGridAlignment();
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mx, int my, float partialTicks) {
        graphics.pose().pushPose();
        {
            graphics.pose().translate(0, 0, 5000);

            currentMouseX = mx;
            currentMouseY = my;
            mouseOver = null;
            isMouseOverAdd = false;
            mouseOverSettingsIcon = false;

            GridLocation gridLocation = getGridLocation();

            for (Map.Entry<SidebarGuiButton, GridLocation> entry : realLocationMap.entrySet()) {
                SidebarGuiButton button = entry.getKey();
                if (entry.getValue().equals(gridLocation)) {
                    mouseOver = button;
                }
            }

            if (isEditMode) {
                renderEditMode(graphics, mx, my);
            }

            renderSidebarButtons(graphics, mx, my);

        }
        graphics.pose().popPose();
    }

    private void renderSidebarButtons(GuiGraphics graphics, int mx, int my) {
        var font = Minecraft.getInstance().font;

        graphics.pose().translate(0, 0, 50);

        //If there are no sidebar buttons enabled render "fake" button
        if (!isEditMode && SidebarButtonManager.INSTANCE.getEnabledButtonList(false).isEmpty()) {
            if (mx >= xRenderStart + 2 && my >= yRenderStart + 2 && mx < xRenderStart + 18 && my < yRenderStart + 18) {
                graphics.renderTooltip(font, noButtonComponents, Optional.empty(), mx, my + 5);
                Color4I.WHITE.withAlpha(33).draw(graphics, xRenderStart + 1, yRenderStart + 1, 16, 16);
                mouseOverSettingsIcon = true;
            }
            Icons.SETTINGS.draw(graphics, xRenderStart + 1, yRenderStart + 1, 16, 16);

        } else {
            for (SidebarGuiButton button : SidebarButtonManager.INSTANCE.getButtonList()) {
                graphics.pose().pushPose();
                GridLocation realGridLocation = realLocationMap.get(button);
                if (isEditMode || (button.equals(selectedButton) || button.isEnabled())) {
                    if (isEditMode && button == selectedButton) {
                        graphics.pose().translate(0, 0, 1000);
                        button.x = mx - mouseOffsetX;
                        button.y = my - mouseOffsetY;
                    } else {
                        if (realGridLocation == null) {
                            continue;
                        }
                        int adjustedX = gridStartRight ? xRenderStart + (currentGirdWidth - realGridLocation.x() - 1) * BUTTON_SPACING : xRenderStart + realGridLocation.x() * BUTTON_SPACING;
                        int adjustedY = gridStartBottom ? yRenderStart + (currentGridHeight - realGridLocation.y() - 1) * BUTTON_SPACING : yRenderStart + realGridLocation.y() * BUTTON_SPACING;

                        button.x = adjustedX + 1;
                        button.y = adjustedY + 1;
                    }
                    GuiHelper.setupDrawing();
                    button.getSidebarButton().getData().icon().draw(graphics, button.x, button.y, 16, 16);

                    if (isEditMode) {
                        if (mx >= button.x + 12 && my <= button.y + 4 && mx < button.x + 16 && my >= button.y) {
                            Icons.CANCEL.draw(graphics, button.x + 11, button.y - 1, 6, 6);
                        } else {
                            Icons.CANCEL.draw(graphics, button.x + 12, button.y, 4, 4);
                        }
                    }

                    if (button == mouseOver) {
                        Color4I.WHITE.withAlpha(33).draw(graphics, button.x, button.y, 16, 16);
                    }

                    graphics.pose().pushPose();
                    graphics.pose().translate(button.x, button.y, 0);
                    for (ButtonOverlayRender buttonOverlayRender : button.getSidebarButton().getExtraRenderers()) {
                        buttonOverlayRender.render(graphics, font, 16);
                    }
                    graphics.pose().popPose();

                }
                if (!isEditMode && mouseOver == button) {
                    graphics.renderTooltip(font, button.getSidebarButton().getTooltip(Screen.hasShiftDown()), Optional.empty(), mx, Math.max(7, my - 9) + 10);
                }
                graphics.pose().popPose();
            }
        }
    }

    private void renderEditMode(GuiGraphics graphics, int mx, int my) {
        drawHoveredGrid(graphics, xRenderStart, yRenderStart, currentGirdWidth, currentGridHeight, BUTTON_SPACING, Color4I.GRAY.withAlpha(70), Color4I.BLACK.withAlpha(90), mx, my, gridStartBottom, gridStartRight, getX(), getY());

        List<SidebarGuiButton> disabledButtonList = SidebarButtonManager.INSTANCE.getDisabledButtonList(isEditMode);
        if (!disabledButtonList.isEmpty()) {
            int addIconY = gridStartBottom ? yRenderStart + ((currentGridHeight - 1) * BUTTON_SPACING) : 0;
            int addIconX = gridStartRight ? xRenderStart - (2 * BUTTON_SPACING) : (currentGirdWidth + 1) * BUTTON_SPACING;
            drawGrid(graphics, addIconX, addIconY, 1, 1, BUTTON_SPACING, BUTTON_SPACING, Color4I.GRAY, Color4I.BLACK);
            Icons.ADD.draw(graphics, addIconX + 1, addIconY + 1, 16, 16);

            if (mx >= addIconX && my >= addIconY && mx < addIconX + 16 && my < addIconY + 16) {
                isMouseOverAdd = true;
                Color4I.WHITE.withAlpha(137).draw(graphics, addIconX + 1, addIconY + 1, 16, 16);
            }

            if (addBoxOpen) {
                int maxWidth = 0;
                for (SidebarGuiButton button : disabledButtonList) {
                    String s = I18n.get(button.getSidebarButton().getLangKey());
                    int width = Minecraft.getInstance().font.width(s);
                    maxWidth = Math.max(maxWidth, width);
                }

                int gridY = gridStartBottom ? addIconY - disabledButtonList.size() * BUTTON_SPACING : BUTTON_SPACING;
                int gridX = gridStartRight ? addIconX - maxWidth - 6 : (currentGirdWidth + 1) * BUTTON_SPACING;

                graphics.pose().pushPose();
                graphics.pose().translate(0, 0, 1000);
                if (gridStartRight) {
                    drawHoveredGrid(graphics, addIconX, gridY, 1, disabledButtonList.size(), BUTTON_SPACING, Color4I.GRAY, Color4I.BLACK, mx, my, gridStartBottom, gridStartRight, gridX, gridY);
                    drawGrid(graphics, addIconX - maxWidth - 6, gridY, 1, disabledButtonList.size(), maxWidth + 6, BUTTON_SPACING, Color4I.GRAY, Color4I.BLACK);
                } else {
                    drawHoveredGrid(graphics, gridX, gridY, 1, disabledButtonList.size(), BUTTON_SPACING, Color4I.GRAY, Color4I.BLACK, mx, my, gridStartBottom, gridStartRight, gridX, gridY);
                    drawGrid(graphics, gridX + BUTTON_SPACING, gridY, 1, disabledButtonList.size(), maxWidth + 6, BUTTON_SPACING, Color4I.GRAY, Color4I.BLACK);
                }

                for (int i = 0; i < disabledButtonList.size(); i++) {
                    SidebarGuiButton button = disabledButtonList.get(i);
                    if (selectedButton != null && selectedButton == button) {
                        continue;
                    }
                    int buttonY = gridY + BUTTON_SPACING * i;
                    button.x = gridStartRight ? addIconX : gridX;
                    button.y = buttonY;
                    GuiHelper.setupDrawing();

                    if (mx >= button.x && my >= button.y && mx < button.x + 16 && my < button.y + 16) {
                        Color4I.WHITE.withAlpha(137).draw(graphics, button.x + 1, button.y + 1, 16, 16);
                        mouseOver = button;
                    }

                    button.getSidebarButton().getData().icon().draw(graphics, button.x + 1, button.y + 1, 16, 16);

                    String langText = I18n.get(button.getSidebarButton().getLangKey());
                    int textXPos = gridStartRight ? addIconX - Minecraft.getInstance().font.width(langText) - 2 : gridX + BUTTON_SPACING + 3;
                    graphics.drawString(Minecraft.getInstance().font, langText, textXPos, buttonY + 5, 0xFFFFFFFF);
                }
                graphics.pose().popPose();

            }
        }
    }

    @Override
    public void onRelease(double d, double e) {
        if (lastMouseClickButton == 1) {
            return;
        }
        super.onRelease(d, e);
        //Normal click action
        if (!isEditMode && mouseOver != null) {
            mouseOver.getSidebarButton().clickButton(Screen.hasShiftDown());
        } else if (selectedButton != null) {
            GridLocation gLocation = getGridLocation();
            // Make sure the placement is in grid and that is not in the same location
            if (!gLocation.isOutOfBounds() && !gLocation.equals(selectedLocation)) {
                updateButtonLocations(gLocation);
            }
            selectedButton = null;
            ensureGridAlignment();
        }
    }

    private void updateButtonLocations(GridLocation gLocation) {
        // checks if moved from the first spot, so we can move other icons over but only as the same row

        boolean isFrom0XTo1X = selectedLocation.y() == gLocation.y() && selectedLocation.x() == 0 && gLocation.x() == 1;
        selectedButton.setGridLocation(gLocation.x(), gLocation.y());

        // Checks for icon that needs to be moved over
        List<SidebarGuiButton> buttonList = SidebarButtonManager.INSTANCE.getButtonList();
        for (SidebarGuiButton button : buttonList) {
            GridLocation realGridLocation = realLocationMap.get(button);
            if (realGridLocation != null) {
                if (!selectedButton.getSidebarButton().getId().equals(button.getSidebarButton().getId())) {
                    if (gLocation.isLatterInColumn(realGridLocation)) {
                        int moveAmount = isFrom0XTo1X && realGridLocation.x() == 1 ? -1 : 1;
                        button.setGridLocation(realGridLocation.x() + moveAmount, realGridLocation.y());
                    }
                }
            }
        }

        // If the icon was disabled enable it
        if (!selectedButton.isEnabled()) {
            selectedButton.setEnabled(true);
            if (SidebarButtonManager.INSTANCE.getDisabledButtonList(isEditMode).isEmpty()) {
                addBoxOpen = false;
            }
        }
    }

    // Aligns icons to a 'realLocationMap' this uses the button config as base, but checks if button is "visible" then aligns it to the grid
    private void ensureGridAlignment() {
        List<SidebarGuiButton> enabledButtonList = SidebarButtonManager.INSTANCE.getEnabledButtonList(isEditMode);
        Map<Integer, List<SidebarGuiButton>> buttonMap = enabledButtonList
                .stream()
                .filter(SidebarGuiButton::isEnabled)
                .collect(Collectors.groupingBy(button -> button.getGirdLocation().y(), TreeMap::new, Collectors.toCollection(LinkedList::new)));

        realLocationMap.clear();

        int y = 0;
        for (Map.Entry<Integer, List<SidebarGuiButton>> entry : buttonMap.entrySet()) {
            entry.getValue().sort(Comparator.comparingInt(b -> b.getGirdLocation().x()));
            int x = 0;
            for (SidebarGuiButton button : entry.getValue()) {
                realLocationMap.put(button, new GridLocation(x, y));
                x++;
            }
            if (x != 0) {
                y++;
            }
        }


        SidebarButtonManager.INSTANCE.saveConfigFromButtonList();
        updateWidgetSize();
    }

    private void updateWidgetSize() {
        // Important: JEI doesn't like negative X/Y values and will silently clamp them,
        // leading it to think the values have changed every frame, and do unnecessary updating
        // of its GUI areas, including resetting the filter textfield's selection
        // https://github.com/FTBTeam/FTB-Mods-Issues/issues/262
        // https://github.com/mezz/JustEnoughItems/issues/2938
        // This calculates the grid size
        int girdAmountX = 1;
        int girdAmountY = 1;
        for (SidebarGuiButton b : SidebarButtonManager.INSTANCE.getEnabledButtonList(isEditMode)) {
            girdAmountX = Math.max(girdAmountX, b.getGirdLocation().x() + 1);
            girdAmountY = Math.max(girdAmountY, b.getGirdLocation().y() + 1);
        }

        if (isEditMode && addBoxOpen) {
            int disabledList = SidebarButtonManager.INSTANCE.getDisabledButtonList(isEditMode).size();
            girdAmountX += 4;
            girdAmountY = Math.max(girdAmountY, disabledList);
        }
        if (isEditMode) {
            // Add 3 extra to the x so that add button is clickable
            girdAmountX += 3;
            // Add one extra y, so you can place widgets at the bottom
            girdAmountY += 1;
        }

        // ensure the grid size is not bigger than the screen
        int screenWidth = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int screenHeight = Minecraft.getInstance().getWindow().getGuiScaledHeight();

        int maxGirdAmountX = screenWidth / BUTTON_SPACING;
        int maxGirdAmountY = screenHeight / BUTTON_SPACING;
        girdAmountX = Math.min(girdAmountX, maxGirdAmountX);
        girdAmountY = Math.min(girdAmountY, maxGirdAmountY);


        width = (girdAmountX) * BUTTON_SPACING;
        height = (girdAmountY) * BUTTON_SPACING;

        FTBLibraryClientConfig.SidebarPosition sidebarPosition = FTBLibraryClientConfig.SIDEBAR_POSITION.get();

        if (sidebarPosition.isBottom()) {
            setY(screenHeight - height - 2);
            gridStartBottom = true;
        } else {
            setY(0);
            gridStartBottom = false;
        }
        if (sidebarPosition.isRight()) {
            setX(screenWidth - width - 2);
            gridStartRight = true;
        } else {
            setX(0);
            gridStartRight = false;
        }

        currentGirdWidth = 1;
        currentGridHeight = 1;

        for (Map.Entry<SidebarGuiButton, GridLocation> value : realLocationMap.entrySet()) {
            GridLocation location = value.getValue();
            currentGirdWidth = Math.max(currentGirdWidth, location.x() + 1);
            currentGridHeight = Math.max(currentGridHeight, location.y() + 1);
        }

        if (isEditMode) {
            currentGirdWidth += 1;
            currentGridHeight += 1;
        }

        xRenderStart = (gridStartRight ? maxGirdAmountX - currentGirdWidth : 0) * BUTTON_SPACING;
        yRenderStart = (gridStartBottom ? maxGirdAmountY - currentGridHeight + 1 : 0) * BUTTON_SPACING;

        if (gridStartBottom) {
            yRenderStart -= 4;
        }
        if (gridStartRight) {
            xRenderStart += 3;
        }

        // Set the last drawn area so recipe viewer knows where we are and we can move it out the way
        lastDrawnArea = new Rect2i(getX(), getY(), width, height);
    }

    private GridLocation getGridLocation() {

        int gridX = (currentMouseX - xRenderStart - 1) / BUTTON_SPACING;
        int gridY = (currentMouseY - yRenderStart - 1) / BUTTON_SPACING;

        if (gridStartRight) {
            gridX = currentGirdWidth - gridX - 1;
        }

        if (gridStartBottom) {
            gridY = currentGridHeight - gridY - 1;
        }

        if (gridX >= currentGirdWidth || gridY >= currentGridHeight || gridX < 0 || gridY < 0) {
            return GridLocation.OUT_OF_BOUNDS;
        }

        return new GridLocation(gridX, gridY);
    }


    @Override
    public void onPress() {
        if (lastMouseClickButton == 1) {
            isEditMode = !isEditMode;
            ensureGridAlignment();
            return;
        }
        if (mouseOverSettingsIcon) {
            FTBLibraryClient.editConfig(true);
            return;
        }
        if (isEditMode && isMouseOverAdd) {
            addBoxOpen = !addBoxOpen;
            updateWidgetSize();
            return;
        }
        if (mouseOver != null) {

            mouseOffsetX = currentMouseX - mouseOver.x;
            mouseOffsetY = currentMouseY - mouseOver.y;

            if (isEditMode) {
                // if the mouse is over the cancel icon
                if (currentMouseX >= mouseOver.x + 12 && currentMouseY <= mouseOver.y + 4 && currentMouseX < mouseOver.x + 16 && currentMouseY >= mouseOver.y) {
                    mouseOver.setEnabled(false);
                    mouseOver = null;
                    ensureGridAlignment();
                    return;
                }

                selectedButton = mouseOver;
                GridLocation realGridLocation = realLocationMap.get(selectedButton);
                selectedLocation = realGridLocation == null ? selectedButton.getGirdLocation() : realGridLocation;
            }
        }
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        defaultButtonNarrationText(narrationElementOutput);
    }

    // Custom handling so our button click locations are where a buttons are not just a box
    @Override
    protected boolean isValidClickButton(int i) {
        boolean inBounds = clicked(currentMouseX, currentMouseY);
        if (!inBounds && isEditMode) {
            isEditMode = false;
            return false;
        }
        lastMouseClickButton = i;
        if (i == 1) {
            return inBounds;
        }
        if (super.isValidClickButton(i)) {
            if (isEditMode) {
                return isMouseOverAdd || selectedButton != null || mouseOver != null;
            } else {
                GridLocation gridLocation = getGridLocation();
                return (gridLocation.y() == 0 && gridLocation.x() == 0) || mouseOver != null;
            }
        }
        return false;
    }

    private static void drawGrid(GuiGraphics graphics, int x, int y, int width, int height, int spacingWidth, int spacingHeight, Color4I backgroundColor, Color4I gridColor) {
        backgroundColor.draw(graphics, x, y, width * spacingWidth, height * spacingHeight);
        for (var i = 0; i < width + 1; i++) {
            gridColor.draw(graphics, x + i * spacingWidth, y, 1, height * spacingHeight + 1);
        }
        for (var i = 0; i < height + 1; i++) {
            gridColor.draw(graphics, x, y + i * spacingHeight, width * spacingWidth, 1);
        }
    }

    public static void drawGrid(GuiGraphics graphics, int x, int y, int width, int height, int spacing, Color4I backgroundColor, Color4I gridColor) {
        drawGrid(graphics, x, y, width, height, spacing, spacing, backgroundColor, gridColor);
    }

    private static void drawHoveredGrid(GuiGraphics graphics, int x, int y, int width, int height, int spacing, Color4I backgroundColor, Color4I gridColor, int mx, int my, boolean gridStartBottom, boolean gridStartRight, int posX, int posY) {
        drawGrid(graphics, x, y, width, height, spacing, backgroundColor, gridColor);

        int adjustedMx = mx;
        int adjustedMy = my;

        if (gridStartRight) {
            adjustedMx = x + width * spacing - (mx - x);
        }

        if (gridStartBottom) {
            adjustedMy = y + height * spacing - (my - y);
        }

        if (adjustedMx >= x + posX && adjustedMy >= y + posY && adjustedMx < x + posX + width * spacing && adjustedMy < y + posY + height * spacing) {
            int gridX = (adjustedMx - x - posX) / spacing;
            int gridY = (adjustedMy - y - posY) / spacing;
            Color4I.WHITE.withAlpha(127).draw(graphics, gridX * BUTTON_SPACING + 1, gridY * BUTTON_SPACING + 1, spacing - 1, spacing - 1);
        }
    }

}
