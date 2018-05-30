package com.github.mikee2509.eventscript.domain.exception.parser;

import org.antlr.v4.runtime.Token;

public class LiteralException extends ParserException {
    private LiteralException(Token token, String message) {
        super(token, message);
    }

    public static LiteralException wrongDurationParameters(Token token) {
        return new LiteralException(token, "Duration constructor takes at most 4 ints as parameters");
    }

    public static LiteralException wrongDatetimeParameters(Token token) {
        return new LiteralException(token, "Datetime constructor takes 5 or 6 ints as parameters");
    }
}
