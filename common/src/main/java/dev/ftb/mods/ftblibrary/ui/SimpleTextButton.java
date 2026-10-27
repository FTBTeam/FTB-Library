package dev.ftb.mods.ftblibrary.ui;


import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import dev.ftb.mods.ftblibrary.util.client.PositionedIngredient;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;

import java.util.Optional;
import java.util.function.Consumer;


public abstract class SimpleTextButton extends Button {
    public SimpleTextButton(Panel panel, Component txt, Icon icon) {
        super(panel, txt, icon);
        setWidth(panel.getGui().getTheme().getStringWidth(txt) + (hasIcon() ? 28 : 8));
        setHeight(20);
    }

    public static SimpleTextButton create(Panel panel, Component txt, Icon icon, Consumer<MouseButton> callback, Component... tooltip) {
        return new SimpleTextButton(panel, txt, icon) {
            @Override
            public void onClicked(MouseButton button) {
                callback.accept(button);
            }

            @Override
            public void addMouseOverText(TooltipList list) {
                for (Component c : tooltip) {
                    list.add(c);
                }
            }
        };
    }

    public static SimpleTextButton accept(Panel panel, Consumer<MouseButton> callback, Component... tooltip) {
        return create(panel, Component.translatable("gui.accept"), Icons.ACCEPT, callback, tooltip);
    }

    public static SimpleTextButton cancel(Panel panel, Consumer<MouseButton> callback, Component... tooltip) {
        return create(panel, Component.translatable("gui.cancel"), Icons.CANCEL, callback, tooltip);
    }

    public static Button.Builder acceptBuilder(Panel panel, Consumer<MouseButton> callback, Component... tooltip) {
        return Button.builder(panel, callback)
                .withLabel(Component.translatable("gui.accept"))
                .withIcon(Icons.ACCEPT)
                .withExtraTooltipLines(tooltipList -> tooltipList.addAll(tooltip));
    }

    public static Button.Builder cancelBuilder(Panel panel, Consumer<MouseButton> callback, Component... tooltip) {
        return Button.builder(panel, callback)
                .withLabel(Component.translatable("gui.cancel"))
                .withIcon(Icons.CANCEL)
                .withExtraTooltipLines(tooltipList -> tooltipList.addAll(tooltip));
    }

    @Override
    public SimpleTextButton setTitle(Component txt) {
        super.setTitle(txt);
        setWidth(getGui().getTheme().getStringWidth(getTitle()) + (hasIcon() ? 28 : 8));
        return this;
    }

    public boolean renderTitleInCenter() {
        return false;
    }

    @Override
    public Optional<PositionedIngredient> getIngredientUnderMouse() {
        return PositionedIngredient.of(icon.getIngredient(), this);
    }

    public boolean hasIcon() {
        return !icon.isEmpty();
    }

    @Override
    public void addMouseOverText(TooltipList list) {
        if (getGui().getTheme().getStringWidth(getTitle()) + (hasIcon() ? 28 : 8) > width) {
            list.add(getTitle());
        }
    }

    @Override
    public void draw(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
        drawBackground(graphics, theme, x, y, w, h);

        var iconSize = h >= 16 ? 16 : 8;
        var iconOffset = (h - iconSize) / 2;

        FormattedText title = getTitle();

        var textWidth = theme.getStringWidth(title);
        var availableWidth = w - (hasIcon() ? iconOffset + iconSize : 0) - 6;
        if (textWidth > availableWidth) {
            textWidth = availableWidth;
            title = theme.trimStringToWidth(title, availableWidth);
        }

        var textX = x + (renderTitleInCenter() ? (availableWidth - textWidth + 6) / 2 : 4);
        var textY = y + (h - theme.getFontHeight() + 1) / 2;
        if (hasIcon()) {
            drawIcon(graphics, theme, x + iconOffset, y + iconOffset, iconSize, iconSize);
            textX += iconOffset + iconSize;
        }

        theme.drawString(graphics, title, textX, textY, theme.getContentColor(getWidgetType()), Theme.SHADOW);
    }
}
