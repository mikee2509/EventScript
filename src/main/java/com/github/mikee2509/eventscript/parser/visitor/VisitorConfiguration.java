package com.github.mikee2509.eventscript.parser.visitor;

import com.github.mikee2509.eventscript.parser.util.LiteralArithmetic;
import com.github.mikee2509.eventscript.parser.util.ScopeManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class VisitorConfiguration {

    @Bean
    @Scope("prototype")
    public ScriptVisitor scriptVisitor(TypeVisitor typeVisitor) {
        ScopeManager scopeManager = new ScopeManager();
        ExpressionVisitor expressionVisitor = new ExpressionVisitor(scopeManager, new LiteralArithmetic());
        StatementVisitor statementVisitor = new StatementVisitor(scopeManager, expressionVisitor, typeVisitor);
        return new ScriptVisitor(scopeManager, statementVisitor, typeVisitor);
    }
}
