package com.github.mikee2509.eventscript.parser;

import com.github.mikee2509.eventscript.EventScriptLexer;
import com.github.mikee2509.eventscript.EventScriptParser;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CodePointCharStream;
import org.antlr.v4.runtime.CommonTokenStream;

public class ParserCreator {
    public EventScriptParser fromString(String input) {
        CodePointCharStream inputStream = CharStreams.fromString(input);
        EventScriptLexer lexer = new EventScriptLexer(inputStream);
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        return new EventScriptParser(tokenStream);
    }
}
