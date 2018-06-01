package com.github.mikee2509.eventscript.parser.visitor;

import com.github.mikee2509.eventscript.EventScriptParser;
import com.github.mikee2509.eventscript.EventScriptParserBaseVisitor;
import com.github.mikee2509.eventscript.domain.exception.FunctionException;
import com.github.mikee2509.eventscript.domain.exception.OperationException;
import com.github.mikee2509.eventscript.domain.expression.Literal;
import com.github.mikee2509.eventscript.domain.expression.Tuple;
import com.github.mikee2509.eventscript.domain.expression.Type;
import com.github.mikee2509.eventscript.parser.util.ScopeManager;
import lombok.extern.java.Log;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.stream.Stream;

import static com.github.mikee2509.eventscript.domain.expression.Type.*;

@Log
public class FunctionVisitor extends EventScriptParserBaseVisitor<Literal> {
    private ScopeManager scope;
    ExpressionListener expressionListener;

    public FunctionVisitor(ScopeManager scope) {
        this.scope = scope;
    }

    public void setExpressionListener(ExpressionListener expressionListener) {
        this.expressionListener = expressionListener;
    }

    private Literal getBuiltInFuncParams(EventScriptParser.BuiltInFunctionContext ctx) {
        EventScriptParser.BuiltInFunctionCallContext builtInFunctionCallContext =
            (EventScriptParser.BuiltInFunctionCallContext) ctx.parent;
        return expressionListener.invoke(builtInFunctionCallContext.parExpressionList());
    }

    private Literal getLiteralFuncExpression(EventScriptParser.LiteralFunctionContext ctx) {
        EventScriptParser.LiteralFuncExpContext literalFuncExpContext =
            (EventScriptParser.LiteralFuncExpContext) ctx.parent.parent;
        return expressionListener.invoke(literalFuncExpContext.expression());
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
    public Literal visitTupleExtractFunc(EventScriptParser.TupleExtractFuncContext ctx) {
        Literal expression = getLiteralFuncExpression(ctx);
        if (!expression.isTupleLiteral()) {
            throw OperationException.differentTypeExpected(ctx.start, TUPLE);
        }
        Tuple tuple = (Tuple) expression.getValue();
        int index = Integer.valueOf(ctx.TUPLE_EXTRACT().getText().substring(1));
        if (tuple.size() < index) {
            throw OperationException.tupleExtractException(ctx.start, tuple.size());
        }
        return tuple.literals()[index-1];
    }
}
