package com.github.mikee2509.eventscript.parser.visitor;

import com.github.mikee2509.eventscript.EventScriptLexer;
import com.github.mikee2509.eventscript.EventScriptParser;
import com.github.mikee2509.eventscript.EventScriptParserBaseVisitor;
import com.github.mikee2509.eventscript.domain.exception.control.ControlFlowException;
import com.github.mikee2509.eventscript.domain.exception.control.ReturnException;
import com.github.mikee2509.eventscript.domain.exception.parser.FunctionException;
import com.github.mikee2509.eventscript.domain.exception.parser.LiteralException;
import com.github.mikee2509.eventscript.domain.exception.parser.OperationException;
import com.github.mikee2509.eventscript.domain.exception.parser.ScopeException;
import com.github.mikee2509.eventscript.domain.expression.Function;
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
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.stream.Stream;

import static com.github.mikee2509.eventscript.domain.exception.parser.Operation.*;
import static com.github.mikee2509.eventscript.domain.expression.Type.*;

@Log
public class ExpressionVisitor extends EventScriptParserBaseVisitor<Literal> {
    private ScopeManager scope;
    private LiteralArithmetic la;
    private List<FunctionCallListener> functionCallListeners = new ArrayList<>();

    public ExpressionVisitor(ScopeManager scope, LiteralArithmetic la) {
        this.scope = scope;
        this.la = la;
    }

    public void addFunctionCallListener(FunctionCallListener functionCallListener) {
        functionCallListeners.add(functionCallListener);
    }

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
            if (tuple.size() < 5 || tuple.size() > 6 || Stream.of(tuple.types()).anyMatch(t -> t != INT)) {
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
        } else if (parameters.isTupleLiteral()) {
            Tuple tuple = (Tuple) parameters.getValue();
            if (tuple.size() > 4 || Stream.of(tuple.types()).anyMatch(t -> t != INT)) {
                throw LiteralException.wrongDurationParameters(ctx.start);
            }
            Duration duration = Duration.ofSeconds(getIntValue(tuple, 0));
            if (tuple.size() > 1) {
                duration = duration.plusMinutes(getIntValue(tuple, 1));
            }
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

        if (left.isStringLiteral() || right.isStringLiteral()) {
            if (ctx.bop.getType() == EventScriptLexer.ADD) {
                return new Literal<>(left.getValue().toString() + right.getValue().toString());
            } else {
                throw new OperationException(ctx.start, left, right, ADDITIVE);
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
            throw new OperationException(ctx.start, left, right, ADDITIVE);
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
        throw new OperationException(ctx.start, left, right, MULTIPLICATIVE);
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

        throw new OperationException(ctx.start, expression, UNARY);
    }

    private Literal applyOperation(EventScriptParser.UnaryExpContext ctx, Literal expression,
                                   LiteralOperation decimalOperation, LiteralOperation floatOperation) {
        if (expression.isDecimalLiteral()) {
            return decimalOperation.execute();
        } else if (expression.isFloatLiteral()) {
            return floatOperation.execute();
        } else {
            throw new OperationException(ctx.start, expression, UNARY);
        }
    }

    @Override
    public Literal visitNegationExp(EventScriptParser.NegationExpContext ctx) {
        Literal expression = ctx.expression().accept(this);

        if (expression.isBoolLiteral()) {
            return new Literal<>(!(Boolean) expression.getValue());
        } else {
            throw new OperationException(ctx.start, expression, NEGATION);
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

        if (left.isDatetimeLiteral() && right.isDatetimeLiteral()) {
            return la.datetimeRelationalOperation(left, right, ctx.bop);
        }
        if (left.isDurationLiteral() && right.isDurationLiteral()) {
            return la.durationRelationalOperation(left, right, ctx.bop);
        }

        Literal result = applyOperation(left, right,
            () -> la.decimalRelationalOperation(left, right, ctx.bop),
            () -> la.floatRelationalOperation(left, right, ctx.bop));

        if (result != null) return result;

        throw new OperationException(ctx.start, left, right, RELATIONAL);
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
    public Literal visitParExpressionList(EventScriptParser.ParExpressionListContext ctx) {
        if (ctx.expressionList() == null) {
            return Literal.voidLiteral();
        }
        return ctx.expressionList().accept(this);
    }

    @Override
    public Literal visitExpressionList(EventScriptParser.ExpressionListContext ctx) {
        //@formatter:off
        Tuple tuple = ctx.expression().stream()
            .map(expCtx -> expCtx.accept(this))
            .collect(Tuple.Creator::new, Tuple.Creator::add, (no, op) -> {})
            .create();
        //@formatter:on

        return new Literal<>(tuple);
    }

    private Literal getBuiltInFuncParams(EventScriptParser.BuiltInFunctionContext ctx) {
        return ((EventScriptParser.BuiltInFunctionCallContext) ctx.parent).parExpressionList().accept(this);
    }

    @Override
    public Literal visitSpeakFunc(EventScriptParser.SpeakFuncContext ctx) {
        Literal params = getBuiltInFuncParams(ctx);
        if (!params.isTupleLiteral()) {
            throw FunctionException.argumentException(ctx.start, ctx.SPEAK().getText(), STRING);
        }
        Tuple tuple = (Tuple) params.getValue();
        Type[] speakableTypes = new Type[]{BOOL, FLOAT, INT, STRING};
        if (tuple.size() != 1 || Stream.of(speakableTypes).noneMatch(type -> type == tuple.types()[0])) {
            throw FunctionException.argumentException(ctx.start, ctx.SPEAK().getText(), STRING);
        }
        log.info(tuple.literals()[0].getValue().toString());
        return Literal.voidLiteral();
    }

    @Override
    public Literal visitLiteralFuncExp(EventScriptParser.LiteralFuncExpContext ctx) {
        return super.visitLiteralFuncExp(ctx);
    }

    //TODO implement tuple value retrieval (tuple._1, tuple._2, ...)
    private Literal getLiteralFuncExpression(EventScriptParser.LiteralFunctionContext ctx) {
        return ((EventScriptParser.LiteralFuncExpContext) ctx.parent.parent).expression().accept(this);
    }

    //TODO move to other class using listener
    @Override
    public Literal visitToStringFunc(EventScriptParser.ToStringFuncContext ctx) {
        Literal expression = getLiteralFuncExpression(ctx);
        Type[] stringableTypes = {BOOL, DATETIME, DURATION, FLOAT, INT, STRING};
        if (Stream.of(stringableTypes).anyMatch(type -> type == expression.getLiteralType())) {
            if (expression.isDatetimeLiteral()) {
                LocalDateTime date = (LocalDateTime) expression.getValue();
                return new Literal<>(date.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)));
            }
            if (expression.isDurationLiteral()) {
                Duration duration = (Duration) expression.getValue();
                StringBuilder d = new StringBuilder();
                if (duration.getSeconds() >= 86400) {
                    d.append(duration.toDays()).append("d ");
                    duration = duration.minusDays(duration.toDays());
                }
                if (duration.getSeconds() >= 3600) {
                    d.append(duration.toHours()).append("h ");
                    duration = duration.minusHours(duration.toHours());
                }
                if (duration.getSeconds() >= 60) {
                    d.append(duration.toMinutes()).append("m ");
                    duration = duration.minusMinutes(duration.toMinutes());
                }
                d.append(duration.getSeconds()).append("s");
                return new Literal<>(d.toString());
            }
            return new Literal<>(expression.getValue().toString());
        } else {
            throw FunctionException.toStringException(ctx.start, stringableTypes);
        }
    }

    @Override
    public Literal visitFunctionCall(EventScriptParser.FunctionCallContext ctx) {
        String funcName = ctx.IDENTIFIER().getText();
        Declarable declarable = scope.lookupSymbol(funcName);
        if (!(declarable instanceof Function)) {
            throw FunctionException.cannotResolve(ctx.start, funcName);
        }
        Function function = (Function) declarable;
        Literal paramsLiteral = ctx.parExpressionList().accept(this);
        if (function.numParams() == 0 && paramsLiteral.isVoidLiteral()) {
            return callFunction(function);
        }
        if (paramsLiteral.isTupleLiteral()) {
            Tuple tuple = (Tuple) paramsLiteral.getValue();
            if (function.checkTypes(tuple.types())) {
                return callFunction(function, tuple);
            }
        }
        throw FunctionException.argumentException(ctx.start, function);
    }

    private Literal callFunction(Function function) {
        return callFunction(function, null);
    }

    private Literal callFunction(Function function, Tuple parameters) {
        scope.subscope(function);
        if (parameters != null) {
            for (int i = 0; i < function.numParams(); i++) {
                //TODO check define result
                scope.defineSymbol(function.getParameters().get(i).getName(), parameters.literals()[i]);
            }
        }
        Tuple returnTuple = null;
        try {
            functionCallListeners.forEach(listener -> listener.invoke(function.getContext()));
        } catch (ReturnException e) {
            returnTuple = e.getReturnTuple();
        } finally {
            scope.abandonScope();
        }

        if (returnTuple == null) {
            return Literal.voidLiteral();
        } else if (returnTuple.size() == 1) {
            return returnTuple.literals()[0];
        } else {
            return new Literal<>(returnTuple);
        }
    }
}
