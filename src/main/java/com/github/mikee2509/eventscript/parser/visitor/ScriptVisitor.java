package com.github.mikee2509.eventscript.parser.visitor;

import com.github.mikee2509.eventscript.EventScriptParser;
import com.github.mikee2509.eventscript.EventScriptParserBaseVisitor;
import com.github.mikee2509.eventscript.domain.Script;
import com.github.mikee2509.eventscript.domain.Statement;
import com.github.mikee2509.eventscript.domain.scope.Scope;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
public class ScriptVisitor extends EventScriptParserBaseVisitor<Script> {
    private Scope globalScope;
    private StatementVisitor statementVisitor;
    private FunctionVisitor functionVisitor;

    @Override
    public Script visitScript(EventScriptParser.ScriptContext ctx) {
        List<Statement> statements = ctx.statement().stream()
            .map(statementContext -> statementContext.accept(statementVisitor))
            .collect(Collectors.toList());
        return new Script(statements);
    }
}
