package com.github.mikee2509.eventscript.domain.scope;

import com.github.mikee2509.eventscript.domain.expression.Function;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@NoArgsConstructor
public class Scope {
    private Map<String, Declarable> symbolTable = new HashMap<>();
    private Scope parentScope;
    private Function function;
    private boolean isLoopScope;

    private Scope(Scope parentScope, Function function, boolean isLoopScope) {
        this.parentScope = parentScope;
        this.function = function;
        this.isLoopScope = isLoopScope;
    }

    public Scope subscope() {
        return new Scope(this, this.function, false);
    }

    public Scope loopSubscope() {
        return new Scope(this, this.function, true);
    }

    public Scope functionSubscope(Function function) {
        return new Scope(this, function, false);
    }

    public Scope getParentScope() {
        return parentScope;
    }

    public Function getFunction() {
        return function;
    }

    public boolean isLoopScope() {
        for (Scope currentScope = this; currentScope != null; currentScope = currentScope.parentScope) {
            if (currentScope.function != null) return false;
            if (currentScope.isLoopScope) return true;
        }
        return false;
    }

    public boolean isFunctionScope() {
        for (Scope currentScope = this; currentScope != null; currentScope = currentScope.parentScope) {
            if (currentScope.function != null) return true;
        }
        return false;
    }

    public boolean defineSymbol(String identifier, Declarable value) {
        if (symbolTable.containsKey(identifier)) {
            return false;
        }
        symbolTable.put(identifier, value);
        return true;
    }

    public Declarable lookupSymbol(String identifier) {
        for (Scope currentScope = this; currentScope != null; currentScope = currentScope.parentScope) {
            if (currentScope != this && currentScope.function != null) continue;
            Declarable value = currentScope.symbolTable.get(identifier);
            if (value != null) return value;
        }
        return null;
    }

    public boolean updateSymbol(String identifier, Declarable value) {
        for (Scope currentScope = this; currentScope != null; currentScope = currentScope.parentScope) {
            if (currentScope != this && currentScope.function != null) continue;
            if (currentScope.symbolTable.containsKey(identifier)) {
                currentScope.symbolTable.put(identifier, value);
                return true;
            }
        }
        return false;
    }

    public int numGloballyDefinedSymbols() {
        int sum = 0;
        for (Scope currentScope = this; currentScope != null; currentScope = currentScope.parentScope) {
            sum += currentScope.symbolTable.size();
        }
        return sum;
    }
}
