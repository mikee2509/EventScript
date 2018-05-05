package com.github.mikee2509.eventscript.parser.visitor;

import com.github.mikee2509.eventscript.EventScriptLexer;
import com.github.mikee2509.eventscript.EventScriptParser;
import com.github.mikee2509.eventscript.EventScriptParserBaseVisitor;
import com.github.mikee2509.eventscript.domain.expression.Literal;
import com.github.mikee2509.eventscript.parser.exception.ArithmeticException;
import com.github.mikee2509.eventscript.parser.util.LiteralArithmetic;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;

@AllArgsConstructor
@Service
public class LiteralVisitor extends EventScriptParserBaseVisitor<Literal> {
    private LiteralArithmetic la;

    @Override
    public Literal visitDecimalLiteral(EventScriptParser.DecimalLiteralContext ctx) {
        return new Literal<>(Integer.valueOf(ctx.getText()));
    }

    @Override
    public Literal visitFloatLiteral(EventScriptParser.FloatLiteralContext ctx) {
        return new Literal<>(Float.valueOf(ctx.getText()));
    }

    @Override
    public Literal visitDatetimeLiteral(EventScriptParser.DatetimeLiteralContext ctx) {
        return super.visitDatetimeLiteral(ctx);
    }

    @Override
    public Literal visitDurationLiteral(EventScriptParser.DurationLiteralContext ctx) {
        return super.visitDurationLiteral(ctx);
    }

    @Override
    public Literal visitStringLiteral(EventScriptParser.StringLiteralContext ctx) {
        String string = ctx.getText().substring(1, ctx.getText().length() - 1);
        String replacedString = string
            .replaceAll("\\\\b", "\b")
            .replaceAll("\\\\t", "\t")
            .replaceAll("\\\\n", "\n")
            .replaceAll("\\\\f", "\f")
            .replaceAll("\\\\r", "\r")
            .replaceAll("\\\\\"", "\"")
            .replaceAll("\\\\\'", "\'")
            .replaceAll("\\\\\\\\", Matcher.quoteReplacement("\\"));
        return new Literal<>(replacedString);
    }

    @Override
    public Literal visitBoolLiteral(EventScriptParser.BoolLiteralContext ctx) {
        return new Literal<>(ctx.getText().equals("true"));
    }

    @Override
    public Literal visitAdditiveExp(EventScriptParser.AdditiveExpContext ctx) {
        Literal left = visitChildren(ctx.expression(0));
        Literal right = visitChildren(ctx.expression(1));

        if (left.isStringLiteral() || right.isStringLiteral()) {
            if (ctx.bop.getType() == EventScriptLexer.ADD) {
                return new Literal<>(left.getValue().toString() + right.getValue().toString());
            } else {
                throw new ArithmeticException(ctx.start, left, right, ArithmeticException.Operation.ADDITIVE);
            }
        }

        if (left.isFloatLiteral()) {
            if (right.isFloatLiteral() || right.isDecimalLiteral()) {
                return la.floatAdditiveOperation(left, right, ctx.bop);
            } else {
                throw new ArithmeticException(ctx.start, left, right, ArithmeticException.Operation.ADDITIVE);
            }
        } else if (left.isDecimalLiteral()) {
            if (right.isDecimalLiteral()) {
                return la.decimalAdditiveOperation(left, right, ctx.bop);
            } else if (right.isFloatLiteral()) {
                return la.floatAdditiveOperation(left, right, ctx.bop);
            } else {
                throw new ArithmeticException(ctx.start, left, right, ArithmeticException.Operation.ADDITIVE);
            }
        }

        throw new ArithmeticException(ctx.start, left, right, ArithmeticException.Operation.ADDITIVE);
    }

    @Override
    public Literal visitMultiplicativeExp(EventScriptParser.MultiplicativeExpContext ctx) {
        Literal left = visitChildren(ctx.expression(0));
        Literal right = visitChildren(ctx.expression(1));

        if (left.isFloatLiteral()) {
            if (right.isFloatLiteral() || right.isDecimalLiteral()) {
                return la.floatMultiplicativeOperation(left, right, ctx.bop);
            }
        } else if (left.isDecimalLiteral()) {
            if (right.isDecimalLiteral()) {
                return la.decimalMultiplicativeOperation(left, right, ctx.bop);
            } else if (right.isFloatLiteral()) {
                return la.floatMultiplicativeOperation(left, right, ctx.bop);
            }
        }

        throw new ArithmeticException(ctx.start, left, right, ArithmeticException.Operation.MULTIPLICATIVE);
    }
}
