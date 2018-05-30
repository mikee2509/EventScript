package com.github.mikee2509.eventscript.parser.visitor;

import com.github.mikee2509.eventscript.EventScriptParser;
import com.github.mikee2509.eventscript.domain.exception.parser.LiteralException;
import com.github.mikee2509.eventscript.domain.expression.Literal;
import com.github.mikee2509.eventscript.domain.expression.Type;
import com.github.mikee2509.eventscript.parser.ParserCreator;
import com.github.mikee2509.eventscript.domain.exception.parser.OperationException;
import com.github.mikee2509.eventscript.domain.exception.parser.ScopeException;
import com.github.mikee2509.eventscript.parser.util.LiteralArithmetic;
import com.github.mikee2509.eventscript.parser.util.ScopeManager;
import org.junit.Before;
import org.junit.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.within;

public class ExpressionVisitorTest {
    private TestUtils testUtils;
    private ParserCreator parserCreator;
    private LiteralArithmetic literalArithmetic;

    @Before
    public void setUp() throws Exception {
        testUtils = new TestUtils(Logger.getLogger(ExpressionVisitor.class.getName()));
        parserCreator = new ParserCreator();
        literalArithmetic = new LiteralArithmetic();
    }

    private Literal expression(String input) {
        return expression(input, new ScopeManager());
    }

    private Literal expression(String input, ScopeManager scopeManager) {
        EventScriptParser parser = parserCreator.fromString(input);
        ExpressionVisitor visitor = new ExpressionVisitor(scopeManager, literalArithmetic);
        return visitor.visit(parser.expression());
    }

    @Test
    public void integerAdditiveOperations() {
        Literal literal = expression("10 + 99");
        assertThat(literal.isDecimalLiteral()).isTrue();
        assertThat(literal.getValue()).isEqualTo(109);

        literal = expression("10 - 99");
        assertThat(literal.isDecimalLiteral()).isTrue();
        assertThat(literal.getValue()).isEqualTo(-89);
    }

    @Test
    public void floatAdditiveOperations() {
        Literal literal = expression("10.0 + 99.5");
        assertThat(literal.isFloatLiteral()).isTrue();
        assertThat(literal.getValue()).isEqualTo(109.5f);

        literal = expression("10.0 - 99.5");
        assertThat(literal.isFloatLiteral()).isTrue();
        assertThat(literal.getValue()).isEqualTo(-89.5f);
    }

    @Test
    public void floatIntegerAdditiveOperations() {
        Literal literal = expression("10.5 + 99");
        assertThat(literal.isFloatLiteral()).isTrue();
        assertThat(literal.getValue()).isEqualTo(109.5f);

        literal = expression("10.5 - 99");
        assertThat(literal.isFloatLiteral()).isTrue();
        assertThat(literal.getValue()).isEqualTo(-88.5f);
    }

    @Test
    public void integerFloatAdditiveOperations() {
        Literal literal = expression("10 + 99.5");
        assertThat(literal.isFloatLiteral()).isTrue();
        assertThat(literal.getValue()).isEqualTo(109.5f);


        literal = expression("10 - 99.5");
        assertThat(literal.isFloatLiteral()).isTrue();
        assertThat(literal.getValue()).isEqualTo(-89.5f);
    }


    @Test
    public void integerMultiplicativeOperations() {
        Literal literal = expression("12 * 2");
        assertThat(literal.isDecimalLiteral()).isTrue();
        assertThat(literal.getValue()).isEqualTo(24);

        literal = expression("5 / 2");
        assertThat(literal.isDecimalLiteral()).isTrue();
        assertThat(literal.getValue()).isEqualTo(2);

        literal = expression("4 % 3");
        assertThat(literal.isDecimalLiteral()).isTrue();
        assertThat(literal.getValue()).isEqualTo(1);
    }

    @Test
    public void floatMultiplicativeOperations() {
        Literal literal = expression("12.0 * 2.0");
        assertThat(literal.isFloatLiteral()).isTrue();
        assertThat(literal.getValue()).isEqualTo(24.0f);

        literal = expression("5.0 / 2.0");
        assertThat(literal.isFloatLiteral()).isTrue();
        assertThat(literal.getValue()).isEqualTo(2.5f);

        literal = expression("4.0 % 3.0");
        assertThat(literal.isFloatLiteral()).isTrue();
        assertThat(literal.getValue()).isEqualTo(1.0f);
    }

    @Test
    public void floatIntegerMultiplicativeOperations() {
        Literal literal = expression("12.0 * 2");
        assertThat(literal.isFloatLiteral()).isTrue();
        assertThat(literal.getValue()).isEqualTo(24.0f);

        literal = expression("5.0 / 2");
        assertThat(literal.isFloatLiteral()).isTrue();
        assertThat(literal.getValue()).isEqualTo(2.5f);

        literal = expression("4.0 % 3");
        assertThat(literal.isFloatLiteral()).isTrue();
        assertThat(literal.getValue()).isEqualTo(1.0f);
    }

    @Test
    public void integerFloatMultiplicativeOperations() {
        Literal literal = expression("12 * 2.0");
        assertThat(literal.isFloatLiteral()).isTrue();
        assertThat(literal.getValue()).isEqualTo(24.0f);

        literal = expression("5 / 2.0");
        assertThat(literal.isFloatLiteral()).isTrue();
        assertThat(literal.getValue()).isEqualTo(2.5f);

        literal = expression("4 % 3.0");
        assertThat(literal.isFloatLiteral()).isTrue();
        assertThat(literal.getValue()).isEqualTo(1.0f);
    }

    @Test
    public void visitDatetimeLiteral() {
        Literal literal = expression("datetime()");
        assertThat(literal.isDatetimeLiteral()).isTrue();
        assertThat((LocalDateTime) literal.getValue()).isCloseTo(LocalDateTime.now(), within(1, ChronoUnit.SECONDS));

        literal = expression("datetime(2018, 5, 10, 14, 45)");
        assertThat(literal.isDatetimeLiteral()).isTrue();
        assertThat(literal.getValue()).isEqualTo(LocalDateTime.of(2018, 5, 10, 14, 45));

        literal = expression("datetime(2018, 5, 10, 14, 45, 15)");
        assertThat(literal.isDatetimeLiteral()).isTrue();
        assertThat(literal.getValue()).isEqualTo(LocalDateTime.of(2018, 5, 10, 14, 45, 15));

        assertThatExceptionOfType(LiteralException.class).isThrownBy(() -> {
            expression("datetime(2018)");
        });

        assertThatExceptionOfType(LiteralException.class).isThrownBy(() -> {
            expression("datetime(2018, 5, 10)");
        });

        assertThatExceptionOfType(LiteralException.class).isThrownBy(() -> {
            expression("datetime(1, 2, 3, 4, 5, 6, 7)");
        });

        assertThatExceptionOfType(LiteralException.class).isThrownBy(() -> {
            expression("datetime(2018, 5, 10, \"14\", 45, 15)");
        });
    }

    @Test
    public void visitDurationLiteral() {
        Literal literal = expression("duration()");
        assertThat(literal.isDurationLiteral()).isTrue();
        assertThat(literal.getValue()).isEqualTo(Duration.ZERO);

        literal = expression("duration(5)");
        assertThat(literal.isDurationLiteral()).isTrue();
        assertThat(literal.getValue()).isEqualTo(Duration.ofSeconds(5));

        literal = expression("duration(5, 10)");
        assertThat(literal.isDurationLiteral()).isTrue();
        assertThat(literal.getValue()).isEqualTo(Duration.ofSeconds(5).plusMinutes(10));

        literal = expression("duration(5, 10, 1)");
        assertThat(literal.isDurationLiteral()).isTrue();
        assertThat(literal.getValue()).isEqualTo(Duration.ofSeconds(5).plusMinutes(10).plusHours(1));

        literal = expression("duration(5, 10, 1, 2)");
        assertThat(literal.isDurationLiteral()).isTrue();
        assertThat(literal.getValue()).isEqualTo(Duration.ofSeconds(5).plusMinutes(10).plusHours(1).plusDays(2));

        assertThatExceptionOfType(LiteralException.class).isThrownBy(() -> {
            expression("duration(\"5\")");
        });

        assertThatExceptionOfType(LiteralException.class).isThrownBy(() -> {
            expression("duration(1, \"5\")");
        });

        assertThatExceptionOfType(LiteralException.class).isThrownBy(() -> {
            expression("duration(1, 2, 3, 4, 5)");
        });
    }


    @Test
    public void stringAdditiveOperations() {
        Literal literal = expression("\"Hello \" + \"World!\"");
        assertThat(literal.isStringLiteral()).isTrue();
        assertThat(literal.getValue()).isEqualTo("Hello World!");

        literal = expression("\"Hello integer \" + 2");
        assertThat(literal.isStringLiteral()).isTrue();
        assertThat(literal.getValue()).isEqualTo("Hello integer 2");

        literal = expression("2.3 + \" says hello\"");
        assertThat(literal.isStringLiteral()).isTrue();
        assertThat(literal.getValue()).isEqualTo("2.3 says hello");

        assertThatExceptionOfType(OperationException.class).isThrownBy(() -> {
            expression("\"Hello \" - \"World!\"");
        });

        // TODO test additive operation between string and time types
    }


    @Test
    public void integerUnaryOperations() {
        Literal literal = expression("-2");
        assertThat(literal.isDecimalLiteral()).isTrue();
        assertThat(literal.getValue()).isEqualTo(-2);

        literal = expression("+2");
        assertThat(literal.isDecimalLiteral()).isTrue();
        assertThat(literal.getValue()).isEqualTo(2);

        literal = expression("--2");
        assertThat(literal.isDecimalLiteral()).isTrue();
        assertThat(literal.getValue()).isEqualTo(1);

        literal = expression("++2");
        assertThat(literal.isDecimalLiteral()).isTrue();
        assertThat(literal.getValue()).isEqualTo(3);
    }

    @Test
    public void floatUnaryOperations() {
        Literal literal = expression("-2.5");
        assertThat(literal.isFloatLiteral()).isTrue();
        assertThat(literal.getValue()).isEqualTo(-2.5f);

        literal = expression("+2.5");
        assertThat(literal.isFloatLiteral()).isTrue();
        assertThat(literal.getValue()).isEqualTo(2.5f);

        literal = expression("--2.5");
        assertThat(literal.isFloatLiteral()).isTrue();
        assertThat(literal.getValue()).isEqualTo(1.5f);

        literal = expression("++2.5");
        assertThat(literal.isFloatLiteral()).isTrue();
        assertThat(literal.getValue()).isEqualTo(3.5f);
    }

    @Test
    public void unaryOperationsExceptions() {
        assertThatExceptionOfType(OperationException.class).isThrownBy(() -> {
            expression("+\"2.5\"");
        });
    }

    @Test
    public void negationOperation() {
        Literal literal = expression("!true");
        assertThat(literal.isBoolLiteral()).isTrue();
        assertThat(literal.getValue()).isEqualTo(false);

        assertThatExceptionOfType(OperationException.class).isThrownBy(() -> {
            expression("!0");
        });
    }

    @Test
    public void logicalAndOperation() {
        Literal literal = expression("true && false");
        assertThat(literal.isBoolLiteral()).isTrue();
        assertThat(literal.getValue()).isEqualTo(false);

        assertThatExceptionOfType(OperationException.class).isThrownBy(() -> {
            expression("true && 1");
        });
    }


    @Test
    public void logicalOrOperation() {
        Literal literal = expression("false || true");
        assertThat(literal.isBoolLiteral()).isTrue();
        assertThat(literal.getValue()).isEqualTo(true);

        assertThatExceptionOfType(OperationException.class).isThrownBy(() -> {
            expression("true || 1");
        });
    }

    @Test
    public void equalityOperation() {
        Literal literal = expression("2 == 2");
        assertThat(literal.isBoolLiteral()).isTrue();
        assertThat(literal.getValue()).isEqualTo(true);

        literal = expression("2.0 == 2.1");
        assertThat(literal.isBoolLiteral()).isTrue();
        assertThat(literal.getValue()).isEqualTo(false);

        literal = expression("\"aaa\" == \"aaa\"");
        assertThat(literal.isBoolLiteral()).isTrue();
        assertThat(literal.getValue()).isEqualTo(true);

        literal = expression("true == false");
        assertThat(literal.isBoolLiteral()).isTrue();
        assertThat(literal.getValue()).isEqualTo(false);

        literal = expression("2 == 2.0");
        assertThat(literal.isBoolLiteral()).isTrue();
        assertThat(literal.getValue()).isEqualTo(true);


        literal = expression("2 != 2");
        assertThat(literal.isBoolLiteral()).isTrue();
        assertThat(literal.getValue()).isEqualTo(false);

        literal = expression("2.0 != 2.1");
        assertThat(literal.isBoolLiteral()).isTrue();
        assertThat(literal.getValue()).isEqualTo(true);

        literal = expression("\"aaa\" != \"aaa\"");
        assertThat(literal.isBoolLiteral()).isTrue();
        assertThat(literal.getValue()).isEqualTo(false);

        literal = expression("true != false");
        assertThat(literal.isBoolLiteral()).isTrue();
        assertThat(literal.getValue()).isEqualTo(true);

        literal = expression("2 != 2.0");
        assertThat(literal.isBoolLiteral()).isTrue();
        assertThat(literal.getValue()).isEqualTo(false);
    }


    @Test
    public void relationalOperation() {
        Literal literal = expression("2 < 3");
        assertThat(literal.isBoolLiteral()).isTrue();
        assertThat(literal.getValue()).isEqualTo(true);

        literal = expression("3 <= 3.0");
        assertThat(literal.isBoolLiteral()).isTrue();
        assertThat(literal.getValue()).isEqualTo(true);

        literal = expression("3.0 > 3");
        assertThat(literal.isBoolLiteral()).isTrue();
        assertThat(literal.getValue()).isEqualTo(false);

        literal = expression("4.0 > 3.9");
        assertThat(literal.isBoolLiteral()).isTrue();
        assertThat(literal.getValue()).isEqualTo(true);

        assertThatExceptionOfType(OperationException.class).isThrownBy(() -> {
            expression("\"1\" < \"2\"");
        });

        // TODO test relational operations between time types
    }

    @Test
    public void identifierOperation() {
        ScopeManager scope = new ScopeManager();
        scope.defineSymbol("myInt", new Literal<>(123));

        Literal literal = expression("myInt", scope);
        assertThat(literal.isDecimalLiteral()).isTrue();
        assertThat(literal.getValue()).isEqualTo(123);

        assertThatExceptionOfType(ScopeException.class).isThrownBy(() -> {
            expression("myFloat", scope);
        });
    }

    @Test
    public void assignmentOperation() {
        ScopeManager scope = new ScopeManager();
        scope.defineSymbol("myInt", new Literal<>(100));
        scope.defineSymbol("secondInt", new Literal<>(0));

        Literal literal = expression("myInt = 200", scope);
        assertThat(literal.isDecimalLiteral()).isTrue();
        assertThat(literal.getValue()).isEqualTo(200);
        assertThat(scope.lookupSymbol("myInt")).isEqualTo(new Literal<>(200));

        assertThatExceptionOfType(OperationException.class).isThrownBy(() -> {
            expression("myInt = 1.0", scope);
        }).withMessageContaining(Type.INT.getName());

        literal = expression("secondInt = myInt = 300", scope);
        assertThat(literal.isDecimalLiteral()).isTrue();
        assertThat(literal.getValue()).isEqualTo(300);
        assertThat(scope.lookupSymbol("secondInt")).isEqualTo(new Literal<>(300));
        assertThat(scope.lookupSymbol("myInt")).isEqualTo(new Literal<>(300));
    }

    @Test
    public void speakFunc() {
        List<String> logRecords = testUtils.captureLogs(() -> {
            expression("Speak(\"apple\")");
        });
        assertThat(logRecords).containsOnly("apple");
    }
}