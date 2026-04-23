package dev.ftb.mods.ftblibrary.expression.exceptions;

/// Thrown when a [dev.ftb.mods.ftblibrary.expression.provider.ContextProvider] declares
/// two or more public methods with the same name, which is not supported because the
/// expression language has no syntax to distinguish overloads at the call site.
public class DuplicateMethodException extends RuntimeException {
    public DuplicateMethodException(String message) {
        super(message);
    }
}
