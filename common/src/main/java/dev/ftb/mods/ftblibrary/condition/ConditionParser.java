package dev.ftb.mods.ftblibrary.condition;

/// Conditional expression parser
/// Example: "modLoaded('ftb-stages') and (level.isDay() or (level.isNight() and level.isRaining())) and (player.isOp() or player.name() is 'testing')"
public class ConditionParser {
    public Expression parser(String input) {
        throw new RuntimeException("Not implemented yet");
    }

    public static class Expression {

    }
}
