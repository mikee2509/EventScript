package com.github.mikee2509.eventscript.parser.util;

import com.github.mikee2509.eventscript.EventScriptLexer;
import com.github.mikee2509.eventscript.domain.expression.Literal;
import org.antlr.v4.runtime.Token;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class LiteralArithmetic {
    public Literal<Integer> decimalAdditiveOperation(Literal<Integer> left, Literal<Integer> right, Token bop) {
        if (bop.getType() == EventScriptLexer.ADD) {
            return new Literal<>(left.getValue() + right.getValue());
        } else {
            return new Literal<>(left.getValue() - right.getValue());
        }
    }

    public Literal<Float> floatAdditiveOperation(Literal<Number> left, Literal<Number> right, Token bop) {
        if (bop.getType() == EventScriptLexer.ADD) {
            return new Literal<>(left.getValue().floatValue() + right.getValue().floatValue());
        } else {
            return new Literal<>(left.getValue().floatValue() - right.getValue().floatValue());
        }
    }

    public Literal<Integer> decimalMultiplicativeOperation(Literal<Integer> left, Literal<Integer> right, Token bop) {
        switch (bop.getType()) {
            case EventScriptLexer.MUL:
                return new Literal<>(left.getValue() * right.getValue());
            case EventScriptLexer.DIV:
                return new Literal<>(left.getValue() / right.getValue());
            default:
                return new Literal<>(left.getValue() % right.getValue());
        }
    }

    public Literal<Float> floatMultiplicativeOperation(Literal<Number> left, Literal<Number> right, Token bop) {
        switch (bop.getType()) {
            case EventScriptLexer.MUL:
                return new Literal<>(left.getValue().floatValue() * right.getValue().floatValue());
            case EventScriptLexer.DIV:
                return new Literal<>(left.getValue().floatValue() / right.getValue().floatValue());
            default:
                return new Literal<>(left.getValue().floatValue() % right.getValue().floatValue());
        }
    }

    public Literal<Boolean> floatEqualityOperation(Literal<Number> left, Literal<Number> right, Token bop) {
        Float floatLeft = left.getValue().floatValue();
        Float floatRight = right.getValue().floatValue();
        if (bop.getType() == EventScriptLexer.EQUAL) {
            return new Literal<>(floatLeft.equals(floatRight));
        } else {
            return new Literal<>(!floatLeft.equals(floatRight));
        }
    }

    public Literal<Boolean> decimalRelationalOperation(Literal<Integer> left, Literal<Integer> right, Token bop) {
        int intLeft = left.getValue();
        int intRight = right.getValue();
        return relationalOperation(bop, intLeft < intRight, intLeft > intRight, intLeft <= intRight,
            intLeft >= intRight);
    }

    public Literal<Boolean> floatRelationalOperation(Literal<Number> left, Literal<Number> right, Token bop) {
        float floatLeft = left.getValue().floatValue();
        float floatRight = right.getValue().floatValue();
        return relationalOperation(bop, floatLeft < floatRight, floatLeft > floatRight, floatLeft <= floatRight,
            floatLeft >= floatRight);
    }

    public Literal<Boolean> durationRelationalOperation(Literal<Duration> leftLiteral, Literal<Duration> rightLiteral,
                                                        Token bop) {
        Duration left = leftLiteral.getValue();
        Duration right = rightLiteral.getValue();
        return relationalOperation(bop, left.compareTo(right) < 0, left.compareTo(right) > 0,
            left.compareTo(right) <= 0, left.compareTo(right) >= 0);
    }

    public Literal<Boolean> datetimeRelationalOperation(Literal<LocalDateTime> leftLiteral,
                                                        Literal<LocalDateTime> rightLiteral, Token bop) {
        LocalDateTime left = leftLiteral.getValue();
        LocalDateTime right = rightLiteral.getValue();
        return relationalOperation(bop, left.compareTo(right) < 0, left.compareTo(right) > 0,
            left.compareTo(right) <= 0, left.compareTo(right) >= 0);
    }

    private Literal<Boolean> relationalOperation(Token bop, boolean lt, boolean gt, boolean le, boolean ge) {
        switch (bop.getType()) {
            case EventScriptLexer.LT:
                return new Literal<>(lt);
            case EventScriptLexer.GT:
                return new Literal<>(gt);
            case EventScriptLexer.LE:
                return new Literal<>(le);
            default:
                return new Literal<>(ge);

        }
    }
}
