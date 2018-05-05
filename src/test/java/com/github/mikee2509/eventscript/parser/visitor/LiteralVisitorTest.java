package com.github.mikee2509.eventscript.parser.visitor;

import com.github.mikee2509.eventscript.EventScriptParser;
import com.github.mikee2509.eventscript.domain.expression.Literal;
import com.github.mikee2509.eventscript.parser.ParserCreator;
import com.github.mikee2509.eventscript.parser.exception.OperationException;
import com.github.mikee2509.eventscript.parser.util.LiteralArithmetic;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class LiteralVisitorTest {
    private ParserCreator parserCreator;
    private LiteralVisitor visitor;

    @Before
    public void setUp() throws Exception {
        parserCreator = new ParserCreator();
        visitor = new LiteralVisitor(new LiteralArithmetic());
    }

    private Literal expression(String input) {
        EventScriptParser parser = parserCreator.fromString(input);
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

//    @Test
//    public void equalityOperation() {
//        Literal literal = expression("2 == 2");
//        assertThat(literal.isBoolLiteral()).isTrue();
//        assertThat(literal.getValue()).isEqualTo(false);
//    }
}