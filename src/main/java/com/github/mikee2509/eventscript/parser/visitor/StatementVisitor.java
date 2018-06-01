package com.github.mikee2509.eventscript.parser.visitor;

import com.github.mikee2509.eventscript.EventScriptParser;
import com.github.mikee2509.eventscript.EventScriptParserBaseVisitor;
import com.github.mikee2509.eventscript.domain.exception.FunctionException;
import com.github.mikee2509.eventscript.domain.exception.OperationException;
import com.github.mikee2509.eventscript.domain.exception.ScopeException;
import com.github.mikee2509.eventscript.domain.exception.control.BreakException;
import com.github.mikee2509.eventscript.domain.exception.control.ContinueException;
import com.github.mikee2509.eventscript.domain.exception.control.ControlFlowException;
import com.github.mikee2509.eventscript.domain.exception.control.ReturnException;
import com.github.mikee2509.eventscript.domain.expression.Literal;
import com.github.mikee2509.eventscript.domain.expression.Returnable;
import com.github.mikee2509.eventscript.domain.expression.Tuple;
import com.github.mikee2509.eventscript.domain.expression.Type;
import com.github.mikee2509.eventscript.domain.scope.Declarable;
import com.github.mikee2509.eventscript.parser.util.ScopeManager;
import org.antlr.v4.runtime.Token;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.github.mikee2509.eventscript.domain.expression.Type.BOOL;
import static com.github.mikee2509.eventscript.domain.expression.Type.VOID;

public class StatementVisitor extends EventScriptParserBaseVisitor<Void> {
    private ScopeManager scope;
    private ExpressionVisitor expressionVisitor;
    private TypeVisitor typeVisitor;

    public StatementVisitor(ScopeManager scope, ExpressionVisitor expressionVisitor, TypeVisitor typeVisitor) {
        this.scope = scope;
        this.expressionVisitor = expressionVisitor;
        this.typeVisitor = typeVisitor;
        expressionVisitor.addFunctionCallListener(functionContext -> functionContext.block().accept(this));
    }

    @Override
    public Void visitVariableDeclaration(EventScriptParser.VariableDeclarationContext ctx) {
        Type type = ctx.type().accept(typeVisitor);
        switch (type) {
            case BOOL:
                defineVariable(ctx.start, ctx.IDENTIFIER().getText(), new Literal<>(false));
                break;
            case DATETIME:
                defineVariable(ctx.start, ctx.IDENTIFIER().getText(), new Literal<>(LocalDateTime.now()));
                break;
            case DURATION:
                defineVariable(ctx.start, ctx.IDENTIFIER().getText(), new Literal<>(Duration.ZERO));
                break;
            case FLOAT:
                defineVariable(ctx.start, ctx.IDENTIFIER().getText(), new Literal<>(0.0f));
                break;
            case INT:
                defineVariable(ctx.start, ctx.IDENTIFIER().getText(), new Literal<>(0));
                break;
            case STRING:
                defineVariable(ctx.start, ctx.IDENTIFIER().getText(), new Literal<>(""));
                break;
            case FUNC:
            case VOID:
            default:
                throw ScopeException.cannotBeDefined(ctx.start, type);
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
            throw OperationException.differentTypeExpected(ctx.start, BOOL);
        }
        if (((Literal<Boolean>) expression).getValue()) {
            ctx.blockOrStatement(0).accept(this);
        } else if (ctx.ELSE() != null) {
            ctx.blockOrStatement(1).accept(this);
        }
        return null;
    }

    @Override
    public Void visitBlockOrStatement(EventScriptParser.BlockOrStatementContext ctx) {
        scope.subscope();
        try {
            visitChildren(ctx);
        } finally {
            scope.abandonScope();
        }
        return null;
    }

    @Override
    public Void visitExpressionStmt(EventScriptParser.ExpressionStmtContext ctx) {
        ctx.statementExpression.accept(expressionVisitor);
        return null;
    }

    @Override
    public Void visitForStmt(EventScriptParser.ForStmtContext ctx) {
        scope.loopSubscope();
        ctx.forInit().accept(this);
        try {
            for (Literal expression = ctx.expression().accept(expressionVisitor);
                 expression.isBoolLiteral() ? (Boolean) expression.getValue() : false;
                 ctx.forUpdate.accept(expressionVisitor), expression = ctx.expression().accept(expressionVisitor)) {
                try {
                    ctx.blockOrStatement().accept(this);
                } catch (ContinueException ignored) {
                }
            }
        } catch (BreakException ignored) {
        } finally {
            scope.abandonScope();
        }
        return null;
    }

    @Override
    public Void visitForInit(EventScriptParser.ForInitContext ctx) {
        visitChildren(ctx);
        return null;
    }

    @Override
    public Void visitExpressionList(EventScriptParser.ExpressionListContext ctx) {
        ctx.accept(expressionVisitor);
        return null;
    }

    @Override
    public Void visitBreakStmt(EventScriptParser.BreakStmtContext ctx) {
        if (scope.isLoopScope()) {
            throw new BreakException(ctx.start);
        } else {
            throw ControlFlowException.breakWrongContext(ctx.start);
        }
    }

    @Override
    public Void visitContinueStmt(EventScriptParser.ContinueStmtContext ctx) {
        if (scope.isLoopScope()) {
            throw new ContinueException(ctx.start);
        } else {
            throw ControlFlowException.continueWrongContext(ctx.start);
        }
    }

    @Override
    public Void visitReturnStmt(EventScriptParser.ReturnStmtContext ctx) {
        if (!scope.isFunctionScope()) {
            throw ControlFlowException.returnWrongContext(ctx.start);
        }
        List<Literal> returnValues = null;
        if (ctx.expressionList() != null) {
            returnValues = ctx.expressionList().expression().stream()
                .map(expCtx -> expCtx.accept(expressionVisitor))
                .collect(Collectors.toList());
        }

        Returnable returnType = scope.getFunction().getReturnType();
        if (returnType instanceof Type) {
            Type requiredType = (Type) returnType;

            if (requiredType == VOID) {
                if (returnValues == null) {
                    throw new ReturnException(ctx.start, null);
                } else {
                    throw FunctionException.returnTypeException(ctx.start, requiredType);
                }
            }

            if (returnValues == null || returnValues.size() != 1 ||
                requiredType != returnValues.get(0).getLiteralType()) {
                throw FunctionException.returnTypeException(ctx.start, requiredType);
            }

            Tuple tuple = Tuple.creator().add(returnValues.get(0)).create();
            throw new ReturnException(ctx.start, tuple);
        } else {
            Tuple requiredTuple = (Tuple) returnType;
            if (returnValues == null) {
                throw FunctionException.returnTypeException(ctx.start, requiredTuple.types());
            }

            Type[] valueTypes = returnValues.stream().map(Literal::getLiteralType).toArray(Type[]::new);
            if (Arrays.equals(requiredTuple.types(), valueTypes)) {
                throw new ReturnException(ctx.start, Tuple.fromLiteralList(returnValues));
            } else {
                throw FunctionException.returnTypeException(ctx.start, requiredTuple.types());
            }
        }
    }
}
