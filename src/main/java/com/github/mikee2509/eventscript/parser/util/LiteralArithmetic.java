package com.github.mikee2509.eventscript.parser.util;

import com.github.mikee2509.eventscript.EventScriptLexer;
import com.github.mikee2509.eventscript.domain.expression.Literal;
import org.antlr.v4.runtime.Token;
import org.springframework.stereotype.Service;

@Service
public class LiteralArithmetic {
    public Literal<Integer> decimalAdditiveOperation(Literal<Integer> left, Literal<Integer> right, Token bop) {
        switch (bop.getType()) {
            case EventScriptLexer.ADD:
                return new Literal<>(left.getValue() + right.getValue());
            default:
                return new Literal<>(left.getValue() - right.getValue());
        }
    }

    public Literal<Float> floatAdditiveOperation(Literal<Number> left, Literal<Number> right, Token bop) {
        switch (bop.getType()) {
            case EventScriptLexer.ADD:
                return new Literal<>(left.getValue().floatValue() + right.getValue().floatValue());
            default:
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
        switch (bop.getType()) {
            case EventScriptLexer.EQUAL:
                return new Literal<>(floatLeft.equals(floatRight));
            default:
                return new Literal<>(!floatLeft.equals(floatRight));
        }
    }
}
