package com.github.mikee2509.eventscript.parser.visitor;

import com.github.mikee2509.eventscript.EventScriptParser;
import com.github.mikee2509.eventscript.domain.exception.FunctionException;
import com.github.mikee2509.eventscript.domain.exception.ScopeException;
import com.github.mikee2509.eventscript.domain.exception.control.ControlFlowException;
import com.github.mikee2509.eventscript.domain.expression.Function;
import com.github.mikee2509.eventscript.domain.expression.Literal;
import com.github.mikee2509.eventscript.domain.expression.Tuple;
import com.github.mikee2509.eventscript.domain.expression.Type;
import com.github.mikee2509.eventscript.parser.ParserCreator;
import com.github.mikee2509.eventscript.parser.util.LiteralArithmetic;
import com.github.mikee2509.eventscript.parser.util.ScopeManager;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.github.mikee2509.eventscript.domain.exception.control.ControlFlowException.*;
import static com.github.mikee2509.eventscript.domain.expression.Type.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.fail;

public class ScriptVisitorTest {
    private TestUtils testUtils;
    private ParserCreator parserCreator;
    private TypeVisitor typeVisitor;
    private LiteralArithmetic literalArithmetic;

    @Before
    public void setUp() {
        testUtils = new TestUtils(Logger.getLogger(FunctionVisitor.class.getName()));
        parserCreator = new ParserCreator();
        typeVisitor = new TypeVisitor();
        literalArithmetic = new LiteralArithmetic();
    }

    private void script(String input) {
        script(input, new ScopeManager());
    }

    private void script(String input, ScopeManager scopeManager) {
        literalArithmetic = new LiteralArithmetic();
        FuncParamVisitor funcParamVisitor = new FuncParamVisitor(scopeManager);
        FunctionVisitor functionVisitor = new FunctionVisitor(funcParamVisitor);
        ExpressionVisitor expressionVisitor = new ExpressionVisitor(scopeManager, literalArithmetic, functionVisitor);
        StatementVisitor statementVisitor = new StatementVisitor(scopeManager, expressionVisitor, typeVisitor);
        ScriptVisitor scriptVisitor = new ScriptVisitor(scopeManager, statementVisitor, typeVisitor);
        EventScriptParser parser = parserCreator.fromString(input);
        scriptVisitor.visit(parser.script());
    }

    @Test
    public void addFunctionsToScope() {
        ScopeManager scope = new ScopeManager();
        Function makePair = Function.builder()
            .name("makePair")
            .parameters(Arrays.asList(
                Function.Parameter.builder().name("a").type(INT).build(),
                Function.Parameter.builder().name("b").type(INT).build()))
            .returnType(Tuple.builder().types(new Type[]{INT, INT}).build())
            .build();

        Function repeat = Function.builder()
            .name("repeat")
            .parameters(Arrays.asList(
                Function.Parameter.builder().name("times").type(INT).build(),
                Function.Parameter.builder().name("callback").type(FUNC).build()))
            .returnType(VOID)
            .build();

        //@formatter:off
        String input =
            "func makePair(a: int, b: int) -> (int, int) {  \n" +
            "    return a, b                                \n" +
            "}                                              \n" +
            "                                               \n" +
            "func repeat(times: int, callback: func) {      \n" +
            "    for(var i = 0; i < times; ++i) {           \n" +
            "        callback()                             \n" +
            "    }                                          \n" +
            "}";
        //@formatter:on

        script(input, scope);
        assertThat(scope.lookupSymbol("makePair"))
            .isEqualToIgnoringGivenFields(makePair, "context");
        assertThat(scope.lookupSymbol("repeat"))
            .isEqualToIgnoringGivenFields(repeat, "context");

        assertThatExceptionOfType(ScopeException.class).isThrownBy(() -> {
            script("var makePair : string;", scope);
        });
    }

    @Test
    public void noParamsFunctionCall() {
        ScopeManager scope = new ScopeManager();
        //@formatter:off
        String input = "var apple = 2            \n" +
                       "incrementApple()         \n" +
                       "                         \n" +
                       "func incrementApple() {  \n" +
                       "    ++apple              \n" +
                       "}                          " ;
        //@formatter:on
        script(input, scope);
        assertThat(scope.lookupSymbol("apple")).isEqualTo(new Literal<>(3));


        //@formatter:off
        String input2 = "var apple = 2            \n" +
                        "incrementApple(3)        \n" +
                        "                         \n" +
                        "func incrementApple() {  \n" +
                        "    ++apple              \n" +
                        "}                          " ;
        //@formatter:on
        assertThatExceptionOfType(FunctionException.class).isThrownBy(() -> {
            script(input2);
        });
    }

    @Test
    public void withParamsFunctionCall() {
        ScopeManager scope = new ScopeManager();
        //@formatter:off
        String input = "var result = sum(2, 2)             \n" +
                       "                                   \n" +
                       "func sum(a: int, b: int) -> int {  \n" +
                       "    return a + b                   \n" +
                       "}                                    " ;
        //@formatter:on
        script(input, scope);
        assertThat(scope.lookupSymbol("result")).isEqualTo(new Literal<>(4));


        //@formatter:off
        String input2 = "var result = sum(2, 2.0)           \n" +
                        "                                   \n" +
                        "func sum(a: int, b: int) -> int {  \n" +
                        "    return a + b                   \n" +
                        "}                                    " ;
        //@formatter:on
        assertThatExceptionOfType(FunctionException.class).isThrownBy(() -> {
            script(input2);
        });
    }

    @Test
    public void tupleReturningFunctionCall() {
        ScopeManager scope = new ScopeManager();
        //@formatter:off
        String input = "var result = addAndMultiply(2, 5)                   \n" +
                       "                                                    \n" +
                       "func addAndMultiply(a: int, b: int) -> (int, int) { \n" +
                       "   return a + b, a * b                              \n" +
                       "}                                                     " ;
        //@formatter:on
        script(input, scope);
        assertThat(scope.lookupSymbol("result")).isEqualTo(
            new Literal<>(
                new Tuple.Creator()
                    .add(new Literal<>(7))
                    .add(new Literal<>(10))
                    .create()
            )
        );


        //@formatter:off
        String input2 = "var result = addAndMultiply(2, 5)                   \n" +
                        "                                                    \n" +
                        "func addAndMultiply(a: int, b: int) -> (int, int) { \n" +
                        "   return a + b                                     \n" +
                        "}                                                     " ;
        //@formatter:on
        assertThatExceptionOfType(FunctionException.class).isThrownBy(() -> {
            script(input2);
        });
    }

    @Test
    public void missingReturnFunctionCall() {
        //@formatter:off
        String input = "var result = addAndMultiply(2, 5)                   \n" +
                       "                                                    \n" +
                       "func addAndMultiply(a: int, b: int) -> (int, int) { \n" +
                       "   a + b                                            \n" +
                       "}                                                     " ;
        //@formatter:on
        assertThatExceptionOfType(FunctionException.class).isThrownBy(() -> {
            script(input);
        });
    }

    @Test
    public void nestedFunctionCalls() {
        ScopeManager scope = new ScopeManager();
        //@formatter:off
        String input = "var result = addThenMultiply(2, 3, 4)                   \n" +
                       "                                                        \n" +
                       "func addThenMultiply(a: int, b: int, c: int) -> int {   \n" +
                       "   return add(a, b) * c                                 \n" +
                       "}                                                       \n" +
                       "                                                        \n" +
                       "func add(a: int, b: int) -> int {                       \n" +
                       "   return a + b                                         \n" +
                       "}                                                         " ;
        //@formatter:on

        script(input, scope);
        assertThat(scope.lookupSymbol("result")).isEqualTo(new Literal<>(20));
    }

    @Test
    public void variableScopes() {
        ScopeManager scope = new ScopeManager();
        //@formatter:off
        String input = "var c = 5                                               \n" +
                       "var result = addThenMultiply(2, 3, 4)                   \n" +
                       "                                                        \n" +
                       "func addThenMultiply(a: int, b: int, c: int) -> int {   \n" +
                       "   return add(a + 1, b) * c                             \n" +
                       "}                                                       \n" +
                       "                                                        \n" +
                       "func add(a: int, b: int) -> int {                       \n" +
                       "   c = 0                                                \n" +
                       "   return a + b                                         \n" +
                       "}                                                         " ;
        //@formatter:on

        script(input, scope);
        assertThat(scope.lookupSymbol("result")).isEqualTo(new Literal<>(24));
        assertThat(scope.lookupSymbol("c")).isEqualTo(new Literal<>(0));
    }

    @Test
    public void duplicateParameterNames() {
        //@formatter:off
        String input = "var result = sum(2, \"3\")              \n" +
                       "                                        \n" +
                       "func sum(a: int, a: string) -> int {    \n" +
                       "    return a + a                        \n" +
                       "}                                         " ;
        //@formatter:on
        assertThatExceptionOfType(FunctionException.class).isThrownBy(() -> {
            script(input);
        });
    }

    @Test
    public void createVariableFromFunction() {
        //@formatter:off
        String input = "var apple = 2             \n" +
                       "var x = incrementApple()  \n" +
                       "                          \n" +
                       "func incrementApple() {   \n" +
                       "    ++apple               \n" +
                       "}                           " ;
        //@formatter:on
        assertThatExceptionOfType(ScopeException.class).isThrownBy(() -> {
            script(input);
        });
    }

    @Test
    public void breakStatementInWrongContext() {
        assertThatExceptionOfType(ControlFlowException.class).isThrownBy(() -> {
            script("break;");
        }).withMessageContaining(BREAK_IN_WRONG_CONTEXT);

        //@formatter:off
        String input = "var apple = 2       \n" +
                       "if (apple == 2) {   \n" +
                       "   break            \n" +
                       "}                     " ;
        //@formatter:on
        assertThatExceptionOfType(ControlFlowException.class).isThrownBy(() -> {
            script(input);
        }).withMessageContaining(BREAK_IN_WRONG_CONTEXT);

        //@formatter:off
        String input2 = "for (var i = 0; i < 5; ++i) {      \n" +
                        "   add(i, i + 1)                   \n" +
                        "}                                  \n" +
                        "                                   \n" +
                        "func add(a: int, b: int) -> int {  \n" +
                        "   break                           \n" +
                        "}                                    " ;
        //@formatter:on
        assertThatExceptionOfType(ControlFlowException.class).isThrownBy(() -> {
            script(input2);
        }).withMessageContaining(BREAK_IN_WRONG_CONTEXT);
    }

    @Test
    public void continueStatementInWrongContext() {
        assertThatExceptionOfType(ControlFlowException.class).isThrownBy(() -> {
            script("continue;");
        }).withMessageContaining(CONTINUE_IN_WRONG_CONTEXT);

        //@formatter:off
        String input = "var apple = 2       \n" +
                       "if (apple == 2) {   \n" +
                       "   continue         \n" +
                       "}                     " ;
        //@formatter:on
        assertThatExceptionOfType(ControlFlowException.class).isThrownBy(() -> {
            script(input);
        }).withMessageContaining(CONTINUE_IN_WRONG_CONTEXT);

        //@formatter:off
        String input2 = "for (var i = 0; i < 5; ++i) {      \n" +
                        "   add(i, i + 1)                   \n" +
                        "}                                  \n" +
                        "                                   \n" +
                        "func add(a: int, b: int) -> int {  \n" +
                        "   continue                        \n" +
                        "}                                    " ;
        //@formatter:on
        assertThatExceptionOfType(ControlFlowException.class).isThrownBy(() -> {
            script(input2);
        }).withMessageContaining(CONTINUE_IN_WRONG_CONTEXT);
    }

    @Test
    public void returnStatementInWrongContext() {
        assertThatExceptionOfType(ControlFlowException.class).isThrownBy(() -> {
            script("return;");
        }).withMessageContaining(RETURN_IN_WRONG_CONTEXT);

        //@formatter:off
        String input = "var apple = 2       \n" +
                       "if (apple == 2) {   \n" +
                       "   return           \n" +
                       "}                     " ;
        //@formatter:on
        assertThatExceptionOfType(ControlFlowException.class).isThrownBy(() -> {
            script(input);
        }).withMessageContaining(RETURN_IN_WRONG_CONTEXT);
    }

    @Test
    public void onIntervalFunc() throws InterruptedException {
        //@formatter:off
        String input = "OnInterval(sayHello, duration(1), duration())   \n" +
                       "                                                \n" +
                       "func sayHello() {                               \n" +
                       "    Speak(\"Hello\")                            \n" +
                       "}                                                 " ;
        //@formatter:on
        List<String> logs = testUtils.captureLogs(() -> {
            script(input);
            try {
                Thread.sleep(2_050);
            } catch (InterruptedException e) {
                fail("Scheduled task was interrupted");
            }
        });

        assertThat(logs).isEqualTo(IntStream.range(0, 3)
            .mapToObj(value -> "Hello")
            .collect(Collectors.toList()));
    }

    @Test
    public void functionsPassedByInvocation() {
        assertThatExceptionOfType(FunctionException.class).isThrownBy(() -> {
            script("OnInterval(Speak(\"Hello\"), duration(1), duration());");
        });
    }

    @Test
    public void onlyNoParamFunctionCanBeScheduled() {
        //@formatter:off
        String input = "OnInterval(sayHello, duration(1), duration())   \n" +
                       "                                                \n" +
                       "func sayHello(a: int) {                         \n" +
                       "    Speak(\"Hello\")                            \n" +
                       "}                                                 " ;
        //@formatter:on
        assertThatExceptionOfType(FunctionException.class).isThrownBy(() -> {
            script(input);
        });
    }
}