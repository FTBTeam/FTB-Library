package dev.ftb.mods.ftblibrary.client.config.gui;

import dev.ftb.mods.ftblibrary.client.config.ConfigCallback;
import dev.ftb.mods.ftblibrary.client.config.editable.EditableStringifiedConfig;
import dev.ftb.mods.ftblibrary.client.gui.input.Key;
import dev.ftb.mods.ftblibrary.client.gui.input.MouseButton;
import dev.ftb.mods.ftblibrary.client.gui.theme.Theme;
import dev.ftb.mods.ftblibrary.client.gui.widget.*;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.icon.Icons;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

public class EditStringConfigOverlay<T> extends ModalPanel {
    protected final EditField textBox;
    private final Button accept, cancel;
    private final EditableStringifiedConfig<T> config;
    private final ConfigCallback callback;
    private final TextField titleField;
    @Nullable
    private final Component title;
    private boolean addAcceptCancelButtons = true;

    @Nullable
    private T currentValue;

    public EditStringConfigOverlay(Panel panel, EditableStringifiedConfig<T> config, ConfigCallback callback) {
        this(panel, config, callback, null);
    }

    public EditStringConfigOverlay(Panel panel, EditableStringifiedConfig<T> config, ConfigCallback callback, @Nullable Component title) {
        super(panel);
        this.config = config;
        this.callback = callback;
        this.currentValue = config.getValue() == null ? null : config.copy(config.getValue());
        this.title = title;

        int stringWidth = currentValue == null ? 0 : getGui().getTheme().getStringWidth(config.getStringFromValue(currentValue));
        width = Math.min(getWindow().getGuiScaledWidth() / 2, stringWidth + config.getExtraEditorWidth());

        titleField = new TextField(this).addFlags(Theme.SHADOW).setText(Objects.requireNonNullElse(title, Component.empty()));
        titleField.setSize(0, 0);
        accept = new SimpleButton(this, Component.translatable("gui.accept"), Icons.ACCEPT, this::onAccepted);
        cancel = new SimpleButton(this, Component.translatable("gui.cancel"), Icons.CANCEL, this::onCancelled);
        textBox = new EditField();
    }

    public EditStringConfigOverlay<T> atPosition(int x, int y) {
        setPos(x, y);
        return this;
    }

    public EditStringConfigOverlay<T> atMousePosition() {
        int absX = Math.min(getMouseX(), getWindow().getGuiScaledWidth() - width);
        int absY = Math.min(getMouseY(), getWindow().getGuiScaledHeight() - height);
        return atPosition(absX - parent.getX(), absY - parent.getY() - (int) parent.getScrollY());
    }

    @Override
    public void addWidgets() {
        if (title != null) {
            add(titleField);
        }
        add(textBox);
        if(addAcceptCancelButtons) {
            add(accept);
            add(cancel);
        }
    }

    @Override
    public void alignWidgets() {
        if (title != null) {
            titleField.setPosAndSize(2, 2, width, getGui().getTheme().getFontHeight() + 4);
        }
        textBox.setPosAndSize(2, titleField.getHeight() + 1, width - 36, 14);
        if(addAcceptCancelButtons) {
            accept.setPos(textBox.width + 2, textBox.getPosY());
            cancel.setPos(accept.getPosX() + accept.width + 2, textBox.getPosY());
        }


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

    protected void onAccepted(Button btn, MouseButton mb) {
        if (textBox.isTextValid()) {
            config.updateValue(currentValue);
            callback.save(true);
            getGui().popModalPanel();
        }
    }

    protected void onCancelled(Button btn, MouseButton mb) {
        callback.save(false);
        getGui().popModalPanel();
    }

    /**
     * @param addAcceptCancelButtons if true, accept and cancel buttons will be added to the overlay
     */
    public void setAddAcceptCancelButtons(boolean addAcceptCancelButtons) {
        this.addAcceptCancelButtons = addAcceptCancelButtons;
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

    protected class EditField extends TextBox {
        public EditField() {
            super(EditStringConfigOverlay.this);

            setText(config.getStringFromValue(EditStringConfigOverlay.this.currentValue));
            textColor = Color4I.WHITE; //config.getColor(currentValue);
            setCursorPos(0);
            setSelectionPos(getText().length());
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

            accept.setIcon(isTextValid() ? Icons.ACCEPT : Icons.ACCEPT.withTint(Color4I.DARK_GRAY.withAlpha(160)));
        }

        @Override
        public void onEnterPressed() {
            if (config.getCanEdit()) {
                accept.onClicked(MouseButton.LEFT);
            }
        }

        @Override
        public boolean mouseScrolled(double scroll) {
            if (currentValue == null) {
                return super.mouseScrolled(scroll);
            }
            return config.scrollValue(currentValue, scroll > 0).map(v -> {
                textBox.setText(config.getStringFromValue(v));
                textBox.setSelectionPos(textBox.getCursorPos());
                return true;
            }).orElse(super.mouseScrolled(scroll));
        }
    }
}
