package com.github.mikee2509.eventscript.domain.exception;

import org.antlr.v4.runtime.Token;

import java.text.MessageFormat;

public class ParserException extends RuntimeException {

    public ParserException(Token token, String message) {
        super(MessageFormat.format("line {0}:{1} {2}", token.getLine(), token.getCharPositionInLine(), message));
    }
}
