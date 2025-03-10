package dev.ftb.mods.ftblibrary.snbt.config;

import dev.ftb.mods.ftblibrary.sidebar.SidebarGuiButton;
import dev.ftb.mods.ftblibrary.snbt.SNBTCompoundTag;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class StringSidebarMapValue extends BaseValue<Map<String, StringSidebarMapValue.SideButtonInfo>> {
    public StringSidebarMapValue(@Nullable SNBTConfig c, String n, Map<String, SideButtonInfo> def) {
        super(c, n, def);
        super.set(new HashMap<>(def));
    }

    @Override
    public void write(SNBTCompoundTag tag) {
        Map<String, SideButtonInfo> map = get();
        SNBTCompoundTag mapTag = new SNBTCompoundTag();

        for (Map.Entry<String, SideButtonInfo> entry : map.entrySet()) {
            SNBTCompoundTag buttonTag = new SNBTCompoundTag();
            buttonTag.putBoolean("enabled", entry.getValue().enabled());
            buttonTag.putInt("x", entry.getValue().xPos());
            buttonTag.putInt("y", entry.getValue().yPos());
            mapTag.put(entry.getKey(), buttonTag);
        }

        tag.put(key, mapTag);
    }

    @Override
    public void read(SNBTCompoundTag tag) {
        Map<String, SideButtonInfo> map = new HashMap<>();

        SNBTCompoundTag compound = tag.getCompound(key);
        for (String key : compound.getAllKeys()) {
            SNBTCompoundTag buttonTag = compound.getCompound(key);
            map.put(key, new SideButtonInfo(
                    buttonTag.getBoolean("enabled"),
                    buttonTag.getInt("x"),
                    buttonTag.getInt("y")
            ));
        }

        set(map);
    }


    public record SideButtonInfo(boolean enabled, int xPos, int yPos) {
        public static SideButtonInfo DISABLED = new SideButtonInfo(false, -1, -1);

        public static SideButtonInfo at(int x, int y) {
            return new SideButtonInfo(true, x, y);
        }

        public static SideButtonInfo ofButton(SidebarGuiButton button) {
            return new SideButtonInfo(button.isEnabled(), button.getGridLocation().x(), button.getGridLocation().y());
        }
    }
}
