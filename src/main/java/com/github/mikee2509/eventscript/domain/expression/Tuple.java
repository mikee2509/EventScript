package com.github.mikee2509.eventscript.domain.expression;

import lombok.Builder;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Builder
@EqualsAndHashCode
public class Tuple implements Returnable {
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

    public static Tuple fromLiteralList(List<Literal> literals) {
        return literals.stream()
            .collect(Tuple.Creator::new, Tuple.Creator::add, (no, op) -> {})
            .create();
    }

    public static Creator creator() {
        return new Creator();
    }

    public Type[] types() {
        return types;
    }

    public Stream<Type> typeStream() {
        return Stream.of(types);
    }

    public Literal[] literals() {
        return literals;
    }

    public Stream<Literal> literalStream() {
        return Stream.of(literals);
    }

    public int size() {
        return types.length;
    }

}
