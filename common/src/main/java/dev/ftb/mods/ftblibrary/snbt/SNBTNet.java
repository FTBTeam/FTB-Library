package dev.ftb.mods.ftblibrary.snbt;

import dev.architectury.utils.NbtType;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.EndTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.ShortTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.Nullable;

public class SNBTNet {
	public static final ByteArrayTag EMPTY_BYTE_ARRAY = new ByteArrayTag(new byte[0]);
	public static final IntArrayTag EMPTY_INT_ARRAY = new IntArrayTag(new int[0]);
	public static final LongArrayTag EMPTY_LONG_ARRAY = new LongArrayTag(new long[0]);

	public static void write(FriendlyByteBuf buf, @Nullable Tag tag) {
		if (tag instanceof ByteTag) {
			buf.writeByte(((ByteTag) tag).getAsByte());
		} else if (tag instanceof ShortTag) {
			buf.writeShort(((ShortTag) tag).getAsShort());
		} else if (tag instanceof IntTag) {
			buf.writeInt(((IntTag) tag).getAsInt());
		} else if (tag instanceof LongTag) {
			buf.writeLong(((LongTag) tag).getAsLong());
		} else if (tag instanceof FloatTag) {
			buf.writeFloat(((FloatTag) tag).getAsFloat());
		} else if (tag instanceof DoubleTag) {
			buf.writeDouble(((DoubleTag) tag).getAsDouble());
		} else if (tag instanceof ByteArrayTag) {
			writeByteArray(buf, (ByteArrayTag) tag);
		} else if (tag instanceof StringTag) {
			buf.writeUtf(tag.getAsString(), Short.MAX_VALUE);
		} else if (tag instanceof ListTag) {
			writeList(buf, (ListTag) tag);
		} else if (tag instanceof CompoundTag) {
			writeCompound(buf, SNBTCompoundTag.of(tag));
		} else if (tag instanceof IntArrayTag) {
			writeIntArray(buf, (IntArrayTag) tag);
		} else if (tag instanceof LongArrayTag) {
			writeLongArray(buf, (LongArrayTag) tag);
		}
	}

	public static void writeCompound(FriendlyByteBuf buf, @Nullable SNBTCompoundTag tag) {
		if (tag == null) {
			buf.writeVarInt(-1);
			return;
		}

		buf.writeVarInt(tag.getAllKeys().size());

		for (String s : tag.getAllKeys()) {
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

			for (Tag value : tag) {
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

		for (byte v : tag.getAsByteArray()) {
			buf.writeByte(v);
		}
	}

	public static void writeIntArray(FriendlyByteBuf buf, @Nullable IntArrayTag tag) {
		if (tag == null) {
			buf.writeVarInt(-1);
			return;
		}

		buf.writeVarInt(tag.size());

		for (int v : tag.getAsIntArray()) {
			buf.writeInt(v);
		}
	}

	public static void writeLongArray(FriendlyByteBuf buf, @Nullable LongArrayTag tag) {
		if (tag == null) {
			buf.writeVarInt(-1);
			return;
		}

		buf.writeVarInt(tag.size());

		for (long v : tag.getAsLongArray()) {
			buf.writeLong(v);
		}
	}

	@Nullable
	public static Tag read(byte type, FriendlyByteBuf buf) {
		return switch (type) {
			case NbtType.END -> EndTag.INSTANCE;
			case NbtType.BYTE -> ByteTag.valueOf(buf.readByte());
			case NbtType.SHORT -> ShortTag.valueOf(buf.readShort());
			case NbtType.INT -> IntTag.valueOf(buf.readInt());
			case NbtType.LONG -> LongTag.valueOf(buf.readLong());
			case NbtType.FLOAT -> FloatTag.valueOf(buf.readFloat());
			case NbtType.DOUBLE -> DoubleTag.valueOf(buf.readDouble());
			case NbtType.BYTE_ARRAY -> readByteArray(buf);
			case NbtType.STRING -> StringTag.valueOf(buf.readUtf(Short.MAX_VALUE));
			case NbtType.LIST -> readList(buf);
			case NbtType.COMPOUND -> readCompound(buf);
			case NbtType.INT_ARRAY -> readIntArray(buf);
			case NbtType.LONG_ARRAY -> readLongArray(buf);
			default -> null;
		};
	}

	@Nullable
	public static SNBTCompoundTag readCompound(FriendlyByteBuf buf) {
		int len = buf.readVarInt();

		if (len == -1) {
			return null;
		}

		SNBTCompoundTag tag = new SNBTCompoundTag();

		for (int i = 0; i < len; i++) {
			String key = buf.readUtf(Short.MAX_VALUE);
			byte type = buf.readByte();
			tag.put(key, read(type, buf));
		}

		return tag;
	}

	@Nullable
	public static ListTag readList(FriendlyByteBuf buf) {
		int len = buf.readVarInt();

		if (len == -1) {
			return null;
		} else if (len == 0) {
			return new ListTag();
		}

		byte type = buf.readByte();

		ListTag tag = new ListTag();

		for (int i = 0; i < len; i++) {
			tag.add(read(type, buf));
		}

		return tag;
	}

	public static ByteArrayTag readByteArray(FriendlyByteBuf buf) {
		int len = buf.readVarInt();

		if (len == -1) {
			return null;
		} else if (len == 0) {
			return EMPTY_BYTE_ARRAY;
		}

		byte[] values = new byte[len];

		for (int i = 0; i < len; i++) {
			values[i] = buf.readByte();
		}

		return new ByteArrayTag(values);
	}

	public static IntArrayTag readIntArray(FriendlyByteBuf buf) {
		int len = buf.readVarInt();

		if (len == -1) {
			return null;
		} else if (len == 0) {
			return EMPTY_INT_ARRAY;
		}

		int[] values = new int[len];

		for (int i = 0; i < len; i++) {
			values[i] = buf.readInt();
		}

		return new IntArrayTag(values);
	}

	public static LongArrayTag readLongArray(FriendlyByteBuf buf) {
		int len = buf.readVarInt();

		if (len == -1) {
			return null;
		} else if (len == 0) {
			return EMPTY_LONG_ARRAY;
		}

		long[] values = new long[len];

		for (int i = 0; i < len; i++) {
			values[i] = buf.readLong();
		}

		return new LongArrayTag(values);
	}
}
