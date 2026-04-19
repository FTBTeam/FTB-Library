package dev.ftb.mods.ftblibrary.expression;

import dev.ftb.mods.ftblibrary.expression.exceptions.ExpressionEvalException;
import dev.ftb.mods.ftblibrary.expression.exceptions.ExpressionParseException;
import dev.ftb.mods.ftblibrary.expression.provider.ContextProvider;
import dev.ftb.mods.ftblibrary.expression.provider.StdContextProvider;

import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/// Entry point for parsing and evaluating expression strings.
///
/// Example:
/// ```java
///   ExpressionEngine engine = new ExpressionEngine()
///       .registerProvider(new LevelContextProvider(level)) // If you want the level
///       .registerProvider(new PlayerContextProvider(player)); // if you want the player, etc
///
///   boolean result = engine.eval("std.isModLoaded('ftb-library') and level.isDay()");
/// ```
public class ExpressionEngine {
    private boolean allowOverriding = false;
    private final Map<String, ProviderInvoker> providers = new HashMap<>();

    public ExpressionEngine() {
        registerProvider(new StdContextProvider());
    }

    /// Register a context provider
    public ExpressionEngine registerProvider(ContextProvider provider) {
        if (providers.containsKey(provider.name())) {
            throw new RuntimeException("A provider with the name '" + provider.name() + "' is already registered.");
        }

        providers.put(provider.name(), new ProviderInvoker(provider));
        return this;
    }

    /// Register a context provider, allowing it to override an existing provider with the same name if present and overriding is enabled.
    /// This is mostly intended for unit testing but also could find itself useful if you want to extend an existing context for some reason.
    public ExpressionEngine overrideProvider(ContextProvider provider) {
        if (!allowOverriding && providers.containsKey(provider.name())) {
            throw new RuntimeException("A provider with the name '" + provider.name() + "' is already registered and overriding is not allowed.");
        }

        providers.put(provider.name(), new ProviderInvoker(provider));
        return this;
    }

    public ExpressionEngine enableOverride() {
        this.allowOverriding = true;
        return this;
    }

    /// Parse and evaluate an expression string.
    ///
    /// @param expression the raw expression string
    /// @return the boolean result of the expression
    /// @throws ExpressionParseException if the expression cannot be parsed
    /// @throws ExpressionEvalException  if evaluation fails
    public boolean eval(String expression) {
        Node ast = new ExpressionParser(expression).parse();
        Object result = evalNode(ast);
        return toBool(result, "top-level expression");
    }

    /// Attempt to evaluate the top level expression node to a boolean, throwing an exception if it's not possible.
    private Object evalNode(Node node) {
        return switch (node) {
            case Node.BoolLiteral b -> b.value();
            case Node.StringLiteral s -> s.value();
            case Node.IntLiteral n -> n.value();   // long
            case Node.FloatLiteral n -> n.value();   // double

            case Node.BinaryOp bin -> evalBinaryOp(bin);
            case Node.UnaryOp un -> evalUnaryOp(un);
            case Node.Comparison cmp -> evalComparison(cmp);

            case Node.ProviderCall call -> evalProviderCall(call);
        };
    }

    /// Switch the operation based on the operator, evaluating the left and right sides as needed and coercing to boolean.
    private boolean evalBinaryOp(Node.BinaryOp bin) {
        return switch (bin.op()) {
            case AND -> toBool(evalNode(bin.left()), "left of AND") && toBool(evalNode(bin.right()), "right of AND");
            case OR -> toBool(evalNode(bin.left()), "left of OR") || toBool(evalNode(bin.right()), "right of OR");
        };
    }

    private boolean evalUnaryOp(Node.UnaryOp un) {
        return !toBool(evalNode(un.operand()), "operand of NOT");
    }

    private boolean evalComparison(Node.Comparison cmp) {
        Object left = evalNode(cmp.left());
        Object right = evalNode(cmp.right());

        return switch (cmp.op()) {
            case IS, EQ -> objectEquals(left, right);
            case IS_NOT, NEQ -> !objectEquals(left, right);
            case LT -> compareNumeric(left, right, "<") < 0;
            case GT -> compareNumeric(left, right, ">") > 0;
            case LTE -> compareNumeric(left, right, "<=") <= 0;
            case GTE -> compareNumeric(left, right, ">=") >= 0;
        };
    }

    /// Evaluate a registered provider call via the providers list and throw if the provider does not provide a specific method name.
    private Object evalProviderCall(Node.ProviderCall call) {
        ProviderInvoker invoker = providers.get(call.providerName());
        if (invoker == null) {
            throw new ExpressionEvalException("Context provider '" + call.providerName() + "' is not available. Available providers: " + providers.keySet());
        }
        if (!invoker.availableMethods().contains(call.methodName())) {
            throw new ExpressionEvalException("Method '" + call.methodName() + "' is not available on provider '" + call.providerName() + "'. Available methods: " + invoker.availableMethods());
        }

        List<Object> args = evalArgs(call.args());
        return invoker.invoke(call.methodName(), args);
    }

    private List<Object> evalArgs(List<Node> argNodes) {
        List<Object> args = new ArrayList<>(argNodes.size());
        for (Node arg : argNodes) {
            args.add(evalNode(arg));
        }
        return args;
    }

    /// Attempt to coerce a value to boolean, throwing an exception if it's not possible. This is used for operands of logical operators and the top-level expression result.
    private boolean toBool(@Nullable Object value, String context) {
        if (value instanceof Boolean bool) return bool;
        throw new ExpressionEvalException("Expected a boolean value for " + context + " but got: " + (value != null ? value.getClass().getSimpleName() : "null") + " (" + value + ")");
    }

    /// Compare two numeric values using Java widening rules:
    /// long vs long stays as long, anything involving a double promotes to double.
    private int compareNumeric(@Nullable Object a, @Nullable Object b, String op) {
        if (!(a instanceof Number numberA)) {
            throw new ExpressionEvalException("Expected a numeric value for left of " + op + " but got: " + (a == null ? "null" : a.getClass().getSimpleName() + " (" + a + ")"));
        }

        if (!(b instanceof Number numberB)) {
            throw new ExpressionEvalException("Expected a numeric value for right of " + op + " but got: " + (b == null ? "null" : b.getClass().getSimpleName() + " (" + b + ")"));
        }

        // If both are ints (Long, Integer, etc. — i.e. not Double/Float) compare as long
        if (isIntegral(numberA) && isIntegral(numberB)) {
            return Long.compare(numberA.longValue(), numberB.longValue());
        }

        return Double.compare(numberA.doubleValue(), numberB.doubleValue());
    }

    /// Test if the given number is an 'int' like value
    private static boolean isIntegral(Number n) {
        return n instanceof Long || n instanceof Integer || n instanceof Short || n instanceof Byte;
    }

    private boolean objectEquals(@Nullable Object a, @Nullable Object b) {
        if (a == null && b == null) {
            return true;
        }

        if (a == null || b == null) {
            return false;
        }

        if (a instanceof String sa && b instanceof String stringVale) {
            return sa.equals(stringVale);
        }

        if (a instanceof Number numberA && b instanceof Number numberB) {
            if (isIntegral(numberA) && isIntegral(numberB)) {
                return numberA.longValue() == numberB.longValue();
            }

            return Double.compare(numberA.doubleValue(), numberB.doubleValue()) == 0;
        }

        return a.equals(b);
    }
}
