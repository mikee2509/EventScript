package com.github.mikee2509.eventscript.parser.visitor;

import com.github.mikee2509.eventscript.EventScriptParser;
import com.github.mikee2509.eventscript.EventScriptParserBaseVisitor;
import com.github.mikee2509.eventscript.domain.expression.Type;
import org.springframework.stereotype.Service;

import static com.github.mikee2509.eventscript.domain.expression.Type.*;

@Service
public class TypeVisitor extends EventScriptParserBaseVisitor<Type> {
    @Override
    public Type visitType(EventScriptParser.TypeContext ctx) {
        if (ctx.BOOL() != null) return BOOL;
        if (ctx.DATETIME() != null) return DATETIME;
        if (ctx.DURATION() != null) return DURATION;
        if (ctx.FLOAT() != null) return FLOAT;
        if (ctx.FUNC() != null) return FUNC;
        if (ctx.INT() != null) return INT;
        if (ctx.STRING() != null) return STRING;
        return VOID;
    }
}
