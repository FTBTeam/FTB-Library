package dev.ftb.mods.ftblibrary.snbt.config;

import dev.ftb.mods.ftblibrary.snbt.SNBTCompoundTag;
import me.shedaniel.architectury.utils.NbtType;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class StringListValue extends BaseValue<List<String>> {
	private List<String> value;

	StringListValue(SNBTConfig c, String n, List<String> def) {
		super(c, n, def);
		value = def;
	}

	public List<String> get() {
		return value;
	}

	@Override
	public void write(SNBTCompoundTag tag) {
		List<String> s = new ArrayList<>(comment);
		s.add("Default: [" + defaultValue.stream().map(StringTag::quoteAndEscape).collect(Collectors.joining(", ")) + "]");
		tag.comment(key, String.join("\n", s));

		ListTag stag = new ListTag();

		for (String s1 : value) {
			stag.add(StringTag.valueOf(s1));
		}

		tag.put(key, stag);
	}

	@Override
	public void read(SNBTCompoundTag tag) {
		Tag stag = tag.get(key);

		if (stag instanceof ListTag && (((ListTag) stag).isEmpty() || ((ListTag) stag).getElementType() == NbtType.STRING)) {
			value = new ArrayList<>();

			for (int i = 0; i < ((ListTag) stag).size(); i++) {
				value.add(((ListTag) stag).getString(i));
			}
		}
	}
}
