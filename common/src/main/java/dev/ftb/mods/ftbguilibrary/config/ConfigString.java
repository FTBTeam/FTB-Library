package dev.ftb.mods.ftbguilibrary.config;

import dev.ftb.mods.ftbguilibrary.icon.Color4I;
import dev.ftb.mods.ftbguilibrary.utils.TooltipList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.regex.Pattern;

/**
 * @author LatvianModder
 */
public class ConfigString extends ConfigFromString<String> {
	public static final Color4I COLOR = Color4I.rgb(0xFFAA49);

	public final Pattern pattern;

	public ConfigString(@Nullable Pattern p) {
		pattern = p;
		defaultValue = "";
		value = "";
	}

	public ConfigString() {
		this(null);
	}

	@Override
	public Color4I getColor(@Nullable String v) {
		return COLOR;
	}

	@Override
	public boolean parse(@Nullable Consumer<String> callback, String string) {
		if (pattern == null || pattern.matcher(string).matches()) {
			if (callback != null) {
				callback.accept(string);
			}

			return true;
		}

		return false;
	}

	@Override
	public Component getStringForGUI(@Nullable String v) {
		return v == null ? NULL_TEXT : new TextComponent('"' + v + '"');
	}

	@Override
	public void addInfo(TooltipList list) {
		super.addInfo(list);

		if (pattern != null) {
			list.add(info("Regex", pattern.pattern()));
		}
	}
}