package dev.ftb.mods.ftblibrary.expression;

import java.util.List;

/// Hierarchy of the lexical structure of the expression language — the nodes of the abstract syntax tree (AST).
/// for consumption via the [ExpressionEngine]
public sealed interface Node
        permits Node.BinaryOp, Node.UnaryOp, Node.Comparison,
                Node.ProviderCall,
                Node.StringLiteral, Node.IntLiteral, Node.FloatLiteral, Node.Float32Literal, Node.BoolLiteral {

    record BinaryOp(Node left, BinaryOp.Op op, Node right) implements Node {
        public enum Op {AND, OR}
    }

    record UnaryOp(UnaryOp.Op op, Node operand) implements Node {
        public enum Op {NOT}
    }

    record Comparison(Node left, Comparison.Op op, Node right) implements Node {
        public enum Op {
            IS, IS_NOT, EQ, NEQ, LT, GT, LTE, GTE;

            public static Op from(Token.TokenType type) {
                return switch (type) {
                    case IS -> IS;
                    case IS_NOT -> IS_NOT;
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

    /// An integer literal — stored as {@code long} to cover the full 64-bit range
    /// without the precision loss that would occur if stored as {@code double}.
    record IntLiteral(long value) implements Node {
    }

    /// A double-precision floating-point literal produced by bare decimals ({@code 3.14}) or the {@code d}/{@code D} suffix ({@code 3.14d}).
    record FloatLiteral(double value) implements Node {
    }

    /// A single-precision floating-point literal produced by the {@code f}/{@code F} suffix ({@code 3.14f}).
    record Float32Literal(float value) implements Node {
    }

    record BoolLiteral(boolean value) implements Node {
    }
}
