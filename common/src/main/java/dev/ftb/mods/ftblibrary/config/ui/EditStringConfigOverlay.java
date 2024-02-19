package dev.ftb.mods.ftblibrary.config.ui;

import dev.ftb.mods.ftblibrary.config.ConfigCallback;
import dev.ftb.mods.ftblibrary.config.ConfigFromString;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.mods.ftblibrary.ui.*;
import dev.ftb.mods.ftblibrary.ui.input.Key;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class EditStringConfigOverlay<T> extends ModalPanel {
    private final EditField textBox;
    private final Button accept, cancel;
    private final ConfigFromString<T> config;
    private final ConfigCallback callback;
    private T currentValue;
    private final int availableWidth;

    public EditStringConfigOverlay(Panel panel, ConfigFromString<T> config, ConfigCallback callback, int availableWidth) {
        super(panel);
        this.config = config;
        this.callback = callback;
        this.currentValue = config.getValue() == null ? null : config.copy(config.getValue());
        this.availableWidth = availableWidth;

        textBox = new EditField();
        accept = new SimpleButton(this, Component.translatable("gui.accept"), Icons.ACCEPT, this::onAccepted);
        cancel = new SimpleButton(this, Component.translatable("gui.cancel"), Icons.CANCEL, this::onCancelled);
    }

    @Override
    public void addWidgets() {
        add(textBox);
        add(accept);
        add(cancel);
    }

    @Override
    public void alignWidgets() {
        textBox.setPosAndSize(2, 1, availableWidth - 32, 14);
//        textBox.setY((16 - textBox.height) / 2);

        width = align(WidgetLayout.HORIZONTAL);
        height = 16;
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
        theme.drawContextMenuBackground(graphics, x - 1, y - 1, w + 2, h + 2);
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

    private class EditField extends TextBox {
        public EditField() {
            super(EditStringConfigOverlay.this);

            setText(config.getStringFromValue(currentValue));
            textColor = Color4I.WHITE; //config.getColor(currentValue);
            setCursorPosition(getText().length());
            setSelectionPos(0);
            setFocused(true);
        }

        @Override
        public boolean allowInput() {
            return config.getCanEdit();
        }

        @Override
        public boolean isValid(String txt) {
            return config.parse(null, txt);
        }

        @Override
        public void onTextChanged() {
            config.parse(t -> currentValue = t, getText());
        }

        @Override
        public void onEnterPressed() {
            if (config.getCanEdit()) {
                accept.onClicked(MouseButton.LEFT);
            }
        }

        @Override
        public boolean mouseScrolled(double scroll) {
            return config.scrollValue(currentValue, scroll > 0).map(v -> {
                textBox.setText(config.getStringFromValue(v));
                textBox.setSelectionPos(textBox.getCursorPosition());
                return true;
            }).orElse(super.mouseScrolled(scroll));
        }
    }

    /**
     * Provides positioning information for when an edit panel needs to be overlaid on the widget
     */
    public interface PosProvider {
        Offset getOverlayOffset();

        record Offset(int x, int y) {
            public static final Offset NONE = new Offset(0, 0);
        }
    }
}
