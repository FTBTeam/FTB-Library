package dev.ftb.mods.ftblibrary.condition.provider;

import net.minecraft.world.level.Level;

/// Context provider for a [Level] providing support for "level." expressions
public class LevelContextProvider extends ContextProvider {
    private final Level level;

    public LevelContextProvider(Level level) {
        super("level");
        this.level = level;
    }

    public boolean isDay() {
        // Check if the gametime is within the day range
        return this.level.getGameTime() % 24000L < 12000L;
    }

    public boolean isNight() {
        return !isDay();
    }

    public boolean isRaining() {
        return this.level.isRaining();
    }
}
