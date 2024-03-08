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
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class EditStringConfigOverlay<T> extends ModalPanel {
    private final EditField textBox;
    private final Button accept, cancel;
    private final ConfigFromString<T> config;
    private final ConfigCallback callback;
    private final TextField titleField;
    private final Component title;

    private T currentValue;

    public EditStringConfigOverlay(Panel panel, ConfigFromString<T> config, ConfigCallback callback) {
        this(panel, config, callback, null);
    }

    public EditStringConfigOverlay(Panel panel, ConfigFromString<T> config, ConfigCallback callback, @Nullable Component title) {
        super(panel);
        this.config = config;
        this.callback = callback;
        this.currentValue = config.getValue() == null ? null : config.copy(config.getValue());
        this.title = title;

        width = currentValue == null ? 100 : getGui().getTheme().getStringWidth(config.getStringFromValue(currentValue)) + 86;

        titleField = new TextField(this).addFlags(Theme.SHADOW).setText(Objects.requireNonNullElse(title, Component.empty()));
        titleField.setSize(0, 0);
        textBox = new EditField();
        accept = new SimpleButton(this, Component.translatable("gui.accept"), Icons.ACCEPT, this::onAccepted);
        cancel = new SimpleButton(this, Component.translatable("gui.cancel"), Icons.CANCEL, this::onCancelled);
    }

    public EditStringConfigOverlay<T> atPosition(int x, int y) {
        setPos(x, y);
        return this;
    }

    public EditStringConfigOverlay<T> atMousePosition() {
        int absX = Math.min(getMouseX(), getScreen().getGuiScaledWidth() - width);
        int absY = Math.min(getMouseY(), getScreen().getGuiScaledHeight() - height);
        return atPosition(absX - parent.getX(), absY - parent.getY() - (int) parent.getScrollY());
    }

    @Override
    public void addWidgets() {
        if (title != null) {
            add(titleField);
        }
        add(textBox);
        add(accept);
        add(cancel);
    }

    @Override
    public void alignWidgets() {
        if (title != null) {
            titleField.setPosAndSize(2, 2, width, getGui().getTheme().getFontHeight() + 4);
        }
        textBox.setPosAndSize(2, titleField.getHeight() + 1, width - 36, 14);
        accept.setPos(textBox.width + 2, textBox.getPosY());
        cancel.setPos(accept.getPosX() + accept.width + 2, textBox.getPosY());

        height = title == null ? 16 : 30;
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
