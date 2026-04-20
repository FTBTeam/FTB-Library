package dev.ftb.mods.ftblibrary.expression;

import dev.ftb.mods.ftblibrary.expression.exceptions.ExpressionParseException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/// Lexer for our expression language. Takes in a user provided string and attempts to break it down into a token tree.
public class Lexer {
    /// Lookup table for single-character operators and punctuation
    private static final Map<Character, Token.TokenType> SINGLE_CHAR_LOOKUP = Map.of(
            '<', Token.TokenType.LT,
            '>', Token.TokenType.GT,
            '!', Token.TokenType.NOT,
            '.', Token.TokenType.DOT,
            '(', Token.TokenType.LPAREN,
            ')', Token.TokenType.RPAREN,
            ',', Token.TokenType.COMMA
    );

    /// Alternative table for dual-character operators, which require lookahead to distinguish from their single-character counterparts.
    /// For example, '!' is a NOT operator, but '!=' is a NEQ operator.
    private static final Map<Character, Map<Character, Token.TokenType>> DUAL_CHAR_LOOKUP = Map.of(
            '&', Map.of('&', Token.TokenType.AND),
            '|', Map.of('|', Token.TokenType.OR),
            '=', Map.of('=', Token.TokenType.EQ),
            '!', Map.of('=', Token.TokenType.NEQ),
            '<', Map.of('=', Token.TokenType.LTE),
            '>', Map.of('=', Token.TokenType.GTE)
    );

    /// Lookup table for reserved keywords
    private static final Map<String, Token.TokenType> KEYWORDS = Map.of(
            "true", Token.TokenType.TRUE,
            "false", Token.TokenType.FALSE
    );

    private final String src;
    private int pos;

    public Lexer(String src) {
        this.src = src;
        this.pos = 0;
    }

    /// Tokenize the input string and return a list of tokens. Throws [ExpressionParseException] if an invalid token is encountered.
    public List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();
        while (pos < src.length()) {
            skipWhitespace();
            if (pos >= src.length()) break;

            char c = src.charAt(pos);

            if (c == '\'') {
                tokens.add(readString());
            } else if (Character.isDigit(c) || (c == '-' && pos + 1 < src.length() && Character.isDigit(src.charAt(pos + 1)))) {
                tokens.add(readNumber());
            } else if (Character.isLetter(c) || c == '_') {
                tokens.add(readIdentifierOrKeyword());
            } else {
                var twoChar = DUAL_CHAR_LOOKUP.get(c);
                if (twoChar != null && twoChar.containsKey(peek())) {
                    Token.TokenType type = twoChar.get(peek());
                    tokens.add(token(type, String.valueOf(c) + peek()));
                    pos += 2;
                } else {
                    Token.TokenType single = SINGLE_CHAR_LOOKUP.get(c);
                    if (single != null) {
                        tokens.add(token(single, String.valueOf(c)));
                        pos++;
                    } else {
                        throw new ExpressionParseException("Unexpected character '" + c + "' at position " + pos);
                    }
                }
            }
        }

        tokens.add(new Token(Token.TokenType.EOF, "", pos));
        return tokens;
    }

    /// Attempts to read a given "string" type token, starting with the opening quote. Handles escaped characters and throws if the string is not properly terminated.
    private Token readString() {
        int start = pos;
        pos++; // consume opening '
        StringBuilder sb = new StringBuilder();
        while (pos < src.length() && src.charAt(pos) != '\'') {
            if (src.charAt(pos) == '\\' && pos + 1 < src.length()) {
                pos++; // skip backslash
                sb.append(src.charAt(pos));
            } else {
                sb.append(src.charAt(pos));
            }
            pos++;
        }
        if (pos >= src.length()) {
            throw new ExpressionParseException("Unterminated string literal starting at position " + start);
        }
        pos++; // consume closing '
        return new Token(Token.TokenType.STRING, sb.toString(), start);
    }

    /// Attempts to read a numeric token, classifying it by suffix (case-insensitive) to match Java literal conventions:
    ///
    /// We support:
    /// - Nothing for a normal integer,
    /// - d/D for double,
    /// - f/F for float,
    /// - l/L for long
    /// - Bi/bi for BigInts
    private Token readNumber() {
        int start = pos;
        StringBuilder builder = new StringBuilder();
        if (src.charAt(pos) == '-') {
            builder.append('-');
            pos++;
        }

        boolean isDecimal = false;
        while (pos < src.length() && (Character.isDigit(src.charAt(pos)) || src.charAt(pos) == '.')) {
            if (src.charAt(pos) == '.') {
                if (isDecimal) break;
                isDecimal = true;
            }
            builder.append(src.charAt(pos));
            pos++;
        }

        // Check for optional type suffix
        if (pos < src.length()) {
            char c = src.charAt(pos);
            char next = (pos + 1 < src.length()) ? src.charAt(pos + 1) : '\0';

            if (c == 'f' || c == 'F') {
                pos++;
                return new Token(Token.TokenType.FLOAT, builder.toString(), start);
            } else if (c == 'd' || c == 'D') {
                pos++;
                return new Token(Token.TokenType.DOUBLE, builder.toString(), start);
            } else if (c == 'l' || c == 'L') {
                pos++;
                return new Token(Token.TokenType.LONG, builder.toString(), start);
            } else if ((c == 'b' || c == 'B') && (next == 'i' || next == 'I')) {
                pos += 2;
                return new Token(Token.TokenType.BIGINT, builder.toString(), start);
            }
        }

        // If the f/d didn't catch then we fall back to a double.
        Token.TokenType type = isDecimal ? Token.TokenType.DOUBLE : Token.TokenType.INT;
        return new Token(type, builder.toString(), start);
    }

    /// Attempt to parse either an 'identifier' or a 'keyword'. We support a small set of keywords which are converted
    /// into their lexical token type.
    private Token readIdentifierOrKeyword() {
        int start = pos;
        StringBuilder sb = new StringBuilder();
        while (pos < src.length()) {
            char c = src.charAt(pos);
            if (Character.isLetterOrDigit(c) || c == '_') {
                sb.append(c);
                pos++;
            } else {
                break;
            }
        }

        String word = sb.toString();
        String lower = word.toLowerCase();

        Token.TokenType keyword = KEYWORDS.get(lower);
        if (keyword != null) {
            return new Token(keyword, word, start);
        }

        return new Token(Token.TokenType.IDENTIFIER, word, start);
    }

    private void skipWhitespace() {
        while (pos < src.length() && Character.isWhitespace(src.charAt(pos))) {
            pos++;
        }
    }

    private char peek() {
        return (pos + 1 < src.length()) ? src.charAt(pos + 1) : '\0';
    }

    private Token token(Token.TokenType type, String value) {
        return new Token(type, value, pos);
    }
}
