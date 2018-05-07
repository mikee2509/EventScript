package com.github.mikee2509.eventscript.parser.visitor;

import com.github.mikee2509.eventscript.EventScriptParser;
import com.github.mikee2509.eventscript.EventScriptParserBaseVisitor;
import com.github.mikee2509.eventscript.domain.expression.Literal;
import com.github.mikee2509.eventscript.domain.expression.Type;
import com.github.mikee2509.eventscript.domain.scope.Declarable;
import com.github.mikee2509.eventscript.parser.exception.OperationException;
import com.github.mikee2509.eventscript.parser.exception.ScopeException;
import com.github.mikee2509.eventscript.parser.util.ScopeManager;
import lombok.AllArgsConstructor;
import org.antlr.v4.runtime.Token;

import java.time.Duration;
import java.time.LocalDateTime;

@AllArgsConstructor
public class StatementVisitor extends EventScriptParserBaseVisitor<Void> {
    private ScopeManager scope;
    private ExpressionVisitor expressionVisitor;
    private TypeVisitor typeVisitor;

    @Override
    public Void visitVariableDeclaration(EventScriptParser.VariableDeclarationContext ctx) {
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
            throw ScopeException.alreadyDefined(position, identifier);
        }
    }

    @Override
    public Void visitVariableDefinition(EventScriptParser.VariableDefinitionContext ctx) {
        Literal expression = ctx.expression().accept(expressionVisitor);
        if (expression.isVoidLiteral()) {
            throw ScopeException.cannotBeDefined(ctx.start, expression.getLiteralType());
        }
        defineVariable(ctx.start, ctx.IDENTIFIER().getText(), expression);
        return null;
    }

    @Override
    public Void visitIfStmt(EventScriptParser.IfStmtContext ctx) {
        Literal expression = ctx.expression().accept(expressionVisitor);
        if (!expression.isBoolLiteral()) {
            throw OperationException.differentTypeExpected(ctx.start, Type.BOOL);
        }
        if (((Literal<Boolean>) expression).getValue()) {
            visitBlockOrStatement(ctx.blockOrStatement(0));
        } else {
            visitBlockOrStatement(ctx.blockOrStatement(1));
        }
        return null;
    }

    @Override
    public Void visitBlockOrStatement(EventScriptParser.BlockOrStatementContext ctx) {
        scope.subscope();
        visitChildren(ctx);
        scope.abandonScope();
        return null;
    }

    @Override
    public Void visitExpressionStmt(EventScriptParser.ExpressionStmtContext ctx) {
        ctx.statementExpression.accept(expressionVisitor);
        return null;
    }
}
