package dev.ftb.mods.ftblibrary.snbt;

import net.minecraft.nbt.*;
import org.jetbrains.annotations.Nullable;

import java.util.*;


public class SNBTCompoundTag extends CompoundTag {
	public static SNBTCompoundTag of(@Nullable Tag tag) {
		if (tag instanceof SNBTCompoundTag) {
			return (SNBTCompoundTag) tag;
		} else if (tag instanceof CompoundTag) {
			var tag1 = new SNBTCompoundTag();

			for (var s : ((CompoundTag) tag).getAllKeys()) {
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

	SNBTTagProperties getOrCreateProperties(String key) {
		if (properties == null) {
			properties = new HashMap<>();
		}

		var p = properties.get(key);

		if (p == null) {
			p = new SNBTTagProperties();
			properties.put(key, p);
		}

		return p;
	}

	SNBTTagProperties getProperties(String key) {
		if (properties != null) {
			var p = properties.get(key);

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
		var s = comment == null ? "" : comment.trim();

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

	public boolean isBoolean(String key) {
		var t = getProperties(key).valueType;
		return t == SNBTTagProperties.TYPE_TRUE || t == SNBTTagProperties.TYPE_FALSE;
	}

	@Override
	public SNBTCompoundTag getCompound(String string) {
		return of(get(string));
	}

	public void putNumber(String key, Number number) {
		if (number instanceof Double) {
			putDouble(key, number.doubleValue());
		} else if (number instanceof Float) {
			putFloat(key, number.floatValue());
		} else if (number instanceof Long) {
			putLong(key, number.longValue());
		} else if (number instanceof Integer) {
			putInt(key, number.intValue());
		} else if (number instanceof Short) {
			putShort(key, number.shortValue());
		} else if (number instanceof Byte) {
			putByte(key, number.byteValue());
		} else if (number.toString().contains(".")) {
			putDouble(key, number.doubleValue());
		} else {
			putInt(key, number.intValue());
		}
	}

	public void putNull(String key) {
		put(key, EndTag.INSTANCE);
	}

	@Nullable
	public ListTag getNullableList(String key, byte type) {
		var tag = get(key);
		return tag instanceof ListTag && (((ListTag) tag).isEmpty() || type == 0 || ((ListTag) tag).getElementType() == type) ? (ListTag) tag : null;
	}

	@SuppressWarnings("unchecked")
	public <T extends Tag> List<T> getList(String key, Class<T> type) {
		var tag = get(key);

		if (!(tag instanceof CollectionTag<?> l)) {
			return Collections.emptyList();
		}

		if (l.isEmpty()) {
			return Collections.emptyList();
		}

		List<T> list = new ArrayList<>(l.size());

		for (Tag t : l) {
			if (type.isAssignableFrom(t.getClass())) {
				list.add((T) t);
			}
		}

		return list;
	}
}
