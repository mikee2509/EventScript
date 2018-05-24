package com.github.mikee2509.eventscript.domain.expression;

import com.github.mikee2509.eventscript.domain.scope.Declarable;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
@EqualsAndHashCode
public class Tuple implements Declarable, Returnable {
    private List<Type> types;
    private List<Literal> values;

    public static class Creator {
        private List<Type> typeList = new ArrayList<>();
        private List<Literal> valueList = new ArrayList<>();

        public Creator add(Literal literal) {
            typeList.add(literal.getLiteralType());
            valueList.add(literal);
            return this;
        }

        public Tuple create() {
            return Tuple.builder()
                .types(typeList)
                .values(valueList)
                .build();
        }
    }
}
