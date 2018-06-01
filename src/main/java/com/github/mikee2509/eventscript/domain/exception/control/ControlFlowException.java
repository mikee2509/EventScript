package com.github.mikee2509.eventscript.domain.exception.control;

import com.github.mikee2509.eventscript.domain.exception.ParserException;
import org.antlr.v4.runtime.Token;

public class ControlFlowException extends ParserException {
    public static final String CONTINUE_IN_WRONG_CONTEXT = "Continue outside for loop";
    public static final String RETURN_IN_WRONG_CONTEXT = "Return outside for loop";
    public static final String BREAK_IN_WRONG_CONTEXT = "Break outside for loop";

    private ControlFlowException(Token token, String message) {
        super(token, message);
    }

    public static ControlFlowException returnWrongContext(Token token) {
        return new ControlFlowException(token, RETURN_IN_WRONG_CONTEXT);
    }

    public static ControlFlowException continueWrongContext(Token token) {
        return new ControlFlowException(token, CONTINUE_IN_WRONG_CONTEXT);
    }

    public static ControlFlowException breakWrongContext(Token token) {
        return new ControlFlowException(token, BREAK_IN_WRONG_CONTEXT);
    }
}
