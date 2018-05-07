package com.github.mikee2509.eventscript.parser.visitor;

import com.github.mikee2509.eventscript.domain.scope.Scope;
import com.github.mikee2509.eventscript.parser.util.LiteralArithmetic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VisitorConfiguration {
    private Scope globalScope = new Scope();

    @Bean
    StatementVisitor statementVisitor(ExpressionVisitor expressionVisitor, TypeVisitor typeVisitor) {
        return new StatementVisitor(globalScope, expressionVisitor, typeVisitor);
    }

    @Bean
    ScriptVisitor scriptVisitor(StatementVisitor statementVisitor, FunctionVisitor functionVisitor) {
        return new ScriptVisitor(globalScope, statementVisitor, functionVisitor);
    }

    @Bean
    ExpressionVisitor expressionVisitor(LiteralArithmetic literalArithmetic) {
        return new ExpressionVisitor(globalScope, literalArithmetic);
    }
}
