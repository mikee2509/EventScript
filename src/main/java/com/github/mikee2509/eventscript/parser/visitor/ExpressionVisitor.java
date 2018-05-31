package com.github.mikee2509.eventscript.parser.visitor;

import com.github.mikee2509.eventscript.EventScriptLexer;
import com.github.mikee2509.eventscript.EventScriptParser;
import com.github.mikee2509.eventscript.EventScriptParserBaseVisitor;
import com.github.mikee2509.eventscript.domain.exception.parser.LiteralException;
import com.github.mikee2509.eventscript.domain.exception.parser.Operation;
import com.github.mikee2509.eventscript.domain.exception.parser.OperationException;
import com.github.mikee2509.eventscript.domain.exception.parser.ScopeException;
import com.github.mikee2509.eventscript.domain.expression.Literal;
import com.github.mikee2509.eventscript.domain.expression.Tuple;
import com.github.mikee2509.eventscript.domain.expression.Type;
import com.github.mikee2509.eventscript.domain.scope.Declarable;
import com.github.mikee2509.eventscript.parser.util.LiteralArithmetic;
import com.github.mikee2509.eventscript.parser.util.ScopeManager;
import lombok.AllArgsConstructor;
import lombok.extern.java.Log;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.stream.Stream;

@Log
@AllArgsConstructor
public class ExpressionVisitor extends EventScriptParserBaseVisitor<Literal> {
    private ScopeManager scope;
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

    private Integer getIntValue(Tuple tuple, int i) {
        return (Integer) tuple.literals()[i].getValue();
    }

    @Override
    public Literal visitDatetimeLiteral(EventScriptParser.DatetimeLiteralContext ctx) {
        Literal parameters = ctx.parExpressionList().accept(this);
        if (parameters.isVoidLiteral()) {
            return new Literal<>(LocalDateTime.now());
        } else if (parameters.isTupleLiteral()) {
            Tuple tuple = (Tuple) parameters.getValue();
            if (tuple.size() < 5 || tuple.size() > 6 || Stream.of(tuple.types()).anyMatch(t -> t != Type.INT)) {
                throw LiteralException.wrongDatetimeParameters(ctx.start);
            }
            LocalDateTime time = LocalDateTime.of(getIntValue(tuple, 0), getIntValue(tuple, 1), getIntValue(tuple, 2),
                getIntValue(tuple, 3), getIntValue(tuple, 4));
            if (tuple.size() == 6) {
                time = time.withSecond(getIntValue(tuple, 5));
            }
            return new Literal<>(time);
        } else {
            throw LiteralException.wrongDatetimeParameters(ctx.start);
        }
    }

    @Override
    public Literal visitDurationLiteral(EventScriptParser.DurationLiteralContext ctx) {
        Literal parameters = ctx.parExpressionList().accept(this);
        if (parameters.isVoidLiteral()) {
            return new Literal<>(Duration.ZERO);
        } else if (parameters.isDecimalLiteral()) {
            return new Literal<>(Duration.ofSeconds((Integer) parameters.getValue()));
        } else if (parameters.isTupleLiteral()) {
            Tuple tuple = (Tuple) parameters.getValue();
            if (tuple.size() > 4 || Stream.of(tuple.types()).anyMatch(t -> t != Type.INT)) {
                throw LiteralException.wrongDurationParameters(ctx.start);
            }
            Duration duration = Duration.ofSeconds(getIntValue(tuple, 0))
                .plusMinutes(getIntValue(tuple, 1));
            if (tuple.size() > 2) {
                duration = duration.plusHours(getIntValue(tuple, 2));
            }
            if (tuple.size() > 3) {
                duration = duration.plusDays(getIntValue(tuple, 3));
            }
            return new Literal<>(duration);
        } else {
            throw LiteralException.wrongDurationParameters(ctx.start);
        }
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
        Literal left = ctx.expression(0).accept(this);
        Literal right = ctx.expression(1).accept(this);

        //TODO implement datetime/duration toString -> no need to explicitly implement to-string-addition

        if (left.isStringLiteral() || right.isStringLiteral()) {
            if (ctx.bop.getType() == EventScriptLexer.ADD) {
                return new Literal<>(left.getValue().toString() + right.getValue().toString());
            } else {
                throw new OperationException(ctx.start, left, right, Operation.ADDITIVE);
            }
        }

        if (left.isDatetimeLiteral() && right.isDurationLiteral()) {
            LocalDateTime date = (LocalDateTime) left.getValue();
            Duration duration = (Duration) right.getValue();
            if (ctx.bop.getType() == EventScriptLexer.ADD) {
                return new Literal<>(date.plus(duration));
            } else {
                return new Literal<>(date.minus(duration));
            }

        }
        if (left.isDurationLiteral() && right.isDatetimeLiteral() && ctx.bop.getType() == EventScriptLexer.ADD) {
            Duration duration = (Duration) left.getValue();
            LocalDateTime date = (LocalDateTime) right.getValue();
            return new Literal<>(date.plus(duration));
        }
        if (left.isDurationLiteral() && right.isDurationLiteral()) {
            Duration leftDuration = (Duration) left.getValue();
            Duration rightDuration = (Duration) right.getValue();
            if (ctx.bop.getType() == EventScriptLexer.ADD) {
                return new Literal<>(leftDuration.plus(rightDuration));
            } else {
                return new Literal<>(leftDuration.minus(rightDuration));
            }
        }

        Literal result = applyOperation(left, right,
            () -> la.decimalAdditiveOperation(left, right, ctx.bop),
            () -> la.floatAdditiveOperation(left, right, ctx.bop));

        if (result != null) {
            return result;
        } else {
            throw new OperationException(ctx.start, left, right, Operation.ADDITIVE);
        }
    }

    @Override
    public Literal visitMultiplicativeExp(EventScriptParser.MultiplicativeExpContext ctx) {
        Literal left = ctx.expression(0).accept(this);
        Literal right = ctx.expression(1).accept(this);

        Literal result = applyOperation(left, right,
            () -> la.decimalMultiplicativeOperation(left, right, ctx.bop),
            () -> la.floatMultiplicativeOperation(left, right, ctx.bop));

        if (result != null) return result;
        throw new OperationException(ctx.start, left, right, Operation.MULTIPLICATIVE);
    }

    private Literal applyOperation(Literal left, Literal right, LiteralOperation decimalOperation,
                                   LiteralOperation floatOperation) {
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
        return null;
    }

    @Override
    public Literal visitUnaryExp(EventScriptParser.UnaryExpContext ctx) {
        Literal expression = ctx.expression().accept(this);
        String variable = null;
        if (ctx.expression() instanceof EventScriptParser.IdentifierExpContext) {
            variable = ((EventScriptParser.IdentifierExpContext) ctx.expression()).IDENTIFIER().getText();
        }

        switch (ctx.prefix.getType()) {
            case EventScriptLexer.INC:
                Literal incLiteral = applyOperation(ctx, expression,
                    () -> new Literal<>((Integer) expression.getValue() + 1),
                    () -> new Literal<>((Float) expression.getValue() + 1.0f));
                if (variable != null) scope.updateSymbol(variable, incLiteral);
                return incLiteral;
            case EventScriptLexer.DEC:
                Literal decLiteral = applyOperation(ctx, expression,
                    () -> new Literal<>((Integer) expression.getValue() - 1),
                    () -> new Literal<>((Float) expression.getValue() - 1.0f));
                if (variable != null) scope.updateSymbol(variable, decLiteral);
                return decLiteral;
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
        Literal expression = ctx.expression().accept(this);

        if (expression.isBoolLiteral()) {
            return new Literal<>(!(Boolean) expression.getValue());
        } else {
            throw new OperationException(ctx.start, expression, Operation.NEGATION);
        }
    }

    @Override
    public Literal visitLogicalAndExp(EventScriptParser.LogicalAndExpContext ctx) {
        Literal left = ctx.expression(0).accept(this);
        Literal right = ctx.expression(1).accept(this);

        if (left.isBoolLiteral() && right.isBoolLiteral()) {
            return new Literal<>((Boolean) left.getValue() && (Boolean) right.getValue());
        }

        throw OperationException.bothOperandsMustBeBool(ctx.start);
    }

    @Override
    public Literal visitLogicalOrExp(EventScriptParser.LogicalOrExpContext ctx) {
        Literal left = ctx.expression(0).accept(this);
        Literal right = ctx.expression(1).accept(this);

        if (left.isBoolLiteral() && right.isBoolLiteral()) {
            return new Literal<>((Boolean) left.getValue() || (Boolean) right.getValue());
        }

        throw OperationException.bothOperandsMustBeBool(ctx.start);
    }

    @Override
    public Literal visitEqualityExp(EventScriptParser.EqualityExpContext ctx) {
        Literal left = ctx.expression(0).accept(this);
        Literal right = ctx.expression(1).accept(this);

        Literal result = applyOperation(left, right,
            () -> null,
            () -> la.floatEqualityOperation(left, right, ctx.bop));

        if (result != null) return result;

        switch (ctx.bop.getType()) {
            case EventScriptLexer.EQUAL:
                return new Literal<>(left.getValue().equals(right.getValue()));
            default:
                return new Literal<>(!left.getValue().equals(right.getValue()));
        }
    }

    @Override
    public Literal visitRelationalExp(EventScriptParser.RelationalExpContext ctx) {
        Literal left = ctx.expression(0).accept(this);
        Literal right = ctx.expression(1).accept(this);

        Literal result = applyOperation(left, right,
            () -> la.decimalRelationalOperation(left, right, ctx.bop),
            () -> la.floatRelationalOperation(left, right, ctx.bop));

        if (result != null) return result;

        // TODO comparison between duration and datetime

        throw new OperationException(ctx.start, left, right, Operation.RELATIONAL);
    }

    @Override
    public Literal visitIdentifierExp(EventScriptParser.IdentifierExpContext ctx) {
        Declarable declarable = scope.lookupSymbol(ctx.IDENTIFIER().getText());
        if (!(declarable instanceof Literal)) {
            throw ScopeException.undefinedVariable(ctx.start, ctx.IDENTIFIER().getText());
        }
        return (Literal) declarable;
    }

    @Override
    public Literal visitAssignmentExp(EventScriptParser.AssignmentExpContext ctx) {
        if (!(ctx.expression(0) instanceof EventScriptParser.IdentifierExpContext)) {
            throw OperationException.variableExpected(ctx.start);
        }
        EventScriptParser.IdentifierExpContext variable = (EventScriptParser.IdentifierExpContext) ctx.expression(0);
        Literal currentValue = visitIdentifierExp(variable);
        Literal newValue = ctx.expression(1).accept(this);
        if (!newValue.isOfSameType(currentValue)) {
            throw OperationException.differentTypeExpected(ctx.start, currentValue.getLiteralType());
        }
        scope.updateSymbol(variable.IDENTIFIER().getText(), newValue); //TODO check return value
        return newValue;
    }

    @Override
    public Literal visitExpressionList(EventScriptParser.ExpressionListContext ctx) {
        ExpressionVisitor expressionVisitor = this;
        if (ctx.expression().size() == 1) {
            return ctx.expression(0).accept(expressionVisitor);
        }
        Tuple tuple = ctx.expression().stream()
            .map(expCtx -> expCtx.accept(expressionVisitor))
            .collect(Tuple.Creator::new, Tuple.Creator::add, (no, op) -> {
            })
            .create();

        return new Literal<>(tuple);
    }

    @Override
    public Literal visitParExpressionList(EventScriptParser.ParExpressionListContext ctx) {
        if (ctx.expressionList() == null) {
            return Literal.voidLiteral();
        }
        return ctx.expressionList().accept(this);
    }

    @Override
    public Literal visitBuiltInFunctionCall(EventScriptParser.BuiltInFunctionCallContext ctx) {
        return ctx.builtInFunction().accept(this);
    }

    @Override
    public Literal visitSpeakFunc(EventScriptParser.SpeakFuncContext ctx) {
        Literal params = getParameters(ctx);
        log.info(params.getValue().toString());
        return Literal.voidLiteral();
    }

    private Literal getParameters(EventScriptParser.BuiltInFunctionContext ctx) {
        return ((EventScriptParser.BuiltInFunctionCallContext) ctx.parent).parExpressionList().accept(this);
    }
}
