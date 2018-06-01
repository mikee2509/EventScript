package com.github.mikee2509.eventscript.parser.visitor;

import com.github.mikee2509.eventscript.EventScriptParser;
import com.github.mikee2509.eventscript.domain.exception.parser.FunctionException;
import com.github.mikee2509.eventscript.domain.exception.parser.ScopeException;
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
import java.util.logging.Logger;

import static com.github.mikee2509.eventscript.domain.expression.Type.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class ScriptVisitorTest {
    private TestUtils testUtils;
    private ParserCreator parserCreator;
    private TypeVisitor typeVisitor;
    private LiteralArithmetic literalArithmetic;

    @Before
    public void setUp() throws Exception {
        testUtils = new TestUtils(Logger.getLogger(FunctionVisitor.class.getName()));
        parserCreator = new ParserCreator();
        typeVisitor = new TypeVisitor();
        literalArithmetic = new LiteralArithmetic();
    }

    private void script(String input, ScopeManager scopeManager) {
        literalArithmetic = new LiteralArithmetic();
        FunctionVisitor functionVisitor = new FunctionVisitor(scopeManager);
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

    //TODO test declaring function with two or more parameters of same name
    // -> maybe checking defineSymbol() result will do the job

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
            script(input2, new ScopeManager());
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
            script(input2, new ScopeManager());
        });
    }

    //TODO test functions returning tuples
    //TODO test function scope by invoking function from function
}