package dev.ftb.mods.ftblibrary.expression;

/// Lexical token produced by the [Lexer] and consumed by the [ExpressionParser].
public record Token(TokenType type, String value, int pos) {

    /// Each token type represents a specific part of the expression provided
    public enum TokenType {
        STRING,         // 'hello'
        INTEGER,        // 42, -7
        FLOAT,          // 3.14, -0.5, 3.14d, 3.14D
        FLOAT32,        // 3.14f, 3.14F
        TRUE,           // true
        FALSE,          // false

        IDENTIFIER,     // text

        AND,            // and / &&
        OR,             // or / ||
        NOT,            // not / !

        IS,             // is
        IS_NOT,         // is not
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
