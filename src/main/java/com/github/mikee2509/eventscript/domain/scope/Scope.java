package com.github.mikee2509.eventscript.domain.scope;

import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@NoArgsConstructor
public class Scope {
    private Map<String, Declarable> symbolTable = new HashMap<>();
    private Scope parentScope;

    private Scope(Scope parentScope) {
        this.parentScope = parentScope;
    }

    public Scope subscope() {
        return new Scope(this);
    }

    public Scope getParentScope() {
        return parentScope;
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
            Declarable value = currentScope.symbolTable.get(identifier);
            if (value != null) return value;
        }
        return null;
    }

    public boolean updateSymbol(String identifier, Declarable value) {
        for (Scope currentScope = this; currentScope != null; currentScope = currentScope.parentScope) {
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
