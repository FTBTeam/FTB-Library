package dev.ftb.mods.ftblibrary.config.ui;

import dev.ftb.mods.ftblibrary.config.ConfigCallback;
import dev.ftb.mods.ftblibrary.config.StringConfig;
import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.mods.ftblibrary.ui.*;
import dev.ftb.mods.ftblibrary.ui.input.Key;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class EditMultilineStringConfigOverlay extends ModalPanel {
    private final MultilineTextBox textBox;
    private final Button accept, cancel;
    private final StringConfig config;
    private final ConfigCallback callback;
    private final PanelScrollBar scrollBar;
    private final Panel textBoxPanel;
    private String currentValue;

    public EditMultilineStringConfigOverlay(Panel panel, StringConfig config, ConfigCallback callback) {
        super(panel);
        this.config = config;
        this.callback = callback;
        this.currentValue = config.getValue() == null ? null : config.copy(config.getValue());

        textBoxPanel = new TextBoxPanel();
        textBox = new MultilineTextBox(textBoxPanel);
        textBox.setText(currentValue);
        textBox.setValueListener(s -> currentValue = s);
        textBox.setFocused(true);
        scrollBar = new PanelScrollBar(this, ScrollBar.Plane.VERTICAL, textBoxPanel);

        accept = new SimpleButton(this, Component.translatable("gui.accept"), Icons.ACCEPT, this::onAccepted);
        cancel = new SimpleButton(this, Component.translatable("gui.cancel"), Icons.CANCEL, this::onCancelled);

        height = (getGui().getTheme().getFontHeight() + 1) * 4;
    }

    @Override
    public void addWidgets() {
        add(textBoxPanel);
        add(scrollBar);
        add(accept);
        add(cancel);
    }

    @Override
    public void alignWidgets() {
        textBoxPanel.setPosAndSize(2, 2, width - 20, height - 4);
        textBoxPanel.addWidgets();
        textBoxPanel.alignWidgets();

        scrollBar.setPosAndSize(width - 30, 0, 12, height);

        accept.setPosAndSize(width - 18, 2, 16, 16);
        cancel.setPosAndSize(width - 18, 18, 16, 16);
    }

    @Override
    public boolean keyPressed(Key key) {
        if (key.esc()) {
            onCancelled(cancel, MouseButton.LEFT);
            return true;
        }
        return super.keyPressed(key);
    }

    @Override
    public void drawBackground(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
        theme.drawWidget(graphics, x, y, w, h, WidgetType.NORMAL);
    }

    private void onAccepted(Button btn, MouseButton mb) {
        config.setCurrentValue(currentValue);
        callback.save(true);
        getGui().popModalPanel();
    }

    private void onCancelled(Button btn, MouseButton mb) {
        callback.save(false);
        getGui().popModalPanel();
    }

    private class TextBoxPanel extends Panel {
        public TextBoxPanel() {
            super(EditMultilineStringConfigOverlay.this);
        }

        @Override
        public void addWidgets() {
            add(textBox);
        }

        @Override
        public void alignWidgets() {
            textBox.setSize(width, height);
            setScrollY(0);
        }

        @Override
        public void drawBackground(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
            theme.drawSlot(graphics, x, y, w, h, WidgetType.NORMAL);
        }
    }
}
