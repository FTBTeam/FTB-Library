package dev.ftb.mods.ftblibrary.snbt.config;

import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.config.NameMap;
import dev.ftb.mods.ftblibrary.snbt.SNBT;
import dev.ftb.mods.ftblibrary.snbt.SNBTCompoundTag;
import dev.ftb.mods.ftblibrary.snbt.SNBTNet;
import dev.architectury.utils.NbtType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.network.FriendlyByteBuf;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public final class SNBTConfig extends BaseValue<List<BaseValue<?>>> {
	public static SNBTConfig create(String name) {
		return new SNBTConfig(null, name);
	}

	private SNBTConfig(SNBTConfig c, String n) {
		super(c, n, new ArrayList<>());
	}

	@Override
	public void write(SNBTCompoundTag tag) {
		if (parent == null) {
			tag.comment("", String.join("\n", comment));

			defaultValue.sort(null);

			for (BaseValue<?> value : defaultValue) {
				value.write(tag);
			}

			return;
		}

		tag.comment(key, String.join("\n", comment));

		SNBTCompoundTag tag1 = new SNBTCompoundTag();

		defaultValue.sort(null);

		for (BaseValue<?> value : defaultValue) {
			value.write(tag1);
		}

		tag.put(key, tag1);
	}

	@Override
	public void read(SNBTCompoundTag tag) {
		if (parent == null) {
			for (BaseValue<?> value : defaultValue) {
				if (tag.contains(value.key)) {
					value.read(tag);
				}
			}

			return;
		}

		if (tag.contains(key, NbtType.COMPOUND)) {
			SNBTCompoundTag tag1 = tag.getCompound(key);

			for (BaseValue<?> value : defaultValue) {
				if (tag1.contains(value.key)) {
					value.read(tag1);
				}
			}
		}
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void createClientConfig(ConfigGroup group) {
		if (parent == null) {
			for (BaseValue<?> value : defaultValue) {
				if (!value.excluded) {
					value.createClientConfig(group);
				}
			}
		} else {
			ConfigGroup g = group.getGroup(key);

			for (BaseValue<?> value : defaultValue) {
				if (!value.excluded) {
					value.createClientConfig(g);
				}
			}
		}
	}

	public void write(FriendlyByteBuf buf) {
		SNBTCompoundTag tag = new SNBTCompoundTag();
		write(tag);
		SNBTNet.write(buf, tag);
	}

	public void read(FriendlyByteBuf buf) {
		read(SNBTNet.readCompound(buf));
	}

	public void load(Path path) {
		SNBTCompoundTag tag = SNBT.read(path);

		if (tag != null) {
			read(tag);
		}

		save(path);
	}

	public void save(Path path) {
		Util.ioPool().execute(() -> saveNow(path));
	}

	public void saveNow(Path path) {
		if (parent != null) {
			parent.saveNow(path);
		} else {
			SNBTCompoundTag tag = new SNBTCompoundTag();
			write(tag);
			SNBT.write(path, tag);
		}
	}

	public void load(Path path, Path defaultPath, Supplier<String[]> comment) {
		if (Files.exists(defaultPath)) {
			if (!Files.exists(path)) {
				try {
					Files.copy(defaultPath, path);
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		} else {
			SNBTConfig defaultConfigFile = SNBTConfig.create(key);
			defaultConfigFile.comment(comment.get());
			defaultConfigFile.save(defaultPath);
		}

		load(path);
	}

	public <T extends BaseValue<?>> T add(T value) {
		defaultValue.add(value);
		return value;
	}

	public SNBTConfig getGroup(String key) {
		return add(new SNBTConfig(this, key));
	}

	public BooleanValue getBoolean(String key, boolean def) {
		return add(new BooleanValue(this, key, def));
	}

	public IntValue getInt(String key, int def) {
		return add(new IntValue(this, key, def));
	}

	public IntValue getInt(String key, int def, int min, int max) {
		return getInt(key, def).range(min, max);
	}

	public LongValue getLong(String key, long def) {
		return add(new LongValue(this, key, def));
	}

	public LongValue getLong(String key, long def, long min, long max) {
		return getLong(key, def).range(min, max);
	}

	public DoubleValue getDouble(String key, double def) {
		return add(new DoubleValue(this, key, def));
	}

	public DoubleValue getDouble(String key, double def, double min, double max) {
		return getDouble(key, def).range(min, max);
	}

	public StringValue getString(String key, String def) {
		return add(new StringValue(this, key, def));
	}

	public StringListValue getStringList(String key, List<String> def) {
		return add(new StringListValue(this, key, def));
	}

	public <T> EnumValue<T> getEnum(String key, NameMap<T> nameMap) {
		return add(new EnumValue<>(this, key, nameMap));
	}

	public IntArrayValue getIntArray(String key, int[] def) {
		return add(new IntArrayValue(this, key, def));
	}
}