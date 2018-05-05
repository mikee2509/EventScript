package com.github.mikee2509.eventscript.parser.exception;

import org.antlr.v4.runtime.Token;

import java.text.MessageFormat;

class ParserException extends RuntimeException {

    ParserException(Token start, String message) {
        super(MessageFormat.format("line {0}:{1} {2}", start.getLine(), start.getCharPositionInLine(), message));
    }
}
