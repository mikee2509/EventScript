package com.github.mikee2509.eventscript.domain.expression;

import com.github.mikee2509.eventscript.domain.scope.Declarable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@AllArgsConstructor
@Getter
@EqualsAndHashCode
public class Literal<T> implements Declarable {
    T value;

    public Type getLiteralType() {
        return Type.fromJavaType(value.getClass());
    }

    public boolean isStringLiteral() {
        return Type.fromJavaType(value.getClass()) == Type.STRING;
    }

    public boolean isFloatLiteral() {
        return Type.fromJavaType(value.getClass()) == Type.FLOAT;
    }

    public boolean isDecimalLiteral() {
        return Type.fromJavaType(value.getClass()) == Type.INT;
    }

    public boolean isBoolLiteral() {
        return Type.fromJavaType(value.getClass()) == Type.BOOL;
    }
}
