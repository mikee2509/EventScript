package com.github.mikee2509.eventscript.parser.visitor;

import com.github.mikee2509.eventscript.EventScriptParser;
import com.github.mikee2509.eventscript.EventScriptParserBaseVisitor;
import com.github.mikee2509.eventscript.domain.Statement;
import com.github.mikee2509.eventscript.domain.expression.Literal;
import com.github.mikee2509.eventscript.domain.expression.Type;
import com.github.mikee2509.eventscript.domain.scope.Declarable;
import com.github.mikee2509.eventscript.domain.scope.Scope;
import com.github.mikee2509.eventscript.parser.exception.ScopeException;
import lombok.AllArgsConstructor;
import org.antlr.v4.runtime.Token;

import java.time.Duration;
import java.time.LocalDateTime;

@AllArgsConstructor
public class StatementVisitor extends EventScriptParserBaseVisitor<Statement> {
    private Scope scope;
    private ExpressionVisitor expressionVisitor;
    private TypeVisitor typeVisitor;

    @Override
    public Statement visitVariableDeclaration(EventScriptParser.VariableDeclarationContext ctx) {
        Type type = ctx.type().accept(typeVisitor);
        switch (type) {
            case BOOL:
                defineVariable(ctx.start, ctx.IDENTIFIER().getText(), new Literal<>(false));
                break;
            case STRING:
                defineVariable(ctx.start, ctx.IDENTIFIER().getText(), new Literal<>(""));
                break;
            case INT:
                defineVariable(ctx.start, ctx.IDENTIFIER().getText(), new Literal<>(0));
                break;
            case FLOAT:
                defineVariable(ctx.start, ctx.IDENTIFIER().getText(), new Literal<>(0.0f));
                break;
            case FUNC:
                throw ScopeException.cannotBeDefined(ctx.start, type);
            case VOID:
                throw ScopeException.cannotBeDefined(ctx.start, type);
            case DATETIME:
                defineVariable(ctx.start, ctx.IDENTIFIER().getText(), new Literal<>(LocalDateTime.now()));
                break;
            case DURATION:
                defineVariable(ctx.start, ctx.IDENTIFIER().getText(), new Literal<>(Duration.ZERO));
                break;
        }
        return null;
    }

    private void defineVariable(Token position, String identifier, Declarable value) {
        if (!scope.defineSymbol(identifier, value)) {
            throw ScopeException.alreadyDefined(position);
        }
    }

    @Override
    public Statement visitVariableDefinition(EventScriptParser.VariableDefinitionContext ctx) {
        Literal expression = ctx.expression().accept(expressionVisitor);
        if (expression.isVoidLiteral()) {
            throw ScopeException.cannotBeDefined(ctx.start, expression.getLiteralType());
        }
        defineVariable(ctx.start, ctx.IDENTIFIER().getText(), expression);
        return null;
    }
}
