package com.github.mikee2509.eventscript.domain.expression;

import com.github.mikee2509.eventscript.EventScriptParser;
import com.github.mikee2509.eventscript.domain.scope.Declarable;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

@Builder
@Getter
@EqualsAndHashCode
public class Function implements Declarable {
    @Getter
    @Builder
    @EqualsAndHashCode
    public static class Parameter {
        private Type type;
        private String name;
    }

    private String name;
    private List<Parameter> parameters;
    private Returnable returnType = Type.VOID;
    private EventScriptParser.FunctionContext context;

    public int numParams() {
        return parameters.size();
    }

    public boolean checkTypes(Type[] types) {
        return Arrays.equals(getParameterTypes(), types);
    }

    public Type[] getParameterTypes() {
        return parameters.stream().map(p -> p.type).toArray(Type[]::new);
    }
}
