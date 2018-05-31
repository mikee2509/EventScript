package com.github.mikee2509.eventscript.parser.visitor;

import com.github.mikee2509.eventscript.EventScriptParser;
import com.github.mikee2509.eventscript.domain.exception.parser.OperationException;
import com.github.mikee2509.eventscript.domain.exception.parser.ScopeException;
import com.github.mikee2509.eventscript.domain.expression.Literal;
import com.github.mikee2509.eventscript.domain.expression.Type;
import com.github.mikee2509.eventscript.domain.scope.Declarable;
import com.github.mikee2509.eventscript.parser.ParserCreator;
import com.github.mikee2509.eventscript.parser.util.LiteralArithmetic;
import com.github.mikee2509.eventscript.parser.util.ScopeManager;
import org.junit.Before;
import org.junit.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.*;

public class StatementVisitorTest {
    private TestUtils testUtils;
    private ParserCreator parserCreator;
    private LiteralArithmetic literalArithmetic;
    private TypeVisitor typeVisitor;

    @Before
    public void setUp() throws Exception {
        testUtils = new TestUtils(Logger.getLogger(ExpressionVisitor.class.getName()));
        parserCreator = new ParserCreator();
        literalArithmetic = new LiteralArithmetic();
        typeVisitor = new TypeVisitor();
    }

    private void statement(String input, ScopeManager scopeManager) {
        ExpressionVisitor expressionVisitor = new ExpressionVisitor(scopeManager, literalArithmetic);
        StatementVisitor statementVisitor = new StatementVisitor(scopeManager, expressionVisitor, typeVisitor);
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

        statement("var myDuration : duration", scope);
        assertThat(scope.lookupSymbol("myDuration")).isEqualTo(new Literal<>(Duration.ZERO));

        statement("var currentDate : datetime;", scope);
        Declarable dateVariable = scope.lookupSymbol("currentDate");
        assertThat(dateVariable).isInstanceOf(Literal.class);
        Literal dateLiteral = (Literal) dateVariable;
        assertThat(dateLiteral.isDatetimeLiteral()).isTrue();
        assertThat((LocalDateTime) dateLiteral.getValue()).isCloseTo(LocalDateTime.now(), within(1, ChronoUnit
            .SECONDS));

        assertThatExceptionOfType(ScopeException.class).isThrownBy(() -> {
            statement("var myVoid : void;", scope);
        });

        assertThatExceptionOfType(ScopeException.class).isThrownBy(() -> {
            statement("var myFunc : func;", scope);
        });

        assertThatExceptionOfType(ScopeException.class).isThrownBy(() -> {
            statement("var myInt : string;", scope);
        });
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

        statement("var myDuration = duration(0, 5);", scope);
        assertThat(scope.lookupSymbol("myDuration")).isEqualTo(new Literal<>(Duration.ofMinutes(5)));

        statement("var myDatetime = datetime(2018, 5, 15, 13, 45);", scope);
        assertThat(scope.lookupSymbol("myDatetime")).isEqualTo(
            new Literal<>(LocalDateTime.of(2018, 5, 15, 13, 45))
        );

        assertThatExceptionOfType(ScopeException.class).isThrownBy(() -> {
            statement("var myInt = \"Test\";", scope);
        });

        // TODO test creating a variable from function returning void
    }

    @Test
    public void visitIfStmt() {
        ScopeManager scope = new ScopeManager();
        scope.defineSymbol("myInt", new Literal<>(0));
        //@formatter:off
        String input = "if (true) {         \n" +
                       "   myInt = 10       \n" +
                       "} else {            \n" +
                       "   my  Int = 20     \n" +
                       "}                     " ;
        //@formatter:on
        statement(input, scope);

        assertThat(scope.lookupSymbol("myInt")).isEqualTo(new Literal<>(10));

        //@formatter:off
        String input2 = "if (false) myInt = 30  \n" +
                        "else myInt = 40        \n" ;
        //@formatter:on

        statement(input2, scope);

        assertThat(scope.lookupSymbol("myInt")).isEqualTo(new Literal<>(40));

        assertThatExceptionOfType(OperationException.class).isThrownBy(() -> {
            statement("if (1) myInt = 50;", scope);
        }).withMessageContaining(Type.BOOL.getName());
    }

    @Test
    public void visitForStmt() {
        ScopeManager scope = new ScopeManager();
        List<String> logRecords = testUtils.captureLogs(() -> {
            //@formatter:off
            String input = "for (var i = 0; i<5; ++i) {     \n" +
                           "    Speak(i)                    \n" +
                           "}                                 " ;
            //@formatter:on
            statement(input, scope);
        });
        assertThat(logRecords).isEqualTo(IntStream.range(0, 5).mapToObj(String::valueOf).collect(Collectors.toList()));
    }


    @Test
    public void visitBreakStmt() {
        ScopeManager scope = new ScopeManager();
        List<String> logRecords = testUtils.captureLogs(() -> {
            //@formatter:off
            String input = "for (var i = 0; i<5; ++i) {   \n" +
                           "    if (i == 3) break         \n" +
                           "    Speak(i)                  \n" +
                           "}                               " ;
            //@formatter:on
            statement(input, scope);
        });
        assertThat(logRecords).isEqualTo(IntStream.range(0, 3).mapToObj(String::valueOf).collect(Collectors.toList()));
        assertThat(scope.isRootScope()).isTrue();
        assertThat(scope.numGloballyDefinedSymbols()).isEqualTo(0);
    }

    @Test
    public void visitBreakStmt_breaksOnlyInnerLoop() {
        ScopeManager scope = new ScopeManager();
        List<String> logRecords = testUtils.captureLogs(() -> {
            //@formatter:off
            String input = "for (var i = 0; i<3; ++i) {       \n" +
                           "    for (var j = 0; j<5; ++j) {   \n" +
                           "        if (j == 2) break         \n" +
                           "        Speak(j)                  \n" +
                           "    }                             \n" +
                           "}                                   " ;
            //@formatter:on
            statement(input, scope);
        });
        assertThat(logRecords).isEqualTo(
            IntStream.range(0, 3)
                .mapToObj(i -> Arrays.asList("0", "1"))
                .flatMap(Collection::stream)
                .collect(Collectors.toList())
        );
        assertThat(scope.isRootScope()).isTrue();
        assertThat(scope.numGloballyDefinedSymbols()).isEqualTo(0);
    }

    @Test
    public void visitContinueStmt() {
        ScopeManager scope = new ScopeManager();
        List<String> logRecords = testUtils.captureLogs(() -> {
            //@formatter:off
            String input = "for (var i = 0; i<5; ++i) {     \n" +
                           "    if (i == 3) continue        \n" +
                           "    Speak(i)                    \n" +
                           "}                                 " ;
            //@formatter:on
            statement(input, scope);
        });
        assertThat(logRecords).isEqualTo(
            IntStream.range(0, 5).filter(i -> i != 3).mapToObj(String::valueOf).collect(Collectors.toList())
        );
        assertThat(scope.isRootScope()).isTrue();
        assertThat(scope.numGloballyDefinedSymbols()).isEqualTo(0);
    }

    @Test
    public void visitContinueStmt_breaksOnlyInnerLoop() {
        ScopeManager scope = new ScopeManager();
        List<String> logRecords = testUtils.captureLogs(() -> {
            //@formatter:off
            String input = "for (var i = 0; i<3; ++i) {         \n" +
                           "    for (var j = 0; j<3; ++j) {     \n" +
                           "        if (j == 1) continue        \n" +
                           "        Speak(j)                    \n" +
                           "    }                               \n" +
                           "}                                     " ;
            //@formatter:on
            statement(input, scope);
        });
        assertThat(logRecords).isEqualTo(
            IntStream.range(0, 3)
                .mapToObj(i -> Arrays.asList("0", "2"))
                .flatMap(Collection::stream)
                .collect(Collectors.toList())
        );
        assertThat(scope.isRootScope()).isTrue();
        assertThat(scope.numGloballyDefinedSymbols()).isEqualTo(0);
    }
}