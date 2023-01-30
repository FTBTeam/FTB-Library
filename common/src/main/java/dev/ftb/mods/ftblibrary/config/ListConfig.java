package dev.ftb.mods.ftblibrary.config;

import dev.ftb.mods.ftblibrary.config.ui.EditConfigListScreen;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LatvianModder
 */
public class ListConfig<E, CV extends ConfigValue<E>> extends ConfigValue<List<E>> {
	public static final Component EMPTY_LIST = Component.literal("[]");
	public static final Component NON_EMPTY_LIST = Component.literal("[...]");

	public static final Color4I COLOR = Color4I.rgb(0xFFAA49);

	private final CV type;

	public ListConfig(CV t) {
		type = t;
	}

	public CV getType() {
		return type;
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
		if (!getValue().isEmpty()) {
			l.add(info("List"));

			for (var value : getValue()) {
				l.add(type.getStringForGUI(value));
			}

			if (!getDefaultValue().isEmpty()) {
				l.blankLine();
			}
		}

		if (!getDefaultValue().isEmpty()) {
			l.add(info("Default"));
			for (var value : getDefaultValue()) {
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
				Component.translatable("ftblibrary.gui.listSize1") :
				Component.translatable("ftblibrary.gui.listSize", v.size());
		return Component.literal("[ ").append(main.withStyle(ChatFormatting.ITALIC, ChatFormatting.GRAY)).append(" ]");
	}
}
