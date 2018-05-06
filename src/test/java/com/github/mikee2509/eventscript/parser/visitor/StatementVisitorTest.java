package com.github.mikee2509.eventscript.parser.visitor;

import com.github.mikee2509.eventscript.EventScriptParser;
import com.github.mikee2509.eventscript.domain.Statement;
import com.github.mikee2509.eventscript.domain.expression.Literal;
import com.github.mikee2509.eventscript.domain.scope.Scope;
import com.github.mikee2509.eventscript.parser.ParserCreator;
import com.github.mikee2509.eventscript.parser.exception.ScopeException;
import com.github.mikee2509.eventscript.parser.util.LiteralArithmetic;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.*;

public class StatementVisitorTest {
    private ParserCreator parserCreator;

    @Before
    public void setUp() throws Exception {
        parserCreator = new ParserCreator();
    }

    private Statement statement(String input, Scope scope) {
        ExpressionVisitor expressionVisitor = new ExpressionVisitor(new LiteralArithmetic());
        TypeVisitor typeVisitor = new TypeVisitor();
        StatementVisitor statementVisitor = new StatementVisitor(scope, expressionVisitor, typeVisitor);
        EventScriptParser parser = parserCreator.fromString(input);
        return statementVisitor.visit(parser.statement());
    }

    @Test
    public void visitVariableDeclaration() {
        Scope scope = new Scope();
        statement("var myBool : bool;", scope);
        assertThat(scope.lookupSymbol("myBool")).isEqualTo(new Literal<>(false));

        statement("var myInt : int;", scope);
        assertThat(scope.lookupSymbol("myInt")).isEqualTo(new Literal<>(0));

        statement("var myFloat : float;", scope);
        assertThat(scope.lookupSymbol("myFloat")).isEqualTo(new Literal<>(0.0f));

        statement("var myString : string;", scope);
        assertThat(scope.lookupSymbol("myString")).isEqualTo(new Literal<>(""));

        assertThatExceptionOfType(ScopeException.class).isThrownBy(() -> {
            statement("var myVoid : void;", scope);
        });

        assertThatExceptionOfType(ScopeException.class).isThrownBy(() -> {
            statement("var myFunc : func;", scope);
        });
    }
}