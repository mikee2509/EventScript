package com.github.mikee2509.eventscript.parser.visitor;

import com.github.mikee2509.eventscript.EventScriptParser;
import com.github.mikee2509.eventscript.EventScriptParserBaseVisitor;
import com.github.mikee2509.eventscript.domain.Statement;
import org.springframework.stereotype.Service;

@Service
public class StatementVisitor extends EventScriptParserBaseVisitor<Statement> {
    @Override
    public Statement visitStatement(EventScriptParser.StatementContext ctx) {
        System.out.println("StatementVisitor.visitStatement");
        return null;
    }
}
