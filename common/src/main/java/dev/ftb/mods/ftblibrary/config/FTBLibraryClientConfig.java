package dev.ftb.mods.ftblibrary.config;

import dev.ftb.mods.ftblibrary.snbt.config.*;

import java.util.HashMap;

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

    SNBTConfig SIDEBAR = CONFIG.addGroup("sidebar");
    BooleanValue SIDEBAR_ENABLED = SIDEBAR.addBoolean("enabled", true)
            .comment("Enable the sidebar");
    EnumValue<SidebarPosition> SIDEBAR_POSITION = SIDEBAR.addEnum("position", SidebarPosition.NAME_MAP, SidebarPosition.TOP_LEFT)
            .comment("Position of the sidebar");

    StringSidebarMapValue SIDEBAR_BUTTONS = SIDEBAR.add(new StringSidebarMapValue(SIDEBAR, "buttons", new HashMap<>()));

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

    public enum SidebarPosition {
        TOP_LEFT(false, false),
        TOP_RIGHT(false, true),
        BOTTOM_LEFT(true, false),
        BOTTOM_RIGHT(true, true);

        private final boolean isBottom;
        private final boolean isRight;

        SidebarPosition(boolean isBottom, boolean isRight) {
            this.isBottom = isBottom;
            this.isRight = isRight;
        }

        public boolean isBottom() {
            return isBottom;
        }

        public boolean isRight() {
            return isRight;
        }

        public static final NameMap<SidebarPosition> NAME_MAP = NameMap.of(TOP_LEFT, SidebarPosition.values()).baseNameKey("ftblibrary.panel.position").create();
    }
}
