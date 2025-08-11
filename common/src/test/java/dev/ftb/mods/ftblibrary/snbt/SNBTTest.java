package dev.ftb.mods.ftblibrary.snbt;

import io.netty.buffer.Unpooled;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class SNBTTest {
    @Test
    void testWriteAndReadbackString() {
        var tag = makeTestCompound();

        var lines = SNBT.writeLines(tag);
        CompoundTag reverseTag = SNBT.readLines(lines);

        assertEquals(tag, reverseTag, "Reverse tag test");
    }

    @Test
    void testWriteAndReadbackNet() {
        var tag = makeTestCompound();

        var byteBuf = new FriendlyByteBuf(Unpooled.buffer());
        ByteBufCodecs.COMPOUND_TAG.encode(byteBuf, tag);
        byteBuf.setIndex(0, byteBuf.capacity());
        var netTag = ByteBufCodecs.COMPOUND_TAG.decode(byteBuf);

        assertEquals(tag, netTag, "Network IO test");
    }

    @Test
    void testReadResourceStream() throws IOException {
        try (var stream = SNBTTest.class.getResourceAsStream("/snbt_test.snbt")) {
            assertNotNull(stream);

            CompoundTag newParserTest = SNBT.readLines(new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).lines().collect(Collectors.toList()));

            var lines2 = SNBT.writeLines(newParserTest);
            CompoundTag newParserTest2 = SNBT.readLines(lines2);

            assertEquals(newParserTest, newParserTest2, "New parser reverse tag test");

            assertEquals("value", newParserTest.getString("test_string").orElseThrow(), "getString");
            assertTrue(newParserTest.getBoolean("testBool").orElseThrow(), "getBoolean");
            assertEquals(1234, newParserTest.getInt("testInt").orElseThrow(), "getInt");
            assertEquals((short) 49, newParserTest.getShort("testShort").orElseThrow(), "getShort");
            assertEquals(304993938434993L, newParserTest.getLong("testLong").orElseThrow(), "getShort");
            assertEquals(49, newParserTest.getIntArray("intArray").orElseThrow()[1], "getIntArray");
            assertEquals(49, newParserTest.getByteArray("byteArray").orElseThrow()[1], "getByteArray");
            assertEquals(-34348L, newParserTest.getLongArray("longArray").orElseThrow()[1], "getLongArray");
            assertTrue(Double.isInfinite(newParserTest.getDouble("testDouble").orElseThrow()), "getDouble (infinity)");
            assertEquals("c $##@! 'string' 3", newParserTest.getList("testList").orElseThrow().getString(2).orElseThrow(), "getList (string)");

            assertFalse(newParserTest.contains("missingField"), "check for missing field");

            CompoundTag subTag = newParserTest.getCompound("testCompound").orElseThrow();
            assertNotNull(subTag, "testCompound presence");
            assertEquals(5, subTag.getInt("s1").orElseThrow(), "testCompound integer");
        }
    }

    private SNBTCompoundTag makeTestCompound() {
        var tag = new SNBTCompoundTag();
        tag.comment("hi", "Hello (Boolean test)");
        tag.putBoolean("hi", false);

        var messages = new ListTag();

        for (var i = 0; i < 3; i++) {
            var message = new SNBTCompoundTag();
            message.singleLine();
            message.putString("sender", "LatvianModder");
            message.putString("content", "Hello\nMy name is\tLat! Here's a slash: \\");
            message.putLong("date", System.currentTimeMillis());
            messages.add(message);
        }

        tag.comment("messages", "Message list", "", "This comment is in multiple lines!", "");
        tag.put("messages", messages);

        tag.comment("test_int", "Just an integer");
        tag.putInt("test_int", 30);

        ListTag list = new ListTag();
        list.add(DoubleTag.valueOf(0.0001d));
        list.add(DoubleTag.valueOf(5.3d));
        list.add(DoubleTag.valueOf(-4.56e-4d));
        tag.put("double_list", list);

        SNBTCompoundTag subTag = new SNBTCompoundTag();
        subTag.putInt("int_field", 1);
        subTag.putString("string_field", "hey");
        subTag.putBoolean("bool_field", true);
        tag.put("compound", subTag);

        return tag;
    }
}
