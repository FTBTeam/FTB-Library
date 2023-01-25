package dev.ftb.mods.ftblibrary.config.ui;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.ftb.mods.ftblibrary.config.ConfigCallback;
import dev.ftb.mods.ftblibrary.config.ConfigFromString;
import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.ui.*;
import dev.ftb.mods.ftblibrary.ui.input.Key;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public class EditConfigFromStringScreen<T> extends BaseScreen {
	private final ConfigFromString<T> config;
	private final ConfigCallback callback;
	private final Button buttonCancel, buttonAccept;
	private final TextBox textBox;

	private T currentValue;
	private Component title = Component.empty();

	public static <E> void open(ConfigFromString<E> type, @Nullable E value, @Nullable E defaultValue, Component title, ConfigCallback callback) {
		var group = new ConfigGroup("group");
		group.add("value", type, value, e -> { }, defaultValue);
		new EditConfigFromStringScreen<>(type, callback).setTitle(title).openGui();
	}

	public static <E> void open(ConfigFromString<E> type, @Nullable E value, @Nullable E defaultValue, ConfigCallback callback) {
		open(type, value, defaultValue, Component.empty(), callback);
	}

	public EditConfigFromStringScreen(ConfigFromString<T> c, ConfigCallback cb) {
		setSize(230, 54);
		config = c;
		callback = cb;
		currentValue = config.getValue() == null ? null : config.copy(config.getValue());

		var bsize = width / 2 - 10;

		buttonCancel = new SimpleTextButton(this, Component.translatable("gui.cancel"), Icon.EMPTY) {
			@Override
			public void onClicked(MouseButton button) {
				playClickSound();
				doCancel();
			}

			@Override
			public boolean renderTitleInCenter() {
				return true;
			}
		};

		buttonCancel.setPosAndSize(8, height - 24, bsize, 16);

		buttonAccept = new SimpleTextButton(this, Component.translatable("gui.accept"), Icon.EMPTY) {
			@Override
			public void onClicked(MouseButton button) {
				playClickSound();
				doAccept();
			}

			@Override
			public WidgetType getWidgetType() {
				return config.getCanEdit() && textBox.isTextValid() ? super.getWidgetType() : WidgetType.DISABLED;
			}

			@Override
			public boolean renderTitleInCenter() {
				return true;
			}
		};

		buttonAccept.setPosAndSize(width - bsize - 8, height - 24, bsize, 16);

		textBox = new TextBox(this) {
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
				config.parse(t -> {
					currentValue = t;
					textColor = config.getColor(t);
				}, getText());
			}

			@Override
			public void onEnterPressed() {
				if (config.getCanEdit()) {
					buttonAccept.onClicked(MouseButton.LEFT);
				}
			}
		};

		textBox.setPosAndSize(8, 8, width - 16, 16);
		textBox.setText(config.getStringFromValue(currentValue));
		textBox.textColor = config.getColor(currentValue);
		textBox.setCursorPosition(textBox.getText().length());
		textBox.setFocused(true);
	}

	@Override
	public void drawForeground(PoseStack matrixStack, Theme theme, int x, int y, int w, int h) {
		super.drawForeground(matrixStack, theme, x, y, w, h);

		if (!title.equals(Component.empty())) {
			theme.drawString(matrixStack, title, getX() + (width / 2f), getY() - theme.getFontHeight() - 2, Color4I.WHITE, Theme.CENTERED);
		}
	}

	public EditConfigFromStringScreen<T> setTitle(Component title) {
		this.title = title;
		return this;
	}

	private void doAccept() {
		config.setCurrentValue(currentValue);
		callback.save(true);
	}

	private void doCancel() {
		callback.save(false);
	}

	@Override
	public boolean onClosedByKey(Key key) {
		if (super.onClosedByKey(key)) {
			if (key.esc()) {
				doCancel();
			} else {
				doAccept();
			}
			return true;
		}

		return false;
	}

	@Override
	public void addWidgets() {
		add(buttonCancel);
		add(buttonAccept);
		add(textBox);
	}

	@Override
	public boolean doesGuiPauseGame() {
		var screen = getPrevScreen();
		return screen != null && screen.isPauseScreen();
	}
}
