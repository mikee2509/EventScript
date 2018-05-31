package com.github.mikee2509.eventscript.parser.visitor;

import com.github.mikee2509.eventscript.EventScriptParser;
import com.github.mikee2509.eventscript.domain.exception.parser.ScopeException;
import com.github.mikee2509.eventscript.domain.expression.Function;
import com.github.mikee2509.eventscript.domain.expression.Tuple;
import com.github.mikee2509.eventscript.domain.expression.Type;
import com.github.mikee2509.eventscript.parser.ParserCreator;
import com.github.mikee2509.eventscript.parser.util.LiteralArithmetic;
import com.github.mikee2509.eventscript.parser.util.ScopeManager;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static com.github.mikee2509.eventscript.domain.expression.Type.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class ScriptVisitorTest {
    private ParserCreator parserCreator;
    private TypeVisitor typeVisitor;
    private LiteralArithmetic literalArithmetic;

    @Before
    public void setUp() throws Exception {
        this.parserCreator = new ParserCreator();
        this.typeVisitor = new TypeVisitor();
        this.literalArithmetic = new LiteralArithmetic();
    }

    private void script(String input, ScopeManager scopeManager) {
        literalArithmetic = new LiteralArithmetic();
        ExpressionVisitor expressionVisitor = new ExpressionVisitor(scopeManager, literalArithmetic);
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
}