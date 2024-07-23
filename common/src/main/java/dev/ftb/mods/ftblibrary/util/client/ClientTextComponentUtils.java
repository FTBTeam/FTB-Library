package dev.ftb.mods.ftblibrary.util.client;

import dev.ftb.mods.ftblibrary.util.CustomComponentParser;
import dev.ftb.mods.ftblibrary.util.StringUtils;
import dev.ftb.mods.ftblibrary.util.TextComponentParser;
import net.minecraft.client.gui.Font;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static net.minecraft.network.chat.CommonComponents.ELLIPSIS;

public class ClientTextComponentUtils {
	private static final Function<String, Component> DEFAULT_STRING_TO_COMPONENT = ClientTextComponentUtils::defaultStringToComponent;

	private static final List<CustomComponentParser> CUSTOM_COMPONENT_PARSERS = new ArrayList<>();

	public static void addCustomParser(CustomComponentParser function) {
		CUSTOM_COMPONENT_PARSERS.add(function);
	}

	public static Component parse(String s) {
		return TextComponentParser.parse(s, DEFAULT_STRING_TO_COMPONENT);
	}

	private static Component defaultStringToComponent(String s) {
		if (s.isEmpty()) {
			return Component.empty();
		}

		if (s.indexOf(':') != -1) {
			var map = StringUtils.splitProperties(s);

			for (var parser : CUSTOM_COMPONENT_PARSERS) {
				var c = parser.parse(s, map);

				if (c != null && c != Component.EMPTY) {
					return c;
				}
			}

			if (map.containsKey("image")) {
				var c = ImageComponent.create(map.get("image"),
						Integer.parseInt(map.getOrDefault("width", "100")),
						Integer.parseInt(map.getOrDefault("height", "100")),
						ImageComponent.ImageAlign.byName(map.getOrDefault("align", "center")),
						map.getOrDefault("fit", "false").equals("true")
				);
				var output = MutableComponent.create(c);
				if (map.containsKey("text")) {
					output.withStyle(Style.EMPTY.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, parse(map.get("text")))));
				}

				return output;
			} else if (map.containsKey("open_url")) {
				return parse(map.get("text")).copy().withStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, map.get("open_url"))));
			}
		}

		return parse(I18n.get(s));
	}

	public static FormattedText ellipsize(Font font, FormattedText text, int maxWidth) {
		final int strWidth = font.width(text);
		final int ellipsisWidth = font.width(ELLIPSIS);
		if (strWidth > maxWidth) {
			return ellipsisWidth >= maxWidth ?
					font.substrByWidth(text, maxWidth) :
					FormattedText.composite(font.substrByWidth(text, maxWidth - ellipsisWidth), ELLIPSIS);
		}
		return text;
	}
}
