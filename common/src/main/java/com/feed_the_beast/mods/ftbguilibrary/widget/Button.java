package com.feed_the_beast.mods.ftbguilibrary.widget;

import com.feed_the_beast.mods.ftbguilibrary.icon.Icon;
import com.feed_the_beast.mods.ftbguilibrary.utils.MouseButton;
import com.mojang.blaze3d.vertex.PoseStack;

import org.jetbrains.annotations.Nullable;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

public abstract class Button extends Widget {
    protected Component title;
    protected Icon icon;

    public Button(Panel panel, Component t, Icon i) {
        super(panel);
        setSize(16, 16);
        icon = i;
        title = t;
    }

    public Button(Panel panel) {
        this(panel, TextComponent.EMPTY, Icon.EMPTY);
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

    public void drawBackground(PoseStack matrixStack, Theme theme, int x, int y, int w, int h) {
        theme.drawButton(matrixStack, x, y, w, h, getWidgetType());
    }

    public void drawIcon(PoseStack matrixStack, Theme theme, int x, int y, int w, int h) {
        icon.draw(matrixStack, x, y, w, h);
    }

    @Override
    public void draw(PoseStack matrixStack, Theme theme, int x, int y, int w, int h) {
        int s = h >= 16 ? 16 : 8;
        drawBackground(matrixStack, theme, x, y, w, h);
        drawIcon(matrixStack, theme, x + (w - s) / 2, y + (h - s) / 2, s, s);
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
    @Nullable
    public Object getIngredientUnderMouse() {
        return new WrappedIngredient(icon.getIngredient()).tooltip();
    }
}