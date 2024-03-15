package dev.ftb.mods.ftblibrary.config;

import dev.ftb.mods.ftblibrary.snbt.config.BooleanValue;
import dev.ftb.mods.ftblibrary.snbt.config.IntArrayValue;
import dev.ftb.mods.ftblibrary.snbt.config.SNBTConfig;

import static dev.ftb.mods.ftblibrary.FTBLibrary.MOD_ID;
import static dev.ftb.mods.ftblibrary.snbt.config.ConfigUtil.LOCAL_DIR;
import static dev.ftb.mods.ftblibrary.snbt.config.ConfigUtil.loadDefaulted;

public interface FTBLibraryClientConfig {
    SNBTConfig CONFIG = SNBTConfig.create(MOD_ID + "-client")
            .comment("Client-specific configuration for FTB Library");

    SNBTConfig TOOLTIPS = CONFIG.addGroup("tooltips");

    BooleanValue ITEM_MODNAME = TOOLTIPS.addBoolean("item_modname", false)
            .comment("Add the name of the mod that items belong to in the item selection GUI.\n" +
                    "Note that several common mods also do this (modnametooltip,WTHIT,EMI...) so this is false by default");
    BooleanValue FLUID_MODNAME = TOOLTIPS.addBoolean("fluid_modname", true)
            .comment("Add the name of the mod that fluids belong to in the fluid selection GUI.");
    BooleanValue IMAGE_MODNAME = TOOLTIPS.addBoolean("image_modname", true)
            .comment("Add the name of the mod that images belong to in the image selection GUI.");

    SNBTConfig COLOR = CONFIG.addGroup("colorselector");
    IntArrayValue RECENT = COLOR.addIntArray("recents", new int[0])
            .comment("Colors recently selected in the color selector");

    static void load() {
        loadDefaulted(CONFIG, LOCAL_DIR, MOD_ID);
    }

    static void save() {
        CONFIG.save(LOCAL_DIR.resolve(MOD_ID + "-client.snbt"));
    }

    static ConfigGroup getConfigGroup() {
        ConfigGroup group = new ConfigGroup(MOD_ID + ".client_settings", accepted -> {
            if (accepted) {
                save();
            }
        });
        CONFIG.createClientConfig(group);

        return group;
    }
}
