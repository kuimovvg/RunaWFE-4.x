package ru.runa.gpd.ltk;

import java.util.List;

import org.eclipse.ltk.core.refactoring.Change;

import ru.runa.gpd.lang.model.Variable;

public abstract class VariableRenameProvider<T> {
    protected T element;

    public final void setElement(T element) {
        this.element = element;
    }

    public abstract List<Change> getChanges(Variable oldVariable, Variable newVariable) throws Exception;
}
