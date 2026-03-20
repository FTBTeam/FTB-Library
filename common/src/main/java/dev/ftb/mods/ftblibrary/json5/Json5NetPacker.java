package dev.ftb.mods.ftblibrary.json5;

import de.marhali.json5.Json5Array;
import de.marhali.json5.Json5Element;
import de.marhali.json5.Json5Object;
import de.marhali.json5.Json5Primitive;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;

/// Json5NetPacker provides a compact binary serialization format for Json5Element instances, suitable for network transmission.
/// The format is designed to be efficient while preserving the full fidelity of Json5 data.
///
/// It's important to note that comments are *not* preserved in this serialization.
public class Json5NetPacker {
    public static final StreamCodec<ByteBuf, Json5Element> CODEC = StreamCodec.of(
            Json5NetPacker::pack,
            Json5NetPacker::unpack
    );

    /// Denotes the type of the element in the byte stream.
    public enum Type {
        OBJECT(0),
        ARRAY(1),
        STRING(2),
        FLOAT(3),
        DOUBLE(4),
        INT(5),
        BIG_INT(6),
        BIG_DECIMAL(7),
        BOOLEAN(8),
        BINARY_NUMBER(9),
        HEX_NUMBER(10),
        OCTAL_NUMBER(11),
        INSTANT(12),
        LONG(13),
        NULL(14);

        private static final Type[] BY_ID = new Type[256];

        static {
            for (Type t : Type.values()) BY_ID[t.getId()] = t;
        }

        private final int id;

        Type(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }

        public static Type fromId(int id) {
            if (id < 0 || id >= BY_ID.length || BY_ID[id] == null)
                throw new IllegalArgumentException("Invalid type ID: " + id);
            return BY_ID[id];
        }
    }

    /// Packs a Json5Element into a ByteBuf using a compact binary format.
    ///
    /// @param buffer the ByteBuf to write the packed data into. This shouldn't need to be created as it should be provided
    ///               by the caller of the codec or by the network context
    /// @param json the Json5Element to pack
    public static void pack(ByteBuf buffer, Json5Element json) {
        packValue(json, buffer, 0);
    }

    private static void packValue(Json5Element json, ByteBuf buffer, int depthTracker) {
        if (depthTracker > 100) throw new IllegalStateException("Too much nesting");

        if (json.isJson5Object()) packObject(json.getAsJson5Object(), buffer, depthTracker);
        else if (json.isJson5Array()) packArray(json.getAsJson5Array(), buffer, depthTracker);
        else if (json.isJson5Null()) buffer.writeByte(Type.NULL.getId()); // Json5Null is NOT a Json5Primitive
        else if (json.isJson5Primitive()) packPrimitive(json.getAsJson5Primitive(), buffer);
        else throw new IllegalStateException("Unknown element type: " + json.getClass());
    }

    private static void packObject(Json5Object json, ByteBuf buffer, int depthTracker) {
        buffer.writeByte(Type.OBJECT.getId());
        buffer.writeInt(json.size());
        for (Map.Entry<String, Json5Element> entry : json.entrySet()) {
            byte[] keyBytes = entry.getKey().getBytes(StandardCharsets.UTF_8);
            buffer.writeInt(keyBytes.length);
            buffer.writeBytes(keyBytes);
            packValue(entry.getValue(), buffer, depthTracker + 1);
        }
    }

    private static void packArray(Json5Array array, ByteBuf buffer, int depthTracker) {
        buffer.writeByte(Type.ARRAY.getId());
        buffer.writeInt(array.size());
        for (Json5Element element : array) {
            packValue(element, buffer, depthTracker + 1);
        }
    }

    private static void writeString(ByteBuf buffer, String s) {
        byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
        buffer.writeInt(bytes.length);
        buffer.writeBytes(bytes);
    }

    private static String readString(ByteBuf buffer) {
        int length = buffer.readInt();
        byte[] bytes = new byte[length];
        buffer.readBytes(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    private static void packPrimitive(Json5Primitive primitive, ByteBuf buffer) {
        // Radix types must be checked before isNumber(), as isNumber() returns
        // true for hex/octal/binary values too and would swallow them as plain ints
        if (primitive.isHexNumber()) {
            buffer.writeByte(Type.HEX_NUMBER.getId());
            writeString(buffer, primitive.getAsHexString());
        } else if (primitive.isOctalNumber()) {
            buffer.writeByte(Type.OCTAL_NUMBER.getId());
            writeString(buffer, primitive.getAsOctalString());
        } else if (primitive.isBinaryNumber()) {
            buffer.writeByte(Type.BINARY_NUMBER.getId());
            writeString(buffer, primitive.getAsBinaryString());
        } else if (primitive.isString()) {
            buffer.writeByte(Type.STRING.getId());
            writeString(buffer, primitive.getAsString());
        } else if (primitive.isNumber()) {
            var num = primitive.getAsRadixNumber().getNumber();
            switch (num) {
                case Integer i -> {
                    buffer.writeByte(Type.INT.getId());
                    buffer.writeInt(i);
                }
                case Long l -> {
                    buffer.writeByte(Type.LONG.getId());
                    buffer.writeLong(l);
                }
                case Float f -> {
                    buffer.writeByte(Type.FLOAT.getId());
                    buffer.writeFloat(f);
                }
                case Double d -> {
                    buffer.writeByte(Type.DOUBLE.getId());
                    buffer.writeDouble(d);
                }
                case BigInteger bigInteger -> {
                    buffer.writeByte(Type.BIG_INT.getId());
                    byte[] bigIntBytes = bigInteger.toByteArray();
                    buffer.writeInt(bigIntBytes.length);
                    buffer.writeBytes(bigIntBytes);
                }
                case BigDecimal bigDecimal -> {
                    buffer.writeByte(Type.BIG_DECIMAL.getId());
                    writeString(buffer, bigDecimal.toString());
                }
                case null, default -> throw new IllegalStateException("Unknown number type: " + (num == null ? "null" : num.getClass()));
            }
        } else if (primitive.isBoolean()) {
            buffer.writeByte(Type.BOOLEAN.getId());
            buffer.writeBoolean(primitive.getAsBoolean());
        } else if (primitive.isInstant()) {
            buffer.writeByte(Type.INSTANT.getId());
            buffer.writeLong(primitive.getAsInstant().toEpochMilli());
        } else if (primitive.isJson5Null()) {
            buffer.writeByte(Type.NULL.getId());
        } else {
            throw new IllegalStateException("Unknown primitive type: " + primitive.getClass() + " value=" + primitive.getAsString());
        }
    }

    /// Unpacks a Json5Element from a ByteBuf that was packed using the pack() method. The buffer should be positioned at the start of the element data.
    /// Other data can be before or after the element but the buffer's next read should be the start of the element.
    ///
    /// @param buffer the ByteBuf to read the packed data from.
    /// @return the unpacked Json5Element
    public static Json5Element unpack(ByteBuf buffer) {
        return unpackValue(buffer, 0);
    }

    private static Json5Element unpackValue(ByteBuf buffer, int depthTracker) {
        if (depthTracker > 100) throw new IllegalStateException("Too much nesting");

        int typeId = buffer.readUnsignedByte();
        Type type = Type.fromId(typeId);

        return switch (type) {
            case OBJECT -> unpackObject(buffer, depthTracker);
            case ARRAY -> unpackArray(buffer, depthTracker);
            default -> unpackPrimitive(buffer, type);
        };
    }

    private static Json5Element unpackObject(ByteBuf buffer, int depthTracker) {
        var obj = new Json5Object();
        int size = buffer.readInt();
        for (int i = 0; i < size; i++) {
            String key = readString(buffer);
            Json5Element value = unpackValue(buffer, depthTracker + 1);
            obj.add(key, value);
        }
        return obj;
    }

    private static Json5Element unpackArray(ByteBuf buffer, int depthTracker) {
        var array = new Json5Array();
        int size = buffer.readInt();
        for (int i = 0; i < size; i++) {
            array.add(unpackValue(buffer, depthTracker + 1));
        }
        return array;
    }

    private static Json5Element unpackPrimitive(ByteBuf buffer, Type type) {
        return switch (type) {
            case STRING -> Json5Primitive.fromString(readString(buffer));
            case FLOAT -> Json5Primitive.fromNumber(buffer.readFloat());
            case DOUBLE -> Json5Primitive.fromNumber(buffer.readDouble());
            case INT -> Json5Primitive.fromNumber(buffer.readInt());
            case LONG -> Json5Primitive.fromNumber(buffer.readLong());
            case BIG_INT -> {
                int length = buffer.readInt();
                byte[] bigIntBytes = new byte[length];
                buffer.readBytes(bigIntBytes);
                yield Json5Primitive.fromNumber(new BigInteger(bigIntBytes));
            }
            case BIG_DECIMAL -> Json5Primitive.fromNumber(new BigDecimal(readString(buffer)));
            case BOOLEAN -> Json5Primitive.fromBoolean(buffer.readBoolean());
            case HEX_NUMBER -> unpackNumberLike(buffer, "0[xX]", 16);
            case OCTAL_NUMBER -> unpackNumberLike(buffer, "0[oO]", 8);
            case BINARY_NUMBER -> unpackNumberLike(buffer, "0[bB]", 2);
            case INSTANT -> Json5Primitive.fromInstant(Instant.ofEpochMilli(buffer.readLong()));
            case NULL -> Json5Primitive.fromNull();
            case OBJECT, ARRAY -> throw new IllegalStateException("Expected primitive type but got " + type);
        };
    }

    private static Json5Element unpackNumberLike(ByteBuf buf, String radixPrefix, int radix) {
        String numStr = readString(buf);
        boolean negative = numStr.startsWith("-");
        String stripped = numStr.replaceFirst("^[+-]", "").replaceFirst("^" + radixPrefix, "");
        long value = Long.parseLong(stripped, radix);
        return Json5Primitive.fromNumber(negative ? -value : value, radix);
    }
}
