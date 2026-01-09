package dev.ftb.mods.ftblibrary.config;

import dev.ftb.mods.ftblibrary.FTBLibrary;
import dev.ftb.mods.ftblibrary.config.value.BooleanValue;
import dev.ftb.mods.ftblibrary.config.value.ConfigGroup;
import dev.ftb.mods.ftblibrary.config.value.IntValue;
import dev.ftb.mods.ftblibrary.config.value.StringValue;

import static dev.ftb.mods.ftblibrary.FTBLibrary.MOD_ID;

public interface FTBLibraryServerConfig {
    String KEY = MOD_ID + "-server";

    ConfigGroup CONFIG = ConfigGroup.create(KEY)
            .comment("Server-specific configuration for FTB Library. Testing only!");

    ConfigGroup SECT1 = CONFIG.addGroup("section1");
    StringValue TEST1 = SECT1.addString("test1", "hello");
    StringValue TEST2 = SECT1.addString("test2", "world");
    BooleanValue TEST3 = SECT1.addBoolean("test3", true);

    ConfigGroup SECT2 = CONFIG.addGroup("section2");
    BooleanValue TEST4 = SECT2.addBoolean("test4", false);
    IntValue TEST5 = SECT2.addInt("test5", 1, 0, 10);

    static void onChanged(boolean isServer) {
        FTBLibrary.LOGGER.info("config has been updated! server = {} - clear any cached data etc. here", isServer);
    }
}
