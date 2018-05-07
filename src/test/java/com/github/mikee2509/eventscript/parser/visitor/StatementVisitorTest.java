package com.github.mikee2509.eventscript.parser.visitor;

import com.github.mikee2509.eventscript.EventScriptParser;
import com.github.mikee2509.eventscript.domain.expression.Literal;
import com.github.mikee2509.eventscript.domain.expression.Type;
import com.github.mikee2509.eventscript.parser.ParserCreator;
import com.github.mikee2509.eventscript.parser.exception.OperationException;
import com.github.mikee2509.eventscript.parser.exception.ScopeException;
import com.github.mikee2509.eventscript.parser.util.LiteralArithmetic;
import com.github.mikee2509.eventscript.parser.util.ScopeManager;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class StatementVisitorTest {
    private ParserCreator parserCreator;

    @Before
    public void setUp() throws Exception {
        parserCreator = new ParserCreator();
    }

    private void statement(String input, ScopeManager scopeManager) {
        ExpressionVisitor expressionVisitor = new ExpressionVisitor(scopeManager, new LiteralArithmetic());
        StatementVisitor statementVisitor = new StatementVisitor(scopeManager, expressionVisitor, new TypeVisitor());
        EventScriptParser parser = parserCreator.fromString(input);
        statementVisitor.visit(parser.statement());
    }

    @Test
    public void visitVariableDeclaration() {
        ScopeManager scope = new ScopeManager();
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

        assertThatExceptionOfType(ScopeException.class).isThrownBy(() -> {
            statement("var myInt : string;", scope);
        });

        // TODO test declaring variable of duration and datetime type
    }


    @Test
    public void visitVariableDefinition() {
        ScopeManager scope = new ScopeManager();
        statement("var myBool = true;", scope);
        assertThat(scope.lookupSymbol("myBool")).isEqualTo(new Literal<>(true));

        statement("var myInt = 111;", scope);
        assertThat(scope.lookupSymbol("myInt")).isEqualTo(new Literal<>(111));

        statement("var myFloat = 10.0;", scope);
        assertThat(scope.lookupSymbol("myFloat")).isEqualTo(new Literal<>(10.0f));

        statement("var myString = \"Hello\";", scope);
        assertThat(scope.lookupSymbol("myString")).isEqualTo(new Literal<>("Hello"));

        assertThatExceptionOfType(ScopeException.class).isThrownBy(() -> {
            statement("var myInt = \"Test\";", scope);
        });

        // TODO test creating a variable from function returning void
        // TODO test creating a variable from duration and datetime literals
    }

    @Test
    public void visitIfStmt() {
        ScopeManager scope = new ScopeManager();
        scope.defineSymbol("myInt", new Literal<>(0));
        statement("if (true) {\n" +
            "   myInt = 10\n" +
            "} else {\n" +
            "   myInt = 20\n" +
            "}", scope);

        assertThat(scope.lookupSymbol("myInt")).isEqualTo(new Literal<>(10));

        statement("if (false) myInt = 30\n" +
            "else myInt = 40\n", scope);

        assertThat(scope.lookupSymbol("myInt")).isEqualTo(new Literal<>(40));

        assertThatExceptionOfType(OperationException.class).isThrownBy(() -> {
            statement("if (1) myInt = 50;", scope);
        }).withMessageContaining(Type.BOOL.getName());
    }
}