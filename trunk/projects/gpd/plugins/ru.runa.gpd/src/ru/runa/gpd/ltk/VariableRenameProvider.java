package ru.runa.gpd.ltk;

import java.util.List;

import org.eclipse.ltk.core.refactoring.Change;

import ru.runa.gpd.lang.model.GraphElement;

public interface VariableRenameProvider<T extends GraphElement> {

    void setElement(T element);

    List<Change> getChanges(String variableName, String replacement) throws Exception;

}
