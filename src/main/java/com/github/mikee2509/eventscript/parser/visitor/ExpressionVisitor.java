package com.github.mikee2509.eventscript.parser.visitor;

import com.github.mikee2509.eventscript.EventScriptLexer;
import com.github.mikee2509.eventscript.EventScriptParser;
import com.github.mikee2509.eventscript.EventScriptParserBaseVisitor;
import com.github.mikee2509.eventscript.domain.exception.FunctionException;
import com.github.mikee2509.eventscript.domain.exception.LiteralException;
import com.github.mikee2509.eventscript.domain.exception.OperationException;
import com.github.mikee2509.eventscript.domain.exception.ScopeException;
import com.github.mikee2509.eventscript.domain.exception.control.ReturnException;
import com.github.mikee2509.eventscript.domain.expression.Function;
import com.github.mikee2509.eventscript.domain.expression.Literal;
import com.github.mikee2509.eventscript.domain.expression.Tuple;
import com.github.mikee2509.eventscript.domain.scope.Declarable;
import com.github.mikee2509.eventscript.parser.util.LiteralArithmetic;
import com.github.mikee2509.eventscript.parser.util.ScopeManager;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.stream.Stream;

import static com.github.mikee2509.eventscript.domain.exception.Operation.*;
import static com.github.mikee2509.eventscript.domain.expression.Type.INT;
import static com.github.mikee2509.eventscript.domain.expression.Type.VOID;

public class ExpressionVisitor extends EventScriptParserBaseVisitor<Literal> {
    private ScopeManager scope;
    private LiteralArithmetic la;
    private FunctionVisitor functionVisitor;
    private FunctionCallListener functionCallListener = (ctx) -> {};

    public ExpressionVisitor(ScopeManager scope, LiteralArithmetic la, FunctionVisitor functionVisitor) {
        this.scope = scope;
        this.la = la;
        this.functionVisitor = functionVisitor;
        this.functionVisitor.setExpressionListener(ctx -> ctx.accept(this));
    }

    public void setFunctionCallListener(FunctionCallListener functionCallListener) {
        this.functionCallListener = functionCallListener;
        functionVisitor.setFunctionCallListener(functionCallListener);
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

        if (ctx.bop.getType() == EventScriptLexer.EQUAL) {
            return new Literal<>(left.getValue().equals(right.getValue()));
        } else {
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
        if (declarable == null) {
            throw ScopeException.undefinedVariable(ctx.start, ctx.IDENTIFIER().getText());
        }
        if (declarable instanceof Function) {
            declarable = Literal.voidLiteral();
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
        if (!scope.updateSymbol(variable.IDENTIFIER().getText(), newValue)) {
            //this should never happen
            throw ScopeException.undefinedVariable(ctx.start, variable.IDENTIFIER().getText());
        }
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

    @Override
    public Literal visitBuiltInFuncExp(EventScriptParser.BuiltInFuncExpContext ctx) {
        return ctx.accept(functionVisitor);
    }

    @Override
    public Literal visitLiteralFuncExp(EventScriptParser.LiteralFuncExpContext ctx) {
        return ctx.accept(functionVisitor);
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
        scope.functionSubscope(function);
        if (parameters != null) {
            for (int i = 0; i < function.numParams(); i++) {
                String parameterName = function.getParameters().get(i).getName();
                if (!scope.defineSymbol(parameterName, parameters.literals()[i])) {
                    // this should never happen
                    throw ScopeException.alreadyDefined(function.getContext().start, parameterName);
                }
            }
        }
        Tuple returnTuple = null;
        try {
            functionCallListener.invoke(function.getContext());
        } catch (ReturnException e) {
            returnTuple = e.getReturnTuple();
        } finally {
            scope.abandonScope();
        }

        if (returnTuple == null) {
            if (function.getReturnType() != VOID) {
                throw FunctionException.missingReturnStatement(function.getContext().start);
            }
            return Literal.voidLiteral();
        } else if (returnTuple.size() == 1) {
            return returnTuple.literals()[0];
        } else {
            return new Literal<>(returnTuple);
        }
    }
}
