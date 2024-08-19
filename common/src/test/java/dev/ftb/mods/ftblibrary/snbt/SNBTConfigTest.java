package dev.ftb.mods.ftblibrary.snbt;

import dev.ftb.mods.ftblibrary.config.Tristate;
import dev.ftb.mods.ftblibrary.snbt.config.*;
import net.minecraft.Util;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SNBTConfigTest {
    @Test
    void testConfig() {
        General.CONFIG.load(Paths.get("config/ftblibrary-config-test.snbt"));

        assertEquals(General.TEST_2.get(), true);
        assertEquals(General.TEST_3.get(), false);
        assertEquals(General.SUB_TEST_BOOLEAN.get(), false);
        assertEquals(General.SUB_TEST_INT.get(), 50);
        assertEquals(General.SUB_TEST_DOUBLE.get(), 0.5D);
        assertEquals(General.SUB_TEST_STRING.get(), "hello");
        assertEquals(General.STRING_LIST.get().get(1), "b");
        assertEquals(General.INT_ARRAY.get()[1], 59);

        Util.shutdownExecutors();
    }

    public interface General {
        SNBTConfig CONFIG = SNBTConfig.create("ftblibrary").comment("Config test", "Line two");
        BooleanValue TEST_1 = CONFIG.addBoolean("test_1", true);
        BooleanValue TEST_2 = CONFIG.addBoolean("test_2", true).comment("Boolean test 2");
        BooleanValue TEST_3 = CONFIG.addBoolean("test 3", false).comment("Boolean test 3");
        StringListValue STRING_LIST = CONFIG.addStringList("string_list", Arrays.asList("a", "b", "c"));

        SNBTConfig SUB_TEST = CONFIG.addGroup("sub_test").comment("Group comment test", "Line 2");
        BooleanValue SUB_TEST_BOOLEAN = SUB_TEST.addBoolean("boolean", false);
        IntValue SUB_TEST_INT = SUB_TEST.addInt("int", 50).range(30, Integer.MAX_VALUE);
        DoubleValue SUB_TEST_DOUBLE = SUB_TEST.addDouble("double", 0.5D).range(0D, 1D);
        StringValue SUB_TEST_STRING = SUB_TEST.addString("string", "hello");
        EnumValue<Tristate> TRISTATE_ENUM = SUB_TEST.addEnum("tristate_enum", Tristate.NAME_MAP);
        IntArrayValue INT_ARRAY = SUB_TEST.addIntArray("int_array", new int[]{3, 59});
    }
}
