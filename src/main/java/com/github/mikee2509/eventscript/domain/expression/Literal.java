package com.github.mikee2509.eventscript.domain.expression;

import com.github.mikee2509.eventscript.domain.scope.Declarable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import static com.github.mikee2509.eventscript.domain.expression.Type.*;

@AllArgsConstructor
@Getter
@EqualsAndHashCode
public class Literal<T> implements Declarable {
    T value;

    private Class<?> getValueClass() {
        if (value == null) return Void.class;
        else return value.getClass();
    }

    public Type getLiteralType() {
        return Type.fromJavaType(getValueClass());
    }

    public boolean isOfSameType(Literal other) {
        return this.getValueClass() == other.getValueClass();
    }

    public boolean isStringLiteral() {
        return getLiteralType() == STRING;
    }

    public boolean isFloatLiteral() {
        return getLiteralType() == FLOAT;
    }

    public boolean isDecimalLiteral() {
        return getLiteralType() == INT;
    }

    public boolean isBoolLiteral() {
        return getLiteralType() == BOOL;
    }

    public boolean isVoidLiteral() {
        return value == null;
    }

    public boolean isDurationLiteral() {
        return getLiteralType() == DURATION;
    }

    public boolean isDatetimeLiteral() {
        return getLiteralType() == DATETIME;
    }

    public boolean isTupleLiteral() {
        return getLiteralType() == TUPLE;
    }

    public static Literal voidLiteral() {
        return new Literal<Void>(null);
    }

    @Override
    public String toString() {
        return "Literal{" +
            "value=" + value +
            " type=" + getValueClass().getCanonicalName() +
            '}';
    }
}
