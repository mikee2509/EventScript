package com.github.mikee2509.eventscript.domain.exception.control;

import com.github.mikee2509.eventscript.domain.expression.Tuple;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReturnException extends ControlFlowException {
    private Tuple returnTuple;
}
