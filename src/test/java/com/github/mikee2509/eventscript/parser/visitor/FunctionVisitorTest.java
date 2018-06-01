package com.github.mikee2509.eventscript.parser.visitor;

import com.github.mikee2509.eventscript.EventScriptParser;
import com.github.mikee2509.eventscript.domain.expression.Literal;
import com.github.mikee2509.eventscript.parser.ParserCreator;
import com.github.mikee2509.eventscript.parser.util.LiteralArithmetic;
import com.github.mikee2509.eventscript.parser.util.ScopeManager;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.*;
import static com.github.mikee2509.eventscript.domain.expression.Type.*;

public class FunctionVisitorTest {
    private TestUtils testUtils;
    private ParserCreator parserCreator;
    private LiteralArithmetic literalArithmetic;

    @Before
    public void setUp() throws Exception {
        testUtils = new TestUtils(Logger.getLogger(FunctionVisitor.class.getName()));
        parserCreator = new ParserCreator();
        literalArithmetic = new LiteralArithmetic();
    }

    private Literal expression(String input) {
        return expression(input, new ScopeManager());
    }

    private Literal expression(String input, ScopeManager scopeManager) {
        EventScriptParser parser = parserCreator.fromString(input);
        FunctionVisitor functionVisitor = new FunctionVisitor(scopeManager);
        ExpressionVisitor visitor = new ExpressionVisitor(scopeManager, literalArithmetic, functionVisitor);
        return visitor.visit(parser.expression());
    }

    @Test
    public void speakFunc() {
        List<String> logRecords = testUtils.captureLogs(() -> {
            expression("Speak(\"apple\")");
        });
        assertThat(logRecords).containsOnly("apple");
    }

    @Test
    public void toStringFunc() {
        Literal expression = expression("duration(1,2,3,4).toString");
        assertThat(expression.isStringLiteral()).isTrue();
        assertThat(expression.getValue()).isEqualTo("4d 3h 2m 1s");

        expression = expression("datetime(2018,5,15,13,45).toString");
        assertThat(expression.isStringLiteral()).isTrue();
        assertThat(expression.getValue()).isEqualTo("2018-05-15 13:45:00");
    }
}