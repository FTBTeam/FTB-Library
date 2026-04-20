package dev.ftb.mods.ftblibrary.expression;

import java.math.BigInteger;
import java.util.List;

/// Hierarchy of the lexical structure of the expression language — the nodes of the abstract syntax tree (AST).
/// for consumption via the [ExpressionEngine]
public sealed interface Node
        permits Node.BinaryOp, Node.UnaryOp, Node.ArithmeticOp, Node.UnaryMinus, Node.Comparison,
                Node.ProviderCall,
                Node.StringLiteral,
                Node.IntLiteral, Node.LongLiteral, Node.FloatLiteral, Node.DoubleLiteral, Node.BigIntLiteral,
                Node.BoolLiteral {

    record BinaryOp(Node left, BinaryOp.Op op, Node right) implements Node {
        public enum Op {AND, OR, XOR}
    }

    record UnaryOp(UnaryOp.Op op, Node operand) implements Node {
        public enum Op {NOT}
    }

    /// A binary arithmetic operation like `a + b`, `b /a`
    record ArithmeticOp(Node left, ArithmeticOp.Op op, Node right) implements Node {
        public enum Op {
            ADD, SUB, MUL, DIV, MOD;

            public static Op from(Token.TokenType type) {
                return switch (type) {
                    case PLUS    -> ADD;
                    case MINUS   -> SUB;
                    case STAR    -> MUL;
                    case SLASH   -> DIV;
                    case PERCENT -> MOD;
                    default -> throw new IllegalArgumentException("Not an arithmetic operator: " + type);
                };
            }
        }
    }

    /// Unary negation like `-test.number()`.
    record UnaryMinus(Node operand) implements Node {
    }

    record Comparison(Node left, Comparison.Op op, Node right) implements Node {
        public enum Op {
            EQ, NEQ, LT, GT, LTE, GTE;

            public static Op from(Token.TokenType type) {
                return switch (type) {
                    case EQ -> EQ;
                    case NEQ -> NEQ;
                    case LT -> LT;
                    case GT -> GT;
                    case LTE -> LTE;
                    case GTE -> GTE;
                    default -> throw new IllegalArgumentException("Not a comparison operator: " + type);
                };
            }
        }
    }

    record ProviderCall(String providerName, String methodName, List<Node> args) implements Node {
    }

    record StringLiteral(String value) implements Node {
    }

    /// A bare integer literal with no suffix `42`.
    record IntLiteral(int value) implements Node {
    }

    /// An explicit long literal `42L`.
    record LongLiteral(long value) implements Node {
    }

    /// A 32-bit float literal `3.141f`.
    record FloatLiteral(float value) implements Node {
    }

    /// A 64-bit double literal `3.141` or `3.141d`
    record DoubleLiteral(double value) implements Node {
    }

    /// An arbitrary-precision integer literal `12345678901234567890bi.
    record BigIntLiteral(BigInteger value) implements Node {
    }

    record BoolLiteral(boolean value) implements Node {
    }
}
