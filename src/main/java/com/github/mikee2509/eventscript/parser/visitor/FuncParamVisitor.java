package com.github.mikee2509.eventscript.parser.visitor;

import com.github.mikee2509.eventscript.EventScriptParser;
import com.github.mikee2509.eventscript.EventScriptParserBaseVisitor;
import com.github.mikee2509.eventscript.domain.exception.FunctionException;
import com.github.mikee2509.eventscript.domain.expression.Function;
import com.github.mikee2509.eventscript.domain.scope.Declarable;
import com.github.mikee2509.eventscript.parser.util.ScopeManager;
import lombok.AllArgsConstructor;

import java.util.Objects;

@AllArgsConstructor
public class FuncParamVisitor extends EventScriptParserBaseVisitor<Function> {
    private ScopeManager scope;

    @Override
    public Function visitIdentifierExp(EventScriptParser.IdentifierExpContext ctx) {
        Declarable declarable = scope.lookupSymbol(ctx.IDENTIFIER().getText());
        if (declarable instanceof Function) {
            return (Function) declarable;
        }
        return null;
    }

    @Override
    public Function visitExpressionList(EventScriptParser.ExpressionListContext ctx) {
        return ctx.expression().stream()
            .map(expCtx -> expCtx.accept(this))
            .filter(Objects::nonNull)
            .findFirst()
            .orElse(null);
    }

    @Override
    public Function visitParExpressionList(EventScriptParser.ParExpressionListContext ctx) {
        Function function = ctx.expressionList().accept(this);
        if (function == null || !function.isSchedulable()) {
            throw FunctionException.cannotSchedule(ctx.start);
        }
        return function;
    }
}
