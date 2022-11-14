package dev.ftb.mods.ftblibrary.config;

import dev.ftb.mods.ftblibrary.config.ui.EditConfigListScreen;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LatvianModder
 */
public class ListConfig<E, CV extends ConfigValue<E>> extends ConfigValue<List<E>> {
	public static final TextComponent EMPTY_LIST = new TextComponent("[]");
	public static final TextComponent NON_EMPTY_LIST = new TextComponent("[...]");

	public static final Color4I COLOR = Color4I.rgb(0xFFAA49);
	public final CV type;

	public ListConfig(CV t) {
		type = t;
	}

	@Override
	public List<E> copy(List<E> v) {
		List<E> list = new ArrayList<>(v.size());

		for (var value : v) {
			list.add(type.copy(value));
		}

		return list;
	}

	@Override
	public Color4I getColor(List<E> v) {
		return COLOR;
	}

	@Override
	public void addInfo(TooltipList l) {
		if (!value.isEmpty()) {
			l.add(info("List"));

			for (var value : value) {
				l.add(type.getStringForGUI(value));
			}

			if (!defaultValue.isEmpty()) {
				l.blankLine();
			}
		}

		if (!defaultValue.isEmpty()) {
			l.add(info("Default"));

			for (var value : defaultValue) {
				l.add(type.getStringForGUI(value));
			}
		}
	}

	@Override
	public void onClicked(MouseButton button, ConfigCallback callback) {
		new EditConfigListScreen<>(this, callback).openGui();
	}

	@Override
	public Component getStringForGUI(List<E> v) {
		return v == null ? NULL_TEXT : v.isEmpty() ? EMPTY_LIST : formatListSize(v);
	}

	private Component formatListSize(List<E> v) {
		MutableComponent main = v.size() == 1 ?
				new TranslatableComponent("ftblibrary.gui.listSize1") :
				new TranslatableComponent("ftblibrary.gui.listSize", v.size());
		return new TextComponent("[ ").append(main.withStyle(ChatFormatting.ITALIC, ChatFormatting.GRAY)).append(" ]");
	}
}