package dev.ftb.mods.ftblibrary.snbt;

import io.netty.buffer.Unpooled;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
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
        SNBTNet.write(byteBuf, tag);
        byteBuf.setIndex(0, byteBuf.capacity());
        var netTag = SNBTNet.readCompound(byteBuf);

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

            assertEquals(newParserTest.getString("test_string"), "value", "getString");
            assertTrue(newParserTest.getBoolean("testBool"), "getBoolean");
            assertEquals(newParserTest.getInt("testInt"), 1234, "getInt");
            assertEquals(newParserTest.getShort("testShort"), 49, "getShort");
            assertEquals(newParserTest.getLong("testLong"), 304993938434993L, "getShort");
            assertEquals(newParserTest.getIntArray("intArray")[1], 49, "getIntArray");
            assertEquals(newParserTest.getByteArray("byteArray")[1], 49, "getByteArray");
            assertEquals(newParserTest.getLongArray("longArray")[1], -34348L, "getLongArray");
            assertTrue(Double.isInfinite(newParserTest.getDouble("testDouble")), "getDouble (infinity)");
            assertEquals(newParserTest.getList("testList", Tag.TAG_STRING).getString(2), "c $##@! 'string' 3", "getList (string)");

            assertFalse(newParserTest.contains("missingField"), "check for missing field");

            CompoundTag subTag = newParserTest.getCompound("testCompound");
            assertNotNull(subTag, "testCompound presence");
            assertEquals(subTag.getInt("s1"), 5, "testCompound integer");
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
        tag.putNull("test_null");

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
