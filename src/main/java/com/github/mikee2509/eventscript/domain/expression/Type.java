package com.github.mikee2509.eventscript.domain.expression;

import lombok.AllArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;

@AllArgsConstructor
public enum Type implements Returnable {
    //@formatter:off
    BOOL        (Boolean.class),
    DATETIME    (LocalDateTime.class),
    DURATION    (Duration.class),
    FLOAT       (Float.class),
    FUNC        (Function.class),
    INT         (Integer.class),
    STRING      (String.class),
    TUPLE       (Tuple.class),
    VOID        (Void.class);
    //@formatter:on

    private Class<?> javaType;

    public static Type fromJavaType(Class<?> javaType) {
        for (Type type : Type.values()) {
            if (type.javaType == javaType) {
                return type;
            }
        }
        throw new RuntimeException("No Type for given javaType");
    }

    public String getName() {
        return this.name().toLowerCase();
    }
}
