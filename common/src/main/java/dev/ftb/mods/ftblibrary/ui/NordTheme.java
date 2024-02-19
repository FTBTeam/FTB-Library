package dev.ftb.mods.ftblibrary.ui;

import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.PartIcon;
import dev.ftb.mods.ftblibrary.ui.misc.NordColors;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

import static dev.ftb.mods.ftblibrary.FTBLibrary.rl;

public class NordTheme extends Theme {
    public static final Theme THEME = new NordTheme();

    private static final ResourceLocation BUTTON_TEX = rl("textures/gui/nord_button.png");
    private static final ResourceLocation BUTTON_TEX_MOUSE_OVER = rl("textures/gui/nord_button_hovered.png");
    private static final ResourceLocation BUTTON_TEX_DISABLED = rl("textures/gui/nord_button_disabled.png");

    private static final Icon BUTTON = PartIcon.wholeTexture(BUTTON_TEX, 200, 20, 4);
    private static final Icon BUTTON_MOUSE_OVER = PartIcon.wholeTexture(BUTTON_TEX_MOUSE_OVER, 200, 20, 4);
    private static final Icon BUTTON_DISABLED = PartIcon.wholeTexture(BUTTON_TEX_DISABLED, 200, 20, 4);

    @Override
    public Color4I getContentColor(WidgetType type) {
        return type == WidgetType.MOUSE_OVER ? NordColors.YELLOW : type == WidgetType.DISABLED ? NordColors.POLAR_NIGHT_3 : NordColors.SNOW_STORM_0;
    }

    @Override
    public void drawScrollBarBackground(GuiGraphics graphics, int x, int y, int w, int h, WidgetType type) {
        NordColors.POLAR_NIGHT_1.draw(graphics, x, y, w, h);
        GuiHelper.drawRectWithShade(graphics, x, y, w, h, NordColors.POLAR_NIGHT_2, -16);
    }

    @Override
    public void drawScrollBar(GuiGraphics graphics, int x, int y, int w, int h, WidgetType type, boolean vertical) {
        NordColors.FROST_3.withAlpha(128).draw(graphics, x + 2, y + 1, w - 4, h - 2);
        GuiHelper.drawRectWithShade(graphics, x + 2, y + 1, w - 4, h - 2, NordColors.FROST_3, 16);
    }

    @Override
    public void drawGui(GuiGraphics graphics, int x, int y, int w, int h, WidgetType type) {
        NordColors.POLAR_NIGHT_1.draw(graphics, x, y, w, h);
        GuiHelper.drawHollowRect(graphics, x - 1, y - 1, w + 2, h + 2, Color4I.rgb(0x101010), true);
    }

    @Override
    public void drawContextMenuBackground(GuiGraphics graphics, int x, int y, int w, int h) {
        NordColors.POLAR_NIGHT_1.draw(graphics, x, y, w, h);
        GuiHelper.drawHollowRect(graphics, x - 1, y - 1, w + 2, h + 2, Color4I.rgb(0x101010), false);
    }

    @Override
    public void drawButton(GuiGraphics graphics, int x, int y, int w, int h, WidgetType type) {
        (type == WidgetType.MOUSE_OVER ? BUTTON_MOUSE_OVER : type == WidgetType.DISABLED ? BUTTON_DISABLED : BUTTON)
                .draw(graphics, x, y, w, h);
    }

    @Override
    public void drawSlot(GuiGraphics graphics, int x, int y, int w, int h, WidgetType type) {
        Color4I col = type == WidgetType.NORMAL ? NordColors.POLAR_NIGHT_1 : NordColors.POLAR_NIGHT_3;
        GuiHelper.drawBorderedPanel(graphics, x, y, w, h, col, false);
    }

    @Override
    public void drawTextBox(GuiGraphics graphics, int x, int y, int w, int h) {
        GuiHelper.drawBorderedPanel(graphics, x, y, w, h, NordColors.FROST_3, false);
    }

    @Override
    public void drawWidget(GuiGraphics graphics, int x, int y, int w, int h, WidgetType type) {
        GuiHelper.drawBorderedPanel(graphics, x, y, w, h, NordColors.POLAR_NIGHT_2, true);
    }
}
