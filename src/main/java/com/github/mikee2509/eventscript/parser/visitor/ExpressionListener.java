package com.github.mikee2509.eventscript.parser.visitor;

import com.github.mikee2509.eventscript.domain.expression.Literal;
import org.antlr.v4.runtime.ParserRuleContext;

public interface ExpressionListener {
    Literal invoke(ParserRuleContext ctx);
}
