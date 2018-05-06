package com.github.mikee2509.eventscript.parser.visitor;

import com.github.mikee2509.eventscript.EventScriptParser;
import com.github.mikee2509.eventscript.EventScriptParserBaseVisitor;
import com.github.mikee2509.eventscript.domain.expression.Type;
import org.springframework.stereotype.Service;

@Service
public class TypeVisitor extends EventScriptParserBaseVisitor<Type> {
    @Override
    public Type visitType(EventScriptParser.TypeContext ctx) {
        if (ctx.BOOL() != null) return Type.BOOL;
        if (ctx.DATETIME() != null) return Type.DATETIME;
        if (ctx.DURATION() != null) return Type.DURATION;
        if (ctx.FLOAT() != null) return Type.FLOAT;
        if (ctx.FUNC() != null) return Type.FUNC;
        if (ctx.INT() != null) return Type.INT;
        if (ctx.STRING() != null) return Type.STRING;
        return Type.VOID;
    }
}
