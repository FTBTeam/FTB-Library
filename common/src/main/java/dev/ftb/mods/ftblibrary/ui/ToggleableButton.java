package dev.ftb.mods.ftblibrary.ui;

import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.Icons;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class ToggleableButton extends SimpleButton {

    private Component enabledText;
    private Component disabledText;

    private boolean state;
    private boolean drawBg;

    public ToggleableButton(Panel panel, boolean defaultState, Icon enabled, Icon disabled, ToggleableCallback toggleableCallback) {
        super(panel, Component.empty(), defaultState ? enabled : disabled, null);

        this.state = defaultState;
        this.setConsumer((widget, button) -> {
            this.state = !this.state;
            widget.setIcon(this.state ? enabled : disabled);
            updateTitle();
            toggleableCallback.onClicked(widget, this.state);
        });
        this.enabledText = Component.translatable("ftblibrary.gui.enabled").withStyle(ChatFormatting.GREEN);
        this.disabledText = Component.translatable("ftblibrary.gui.disabled").withStyle(ChatFormatting.RED);
        updateTitle();
    }

    public ToggleableButton(Panel panel, boolean defaultState, ToggleableCallback toggleableCallback) {
        this(panel, defaultState, Icons.VISIBILITY_SHOW, Icons.VISIBILITY_HIDE, toggleableCallback);
    }

    @Override
    public void drawBackground(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
        if (drawBg) {
            theme.drawButton(graphics, x, y, w, h, WidgetType.mouseOver(isMouseOver));
        }
    }

    public ToggleableButton shouldDrawBackground(boolean drawBg) {
        this.drawBg = drawBg;
        return this;
    }

    public Component getEnabledText() {
        return enabledText;
    }

    public ToggleableButton setEnabledText(Component enabledText) {
        this.enabledText = enabledText;
        updateTitle();
        return this;
    }

    public Component getDisabledText() {
        return disabledText;
    }

    public ToggleableButton setDisabledText(Component disabledText) {
        this.disabledText = disabledText;
        updateTitle();
        return this;
    }

    private void updateTitle() {
        setTitle(state ? enabledText : disabledText);
    }

    public boolean isChecked() {
        return state;
    }

    public interface ToggleableCallback {
        void onClicked(SimpleButton widget, boolean newState);
    }
}
