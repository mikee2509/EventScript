package com.github.mikee2509.eventscript.parser.exception;

import com.github.mikee2509.eventscript.domain.expression.Literal;
import org.antlr.v4.runtime.Token;

import java.text.MessageFormat;

public class OperationException extends ParserException {

    private OperationException(Token token, String message) {
        super(token, message);
    }

    public OperationException(Token token, Literal leftOperand, Literal rightOperand, Operation operation) {
        super(token, MessageFormat.format("Unsupported {0} operation between {1} and {2}",
            operation.getName(), leftOperand.getLiteralType().getName(), rightOperand.getLiteralType().getName()));
    }

    public OperationException(Token token, Literal expression, Operation operation) {
        super(token, MessageFormat.format("Unsupported {0} operation on {1}", operation.getName(),
            expression.getLiteralType().getName()));
    }

    public static OperationException bothOperandsMustBeBool(Token token) {
        return new OperationException(token, "Both operands must be of bool type");
    }

}
