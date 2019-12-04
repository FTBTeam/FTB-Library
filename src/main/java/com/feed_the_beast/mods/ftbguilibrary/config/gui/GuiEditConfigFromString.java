package com.feed_the_beast.mods.ftbguilibrary.config.gui;

import com.feed_the_beast.mods.ftbguilibrary.config.ConfigFromString;
import com.feed_the_beast.mods.ftbguilibrary.config.ConfigValue;
import com.feed_the_beast.mods.ftbguilibrary.icon.Icon;
import com.feed_the_beast.mods.ftbguilibrary.utils.Key;
import com.feed_the_beast.mods.ftbguilibrary.utils.MouseButton;
import com.feed_the_beast.mods.ftbguilibrary.widget.Button;
import com.feed_the_beast.mods.ftbguilibrary.widget.GuiBase;
import com.feed_the_beast.mods.ftbguilibrary.widget.SimpleTextButton;
import com.feed_the_beast.mods.ftbguilibrary.widget.TextBox;
import com.feed_the_beast.mods.ftbguilibrary.widget.WidgetType;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resources.I18n;

import java.util.Optional;
import java.util.function.Consumer;

public class GuiEditConfigFromString<T> extends GuiBase
{
	public static <E> void open(ConfigValue<E> type, E value, Consumer<E> setter, E defaultValue, Runnable callback)
	{
		new GuiEditConfigFromString<E>(type.init(value, setter, defaultValue), callback).openGui();
	}

	private final ConfigFromString<T> value;
	private final Runnable callback;
	private T initialValue;

	private final Button buttonCancel, buttonAccept;
	private final TextBox textBox;

	public GuiEditConfigFromString(ConfigFromString<T> val, Runnable c)
	{
		setSize(230, 54);
		value = val;
		callback = c;
		initialValue = val.current;

		int bsize = width / 2 - 10;

		buttonCancel = new SimpleTextButton(this, I18n.format("gui.cancel"), Icon.EMPTY)
		{
			@Override
			public void onClicked(MouseButton button)
			{
				playClickSound();
				value.setCurrentValue(initialValue);
				callback.run();
			}

			@Override
			public boolean renderTitleInCenter()
			{
				return true;
			}
		};

		buttonCancel.setPosAndSize(8, height - 24, bsize, 16);

		buttonAccept = new SimpleTextButton(this, I18n.format("gui.accept"), Icon.EMPTY)
		{
			@Override
			public void onClicked(MouseButton button)
			{
				playClickSound();
				callback.run();
			}

			@Override
			public WidgetType getWidgetType()
			{
				return value.canEdit && textBox.isTextValid() ? super.getWidgetType() : WidgetType.DISABLED;
			}

			@Override
			public boolean renderTitleInCenter()
			{
				return true;
			}
		};

		buttonAccept.setPosAndSize(width - bsize - 8, height - 24, bsize, 16);

		textBox = new TextBox(this)
		{
			@Override
			public boolean allowInput()
			{
				return value.canEdit;
			}

			@Override
			public boolean isValid(String txt)
			{
				Optional<T> v = value.getValueFromString(txt);
				return v.isPresent() && value.isValid(v.get());
			}

			@Override
			public void onTextChanged()
			{
				Optional<T> v = value.getValueFromString(getText());

				if (v.isPresent() && value.setCurrentValue(v.get()))
				{
					textColor = value.getColor(value.current);
				}
			}

			@Override
			public void onEnterPressed()
			{
				if (value.canEdit)
				{
					buttonAccept.onClicked(MouseButton.LEFT);
				}
			}
		};

		textBox.setPosAndSize(8, 8, width - 16, 16);
		textBox.setText(value.getStringFromValue(value.current));
		textBox.textColor = value.getColor(value.current);
		textBox.setCursorPosition(textBox.getText().length());
		textBox.setFocused(true);
	}

	@Override
	public boolean onClosedByKey(Key key)
	{
		if (super.onClosedByKey(key))
		{
			callback.run();
			return false;
		}

		return false;
	}

	@Override
	public void addWidgets()
	{
		add(buttonCancel);
		add(buttonAccept);
		add(textBox);
	}

	@Override
	public boolean doesGuiPauseGame()
	{
		Screen screen = getPrevScreen();
		return screen != null && screen.isPauseScreen();
	}
}