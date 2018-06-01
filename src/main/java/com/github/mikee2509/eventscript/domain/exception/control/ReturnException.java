package com.github.mikee2509.eventscript.domain.exception.control;

import com.github.mikee2509.eventscript.domain.exception.ParserException;
import com.github.mikee2509.eventscript.domain.expression.Tuple;
import lombok.Getter;
import org.antlr.v4.runtime.Token;

@Getter
public class ReturnException extends ParserException {
    private final Tuple returnTuple;

    public ReturnException(Token token, Tuple returnTuple) {
        super(token, "This exception should be caught by function visitor");
        this.returnTuple = returnTuple;
    }

}
