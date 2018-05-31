package com.github.mikee2509.eventscript.parser.visitor;

import com.github.mikee2509.eventscript.EventScriptParser;

public interface FunctionCallListener {
    void invoke(EventScriptParser.FunctionContext context);
}
