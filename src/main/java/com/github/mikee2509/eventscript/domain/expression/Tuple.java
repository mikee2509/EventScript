package com.github.mikee2509.eventscript.domain.expression;

import com.github.mikee2509.eventscript.domain.scope.Declarable;
import lombok.Builder;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@Builder
@EqualsAndHashCode
public class Tuple implements Declarable, Returnable {
    private Type[] types;
    private Literal[] literals;

    public static class Creator {
        private List<Type> typeList = new ArrayList<>();
        private List<Literal> literalList = new ArrayList<>();

        public Creator add(Literal literal) {
            typeList.add(literal.getLiteralType());
            literalList.add(literal);
            return this;
        }

        public Tuple create() {
            return Tuple.builder()
                .types(typeList.toArray(new Type[0]))
                .literals(literalList.toArray(new Literal[0]))
                .build();
        }
    }

    public Type[] types() {
        return types;
    }

    public Literal[] literals() {
        return literals;
    }

    public int size() {
        return types.length;
    }

}
