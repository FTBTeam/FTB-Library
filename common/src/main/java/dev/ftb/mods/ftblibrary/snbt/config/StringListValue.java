package dev.ftb.mods.ftblibrary.snbt.config;

import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.config.StringConfig;
import dev.ftb.mods.ftblibrary.snbt.SNBTCompoundTag;
import me.shedaniel.architectury.utils.NbtType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class StringListValue extends BaseValue<List<String>> {
	StringListValue(SNBTConfig c, String n, List<String> def) {
		super(c, n, def);
		super.set(new ArrayList<>(def));
	}

	@Override
	public void set(List<String> v) {
		get().clear();
		get().addAll(v);
	}

	@Override
	public void write(SNBTCompoundTag tag) {
		List<String> s = new ArrayList<>(comment);
		s.add("Default: [" + defaultValue.stream().map(StringTag::quoteAndEscape).collect(Collectors.joining(", ")) + "]");
		tag.comment(key, String.join("\n", s));

		ListTag stag = new ListTag();

		for (String s1 : get()) {
			stag.add(StringTag.valueOf(s1));
		}

		tag.put(key, stag);
	}

	@Override
	public void read(SNBTCompoundTag tag) {
		Tag stag = tag.get(key);

		if (stag instanceof ListTag && (((ListTag) stag).isEmpty() || ((ListTag) stag).getElementType() == NbtType.STRING)) {
			get().clear();

			for (int i = 0; i < ((ListTag) stag).size(); i++) {
				get().add(((ListTag) stag).getString(i));
			}
		}
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void createClientConfig(ConfigGroup group) {
		group.addList(key, get(), new StringConfig(null), "")
				.setCanEdit(enabled.getAsBoolean())
		;
	}
}
