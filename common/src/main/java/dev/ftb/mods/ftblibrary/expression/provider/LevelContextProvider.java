package dev.ftb.mods.ftblibrary.expression.provider;

import net.minecraft.world.level.Level;

/// Context provider for a [Level] providing support for {@code level.} expressions.
public class LevelContextProvider extends ContextProvider {
    private final Level level;

    public LevelContextProvider(Level level) {
        super("level");
        this.level = level;
    }

    public boolean isDay() {
        return this.level.getGameTime() % 24000L < 12000L;
    }

    public boolean isNight() {
        return !isDay();
    }

    public boolean isRaining() {
        return this.level.isRaining();
    }
}
