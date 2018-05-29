package com.github.mikee2509.eventscript.parser.util;

import com.github.mikee2509.eventscript.domain.scope.Declarable;
import com.github.mikee2509.eventscript.domain.scope.Scope;

public class ScopeManager {
    private Scope scope;

    public ScopeManager() {
        this.scope = new Scope();
    }

    public void subscope() {
        scope = scope.subscope();
    }

    public void abandonScope() {
        scope = scope.getParentScope();
    }

    public boolean defineSymbol(String identifier, Declarable value) {
        return scope.defineSymbol(identifier, value);
    }

    public Declarable lookupSymbol(String identifier) {
        return scope.lookupSymbol(identifier);
    }

    public boolean updateSymbol(String identifier, Declarable value) {
        return scope.updateSymbol(identifier, value);
    }

    public boolean isRootScope() {
        return scope.getParentScope() == null;
    }

    public int numGloballyDefinedSymbols() {
        return scope.numGloballyDefinedSymbols();
    }
}
