package dev.ftb.mods.ftblibrary.config;

import dev.ftb.mods.ftblibrary.config.manager.ConfigManager;
import dev.ftb.mods.ftblibrary.config.value.*;
import dev.ftb.mods.ftblibrary.util.NameMap;

import java.util.HashMap;

import static dev.ftb.mods.ftblibrary.FTBLibrary.MOD_ID;

public interface FTBLibraryClientConfig {
    String KEY = MOD_ID + "-client";

    Config CONFIG = Config.create(KEY)
            .comment("Client-specific configuration for FTB Library");

    Config TOOLTIPS = CONFIG.addGroup("tooltips");

    BooleanValue ITEM_MODNAME = TOOLTIPS.addBoolean("item_modname", false)
            .comment("Add the name of the mod that items belong to in the item selection GUI.\n" +
                    "Note that several common mods also do this (modnametooltip,WTHIT,EMI...) so this is false by default");
    BooleanValue FLUID_MODNAME = TOOLTIPS.addBoolean("fluid_modname", true)
            .comment("Add the name of the mod that fluids belong to in the fluid selection GUI.");
    BooleanValue IMAGE_MODNAME = TOOLTIPS.addBoolean("image_modname", true)
            .comment("Add the name of the mod that images belong to in the image selection GUI.");
    BooleanValue ENTITY_MODNAME = TOOLTIPS.addBoolean("entity_modname", true)
            .comment("Add the name of the mod that entities belong to in the entity face selection GUI.");

    Config COLOR = CONFIG.addGroup("colorselector");
    IntArrayValue RECENT = COLOR.addIntArray("recents", new int[0])
            .comment("Colors recently selected in the color selector")
            .excludedFromGui();

    Config SIDEBAR = CONFIG.addGroup("sidebar");
    BooleanValue SIDEBAR_ENABLED = SIDEBAR.addBoolean("enabled", true)
            .comment("Enable the sidebar");
    EnumValue<SidebarPosition> SIDEBAR_POSITION = SIDEBAR.addEnum("position", SidebarPosition.NAME_MAP, SidebarPosition.TOP_LEFT)
            .comment("Position of the sidebar");

    StringSidebarMapValue SIDEBAR_BUTTONS = SIDEBAR.add(new StringSidebarMapValue(SIDEBAR, "buttons", new HashMap<>()));

    /**
     * Just for convenience.
     */
    static void save() {
        ConfigManager.getInstance().save(KEY);
    }

    enum SidebarPosition {
        TOP_LEFT(false, false),
        TOP_RIGHT(false, true),
        BOTTOM_LEFT(true, false),
        BOTTOM_RIGHT(true, true);

        public static final NameMap<SidebarPosition> NAME_MAP = NameMap.of(TOP_LEFT, SidebarPosition.values()).baseNameKey("ftblibrary.panel.position").create();
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
    }
}
