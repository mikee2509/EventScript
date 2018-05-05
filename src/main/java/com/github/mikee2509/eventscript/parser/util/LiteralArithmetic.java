package com.github.mikee2509.eventscript.parser.util;

import com.github.mikee2509.eventscript.EventScriptLexer;
import com.github.mikee2509.eventscript.domain.expression.Literal;
import org.antlr.v4.runtime.Token;
import org.springframework.stereotype.Service;

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
        if (bop.getType() == EventScriptLexer.MUL) {
            return new Literal<>(left.getValue() * right.getValue());
        } else if (bop.getType() == EventScriptLexer.DIV) {
            return new Literal<>(left.getValue() / right.getValue());
        } else {
            return new Literal<>(left.getValue() % right.getValue());
        }
    }

    public Literal<Float> floatMultiplicativeOperation(Literal<Number> left, Literal<Number> right, Token bop) {
        if (bop.getType() == EventScriptLexer.MUL) {
            return new Literal<>(left.getValue().floatValue() * right.getValue().floatValue());
        } else if (bop.getType() == EventScriptLexer.DIV) {
            return new Literal<>(left.getValue().floatValue() / right.getValue().floatValue());
        } else {
            return new Literal<>(left.getValue().floatValue() % right.getValue().floatValue());
        }
    }
}
