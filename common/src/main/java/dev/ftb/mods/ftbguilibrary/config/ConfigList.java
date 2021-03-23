package dev.ftb.mods.ftbguilibrary.config;

import dev.ftb.mods.ftbguilibrary.config.gui.GuiEditConfigList;
import dev.ftb.mods.ftbguilibrary.icon.Color4I;
import dev.ftb.mods.ftbguilibrary.utils.MouseButton;
import dev.ftb.mods.ftbguilibrary.utils.TooltipList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LatvianModder
 */
public class ConfigList<E, CV extends ConfigValue<E>> extends ConfigValue<List<E>> {
	public static final TextComponent EMPTY_LIST = new TextComponent("[]");
	public static final TextComponent NON_EMPTY_LIST = new TextComponent("[...]");

	public static final Color4I COLOR = Color4I.rgb(0xFFAA49);
	public final CV type;

	public ConfigList(CV t) {
		type = t;
	}

	@Override
	public List<E> copy(List<E> v) {
		List<E> list = new ArrayList<>(v.size());

		for (E value : v) {
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

			for (E value : value) {
				l.add(type.getStringForGUI(value));
			}

			if (!defaultValue.isEmpty()) {
				l.blankLine();
			}
		}

		if (!defaultValue.isEmpty()) {
			l.add(info("Default"));

			for (E value : defaultValue) {
				l.add(type.getStringForGUI(value));
			}
		}
	}

	@Override
	public void onClicked(MouseButton button, ConfigCallback callback) {
		new GuiEditConfigList<>(this, callback).openGui();
	}

	@Override
	public Component getStringForGUI(List<E> v) {
		return v == null ? NULL_TEXT : v.isEmpty() ? EMPTY_LIST : NON_EMPTY_LIST;
	}
}