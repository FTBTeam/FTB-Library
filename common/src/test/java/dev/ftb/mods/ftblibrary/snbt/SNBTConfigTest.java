package dev.ftb.mods.ftblibrary.snbt;

import dev.ftb.mods.ftblibrary.config.Tristate;
import dev.ftb.mods.ftblibrary.snbt.config.BooleanValue;
import dev.ftb.mods.ftblibrary.snbt.config.DoubleValue;
import dev.ftb.mods.ftblibrary.snbt.config.EnumValue;
import dev.ftb.mods.ftblibrary.snbt.config.IntArrayValue;
import dev.ftb.mods.ftblibrary.snbt.config.IntValue;
import dev.ftb.mods.ftblibrary.snbt.config.SNBTConfig;
import dev.ftb.mods.ftblibrary.snbt.config.StringListValue;
import dev.ftb.mods.ftblibrary.snbt.config.StringValue;
import net.minecraft.Util;

import java.nio.file.Paths;
import java.util.Arrays;

public class SNBTConfigTest {
	public interface General {
		SNBTConfig CONFIG = SNBTConfig.create("ftblibrary");
		BooleanValue TEST_1 = CONFIG.getBoolean("test_1", true);
		BooleanValue TEST_2 = CONFIG.getBoolean("test_2", true).comment("Boolean test 2");
		BooleanValue TEST_3 = CONFIG.getBoolean("test 3", false).comment("Boolean test 3");
		StringListValue STRING_LIST = CONFIG.getStringList("string_list", Arrays.asList("a", "b", "c"));

		SNBTConfig SUB_TEST = CONFIG.getGroup("sub_test").comment("Group comment test", "Line 2");
		BooleanValue SUB_TEST_BOOLEAN = SUB_TEST.getBoolean("boolean", false);
		IntValue SUB_TEST_INT = SUB_TEST.getInt("int", 50).range(30, Integer.MAX_VALUE);
		DoubleValue SUB_TEST_DOUBLE = SUB_TEST.getDouble("double", 0.5D).range(0D, 1D);
		StringValue SUB_TEST_STRING = SUB_TEST.getString("string", "hello");
		EnumValue<Tristate> TRISTATE_ENUM = SUB_TEST.getEnum("tristate_enum", Tristate.NAME_MAP);
		IntArrayValue INT_ARRAY = SUB_TEST.getIntArray("int_array", new int[]{3, 59});
	}

	public static void main(String[] args) {
		General.CONFIG.load(Paths.get("config/ftblibrary-config-test.snbt"));

		System.out.println(General.TEST_2.get());
		System.out.println(General.SUB_TEST_BOOLEAN.get());
		System.out.println(General.SUB_TEST_INT.get());
		System.out.println(General.SUB_TEST_DOUBLE.get());
		System.out.println(General.SUB_TEST_STRING.get());
		System.out.println(General.STRING_LIST.get());
		System.out.println(Arrays.toString(General.INT_ARRAY.get()));

		Util.shutdownExecutors();
	}
}
