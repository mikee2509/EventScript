package com.github.mikee2509.eventscript.domain.expression;

import com.github.mikee2509.eventscript.EventScriptParser;
import com.github.mikee2509.eventscript.domain.scope.Declarable;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
@EqualsAndHashCode
public class Function implements Declarable {
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
}
