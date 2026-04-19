package dev.ftb.mods.ftblibrary.expression.exceptions;

/// Thrown when an expression cannot be evaluated, e.g. a required context
/// provider is absent or a method call returns an unexpected type.
public class ExpressionEvalException extends RuntimeException {
    public ExpressionEvalException(String message) {
        super(message);
    }

    public ExpressionEvalException(String message, Throwable cause) {
        super(message, cause);
    }
}
