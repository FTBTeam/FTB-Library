package dev.ftb.mods.ftblibrary.ui;

import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class ContextButton extends Button {
    public final ContextMenuItem item;
    private final boolean hasIcons;

    public ContextButton(Panel panel, ContextMenuItem item, boolean hasIcons) {
        super(panel, item.getTitle(), item.getIcon());
        this.hasIcons = hasIcons;
        this.item = item;
        setSize(panel.getGui().getTheme().getStringWidth(item.getTitle()) + (hasIcons ? 14 : 4), 12);
    }

    @Override
    public void addMouseOverText(TooltipList list) {
        item.addMouseOverText(list);
    }

    @Override
    public WidgetType getWidgetType() {
        if (!item.isClickable()) {
            return WidgetType.NORMAL;  // no hovered highlighting
        }

        return item.isEnabled() ? super.getWidgetType() : WidgetType.DISABLED;
    }

    @Override
    public void drawIcon(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
        item.drawIcon(graphics, theme, x, y, w, h);
    }

    @Override
    public void draw(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
        GuiHelper.setupDrawing();

        if (hasIcons) {
            drawIcon(graphics, theme, x + 1, y + 2, 8, 8);
            theme.drawString(graphics, getTitle(), x + 11, y + 2, theme.getContentColor(getWidgetType()), Theme.SHADOW);
        } else {
            theme.drawString(graphics, getTitle(), x + 2, y + 2, theme.getContentColor(getWidgetType()), Theme.SHADOW);
        }
    }

    @Override
    public void onClicked(MouseButton button) {
        if (item.isClickable()) {
            playClickSound();
        }

        if (item.getYesNoText().getString().isEmpty()) {
            item.onClicked(ContextButton.this, parent, button);
        } else {
            getGui().openYesNo(item.getYesNoText(), Component.literal(""), () -> item.onClicked(ContextButton.this, parent, button));
        }
    }

}
