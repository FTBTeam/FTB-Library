package dev.ftb.mods.ftblibrary.config2;

import com.mojang.serialization.Codec;
import dev.ftb.mods.ftblibrary.FTBLibrary;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public enum ConfigManager {
    INSTANCE;

    private final List<FTBConfig> configs = new ArrayList<>();

    public static void main(String[] args) {
        FTBConfig testConfig = new FTBConfig(ResourceLocation.fromNamespaceAndPath(FTBLibrary.MOD_ID, "test_config"));

        testConfig.stringValue("string_value", "default_value")
            .comment("This is a test key", "It has multiple lines");

        testConfig.integerValue("int_value", 100);

        testConfig.boolValue("test_bool", true)
            .comment("This is a test bool", "It has multiple lines");

        testConfig.codecValue("codec_block_value",
            new BlockPos(0, 0, 0),
            BlockPos.CODEC
        );

        testConfig.doubleValue("test_double_value", 1.0);
        testConfig.floatValue("test_float_value", 1.0f);
        testConfig.longValue("test_long_value", 1L);
        testConfig.byteValue("test_byte_value", (byte) 1);
        testConfig.shortValue("test_short_value", (short) 1);
        testConfig.stringListValue("test_string_list", List.of("a", "b", "c"));
        testConfig.integerListValue("test_int_list", List.of(1, 2, 3));

        testConfig.mapValue("map_test_value", Map.of("a", 1, "b", 2), Codec.unboundedMap(Codec.STRING, Codec.INT));

        var group = testConfig.group("group_test");
        group.stringValue("group_string_value", "default_value");
        group.integerValue("group_int_value", 100);
        group.boolValue("group_test_bool", true)
            .comment("This is a test bool", "It has multiple lines");

        var group2 = group.group("group_test_2");
        group2.stringValue("group_2_string_value", "default_value");
        group2.integerValue("group_2_int_value", 100);
        group2.boolValue("group_2_test_bool", true)
            .comment("This is a test bool", "It has multiple lines");

        testConfig.writeToIo();
        testConfig.readFromIo();
    }

    public void registerConfig(FTBConfig config) {
        configs.add(config);
    }
}
