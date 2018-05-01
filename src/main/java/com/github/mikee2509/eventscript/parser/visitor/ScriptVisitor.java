package com.github.mikee2509.eventscript.parser.visitor;

import com.github.mikee2509.eventscript.EventScriptParser;
import com.github.mikee2509.eventscript.EventScriptParserBaseVisitor;
import com.github.mikee2509.eventscript.domain.Script;
import com.github.mikee2509.eventscript.domain.Statement;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ScriptVisitor extends EventScriptParserBaseVisitor<Script> {
    private StatementVisitor statementVisitor;

    @Override
    public Script visitScript(EventScriptParser.ScriptContext ctx) {
        List<Statement> statements = ctx.statement().stream()
            .map(statementContext -> statementContext.accept(statementVisitor))
            .collect(Collectors.toList());
        return new Script(statements);
    }
}
