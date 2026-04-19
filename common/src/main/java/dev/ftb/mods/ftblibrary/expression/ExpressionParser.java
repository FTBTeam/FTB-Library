package dev.ftb.mods.ftblibrary.expression;

import dev.ftb.mods.ftblibrary.expression.exceptions.ExpressionParseException;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/// A 'Recursive-descent' expression parser for our expression language roughly based on the systems outlined per
/// [The wiki on Recursive-descent](https://en.wikipedia.org/wiki/Recursive_descent_parser)
public class ExpressionParser {
    private final List<Token> tokens;
    private int cursor;

    public ExpressionParser(String input) {
        this.tokens = new Lexer(input).tokenize();
        this.cursor = 0;
    }

    public Node parse() {
        Node result = parseOr();
        expect(Token.TokenType.EOF);
        return result;
    }

    private Node parseOr() {
        Node left = parseAnd();
        while (check(Token.TokenType.OR)) {
            advance();
            Node right = parseAnd();
            left = new Node.BinaryOp(left, Node.BinaryOp.Op.OR, right);
        }
        return left;
    }

    private Node parseAnd() {
        Node left = parseNot();
        while (check(Token.TokenType.AND)) {
            advance();
            Node right = parseNot();
            left = new Node.BinaryOp(left, Node.BinaryOp.Op.AND, right);
        }
        return left;
    }

    private Node parseNot() {
        if (check(Token.TokenType.NOT)) {
            advance();
            Node operand = parseNot();
            return new Node.UnaryOp(Node.UnaryOp.Op.NOT, operand);
        }
        return parseComparison();
    }

    private Node parseComparison() {
        Node left = parseAtom();
        if (isComparisonOp(peek().type())) {
            Token op = advance();
            Node right = parseAtom();
            return new Node.Comparison(left, Node.Comparison.Op.from(op.type()), right);
        }
        return left;
    }

    /// Parse the individual 'atom' elements of the expression: literals, parenthesized sub-expressions, and provider calls.
    private Node parseAtom() {
        Token token = peek();

        return switch (token.type()) {
            case LPAREN -> {
                advance(); // consume (
                Node inner = parseOr();
                expect(Token.TokenType.RPAREN);
                yield inner;
            }
            case TRUE -> {
                advance();
                yield new Node.BoolLiteral(true);
            }
            case FALSE -> {
                advance();
                yield new Node.BoolLiteral(false);
            }
            case STRING -> {
                advance();
                yield new Node.StringLiteral(token.value());
            }
            case INT -> {
                advance();
                yield parseIntLiteral(token);
            }
            case LONG -> {
                advance();
                yield parseLongLiteral(token);
            }
            case FLOAT -> {
                advance();
                yield parseFloatLiteral(token);
            }
            case DOUBLE -> {
                advance();
                yield parseDoubleLiteral(token);
            }
            case BIGINT -> {
                advance();
                yield parseBigIntLiteral(token);
            }
            case IDENTIFIER -> {
                advance();
                String firstName = token.value();

                if (check(Token.TokenType.DOT)) {
                    advance();
                    Token method = expect(Token.TokenType.IDENTIFIER);
                    expect(Token.TokenType.LPAREN);
                    List<Node> args = parseArgList();
                    expect(Token.TokenType.RPAREN);
                    yield new Node.ProviderCall(firstName, method.value(), args);
                } else {
                    throw new ExpressionParseException("Unexpected bare identifier '" + firstName + "' at position " + token.pos() + ". All calls must be namespaced: '" + firstName + ".method()'.");
                }
            }
            default -> throw new ExpressionParseException(
                    "Unexpected token " + token + " at position " + token.pos());
        };
    }

    private List<Node> parseArgList() {
        List<Node> args = new ArrayList<>();
        if (!check(Token.TokenType.RPAREN)) {
            args.add(parseOr());
            while (check(Token.TokenType.COMMA)) {
                advance();
                args.add(parseOr());
            }
        }
        return args;
    }

    private boolean isComparisonOp(Token.TokenType type) {
        return switch (type) {
            case IS, IS_NOT, EQ, NEQ, LT, GT, LTE, GTE -> true;
            default -> false;
        };
    }

    private Node.IntLiteral parseIntLiteral(Token t) {
        try {
            return new Node.IntLiteral(Integer.parseInt(t.value()));
        } catch (NumberFormatException e) {
            throw new ExpressionParseException("Invalid integer literal '" + t.value() + "' at position " + t.pos());
        }
    }

    private Node.LongLiteral parseLongLiteral(Token t) {
        try {
            return new Node.LongLiteral(Long.parseLong(t.value()));
        } catch (NumberFormatException e) {
            throw new ExpressionParseException("Invalid long literal '" + t.value() + "' at position " + t.pos());
        }
    }

    private Node.FloatLiteral parseFloatLiteral(Token t) {
        try {
            return new Node.FloatLiteral(Float.parseFloat(t.value()));
        } catch (NumberFormatException e) {
            throw new ExpressionParseException("Invalid float literal '" + t.value() + "' at position " + t.pos());
        }
    }

    private Node.DoubleLiteral parseDoubleLiteral(Token t) {
        try {
            return new Node.DoubleLiteral(Double.parseDouble(t.value()));
        } catch (NumberFormatException e) {
            throw new ExpressionParseException("Invalid double literal '" + t.value() + "' at position " + t.pos());
        }
    }

    private Node.BigIntLiteral parseBigIntLiteral(Token t) {
        try {
            return new Node.BigIntLiteral(new BigInteger(t.value()));
        } catch (NumberFormatException e) {
            throw new ExpressionParseException("Invalid BigInteger literal '" + t.value() + "' at position " + t.pos());
        }
    }

    private Token peek() {
        return tokens.get(cursor);
    }

    private boolean check(Token.TokenType type) {
        return peek().type() == type;
    }

    private Token advance() {
        Token t = tokens.get(cursor);
        cursor++;
        return t;
    }

    private Token expect(Token.TokenType type) {
        Token t = peek();
        if (t.type() != type) {
            throw new ExpressionParseException(
                    "Expected " + type + " but found " + t.type() + " (\"" + t.value() + "\") at position " + t.pos());
        }
        return advance();
    }
}
