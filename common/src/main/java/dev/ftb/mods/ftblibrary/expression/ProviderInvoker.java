package dev.ftb.mods.ftblibrary.expression;

import dev.ftb.mods.ftblibrary.expression.exceptions.ExpressionEvalException;
import dev.ftb.mods.ftblibrary.expression.provider.ContextProvider;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/// Wraps a [ContextProvider] and handles all reflection-based method discovery
/// and invocation logic, including argument coercion and error handling.
class ProviderInvoker {
    private final ContextProvider provider;
    private final Map<String, Method> methodCache;

    ProviderInvoker(ContextProvider provider) {
        this.provider = provider;
        this.methodCache = buildMethodCache(provider);
    }

    Set<String> availableMethods() {
        return methodCache.keySet();
    }

    Object invoke(String method, java.util.List<Object> args) {
        Method cachedMethod = methodCache.get(method);
        if (cachedMethod == null) {
            throw new ExpressionEvalException("Method '" + method + "' is not available on provider '" + provider.name() + "'. Available: " + methodCache.keySet());
        }

        // We now need to attempt to take the given args and push them into the correct type for the method
        // I'm not super happy about how this is working
        @Nullable Object[] coerced = coerceArgs(cachedMethod, args);
        try {
            return cachedMethod.invoke(provider, coerced);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            throw new ExpressionEvalException("Error invoking '" + provider.name() + "." + method + "': " + cause.getMessage(), cause);
        } catch (IllegalAccessException e) {
            throw new ExpressionEvalException("Cannot access method '" + provider.name() + "." + method + "': " + e.getMessage(), e);
        }
    }

    private static Map<String, Method> buildMethodCache(ContextProvider provider) {
        Map<String, Method> cache = new HashMap<>();
        for (Method m : provider.getClass().getMethods()) {
            if (m.isBridge() || m.isSynthetic()) {
                continue;
            }

            m.setAccessible(true);
            cache.put(m.getName(), m);
        }
        return Map.copyOf(cache);
    }

    @Nullable
    private Object[] coerceArgs(Method m, java.util.List<Object> args) {
        Class<?>[] params = m.getParameterTypes();
        if (params.length != args.size()) {
            throw new ExpressionEvalException("Method '" + provider.name() + "." + m.getName() + "' expects " + params.length + " argument(s) but got " + args.size());
        }

        @Nullable Object[] coerced = new Object[params.length];
        for (int i = 0; i < params.length; i++) {
            coerced[i] = coerce(args.get(i), params[i], provider.name() + "." + m.getName() + " arg[" + i + "]");
        }

        return coerced;
    }

    /// Attempt to 'coerce' a given 'object' into the given target type, throwing an exception if it's not possible.
    /// This is used to allow some flexibility in how provider method arguments are passed, e.g. allowing an int literal to be passed to a method expecting a long.
    @Nullable
    private static Object coerce(@Nullable Object value, Class<?> target, String context) {
        if (value == null) return null;
        if (target.isInstance(value)) return value;

        // Attempt to be more clever about how the numbers are being handled.
        if (value instanceof BigInteger bi) {
            try {
                if (target == BigInteger.class) return bi;
                if (target == long.class || target == Long.class) return bi.longValueExact();
                if (target == int.class || target == Integer.class) return bi.intValueExact();
            } catch (ArithmeticException e) {
                throw new ExpressionEvalException("Cannot coerce value '" + value + "' (" + value.getClass().getSimpleName() + ") to " + target.getSimpleName() + " for " + context, e);
            }
        }

        if (value instanceof Number n) {
            // Fallback value for if a number has the target of a bigint, not that this would happen to often as it's rare
            // methods use the raw Number call for input regardless.
            if (target == BigInteger.class) return BigInteger.valueOf(n.longValue());
            if (target == double.class || target == Double.class) return n.doubleValue();
            if (target == float.class || target == Float.class) return n.floatValue();
            if (target == long.class || target == Long.class) return n.longValue();
            if (target == int.class || target == Integer.class) return n.intValue();
        }

        if (value instanceof String s && target == String.class) {
            return s;
        }

        if (value instanceof Boolean b && (target == boolean.class || target == Boolean.class)) {
            return b;
        }

        throw new ExpressionEvalException("Cannot coerce value '" + value + "' (" + value.getClass().getSimpleName() + ") to " + target.getSimpleName() + " for " + context);
    }
}
