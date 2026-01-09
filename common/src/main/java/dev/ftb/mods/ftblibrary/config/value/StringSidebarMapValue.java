package dev.ftb.mods.ftblibrary.config.value;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.ftb.mods.ftblibrary.config.serializer.ConfigSerializer;
import dev.ftb.mods.ftblibrary.sidebar.SidebarGuiButton;

import java.util.HashMap;
import java.util.Map;

public class StringSidebarMapValue extends AbstractMapValue<StringSidebarMapValue.SideButtonInfo> {
    public StringSidebarMapValue(ConfigGroup parent, String key, Map<String, SideButtonInfo> defaultValue) {
        super(parent, key, defaultValue, SideButtonInfo.CODEC);

        super.set(new HashMap<>(defaultValue));
    }

    public record SideButtonInfo(boolean enabled, int xPos, int yPos) {
        public static final Codec<SideButtonInfo> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            Codec.BOOL.optionalFieldOf("enabled", false).forGetter(SideButtonInfo::enabled),
            Codec.INT.optionalFieldOf("x", 0).forGetter(SideButtonInfo::xPos),
            Codec.INT.optionalFieldOf("y", 0).forGetter(SideButtonInfo::yPos)
        ).apply(builder, SideButtonInfo::new));

        public static SideButtonInfo DISABLED = new SideButtonInfo(false, -1, -1);

        public static SideButtonInfo at(int x, int y) {
            return new SideButtonInfo(true, x, y);
        }

        public static SideButtonInfo ofButton(SidebarGuiButton button) {
            return new SideButtonInfo(button.isEnabled(), button.getGridLocation().x(), button.getGridLocation().y());
        }
    }
}
