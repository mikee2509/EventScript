package com.github.mikee2509.eventscript.parser.visitor;

import com.github.mikee2509.eventscript.EventScriptParser;
import com.github.mikee2509.eventscript.EventScriptParserBaseVisitor;
import com.github.mikee2509.eventscript.parser.util.ScopeManager;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ScriptVisitor extends EventScriptParserBaseVisitor<Void> {
    private ScopeManager scope;
    private StatementVisitor statementVisitor;
    private FunctionVisitor functionVisitor;

    @Override
    public Void visitScript(EventScriptParser.ScriptContext ctx) {
        ctx.statement().forEach(statementContext -> statementContext.accept(statementVisitor));
        return null;
    }
}
