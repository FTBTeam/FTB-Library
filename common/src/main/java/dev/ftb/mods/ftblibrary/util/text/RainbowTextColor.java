package dev.ftb.mods.ftblibrary.util.text;

import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.util.Lazy;
import net.minecraft.client.Minecraft;

public class RainbowTextColor extends CustomTextColor {
    private static final Lazy<Integer[]> RAINBOW_COLORS = Lazy.of(() -> {
        int hue = 0;

        Integer[] colors = new Integer[255];
        for (int i = 0; i < 255; i++) {
            colors[i] = Color4I.hsb(hue / 255F, .8F, 1F).rgb();
            hue++;
        }

        return colors;
    });

    public static final RainbowTextColor INSTANCE = new RainbowTextColor();

    private RainbowTextColor() {
        super("ftb:rainbow");
    }

    @Override
    public int getValue() {
        if (Platform.getEnvironment() != Env.CLIENT) {
            return super.getValue();
        }

        int ticks = (int) Minecraft.getInstance().clientTickCount;

        return RAINBOW_COLORS.get()[(ticks * 2) % RAINBOW_COLORS.get().length];
    }

    public static Lazy<Integer[]> getRainbowColors() {
        return RAINBOW_COLORS;
    }
}
