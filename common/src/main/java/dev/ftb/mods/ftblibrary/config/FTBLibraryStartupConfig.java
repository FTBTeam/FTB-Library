package dev.ftb.mods.ftblibrary.config;

import dev.ftb.mods.ftblibrary.config.value.*;

import java.util.List;

import static dev.ftb.mods.ftblibrary.FTBLibrary.MOD_ID;

public interface FTBLibraryStartupConfig {
    String KEY = MOD_ID + "-startup";

    ConfigGroup CONFIG = ConfigGroup.create(KEY)
            .comment("Startup configuration for FTB Library. Testing only!");

    ConfigGroup SECT1 = CONFIG.addGroup("section1")
            .comment("This is section1", "Hey");
    StringValue TEST1 = SECT1.addString("test1", "hello").comment("hello world!");
    StringValue TEST2 = SECT1.addString("test2", "world");
    BooleanValue TEST3 = SECT1.addBoolean("test3", true);

    ConfigGroup SECT2 = CONFIG.addGroup("section2");
    BooleanValue TEST4 = SECT2.addBoolean("test4", false);
    IntValue TEST5 = SECT2.addInt("test5", 1, 0, 10);

    ConfigGroup SECT3 = SECT2.addGroup("section3").comment("I'm nested!");
    StringListValue TEST6 = SECT3.addStringList("test6", List.of("a", "b", "c"));
}