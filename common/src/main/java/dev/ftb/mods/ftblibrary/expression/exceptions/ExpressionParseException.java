package dev.ftb.mods.ftblibrary.expression.exceptions;

/// Thrown when the expression string cannot be lexed or parsed.
public class ExpressionParseException extends RuntimeException {
    public ExpressionParseException(String message) {
        super(message);
    }

    public ExpressionParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
