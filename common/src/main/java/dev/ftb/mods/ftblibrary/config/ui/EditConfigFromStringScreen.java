package dev.ftb.mods.ftblibrary.config.ui;

import dev.ftb.mods.ftblibrary.config.ConfigCallback;
import dev.ftb.mods.ftblibrary.config.ConfigFromString;
import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.ui.BaseScreen;
import dev.ftb.mods.ftblibrary.ui.Button;
import dev.ftb.mods.ftblibrary.ui.SimpleTextButton;
import dev.ftb.mods.ftblibrary.ui.TextBox;
import dev.ftb.mods.ftblibrary.ui.WidgetType;
import dev.ftb.mods.ftblibrary.ui.input.Key;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TranslatableComponent;
import org.jetbrains.annotations.Nullable;

public class EditConfigFromStringScreen<T> extends BaseScreen {
	public static <E> void open(ConfigFromString<E> type, @Nullable E value, @Nullable E defaultValue, ConfigCallback callback) {
		ConfigGroup group = new ConfigGroup("group");
		group.add("value", type, value, e -> {
		}, defaultValue);
		new EditConfigFromStringScreen<>(type, callback).openGui();
	}

	private final ConfigFromString<T> config;
	private final ConfigCallback callback;
	private T current;

	private final Button buttonCancel, buttonAccept;
	private final TextBox textBox;

	public EditConfigFromStringScreen(ConfigFromString<T> c, ConfigCallback cb) {
		setSize(230, 54);
		config = c;
		callback = cb;
		current = config.value == null ? null : config.copy(config.value);

		int bsize = width / 2 - 10;

		buttonCancel = new SimpleTextButton(this, new TranslatableComponent("gui.cancel"), Icon.EMPTY) {
			@Override
			public void onClicked(MouseButton button) {
				playClickSound();
				callback.save(false);
			}

			@Override
			public boolean renderTitleInCenter() {
				return true;
			}
		};

		buttonCancel.setPosAndSize(8, height - 24, bsize, 16);

		buttonAccept = new SimpleTextButton(this, new TranslatableComponent("gui.accept"), Icon.EMPTY) {
			@Override
			public void onClicked(MouseButton button) {
				playClickSound();
				config.setCurrentValue(current);
				callback.save(true);
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
					current = t;
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
		textBox.setText(config.getStringFromValue(current));
		textBox.textColor = config.getColor(current);
		textBox.setCursorPosition(textBox.getText().length());
		textBox.setFocused(true);
	}

	@Override
	public boolean onClosedByKey(Key key) {
		if (super.onClosedByKey(key)) {
			config.setCurrentValue(current);
			callback.save(true);
			return false;
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
		Screen screen = getPrevScreen();
		return screen != null && screen.isPauseScreen();
	}
}