package dev.ftb.mods.ftblibrary.snbt;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * @author LatvianModder
 */
public class SNBTCompoundTag extends CompoundTag {
	public static SNBTCompoundTag of(@Nullable Tag tag) {
		if (tag instanceof SNBTCompoundTag) {
			return (SNBTCompoundTag) tag;
		} else if (tag instanceof CompoundTag) {
			SNBTCompoundTag tag1 = new SNBTCompoundTag();

			for (String s : ((CompoundTag) tag).getAllKeys()) {
				tag1.put(s, ((CompoundTag) tag).get(s));
			}

			return tag1;
		}

		return new SNBTCompoundTag();
	}

	private HashMap<String, SNBTTagProperties> properties;
	boolean singleLine;

	public SNBTCompoundTag() {
		super(new LinkedHashMap<>());
		singleLine = false;
	}

	private SNBTTagProperties getOrCreateProperties(String key) {
		if (properties == null) {
			properties = new HashMap<>();
		}

		SNBTTagProperties p = properties.get(key);

		if (p == null) {
			p = new SNBTTagProperties();
			properties.put(key, p);
		}

		return p;
	}

	SNBTTagProperties getProperties(String key) {
		if (properties != null) {
			SNBTTagProperties p = properties.get(key);

			if (p != null) {
				return p;
			}
		}

		return SNBTTagProperties.DEFAULT;
	}

	public void comment(String key, String... comment) {
		if (comment.length > 0) {
			comment(key, String.join("\n", comment));
		}
	}

	public void comment(String key, String comment) {
		String s = comment == null ? "" : comment.trim();

		if (!s.isEmpty()) {
			getOrCreateProperties(key).comment = comment;
		}
	}

	public String getComment(String key) {
		return getProperties(key).comment;
	}

	public void singleLine() {
		singleLine = true;
	}

	public void singleLine(String key) {
		getOrCreateProperties(key).singleLine = true;
	}

	@Override
	public void putBoolean(String key, boolean value) {
		getOrCreateProperties(key).valueType = value ? SNBTTagProperties.TYPE_TRUE : SNBTTagProperties.TYPE_FALSE;
		super.putBoolean(key, value);
	}

	@Override
	public SNBTCompoundTag getCompound(String string) {
		return of(get(string));
	}
}
