package dev.ftb.mods.ftblibrary.expression;

import dev.ftb.mods.ftblibrary.expression.exceptions.ExpressionEvalException;
import dev.ftb.mods.ftblibrary.expression.exceptions.ExpressionParseException;
import dev.ftb.mods.ftblibrary.expression.provider.ContextProvider;
import dev.ftb.mods.ftblibrary.expression.provider.MathContextProvider;
import dev.ftb.mods.ftblibrary.expression.provider.StdContextProvider;
import dev.ftb.mods.ftblibrary.expression.provider.StringContextProvider;
import dev.ftb.mods.ftblibrary.util.LRUCache;
import org.jspecify.annotations.Nullable;

import java.math.BigInteger;
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
    private static final int CACHE_SIZE = 256;

    private boolean allowOverriding = false;
    private final Map<String, ProviderInvoker> providers = new HashMap<>();
    private final LRUCache<String, Node> parseCache;

    public ExpressionEngine() {
        this(CACHE_SIZE);
    }

    /// Set up an expression engine with a set parse cache size.
    public ExpressionEngine(int parseCacheSize) {
        this.parseCache = new LRUCache<>(parseCacheSize);
        registerProvider(new StdContextProvider());
        registerProvider(new MathContextProvider());
        registerProvider(new StringContextProvider());
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

    /// Parse and evaluate an expression string, returning the raw result.
    ///
    /// The return type depends on the expression: arithmetic expressions return a number, etc.
    ///
    /// @param expression the raw expression string
    /// @return the result of the expression as an Object
    ///
    /// @throws ExpressionParseException if the expression cannot be parsed
    /// @throws ExpressionEvalException  if evaluation fails
    public Object evaluate(String expression) {
        Node ast = parseCache.get(expression);
        if (ast == null) {
            ast = new ExpressionParser(expression).parse();
            parseCache.put(expression, ast);
        }
        return evalNode(ast);
    }

    /// Parse and evaluate an expression string, coercing the result to a boolean.
    ///
    /// @param expression the raw expression string
    /// @return the boolean result of the expression
    ///
    /// @throws ExpressionParseException if the expression cannot be parsed
    /// @throws ExpressionEvalException  if evaluation fails or the result is not a boolean
    public boolean eval(String expression) {
        return toBool(evaluate(expression), "top-level expression");
    }

    /// Attempt to evaluate the top level expression node to a boolean, throwing an exception if it's not possible.
    private Object evalNode(Node node) {
        return switch (node) {
            case Node.BoolLiteral bool -> bool.value();
            case Node.StringLiteral str -> str.value();
            case Node.IntLiteral intNum -> intNum.value();
            case Node.LongLiteral longNum -> longNum.value();
            case Node.FloatLiteral floatNum -> floatNum.value();
            case Node.DoubleLiteral doubleNum -> doubleNum.value();
            case Node.BigIntLiteral bigNum -> bigNum.value();

            case Node.BinaryOp binaryOp -> evalBinaryOp(binaryOp);
            case Node.UnaryOp unaryOp -> evalUnaryOp(unaryOp);
            case Node.ArithmeticOp arithmeticOp -> evalArithmetic(arithmeticOp);
            case Node.UnaryMinus unaryMinus -> evalUnaryMinus(unaryMinus);
            case Node.Comparison comparison -> evalComparison(comparison);

            case Node.ProviderCall call -> evalProviderCall(call);
        };
    }

    /// Switch the operation based on the operator, evaluating the left and right sides as needed and coercing to boolean.
    private boolean evalBinaryOp(Node.BinaryOp bin) {
        return switch (bin.op()) {
            case AND -> toBool(evalNode(bin.left()), "left of AND") && toBool(evalNode(bin.right()), "right of AND");
            case OR -> toBool(evalNode(bin.left()), "left of OR") || toBool(evalNode(bin.right()), "right of OR");
            case XOR -> toBool(evalNode(bin.left()), "left of XOR") ^ toBool(evalNode(bin.right()), "right of XOR");
        };
    }

    private boolean evalUnaryOp(Node.UnaryOp un) {
        return !toBool(evalNode(un.operand()), "operand of NOT");
    }

    /// Evaluate an arithmetic operation, coercing the left and right sides to numbers and applying Java-like standards.
    private Number evalArithmetic(Node.ArithmeticOp arithmeticOp) {
        Object left = evalNode(arithmeticOp.left());
        Object right = evalNode(arithmeticOp.right());

        if (!(left instanceof Number l)) {
            throw new ExpressionEvalException("Expected a numeric value on left of arithmetic but got: " + (left == null ? "null" : left.getClass().getSimpleName()));
        }
        if (!(right instanceof Number r)) {
            throw new ExpressionEvalException("Expected a numeric value on right of arithmetic but got: " + (right == null ? "null" : right.getClass().getSimpleName()));
        }

        return arithmeticOp.op().applyByType(l, r);
    }

    private Number evalUnaryMinus(Node.UnaryMinus um) {
        Object val = evalNode(um.operand());
        if (!(val instanceof Number number)) {
            throw new ExpressionEvalException("Expected a numeric value for unary minus but got: " + (val == null ? "null" : val.getClass().getSimpleName()));
        }

        return switch (number) {
            case Double d -> -d;
            case Float f -> -f;
            case Long lg -> -lg;
            case BigInteger bi -> bi.negate();
            default -> -number.intValue();
        };
    }

    private boolean evalComparison(Node.Comparison cmp) {
        Object left = evalNode(cmp.left());
        Object right = evalNode(cmp.right());

        return switch (cmp.op()) {
            case EQ -> objectEquals(left, right);
            case NEQ -> !objectEquals(left, right);
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

    /// Compare two numeric values using Java-like promotion rules:
    /// - BigInteger vs integral → compare as BigInteger
    /// - BigInteger vs float/double → runtime error
    /// - long vs long → compare as long
    /// - float vs float (or float vs integral) → compare as float
    /// - anything vs double (or double vs anything) → compare as double
    private int compareNumeric(@Nullable Object a, @Nullable Object b, String op) {
        if (!(a instanceof Number numberA)) {
            throw new ExpressionEvalException("Expected a numeric value for left of " + op + " but got: " + (a == null ? "null" : a.getClass().getSimpleName() + " (" + a + ")"));
        }

        if (!(b instanceof Number numberB)) {
            throw new ExpressionEvalException("Expected a numeric value for right of " + op + " but got: " + (b == null ? "null" : b.getClass().getSimpleName() + " (" + b + ")"));
        }

        if (a instanceof BigInteger biA) {
            if (b instanceof BigInteger biB) return biA.compareTo(biB);
            if (b instanceof Number nb && isIntegral(nb)) return biA.compareTo(BigInteger.valueOf(nb.longValue()));
            throw new ExpressionEvalException("Cannot compare BigInteger with " + b.getClass().getSimpleName() + " for " + op);
        }
        if (b instanceof BigInteger biB) {
            if (a instanceof Number na && isIntegral(na)) return BigInteger.valueOf(na.longValue()).compareTo(biB);
            throw new ExpressionEvalException("Cannot compare " + a.getClass().getSimpleName() + " with BigInteger for " + op);
        }

        if (isIntegral(numberA) && isIntegral(numberB)) {
            return Long.compare(numberA.longValue(), numberB.longValue());
        }

        if (isNotDouble(numberA) && isNotDouble(numberB)) {
            // At least one side is float, neither is double — compare at float precision
            return Float.compare(numberA.floatValue(), numberB.floatValue());
        }

        return Double.compare(numberA.doubleValue(), numberB.doubleValue());
    }

    /// Test if the given number is an integral (non-floating-point) value
    private static boolean isIntegral(Number n) {
        return n instanceof Long || n instanceof Integer || n instanceof Short || n instanceof Byte;
    }

    private static boolean isNotDouble(Number n) {
        return !(n instanceof Double);
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

        // BigInteger handling
        if (a instanceof BigInteger biA) {
            if (b instanceof BigInteger biB) return biA.compareTo(biB) == 0;
            if (b instanceof Number nb && isIntegral(nb)) return biA.compareTo(BigInteger.valueOf(nb.longValue())) == 0;
            return false;
        }
        if (b instanceof BigInteger biB) {
            if (a instanceof Number na && isIntegral(na)) return BigInteger.valueOf(na.longValue()).compareTo(biB) == 0;
            return false;
        }

        if (a instanceof Number numberA && b instanceof Number numberB) {
            if (isIntegral(numberA) && isIntegral(numberB)) {
                return numberA.longValue() == numberB.longValue();
            }

            if (isNotDouble(numberA) && isNotDouble(numberB)) {
                // At least one side is float, neither is double — compare at float precision
                return Float.compare(numberA.floatValue(), numberB.floatValue()) == 0;
            }

            return Double.compare(numberA.doubleValue(), numberB.doubleValue()) == 0;
        }

        return a.equals(b);
    }
}
