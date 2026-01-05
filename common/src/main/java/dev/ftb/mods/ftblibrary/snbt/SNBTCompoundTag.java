package dev.ftb.mods.ftblibrary.snbt;

import net.minecraft.nbt.CollectionTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.EndTag;
import net.minecraft.nbt.Tag;
import org.jspecify.annotations.Nullable;

import java.util.*;


public class SNBTCompoundTag extends CompoundTag {
    boolean singleLine;
    private HashMap<String, SNBTTagProperties> properties;

    public SNBTCompoundTag() {
        super(new LinkedHashMap<>());
        singleLine = false;
    }

    public static SNBTCompoundTag of(@Nullable Tag tag) {
        if (tag instanceof SNBTCompoundTag) {
            return (SNBTCompoundTag) tag;
        } else if (tag instanceof CompoundTag c) {
            var tag1 = new SNBTCompoundTag();

            for (var s : c.keySet()) {
                tag1.put(s, c.get(s));
            }

            return tag1;
        }

        return new SNBTCompoundTag();
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

    // TODO: verify this isn't needed
//    @Override
//    public Optional<CompoundTag> getCompound(String string) {
//        return Optional.of(of(get(string)));
//    }

    public SNBTCompoundTag getAsSnbtComponent(String string) {
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

    @SuppressWarnings("unchecked")
    public <T extends Tag> List<T> getList(String key, Class<T> type) {
        var tag = get(key);

        if (!(tag instanceof CollectionTag l)) {
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

    /**
     * Recursively merge another compound tag into this one.
     * @param into the compound tag to merge into
     * @param from the compound tag to merge from
     * @param overwrite allow fields in the "from" compound tag to overwrite fields in the "into" compound tag
     * @return the "into" tag, modified in-place
     */
    public static CompoundTag merge(CompoundTag into, CompoundTag from, boolean overwrite) {
        for (String key : from.keySet()) {
            Tag subTag = from.get(key);
            if (subTag != null && (overwrite || !into.contains(key))) {
                if (subTag.getId() == Tag.TAG_COMPOUND) {
                    if (into.contains(key)) {
                        merge(into.getCompound(key).orElseThrow(), from, overwrite);
                    } else {
                        into.put(key, subTag.copy());
                    }
                } else {
                    into.put(key, subTag.copy());
                }
            }
        }

        return into;
    }
}
