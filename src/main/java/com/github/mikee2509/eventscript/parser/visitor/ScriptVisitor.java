package com.github.mikee2509.eventscript.parser.visitor;

import com.github.mikee2509.eventscript.EventScriptParser;
import com.github.mikee2509.eventscript.EventScriptParserBaseVisitor;
import com.github.mikee2509.eventscript.domain.expression.Function;
import com.github.mikee2509.eventscript.domain.expression.Returnable;
import com.github.mikee2509.eventscript.domain.expression.Tuple;
import com.github.mikee2509.eventscript.domain.expression.Type;
import com.github.mikee2509.eventscript.domain.exception.parser.FunctionException;
import com.github.mikee2509.eventscript.parser.util.ScopeManager;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
public class ScriptVisitor extends EventScriptParserBaseVisitor<Void> {
    private ScopeManager scope;
    private StatementVisitor statementVisitor;
    private TypeVisitor typeVisitor;

    @Override
    public Void visitScript(EventScriptParser.ScriptContext ctx) {
        ctx.function().forEach(this::visitFunction);
        ctx.statement().forEach(statementContext -> statementContext.accept(statementVisitor));
        return null;
    }

    @Override
    public Void visitFunction(EventScriptParser.FunctionContext ctx) {
        Function function = Function.builder()
            .name(ctx.IDENTIFIER().getText())
            .parameters(visitParameters(ctx.parameterList()))
            .returnType(visitReturnTypes(ctx.returnType()))
            .context(ctx)
            .build();

        scope.defineSymbol(function.getName(), function);
        return null;
    }

    private List<Function.Parameter> visitParameters(EventScriptParser.ParameterListContext ctx) {
        return ctx.parameter().stream()
            .map(this::extractParameter)
            .collect(Collectors.toList());
    }

    private Function.Parameter extractParameter(EventScriptParser.ParameterContext ctx) {
        Type type = ctx.type().accept(typeVisitor);
        if (type == Type.VOID) throw FunctionException.voidParameter(ctx.start);
        return Function.Parameter.builder()
            .name(ctx.IDENTIFIER().getText())
            .type(type)
            .build();
    }

    private Returnable visitReturnTypes(EventScriptParser.ReturnTypeContext ctx) {
        if (ctx == null) {
            return Type.VOID;
        }
        if (ctx.type().size() == 1) {
            return ctx.type(0).accept(typeVisitor);
        }

        List<Type> types = ctx.type().stream()
            .map(typeCtx -> typeCtx.accept(typeVisitor))
            .collect(Collectors.toList());
        return Tuple.builder()
            .types(types)
            .build();
    }
}
