package com.github.mikee2509.eventscript.parser.exception;

import com.github.mikee2509.eventscript.domain.expression.Literal;
import lombok.AllArgsConstructor;
import org.antlr.v4.runtime.Token;

import java.text.MessageFormat;

public class ArithmeticException extends ParserException {

    public ArithmeticException(Token start, Literal leftOperand, Literal rightOperand, Operation operation) {
        super(start, MessageFormat.format("Unsupported {0} arithmetic operation between {1} and {2}",
            operation.name, leftOperand.getLiteralType().getName(), rightOperand.getLiteralType().getName()));
    }

    public ArithmeticException(Token start, Literal expression, Operation operation) {
        super(start, MessageFormat.format("Unsupported {0} operation on {1}", operation.name,
            expression.getLiteralType().getName()));
    }

    @AllArgsConstructor
    public enum Operation {
        ADDITIVE("additive"), MULTIPLICATIVE("multiplicative"), UNARY("unary");

        private String name;
    }
}
