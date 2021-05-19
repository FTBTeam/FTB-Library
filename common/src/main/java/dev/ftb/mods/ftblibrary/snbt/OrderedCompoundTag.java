package dev.ftb.mods.ftblibrary.snbt;

import net.minecraft.nbt.CompoundTag;

import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * @author LatvianModder
 */
public class OrderedCompoundTag extends CompoundTag {
	private HashMap<String, TagProperties> properties;
	public boolean singleLine;

	public OrderedCompoundTag() {
		super(new LinkedHashMap<>());
		singleLine = false;
	}

	private TagProperties getOrCreateProperties(String key) {
		if (properties == null) {
			properties = new HashMap<>();
		}

		TagProperties p = properties.get(key);

		if (p == null) {
			p = new TagProperties();
			properties.put(key, p);
		}

		return p;
	}

	TagProperties getProperties(String key) {
		if (properties != null) {
			TagProperties p = properties.get(key);

			if (p != null) {
				return p;
			}
		}

		return TagProperties.DEFAULT;
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
		getOrCreateProperties(key).booleanKey = true;
		super.putBoolean(key, value);
	}
}
