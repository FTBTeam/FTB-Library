package dev.ftb.mods.ftblibrary.expression;

/// Lexical token produced by the [Lexer] and consumed by the [ExpressionParser].
public record Token(TokenType type, String value, int pos) {

    /// Each token type represents a specific part of the expression provided
    public enum TokenType {
        STRING,         // 'hello'
        INT,            // 42, -7
        LONG,           // 42L, 42l
        FLOAT,          // 3.14f, 3.14F
        DOUBLE,         // 3.14, 3.14d, 3.14D
        BIGINT,         // 42bi, 42BI
        TRUE,           // true
        FALSE,          // false

        IDENTIFIER,     // text

        AND,            // &&
        OR,             // ||
        XOR,            // ^
        NOT,            // !

        PLUS,           // +
        MINUS,          // -
        STAR,           // *
        SLASH,          // /
        PERCENT,        // %

        EQ,             // ==
        NEQ,            // !=
        LT,             // <
        GT,             // >
        LTE,            // <=
        GTE,            // >=

        DOT,            // .
        LPAREN,         // (
        RPAREN,         // )
        COMMA,          // ,

        EOF
    }

    @Override
    public String toString() {
        return "[" + type + " \"" + value + "\" @" + pos + "]";
    }
}
