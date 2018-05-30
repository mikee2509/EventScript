package com.github.mikee2509.eventscript.domain.exception.parser;

import com.github.mikee2509.eventscript.domain.expression.Type;
import org.antlr.v4.runtime.Token;

import java.text.MessageFormat;

public class ScopeException extends ParserException {
    private ScopeException(Token token, String message) {
        super(token, message);
    }

    public static ScopeException alreadyDefined(Token token, String identifier) {
        return new ScopeException(token, MessageFormat.format("''{0}'' is already defined in this scope", identifier));
    }

    public static ScopeException undefinedVariable(Token token, String identifier) {
        return new ScopeException(token, MessageFormat.format("Variable ''{0}'' is not defined in this scope",
            identifier));
    }

    public static ScopeException cannotBeDefined(Token token, Type type) {
        return new ScopeException(token, MessageFormat.format("Variable of type {0} cannot be defined",
            type.getName()));
    }

}
