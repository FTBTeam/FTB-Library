package dev.ftb.mods.ftblibrary.ui;

import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftblibrary.util.client.PositionedIngredient;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.Optional;

public abstract class Button extends Widget {
    protected Component title;
    protected Icon icon;
    private boolean forceButtonSize;

    public Button(Panel panel, Component t, Icon i) {
        super(panel);
        setSize(16, 16);
        icon = i;
        title = t;
    }

    public Button(Panel panel) {
        this(panel, Component.empty(), Icon.empty());
    }

    @Override
    public Component getTitle() {
        return title;
    }

    public Button setTitle(Component s) {
        title = s;
        return this;
    }

    public Button setIcon(Icon i) {
        icon = i;
        return this;
    }

    public Button setForceButtonSize(boolean forceButtonSize) {
        this.forceButtonSize = forceButtonSize;
        return this;
    }

    public void drawBackground(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
        theme.drawButton(graphics, x, y, w, h, getWidgetType());
    }

    public void drawIcon(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
        icon.draw(graphics, x, y, w, h);
    }

    @Override
    public void draw(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
        GuiHelper.setupDrawing();
        drawBackground(graphics, theme, x, y, w, h);
        if(forceButtonSize) {
            var s = h >= 16 ? 16 : 8;
            drawIcon(graphics, theme, x + (w - s) / 2, y + (h - s) / 2, s, s);
        }else {
            drawIcon(graphics, theme, x, y, w, h);
        }
    }

    @Override
    public boolean mousePressed(MouseButton button) {
        if (isMouseOver()) {
            if (getWidgetType() != WidgetType.DISABLED) {
                onClicked(button);
            }

            return true;
        }

        return false;
    }

    public abstract void onClicked(MouseButton button);

    @Override
    public Optional<PositionedIngredient> getIngredientUnderMouse() {
        return PositionedIngredient.of(icon.getIngredient(), this, true);
    }

    @Override
    public CursorType getCursor() {
        return CursorType.HAND;
    }
}
