package com.github.mikee2509.eventscript.parser.exception;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum Operation {
    ADDITIVE, MULTIPLICATIVE, UNARY, NEGATION;

    public String getName() {
        return this.name().toLowerCase();
    }
}
