package com.github.mikee2509.eventscript.parser.exception;

import org.antlr.v4.runtime.Token;

public class FunctionException extends ParserException {
    private FunctionException(Token token, String message) {
        super(token, message);
    }

    public static FunctionException voidParameter(Token token) {
        return new FunctionException(token, "Function parameter cannot be of void type");
    }
}
