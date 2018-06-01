package com.github.mikee2509.eventscript.domain.exception.control;

import com.github.mikee2509.eventscript.domain.exception.ParserException;
import org.antlr.v4.runtime.Token;

public class BreakException extends ParserException {
    public BreakException(Token token) {
        super(token, "This exception should be caught by for loop visitor");
    }
}
