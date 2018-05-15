package com.github.mikee2509.eventscript.domain.expression;

import com.github.mikee2509.eventscript.domain.scope.Declarable;
import lombok.Builder;
import lombok.EqualsAndHashCode;

import java.util.List;

@Builder
@EqualsAndHashCode
public class Tuple implements Declarable, Returnable{
    private List<Type> types;
    private List<Literal> values;
}
