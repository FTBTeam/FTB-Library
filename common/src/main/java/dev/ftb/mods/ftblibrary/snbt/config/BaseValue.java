package dev.ftb.mods.ftblibrary.snbt.config;

import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.snbt.SNBTCompoundTag;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class BaseValue<T> implements Comparable<BaseValue<T>> {
	public final SNBTConfig parent;
	public final String key;
	public final T defaultValue;
	boolean excluded;

	List<String> comment = new ArrayList<>(0);

	BaseValue(@Nullable SNBTConfig c, String n, T def) {
		parent = c;
		key = n;
		defaultValue = def;
	}

	@Override
	public String toString() {
		if (parent == null) {
			return key;
		}

		return parent + "/" + key;
	}

	public <E extends BaseValue<T>> E comment(String... s) {
		comment.addAll(Arrays.asList(s));
		return (E) this;
	}

	public <E extends BaseValue<T>> E excluded() {
		excluded = true;
		return (E) this;
	}

	public abstract void write(SNBTCompoundTag tag);

	public abstract void read(SNBTCompoundTag tag);

	private int getOrder() {
		return this instanceof SNBTConfig ? 1 : 0;
	}

	@Override
	public int compareTo(BaseValue<T> o) {
		int i = Integer.compare(getOrder(), o.getOrder());
		return i == 0 ? key.compareToIgnoreCase(o.key) : i;
	}

	@Environment(EnvType.CLIENT)
	public void createClientConfig(ConfigGroup group) {
	}
}
