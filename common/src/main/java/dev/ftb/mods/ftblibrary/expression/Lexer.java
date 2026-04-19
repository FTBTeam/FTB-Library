package dev.ftb.mods.ftblibrary.expression;

import dev.ftb.mods.ftblibrary.expression.exceptions.ExpressionParseException;

import java.util.ArrayList;
import java.util.List;

/// Lexer for our expression language. Takes in a user provided string and attempts to break it down into a token tree.
public class Lexer {

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
            } else if (c == '&' && peek() == '&') {
                tokens.add(token(Token.TokenType.AND, "&&"));
                pos += 2;
            } else if (c == '|' && peek() == '|') {
                tokens.add(token(Token.TokenType.OR, "||"));
                pos += 2;
            } else if (c == '=' && peek() == '=') {
                tokens.add(token(Token.TokenType.EQ, "=="));
                pos += 2;
            } else if (c == '!' && peek() == '=') {
                tokens.add(token(Token.TokenType.NEQ, "!="));
                pos += 2;
            } else if (c == '<' && peek() == '=') {
                tokens.add(token(Token.TokenType.LTE, "<="));
                pos += 2;
            } else if (c == '>' && peek() == '=') {
                tokens.add(token(Token.TokenType.GTE, ">="));
                pos += 2;
            } else if (c == '<') {
                tokens.add(token(Token.TokenType.LT, "<"));
                pos++;
            } else if (c == '>') {
                tokens.add(token(Token.TokenType.GT, ">"));
                pos++;
            } else if (c == '!') {
                tokens.add(token(Token.TokenType.NOT, "!"));
                pos++;
            } else if (c == '.') {
                tokens.add(token(Token.TokenType.DOT, "."));
                pos++;
            } else if (c == '(') {
                tokens.add(token(Token.TokenType.LPAREN, "("));
                pos++;
            } else if (c == ')') {
                tokens.add(token(Token.TokenType.RPAREN, ")"));
                pos++;
            } else if (c == ',') {
                tokens.add(token(Token.TokenType.COMMA, ","));
                pos++;
            } else {
                throw new ExpressionParseException("Unexpected character '" + c + "' at position " + pos);
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

    /// Attempts to read a "number" then breaking it down into INTEGER, FLOAT (double), or FLOAT32 (float).
    ///
    /// Suffix rules (case-insensitive, mimicking Java):
    /// - {@code f} / {@code F} → FLOAT32 (32-bit float precision)
    /// - {@code d} / {@code D} → FLOAT (64-bit double precision, explicit)
    /// - No suffix + {@code .} present → FLOAT
    /// - No suffix + no {@code .} → INTEGER
    private Token readNumber() {
        int start = pos;
        StringBuilder builder = new StringBuilder();
        if (src.charAt(pos) == '-') {
            builder.append('-');
            pos++;
        }

        boolean isFloat = false;
        while (pos < src.length() && (Character.isDigit(src.charAt(pos)) || src.charAt(pos) == '.')) {
            if (src.charAt(pos) == '.') {
                if (isFloat) break;
                isFloat = true;
            }
            builder.append(src.charAt(pos));
            pos++;
        }

        // Check for optional type suffix: f/F → FLOAT32, d/D → FLOAT (double)
        if (pos < src.length()) {
            char suffix = src.charAt(pos);
            if (suffix == 'f' || suffix == 'F') {
                pos++;
                return new Token(Token.TokenType.FLOAT32, builder.toString(), start);
            } else if (suffix == 'd' || suffix == 'D') {
                pos++;
                return new Token(Token.TokenType.FLOAT, builder.toString(), start);
            }
        }

        Token.TokenType type = isFloat ? Token.TokenType.FLOAT : Token.TokenType.INTEGER;
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
        return switch (word.toLowerCase()) {
            case "and" -> new Token(Token.TokenType.AND, word, start);
            case "or" -> new Token(Token.TokenType.OR, word, start);
            case "not" -> new Token(Token.TokenType.NOT, word, start);
            case "true" -> new Token(Token.TokenType.TRUE, word, start);
            case "false" -> new Token(Token.TokenType.FALSE, word, start);
            // 'is' is a little more complicated as we need to look ahead to see if we're doing an 'is' or an 'is not'
            // expression which changes the meaning of the keyword.
            case "is" -> {
                int savedPos = pos;
                skipWhitespace();
                if (pos + 3 <= src.length() && src.substring(pos, pos + 3).equalsIgnoreCase("not")
                        && (pos + 3 >= src.length() || !Character.isLetterOrDigit(src.charAt(pos + 3)))) {
                    pos += 3;
                    yield new Token(Token.TokenType.IS_NOT, "is not", start);
                }

                pos = savedPos;
                yield new Token(Token.TokenType.IS, word, start);
            }
            // If we're not a keyword, we're an 'identifier'
            default -> new Token(Token.TokenType.IDENTIFIER, word, start);
        };
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
