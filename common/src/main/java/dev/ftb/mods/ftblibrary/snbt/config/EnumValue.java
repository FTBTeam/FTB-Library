package dev.ftb.mods.ftblibrary.snbt.config;

import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.config.NameMap;
import dev.ftb.mods.ftblibrary.snbt.SNBTCompoundTag;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.nbt.StringTag;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class EnumValue<T> extends BaseValue<T> {
	private final NameMap<T> nameMap;
	private T value;

	EnumValue(SNBTConfig c, String n, NameMap<T> nm) {
		super(c, n, nm.defaultValue);
		nameMap = nm;
		value = nameMap.defaultValue;
	}

	public T get() {
		return value;
	}

	@Override
	public void write(SNBTCompoundTag tag) {
		List<String> s = new ArrayList<>(comment);
		s.add("Default: \"" + nameMap.getName(defaultValue) + "\"");
		s.add("Valid values: " + nameMap.keys.stream().map(StringTag::quoteAndEscape).collect(Collectors.joining(", ")));
		tag.comment(key, String.join("\n", s));
		tag.putString(key, nameMap.getName(value));
	}

	@Override
	public void read(SNBTCompoundTag tag) {
		value = nameMap.get(tag.getString(key));
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void createClientConfig(ConfigGroup group) {
		group.addEnum(key, value, v -> value = v, nameMap);
	}
}
