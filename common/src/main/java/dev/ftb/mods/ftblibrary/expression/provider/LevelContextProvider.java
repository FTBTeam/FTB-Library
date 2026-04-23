package dev.ftb.mods.ftblibrary.expression.provider;

import net.minecraft.world.Difficulty;
import net.minecraft.world.level.Level;

/// Context provider for a [Level] providing support for `level.` expressions.
public class LevelContextProvider extends ContextProvider {
    private final Level level;

    public LevelContextProvider(Level level) {
        super("level");
        this.level = level;
    }

    public long getClockTime() {
        return this.level.getDefaultClockTime();
    }

    public long getGameTime() {
        return this.level.getGameTime();
    }

    public boolean isPeaceful() {
        return this.level.getDifficulty() == Difficulty.PEACEFUL;
    }

    public boolean isEasy() {
        return this.level.getDifficulty() == Difficulty.EASY;
    }

    public boolean isNormal() {
        return this.level.getDifficulty() == Difficulty.NORMAL;
    }

    public boolean isHard() {
        return this.level.getDifficulty() == Difficulty.HARD;
    }

    public boolean isOverworld() {
        return this.level.dimension() == Level.OVERWORLD;
    }

    public boolean isNether() {
        return this.level.dimension() == Level.NETHER;
    }

    public boolean isEnd() {
        return this.level.dimension() == Level.END;
    }

    /// Checks if the time is within the day range.
    public boolean isBrightOutside() {
        return this.level.isBrightOutside();
    }

    public boolean isDarkOutside() {
        return !this.level.isBrightOutside();
    }

    public boolean isRaining() {
        return this.level.isRaining();
    }

    public boolean isThundering() {
        return this.level.isThundering();
    }

    public int randomInt() {
        return this.level.getRandom().nextInt();
    }

    public float randomFloat() {
        return this.level.getRandom().nextFloat();
    }

    public double randomDouble() {
        return this.level.getRandom().nextDouble();
    }
}
