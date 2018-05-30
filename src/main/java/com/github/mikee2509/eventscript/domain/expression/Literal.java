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

    public boolean isOfSameType(Literal other) {
        return this.value.getClass() == other.value.getClass();
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

    public boolean isVoidLiteral() {
        return value == null;
    }

    public boolean isDurationLiteral() {
        return Type.fromJavaType(value.getClass()) == Type.DURATION;
    }

    public boolean isDatetimeLiteral() {
        return Type.fromJavaType(value.getClass()) == Type.DATETIME;
    }

    public boolean isTupleLiteral() {
        return Type.fromJavaType(value.getClass()) == Type.TUPLE;
    }

    public static Literal voidLiteral() {
        return new Literal<Void>(null);
    }

    @Override
    public String toString() {
        return "Literal{" +
            "value=" + value +
            " type=" + value.getClass().getCanonicalName() +
            '}';
    }
}
