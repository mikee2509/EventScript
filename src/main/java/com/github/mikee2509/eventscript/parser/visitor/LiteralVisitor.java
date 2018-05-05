package com.github.mikee2509.eventscript.parser.visitor;

import com.github.mikee2509.eventscript.EventScriptLexer;
import com.github.mikee2509.eventscript.EventScriptParser;
import com.github.mikee2509.eventscript.EventScriptParserBaseVisitor;
import com.github.mikee2509.eventscript.domain.expression.Literal;
import com.github.mikee2509.eventscript.parser.exception.Operation;
import com.github.mikee2509.eventscript.parser.exception.OperationException;
import com.github.mikee2509.eventscript.parser.util.LiteralArithmetic;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;

@AllArgsConstructor
@Service
public class LiteralVisitor extends EventScriptParserBaseVisitor<Literal> {
    private LiteralArithmetic la;

    private interface LiteralOperation {
        Literal execute();
    }

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
                throw new OperationException(ctx.start, left, right, Operation.ADDITIVE);
            }
        }

        return applyOperation(left, right,
            () -> la.decimalAdditiveOperation(left, right, ctx.bop),
            () -> la.floatAdditiveOperation(left, right, ctx.bop),
            new OperationException(ctx.start, left, right, Operation.ADDITIVE));
    }

    @Override
    public Literal visitMultiplicativeExp(EventScriptParser.MultiplicativeExpContext ctx) {
        Literal left = visitChildren(ctx.expression(0));
        Literal right = visitChildren(ctx.expression(1));

        return applyOperation(left, right,
            () -> la.decimalMultiplicativeOperation(left, right, ctx.bop),
            () -> la.floatMultiplicativeOperation(left, right, ctx.bop),
            new OperationException(ctx.start, left, right, Operation.MULTIPLICATIVE));
    }

    private Literal applyOperation(Literal left, Literal right, LiteralOperation decimalOperation,
                                   LiteralOperation floatOperation, OperationException exception) {
        if (left.isFloatLiteral()) {
            if (right.isFloatLiteral() || right.isDecimalLiteral()) {
                return floatOperation.execute();
            }
        } else if (left.isDecimalLiteral()) {
            if (right.isDecimalLiteral()) {
                return decimalOperation.execute();
            } else if (right.isFloatLiteral()) {
                return floatOperation.execute();
            }
        }

        throw exception;
    }

    @Override
    public Literal visitUnaryExp(EventScriptParser.UnaryExpContext ctx) {
        Literal expression = visitChildren(ctx.expression());

        switch (ctx.prefix.getType()) {
            case EventScriptLexer.INC:
                return applyOperation(ctx, expression,
                    () -> new Literal<>((Integer) expression.getValue() + 1),
                    () -> new Literal<>((Float) expression.getValue() + 1.0f));
            case EventScriptLexer.DEC:
                return applyOperation(ctx, expression,
                    () -> new Literal<>((Integer) expression.getValue() - 1),
                    () -> new Literal<>((Float) expression.getValue() - 1.0f));
            case EventScriptLexer.ADD:
                return applyOperation(ctx, expression,
                    () -> expression,
                    () -> expression);
            case EventScriptLexer.SUB:
                return applyOperation(ctx, expression,
                    () -> new Literal<>(-(Integer) expression.getValue()),
                    () -> new Literal<>(-(Float) expression.getValue()));
        }

        throw new OperationException(ctx.start, expression, Operation.UNARY);
    }

    private Literal applyOperation(EventScriptParser.UnaryExpContext ctx, Literal expression,
                                   LiteralOperation decimalOperation, LiteralOperation floatOperation) {
        if (expression.isDecimalLiteral()) {
            return decimalOperation.execute();
        } else if (expression.isFloatLiteral()) {
            return floatOperation.execute();
        } else {
            throw new OperationException(ctx.start, expression, Operation.UNARY);
        }
    }

    @Override
    public Literal visitNegationExp(EventScriptParser.NegationExpContext ctx) {
        Literal expression = visitChildren(ctx.expression());

        if (expression.isBoolLiteral()) {
            return new Literal<>(!(Boolean) expression.getValue());
        } else {
            throw new OperationException(ctx.start, expression, Operation.NEGATION);
        }
    }
//
//    @Override
//    public Literal visitEqualityExp(EventScriptParser.EqualityExpContext ctx) {
//        Literal left = visitChildren(ctx.expression(0));
//        Literal right = visitChildren(ctx.expression(1));
//
//        if (left.isBoolLiteral() && right.isBoolLiteral()) {
//            switch (ctx.bop.getType()) {
//                case EventScriptLexer.EQUAL:
//                    return new Literal<>(left.getValue().equals(right.getValue()));
//                case EventScriptLexer.NOTEQUAL:
//                    return new Literal<>(!left.getValue().equals(right.getValue()));
//            }
//        }
//
//        throw OperationException.bothOperandsMustBeBool(ctx.start);
//    }


    @Override
    public Literal visitLogicalAndExp(EventScriptParser.LogicalAndExpContext ctx) {
        Literal left = visitChildren(ctx.expression(0));
        Literal right = visitChildren(ctx.expression(1));

        if (left.isBoolLiteral() && right.isBoolLiteral()) {
            return new Literal<>((Boolean) left.getValue() && (Boolean) right.getValue());
        }

        throw OperationException.bothOperandsMustBeBool(ctx.start);
    }

    @Override
    public Literal visitLogicalOrExp(EventScriptParser.LogicalOrExpContext ctx) {
        Literal left = visitChildren(ctx.expression(0));
        Literal right = visitChildren(ctx.expression(1));

        if (left.isBoolLiteral() && right.isBoolLiteral()) {
            return new Literal<>((Boolean) left.getValue() || (Boolean) right.getValue());
        }

        throw OperationException.bothOperandsMustBeBool(ctx.start);
    }
}
