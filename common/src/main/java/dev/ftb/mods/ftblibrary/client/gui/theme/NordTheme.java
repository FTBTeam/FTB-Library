package dev.ftb.mods.ftblibrary.client.gui.theme;

import dev.ftb.mods.ftblibrary.client.gui.GuiHelper;
import dev.ftb.mods.ftblibrary.client.gui.WidgetType;
import dev.ftb.mods.ftblibrary.client.icon.IconHelper;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.PartIcon;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;

import static dev.ftb.mods.ftblibrary.FTBLibrary.id;

public class NordTheme extends Theme {
    public static final Theme THEME = new NordTheme();

    private static final Identifier BUTTON_TEX = id("textures/gui/nord_button.png");
    private static final Identifier BUTTON_TEX_MOUSE_OVER = id("textures/gui/nord_button_hovered.png");
    private static final Identifier BUTTON_TEX_DISABLED = id("textures/gui/nord_button_disabled.png");

    private static final Icon<?> BUTTON = PartIcon.wholeTexture(BUTTON_TEX.toString(), 200, 20, 4);
    private static final Icon<?> BUTTON_MOUSE_OVER = PartIcon.wholeTexture(BUTTON_TEX_MOUSE_OVER.toString(), 200, 20, 4);
    private static final Icon<?> BUTTON_DISABLED = PartIcon.wholeTexture(BUTTON_TEX_DISABLED.toString(), 200, 20, 4);

    @Override
    public Color4I getContentColor(WidgetType type) {
        return type == WidgetType.MOUSE_OVER ? NordColors.YELLOW : type == WidgetType.DISABLED ? NordColors.POLAR_NIGHT_3 : NordColors.SNOW_STORM_0;
    }

    @Override
    public void drawScrollBarBackground(GuiGraphicsExtractor graphics, int x, int y, int w, int h, WidgetType type) {
        GuiHelper.drawBorderedPanel(graphics, x, y, w, h, NordColors.POLAR_NIGHT_1, false);
    }

    @Override
    public void drawScrollBar(GuiGraphicsExtractor graphics, int x, int y, int w, int h, WidgetType type, boolean vertical) {
        GuiHelper.drawBorderedPanel(graphics, x + 2, y + 1, w - 4, h - 3, NordColors.POLAR_NIGHT_3, true);
    }

    @Override
    public void drawGui(GuiGraphicsExtractor graphics, int x, int y, int w, int h, WidgetType type) {
        GuiHelper.drawBorderedPanel(graphics, x - 1, y - 1, w + 2, h + 2, NordColors.POLAR_NIGHT_1, true);
        GuiHelper.drawHollowRect(graphics, x - 2, y - 2, w + 4, h + 4, Color4I.rgb(0x101010), true);
    }

    @Override
    public void drawContextMenuBackground(GuiGraphicsExtractor graphics, int x, int y, int w, int h) {
        IconHelper.renderIcon(NordColors.POLAR_NIGHT_1, graphics, x, y, w, h);
        GuiHelper.drawHollowRect(graphics, x - 1, y - 1, w + 2, h + 2, Color4I.rgb(0x101010), false);
    }

    @Override
    public void drawButton(GuiGraphicsExtractor graphics, int x, int y, int w, int h, WidgetType type) {
        Icon<?> icon = type == WidgetType.MOUSE_OVER ? BUTTON_MOUSE_OVER : type == WidgetType.DISABLED ? BUTTON_DISABLED : BUTTON;
        IconHelper.renderIcon(icon, graphics, x, y, w, h);
    }

    @Override
    public void drawSlot(GuiGraphicsExtractor graphics, int x, int y, int w, int h, WidgetType type) {
        Color4I col = type == WidgetType.NORMAL ? NordColors.POLAR_NIGHT_1 : NordColors.POLAR_NIGHT_3;
        GuiHelper.drawBorderedPanel(graphics, x, y, w, h, col, false);
    }

    @Override
    public void drawTextBox(GuiGraphicsExtractor graphics, int x, int y, int w, int h) {
        GuiHelper.drawBorderedPanel(graphics, x, y, w, h, NordColors.FROST_3, false);
    }

    @Override
    public void drawWidget(GuiGraphicsExtractor graphics, int x, int y, int w, int h, WidgetType type) {
        GuiHelper.drawBorderedPanel(graphics, x, y, w, h, NordColors.POLAR_NIGHT_2, true);
    }

    @Override
    public void drawPanelBackground(GuiGraphicsExtractor graphics, int x, int y, int w, int h) {
        IconHelper.renderIcon(NordColors.POLAR_NIGHT_2, graphics, x, y, w, h);
    }

    @Override
    public boolean hasDarkBackground() {
        return true;
    }
}
