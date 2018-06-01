package com.github.mikee2509.eventscript.domain.exception;

import com.github.mikee2509.eventscript.domain.expression.Function;
import com.github.mikee2509.eventscript.domain.expression.Type;
import org.antlr.v4.runtime.Token;

import java.text.MessageFormat;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FunctionException extends ParserException {
    private FunctionException(Token token, String message) {
        super(token, message);
    }

    public static FunctionException voidParameter(Token token) {
        return new FunctionException(token, "Function parameter cannot be of void type");
    }

    public static FunctionException argumentException(Token token, String function, Type[] paramTypes) {
        String parameterList = Stream.of(paramTypes)
            .map(Type::getName)
            .collect(Collectors.joining(", ", "(", ")"));
        return new FunctionException(token, MessageFormat.format("Function {0} expects parameters of types {1}",
            function, parameterList));
    }

    public static FunctionException argumentException(Token token, String function, Type paramType) {
        return new FunctionException(token, MessageFormat.format("Function {0} expects parameter of type {1}",
            function, paramType.getName()));
    }

    public static FunctionException argumentException(Token token, String function) {
        return new FunctionException(token, MessageFormat.format("Function {0} takes no parameters", function));
    }

    public static FunctionException argumentException(Token token, Function function) {
        if (function.numParams() == 0) {
            return argumentException(token, function.getName());
        } else if (function.numParams() == 1) {
            return argumentException(token, function.getName(), function.getParameterTypes()[0]);
        } else {
            return argumentException(token, function.getName(), function.getParameterTypes());
        }
    }

    public static FunctionException toStringException(Token token, Type[] stringableTypes) {
        return new FunctionException(token, "toString can only be invoked on types: " +
            Stream.of(stringableTypes).map(Type::getName).collect(Collectors.joining(", ")));
    }

    public static FunctionException cannotResolve(Token token, String functionName) {
        return new FunctionException(token, "Cannot resolve function " + functionName);
    }

    public static FunctionException returnTypeException(Token token, Type type) {
        return returnTypeException(token, new Type[]{type});
    }

    public static FunctionException returnTypeException(Token token, Type[] types) {
        String typeString;
        if (types.length == 1) {
            typeString = types[0].getName();
        } else {
            typeString = Stream.of(types).map(Type::getName).collect(Collectors.joining(", ", "(", ")"));
        }
        return new FunctionException(token, "Function returns: " + typeString);
    }

    public static FunctionException missingReturnStatement(Token token) {
        return new FunctionException(token, "Missing return statement");
    }

    public static FunctionException duplicateParameterNames(Token token) {
        return new FunctionException(token, "Duplicate parameter names");
    }

    public static FunctionException unimplementedFunction(Token token, String name) {
        return new FunctionException(token, MessageFormat.format("Function {0} not yet implemented", name));
    }
}
