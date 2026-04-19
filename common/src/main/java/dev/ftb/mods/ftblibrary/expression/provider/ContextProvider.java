package dev.ftb.mods.ftblibrary.expression.provider;

/// Base class for all expression context providers.
///
/// A provider represents a named namespace (e.g. {@code "level"}, {@code "player"})
/// whose public methods are callable from expression strings as
/// {@code namespace.method(args...)}.
public abstract class ContextProvider {
    private final String name;

    protected ContextProvider(String name) {
        this.name = name;
    }

    /// The namespace used to address this provider in expressions.
    public final String name() {
        return name;
    }
}
