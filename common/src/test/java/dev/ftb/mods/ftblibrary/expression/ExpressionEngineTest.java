package dev.ftb.mods.ftblibrary.expression;

import dev.ftb.mods.ftblibrary.expression.exceptions.DuplicateMethodException;
import dev.ftb.mods.ftblibrary.expression.exceptions.ExpressionEvalException;
import dev.ftb.mods.ftblibrary.expression.exceptions.ExpressionParseException;
import dev.ftb.mods.ftblibrary.expression.provider.ContextProvider;
import dev.ftb.mods.ftblibrary.expression.provider.StdContextProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/// Testing for the expression language
/// TODO: Maybe split this up into test classes?
public class ExpressionEngineTest {
    FakeStdProvider std;
    FakeLevelProvider level;
    ExpressionEngine engine;

    @BeforeEach
    void setUp() {
        std = new FakeStdProvider();
        level = new FakeLevelProvider();
        engine = new ExpressionEngine()
                .enableOverride()
                .overrideProvider(std)
                .registerProvider(new TestContextProvider())
                .registerProvider(level);
    }

    @Nested
    class LexerAndParser {
        @Test
        void parsesSimpleBoolLiterals() {
            assertTrue(engine.eval("test.alwaysTrue()"));
            assertFalse(engine.eval("test.alwaysFalse()"));
        }

        @Test
        void parsesParenthesisedExpression() {
            assertTrue(engine.eval("(test.alwaysTrue())"));
        }

        @Test
        void parsesBoolLiteralTrue() {
            assertTrue(engine.eval("test.alwaysTrue() == true"));
            assertFalse(engine.eval("test.alwaysFalse() == true"));
        }

        @Test
        void parsesStringArgument() {
            assertTrue(engine.eval("std.isModLoaded('ftb-stages')"));
            assertFalse(engine.eval("std.isModLoaded('ftb-quests')"));
        }

        @Test
        void unexpectedTokenThrowsParseException() {
            assertThrows(ExpressionParseException.class, () -> engine.eval("???"));
        }

        @Test
        void unterminatedStringThrowsParseException() {
            assertThrows(ExpressionParseException.class, () -> engine.eval("std.isModLoaded('unterminated"));
        }

        @Test
        void missingClosingParenThrowsParseException() {
            assertThrows(ExpressionParseException.class, () -> engine.eval("std.isModLoaded('ftb-stages'"));
        }

        @Test
        void bareIdentifierThrowsParseException() {
            assertThrows(ExpressionParseException.class, () -> engine.eval("level"));
        }

        @Test
        void bareCallWithoutNamespaceThrowsParseException() {
            assertThrows(ExpressionParseException.class, () -> engine.eval("isModLoaded('ftb-stages')"));
        }
    }

    @Nested
    class LogicalOperators {
        @Test
        void notKeywordBang() {
            assertFalse(engine.eval("!test.alwaysTrue()"));
            assertTrue(engine.eval("!test.alwaysFalse()"));
        }

        @Test
        void doubleNot() {
            assertTrue(engine.eval("!!test.alwaysTrue()"));
        }

        @Test
        void andHasHigherPrecedenceThanOr() {
            assertTrue(engine.eval("test.alwaysFalse() || test.alwaysTrue() && test.alwaysTrue()"));
            assertFalse(engine.eval("test.alwaysFalse() || test.alwaysTrue() && test.alwaysFalse()"));
        }

        @Test
        void notHasHigherPrecedenceThanAnd() {
            assertTrue(engine.eval("!test.alwaysFalse() && test.alwaysTrue()"));
        }

        @Test
        void symbolAmpersandAmpersand() {
            assertTrue(engine.eval("test.alwaysTrue() && test.alwaysTrue()"));
            assertFalse(engine.eval("test.alwaysTrue() && test.alwaysFalse()"));
        }

        @Test
        void symbolPipePipe() {
            assertTrue(engine.eval("test.alwaysFalse() || test.alwaysTrue()"));
        }

        @Test
        void xorOneDifferent() {
            assertTrue(engine.eval("test.alwaysTrue() ^ test.alwaysFalse()"));
            assertTrue(engine.eval("test.alwaysFalse() ^ test.alwaysTrue()"));
        }

        @Test
        void xorBothSame() {
            assertFalse(engine.eval("test.alwaysTrue() ^ test.alwaysTrue()"));
            assertFalse(engine.eval("test.alwaysFalse() ^ test.alwaysFalse()"));
        }

        @Test
        void xorPrecedenceHigherThanOr() {
            // true ^ false = true, true || true = true
            assertTrue(engine.eval("test.alwaysTrue() || test.alwaysTrue() ^ test.alwaysFalse()"));
            // false ^ false = false, false || false = false
            assertFalse(engine.eval("test.alwaysFalse() || test.alwaysFalse() ^ test.alwaysFalse()"));
        }

        @Test
        void complexNestedLogic() {
            assertTrue(engine.eval("(test.alwaysFalse() || (test.alwaysTrue() && test.alwaysTrue())) && !test.alwaysFalse()"));
        }
    }

    @Nested
    class ProviderCalls {
        @Test
        void levelIsDay() {
            level.day = true;
            assertTrue(engine.eval("level.isDay()"));
        }

        @Test
        void unknownProviderThrowsEvalException() {
            assertThrows(ExpressionEvalException.class, () -> engine.eval("world.isLoaded()"));
        }

        @Test
        void unknownMethodOnProviderThrowsEvalException() {
            assertThrows(ExpressionEvalException.class, () -> engine.eval("level.explode()"));
        }

        @Test
        void wrongArgCountThrowsEvalException() {
            assertThrows(ExpressionEvalException.class, () -> engine.eval("level.isDay('extra')"));
        }
    }

    @Nested
    class Comparisons {
        @Test
        void stringIsCaseSensitive() {
            assertFalse(engine.eval("test.testString() == 'TEST'"));
            assertTrue(engine.eval("test.testString() == 'test'"));
        }

        @Test
        void isStringEquality() {
            assertTrue(engine.eval("test.testString() == 'test'"));
            assertFalse(engine.eval("test.testString() == 'other'"));
        }

        @Test
        void isNotStringEquality() {
            assertTrue(engine.eval("test.testString() != 'other'"));
            assertFalse(engine.eval("test.testString() != 'test'"));
        }

        @Test
        void doubleEquals() {
            assertTrue(engine.eval("test.testString() == 'test'"));
            assertFalse(engine.eval("test.testString() == 'other'"));
        }

        @Test
        void notEquals() {
            assertFalse(engine.eval("test.testString() != 'test'"));
            assertTrue(engine.eval("test.testString() != 'other'"));
        }

        @Test
        void numericLessThan() {
            assertTrue(engine.eval("test.number() < 45"));
            assertFalse(engine.eval("test.number() < 3"));
        }

        @Test
        void numericGreaterThan() {
            assertTrue(engine.eval("test.number() > 40"));
            assertFalse(engine.eval("test.number() > 100"));
        }

        @Test
        void numericLessThanOrEqual() {
            assertTrue(engine.eval("test.number() <= 45"));
            assertFalse(engine.eval("test.number() <= 10"));
        }

        @Test
        void numericGreaterThanOrEqual() {
            assertTrue(engine.eval("test.number() >= 42"));
            assertFalse(engine.eval("test.number() >= 1000"));
        }

        @Test
        void floatComparison() {
            assertTrue(engine.eval("test.floatNumber() > 3.0"));
            assertFalse(engine.eval("test.floatNumber() < 1"));
            assertTrue(engine.eval("test.floatNumber() == 3.141f"));
        }

        @Test
        void floatSuffixComparesAtFloatPrecision() {
            assertTrue(engine.eval("test.floatNumber() == 3.141f"));
            assertFalse(engine.eval("test.floatNumber() == 3.141"));
        }

        @Test
        void doubleSuffixComparesAtDoublePrecision() {
            assertTrue(engine.eval("test.floatNumber() > 3.0d"));
            assertFalse(engine.eval("test.floatNumber() == 3.141d"));
        }

        @Test
        void floatArithmetic() {
            assertTrue(engine.eval("test.addFloat(1.5f, 1.5f) == 3.0f"));
        }

        @Test
        void longLiteralComparison() {
            assertTrue(engine.eval("test.longNumber() == 42L"));
            assertTrue(engine.eval("test.longNumber() == 42"));
            assertFalse(engine.eval("test.longNumber() == 43L"));
        }

        @Test
        void bigIntLiteralComparison() {
            assertTrue(engine.eval("test.bigIntNumber() == 99999999999999999999bi"));
            assertFalse(engine.eval("test.bigIntNumber() == 1bi"));
        }

        @Test
        void bigIntVsIntegralComparison() {
            assertTrue(engine.eval("test.smallBigInt() == 42"));
            assertTrue(engine.eval("test.smallBigInt() == 42L"));
        }
    }

    // More complex testing.
    @Nested
    class Arithmetic {
        @Test
        void intAddition() {
            assertTrue(engine.eval("test.number() + 8 == 50"));
        }

        @Test
        void intSubtraction() {
            assertTrue(engine.eval("test.number() - 2 == 40"));
        }

        @Test
        void intMultiplication() {
            assertTrue(engine.eval("test.number() * 2 == 84"));
        }

        @Test
        void intDivision() {
            assertTrue(engine.eval("test.number() / 2 == 21"));
        }

        @Test
        void intModulo() {
            assertTrue(engine.eval("test.number() % 5 == 2"));
        }

        @Test
        void operatorPrecedenceMulBeforeAdd() {
            // number() = 42; 42 + 2 * 4 = 42 + 8 = 50, not (42+2)*4=176
            assertTrue(engine.eval("test.number() + 2 * 4 == 50"));
        }

        @Test
        void parenthesesOverridePrecedence() {
            assertTrue(engine.eval("(test.number() + 2) * 4 == 176"));
        }

        @Test
        void floatAddition() {
            assertTrue(engine.eval("test.floatNumber() + 0.0f == 3.141f"));
        }

        @Test
        void unaryMinusOnLiteral() {
            assertTrue(engine.eval("-10 + test.number() == 32"));
        }

        @Test
        void unaryMinusOnProviderCall() {
            assertTrue(engine.eval("-test.number() + 84 == 42"));
        }

        @Test
        void doubleUnaryMinus() {
            assertTrue(engine.eval("--test.number() == 42"));
        }

        @Test
        void arithmeticInComparison() {
            assertTrue(engine.eval("test.number() + 10 < 100"));
            assertFalse(engine.eval("test.number() + 10 < 50"));
        }

        @Test
        void longArithmetic() {
            assertTrue(engine.eval("test.longNumber() + 1L == 43L"));
        }

        @Test
        void integerDivisionTruncates() {
            // 42 / 5 = 8 in integer division (truncated, not 8.4)
            assertTrue(engine.eval("test.number() / 5 == 8"));
        }

        @Test
        void mixedIntDoublePromotesToDouble() {
            assertTrue(engine.eval("test.number() + 0.0 == 42.0"));
        }

        @Test
        void nonNumericArithmeticThrows() {
            assertThrows(ExpressionEvalException.class, () -> engine.eval("test.testString() + 1 == 2"));
        }
    }

    // More complex testing.
    @Nested
    class ComplexExpressions {
        @Test
        void complexExample() {
            level.day = false;
            level.raining = true;

            assertTrue(engine.eval(
                    "std.isModLoaded('ftb-stages') && (level.isDay() || (level.isNight() && level.isRaining())) && (test.alwaysFalse() || test.testString() == 'test')"
            ));
        }

        @Test
        void complexExampleFailsOnMissingMod() {
            level.day = false;
            level.raining = true;

            assertFalse(engine.eval(
                    "std.isModLoaded('other-mod') && (level.isDay() || (level.isNight() && level.isRaining())) && (test.alwaysFalse() || test.testString() == 'test')"
            ));
        }

        @Test
        void deeplyNestedParens() {
            assertTrue(engine.eval("((((test.alwaysTrue()))))"));
        }

        @Test
        void longAndChain() {
            level.day = true;
            level.raining = false;

            assertTrue(engine.eval(
                    "level.isDay() && !level.isRaining() && test.alwaysTrue() && std.isModLoaded('ftb-stages')"
            ));
        }

        @Test
        void notOnProviderCall() {
            level.day = true;
            assertFalse(engine.eval("!level.isDay()"));
            assertTrue(engine.eval("!level.isNight()"));
        }

        @Test
        void whitespaceVariations() {
            level.day = true;
            assertTrue(engine.eval("level.isDay()  &&  !level.isNight()"));
        }
    }

    @Nested
    class ProviderReflection {
        @Test
        void availableMethodsContainsPublicMethods() {
            var methods = new ProviderInvoker(level).availableMethods();
            assertTrue(methods.contains("isDay"));
        }

        @Test
        void availableMethodsExcludesBaseMethods() {
            var methods = new ProviderInvoker(level).availableMethods();
            assertFalse(methods.contains("availableMethods"), "availableMethods() should be excluded");
            assertFalse(methods.contains("invoke"), "invoke() should be excluded");
        }

        @Test
        void invokeCaseSensitive() {
            var invoker = new ProviderInvoker(level);
            assertThrows(ExpressionEvalException.class, () -> invoker.invoke("IsDay", List.of()));
            assertThrows(ExpressionEvalException.class, () -> invoker.invoke("ISDAY", List.of()));
        }

        @Test
        void argCoercionDoubleToInt() {
            var invoker = new ProviderInvoker(new TestContextProvider());
            assertDoesNotThrow(() -> invoker.invoke("addFloat", List.of(3.0, 4.0)));
            assertEquals(7f, invoker.invoke("addFloat", List.of(3.0, 4.0)));
        }

        @Test
        void argCoercionStringPassthrough() {
            var invoker = new ProviderInvoker(new TestContextProvider());
            assertEquals(true, invoker.invoke("startsWith", List.of("test world", "test")));
            assertEquals(false, invoker.invoke("startsWith", List.of("not my input", "nope")));
        }

        @Test
        void wrongArgTypeThrowsEvalException() {
            var invoker = new ProviderInvoker(new TestContextProvider());
            assertThrows(ExpressionEvalException.class, () -> invoker.invoke("add", List.of("notANumber", "notANumber")));
        }

        @Test
        void objectMethodsExcludedFromAvailableMethods() {
            var methods = new ProviderInvoker(level).availableMethods();

            // Ensure an objects native / inherited methods are not included as available methods on the provider
            assertFalse(methods.contains("equals"), "equals() from Object should be excluded");
            assertFalse(methods.contains("hashCode"), "hashCode() from Object should be excluded");
            assertFalse(methods.contains("toString"), "toString() from Object should be excluded");
            assertFalse(methods.contains("wait"), "wait() from Object should be excluded");
            assertFalse(methods.contains("notify"), "notify() from Object should be excluded");
            assertFalse(methods.contains("notifyAll"), "notifyAll() from Object should be excluded");
            assertFalse(methods.contains("getClass"), "getClass() from Object should be excluded");
        }

        @Test
        void contextProviderNameMethodExcluded() {
            // The context provider provides a name method for its own namespace and this should be excluded
            var methods = new ProviderInvoker(level).availableMethods();
            assertFalse(methods.contains("name"), "name() from ContextProvider should be excluded");
        }

        @Test
        void overloadedMethodThrowsDuplicateMethodException() {
            assertThrows(DuplicateMethodException.class, () -> new ProviderInvoker(new OverloadedProvider()));
        }

        @Test
        void customEqualsWithDifferentArityIsAllowed() {
            // A provider that declares its own equals(String, String) — 2-arg, nothing to do with
            // Object.equals — should not be filtered and should not throw.
            var invoker = new ProviderInvoker(new CustomEqualsProvider());
            assertTrue(invoker.availableMethods().contains("equals"));
            assertEquals(true, invoker.invoke("equals", List.of("hello", "hello")));
        }
    }

    @Nested
    class Evaluate {
        @Test
        void returnsNumberForArithmetic() {
            assertEquals(3, engine.evaluate("1 + 2"));
        }

        @Test
        void returnsBooleanForComparison() {
            assertEquals(true, engine.evaluate("1 < 2"));
            assertEquals(false, engine.evaluate("2 < 1"));
        }

        @Test
        void returnsBooleanForBooleanExpr() {
            assertEquals(true, engine.evaluate("true && true"));
            assertEquals(false, engine.evaluate("true && false"));
        }

        @Test
        void returnsDoubleForFloatingPointArithmetic() {
            Object result = engine.evaluate("1.0 + 2.0");
            assertInstanceOf(Double.class, result);
            assertEquals(3.0, (Double) result, 1e-9);
        }

        @Test
        void evalStillReturnsBool() {
            assertTrue(engine.eval("1 < 2"));
            assertFalse(engine.eval("2 < 1"));
        }

        @Test
        void evalThrowsOnNonBoolean() {
            assertThrows(ExpressionEvalException.class, () -> engine.eval("1 + 2"));
        }

        @Test
        void complexArithmeticExpression() {
            // 1 + 2 / (4 * 2) = 1 + 2/8 = 1 + 0 = 1 (integer division)
            Object result = engine.evaluate("1 + 2 / (4 * 2)");
            assertInstanceOf(Integer.class, result);
            assertEquals(1, result);
        }

        @Test
        void complexFloatExpression() {
            // 1.0 + 2.0 / (4.0 * 2.0) = 1.0 + 0.25 = 1.25
            Object result = engine.evaluate("1.0 + 2.0 / (4.0 * 2.0)");
            assertInstanceOf(Double.class, result);
            assertEquals(1.25, (Double) result, 1e-9);
        }
    }

    @Nested
    class MathProvider {
        ExpressionEngine mathEngine;

        @BeforeEach
        void setUp() {
            mathEngine = new ExpressionEngine();
        }

        @Test
        void sqrt() {
            assertEquals(2.0, (Double) mathEngine.evaluate("math.sqrt(4.0)"), 1e-9);
        }

        @Test
        void pow() {
            assertEquals(8.0, (Double) mathEngine.evaluate("math.pow(2.0, 3.0)"), 1e-9);
        }

        @Test
        void floor() {
            assertEquals(3L, mathEngine.evaluate("math.floor(3.9)"));
        }

        @Test
        void ceil() {
            assertEquals(4L, mathEngine.evaluate("math.ceil(3.1)"));
        }

        @Test
        void round() {
            assertEquals(4L, mathEngine.evaluate("math.round(3.6)"));
            assertEquals(3L, mathEngine.evaluate("math.round(3.4)"));
        }

        @Test
        void absLong() {
            assertEquals(-5L, -5L); // sanity
            assertEquals(5L, mathEngine.evaluate("math.abs(-5)"));
        }

        @Test
        void minMax() {
            assertEquals(2L, mathEngine.evaluate("math.min(2, 5)"));
            assertEquals(5L, mathEngine.evaluate("math.max(2, 5)"));
        }

        @Test
        void clamp() {
            assertEquals(5, mathEngine.evaluate("math.clamp(10, 0, 5)"));
            assertEquals(0, mathEngine.evaluate("math.clamp(-3, 0, 5)"));
            assertEquals(3, mathEngine.evaluate("math.clamp(3, 0, 5)"));
        }

        @Test
        void log() {
            assertEquals(Math.log(Math.E), (Double) mathEngine.evaluate("math.log(math.e())"), 1e-9);
        }

        @Test
        void pi() {
            assertEquals(Math.PI, (Double) mathEngine.evaluate("math.pi()"), 1e-9);
        }

        @Test
        void composedInArithmetic() {
            // 1.0 + 2.0 / (4.0 * math.sqrt(4.0)) = 1.0 + 2.0 / 8.0 = 1.25
            Object result = mathEngine.evaluate("1.0 + 2.0 / (4.0 * math.sqrt(4.0))");
            assertInstanceOf(Double.class, result);
            assertEquals(1.25, (Double) result, 1e-9);
        }

        @Test
        void comparisonWithMathResult() {
            assertTrue(mathEngine.eval("math.sqrt(4.0) > 1.0"));
            assertFalse(mathEngine.eval("math.sqrt(4.0) > 5.0"));
        }
    }

    /// Provider with two methods sharing the same name — should fail at construction time.
    @SuppressWarnings("unused")
    static class OverloadedProvider extends ContextProvider {
        OverloadedProvider() {
            super("overloaded");
        }

        public boolean check(String value) {
            return true;
        }

        public boolean check(int value) {
            return true;
        }
    }

    /// Provider with a custom 2-arg equals — must not be confused with Object.equals.
    @SuppressWarnings("unused")
    static class CustomEqualsProvider extends ContextProvider {
        CustomEqualsProvider() {
            super("custom");
        }

        public boolean equals(String a, String b) {
            return a.equals(b);
        }
    }

    /// Fake "std" provider
    @SuppressWarnings("unused")
    static class FakeStdProvider extends StdContextProvider {
        @Override
        public boolean isModLoaded(String modId) {
            return modId.equals("ftb-stages");
        }
    }

    /// Fake "level" provider
    @SuppressWarnings("unused")
    static class FakeLevelProvider extends ContextProvider {
        boolean day = true;
        boolean raining = false;
        boolean thundering = false;

        FakeLevelProvider() {
            super("level");
        }

        public boolean isDay() {
            return day;
        }

        public boolean isNight() {
            return !day;
        }

        public boolean isRaining() {
            return raining;
        }

        public boolean isThundering() {
            return thundering;
        }
    }

    @SuppressWarnings("unused")
    static class TestContextProvider extends ContextProvider {
        TestContextProvider() {
            super("test");
        }

        public String testString() {
            return "test";
        }

        public int add(int a, int b) {
            return a + b;
        }

        public float addFloat(float a, float b) {
            return a + b;
        }

        public int number() {
            return 42;
        }

        public float floatNumber() {
            return 3.141f;
        }

        public boolean startsWith(String input, String prefix) {
            return input.startsWith(prefix);
        }

        public boolean alwaysTrue() {
            return true;
        }

        public boolean alwaysFalse() {
            return false;
        }

        public long longNumber() {
            return 42L;
        }

        public BigInteger bigIntNumber() {
            return new BigInteger("99999999999999999999");
        }

        public BigInteger smallBigInt() {
            return BigInteger.valueOf(42);
        }
    }
}
