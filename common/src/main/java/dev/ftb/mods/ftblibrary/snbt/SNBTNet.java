package dev.ftb.mods.ftblibrary.snbt;

import net.minecraft.nbt.*;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.Nullable;

public class SNBTNet {
	public static final ByteArrayTag EMPTY_BYTE_ARRAY = new ByteArrayTag(new byte[0]);
	public static final IntArrayTag EMPTY_INT_ARRAY = new IntArrayTag(new int[0]);
	public static final LongArrayTag EMPTY_LONG_ARRAY = new LongArrayTag(new long[0]);

	public static void write(FriendlyByteBuf buf, @Nullable Tag tag) {
        switch (tag) {
            case ByteTag byteTag -> buf.writeByte(byteTag.getAsByte());
            case ShortTag shortTag -> buf.writeShort(shortTag.getAsShort());
            case IntTag intTag -> buf.writeInt(intTag.getAsInt());
            case LongTag longTag -> buf.writeLong(longTag.getAsLong());
            case FloatTag floatTag -> buf.writeFloat(floatTag.getAsFloat());
            case DoubleTag doubleTag -> buf.writeDouble(doubleTag.getAsDouble());
            case ByteArrayTag byteTags -> writeByteArray(buf, byteTags);
            case StringTag stringTag -> buf.writeUtf(stringTag.getAsString(), Short.MAX_VALUE);
            case ListTag tags -> writeList(buf, tags);
            case CompoundTag compoundTag -> writeCompound(buf, SNBTCompoundTag.of(compoundTag));
            case IntArrayTag intTags -> writeIntArray(buf, intTags);
            case LongArrayTag longTags -> writeLongArray(buf, longTags);
            case null, default -> {
            }
        }
	}

	public static void writeCompound(FriendlyByteBuf buf, @Nullable SNBTCompoundTag tag) {
		if (tag == null) {
			buf.writeVarInt(-1);
			return;
		}

		buf.writeVarInt(tag.getAllKeys().size());

		for (var s : tag.getAllKeys()) {
			buf.writeUtf(s, Short.MAX_VALUE);
			buf.writeByte(tag.get(s).getId());
			write(buf, tag.get(s));
		}
	}

	public static void writeList(FriendlyByteBuf buf, @Nullable ListTag tag) {
		if (tag == null) {
			buf.writeVarInt(-1);
			return;
		}

		buf.writeVarInt(tag.size());

		if (!tag.isEmpty()) {
			buf.writeByte(tag.getElementType());

			for (var value : tag) {
				write(buf, value);
			}
		}
	}

	public static void writeByteArray(FriendlyByteBuf buf, @Nullable ByteArrayTag tag) {
		if (tag == null) {
			buf.writeVarInt(-1);
			return;
		}

		buf.writeVarInt(tag.size());

		for (var v : tag.getAsByteArray()) {
			buf.writeByte(v);
		}
	}

	public static void writeIntArray(FriendlyByteBuf buf, @Nullable IntArrayTag tag) {
		if (tag == null) {
			buf.writeVarInt(-1);
			return;
		}

		buf.writeVarInt(tag.size());

		for (var v : tag.getAsIntArray()) {
			buf.writeInt(v);
		}
	}

	public static void writeLongArray(FriendlyByteBuf buf, @Nullable LongArrayTag tag) {
		if (tag == null) {
			buf.writeVarInt(-1);
			return;
		}

		buf.writeVarInt(tag.size());

		for (var v : tag.getAsLongArray()) {
			buf.writeLong(v);
		}
	}

	@Nullable
	public static Tag read(byte type, FriendlyByteBuf buf) {
		return switch (type) {
			case Tag.TAG_END -> EndTag.INSTANCE;
			case Tag.TAG_BYTE -> ByteTag.valueOf(buf.readByte());
			case Tag.TAG_SHORT -> ShortTag.valueOf(buf.readShort());
			case Tag.TAG_INT -> IntTag.valueOf(buf.readInt());
			case Tag.TAG_LONG -> LongTag.valueOf(buf.readLong());
			case Tag.TAG_FLOAT -> FloatTag.valueOf(buf.readFloat());
			case Tag.TAG_DOUBLE -> DoubleTag.valueOf(buf.readDouble());
			case Tag.TAG_BYTE_ARRAY -> readByteArray(buf);
			case Tag.TAG_STRING -> StringTag.valueOf(buf.readUtf(Short.MAX_VALUE));
			case Tag.TAG_LIST -> readList(buf);
			case Tag.TAG_COMPOUND -> readCompound(buf);
			case Tag.TAG_INT_ARRAY -> readIntArray(buf);
			case Tag.TAG_LONG_ARRAY -> readLongArray(buf);
			default -> null;
		};
	}

	@Nullable
	public static SNBTCompoundTag readCompound(FriendlyByteBuf buf) {
		var len = buf.readVarInt();

		if (len == -1) {
			return null;
		}

		var tag = new SNBTCompoundTag();

		for (var i = 0; i < len; i++) {
			var key = buf.readUtf(Short.MAX_VALUE);
			var type = buf.readByte();
			tag.put(key, read(type, buf));
		}

		return tag;
	}

	@Nullable
	public static ListTag readList(FriendlyByteBuf buf) {
		var len = buf.readVarInt();

		if (len == -1) {
			return null;
		} else if (len == 0) {
			return new ListTag();
		}

		var type = buf.readByte();

		var tag = new ListTag();

		for (var i = 0; i < len; i++) {
			tag.add(read(type, buf));
		}

		return tag;
	}

	public static ByteArrayTag readByteArray(FriendlyByteBuf buf) {
		var len = buf.readVarInt();

		if (len == -1) {
			return null;
		} else if (len == 0) {
			return EMPTY_BYTE_ARRAY;
		}

		var values = new byte[len];

		for (var i = 0; i < len; i++) {
			values[i] = buf.readByte();
		}

		return new ByteArrayTag(values);
	}

	public static IntArrayTag readIntArray(FriendlyByteBuf buf) {
		var len = buf.readVarInt();

		if (len == -1) {
			return null;
		} else if (len == 0) {
			return EMPTY_INT_ARRAY;
		}

		var values = new int[len];

		for (var i = 0; i < len; i++) {
			values[i] = buf.readInt();
		}

		return new IntArrayTag(values);
	}

	public static LongArrayTag readLongArray(FriendlyByteBuf buf) {
		var len = buf.readVarInt();

		if (len == -1) {
			return null;
		} else if (len == 0) {
			return EMPTY_LONG_ARRAY;
		}

		var values = new long[len];

		for (var i = 0; i < len; i++) {
			values[i] = buf.readLong();
		}

		return new LongArrayTag(values);
	}
}
