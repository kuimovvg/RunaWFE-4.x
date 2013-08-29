package ru.runa.gpd.search;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.Variable;

public class VariableSearchQuery extends BaseSearchQuery {
    private final IFile definitionFile;
    private final ProcessDefinition definition;
    private final Variable variable;

    public VariableSearchQuery(IFile definitionFile, ProcessDefinition definition, Variable variable) {
        super(variable.getName(), definition.getName());
        this.definitionFile = definitionFile;
        this.definition = definition;
        this.variable = variable;
    }

    public ProcessDefinition getProcessDefinition() {
        return definition;
    }

    public IFile getDefinitionFile() {
        return definitionFile;
    }

    public Variable getVariable() {
        return variable;
    }

    @Override
    public IStatus run(final IProgressMonitor monitor) {
        getSearchResult().removeAll();
        return new VariableSearchVisitor(this).search(monitor);
    }
}
