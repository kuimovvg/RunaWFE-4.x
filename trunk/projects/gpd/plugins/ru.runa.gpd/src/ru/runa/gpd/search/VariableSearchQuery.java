package ru.runa.gpd.search;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

import ru.runa.gpd.lang.model.ProcessDefinition;

public class VariableSearchQuery extends BaseSearchQuery {
    private final IFile definitionFile;
    private final ProcessDefinition definition;

    public VariableSearchQuery(IFile definitionFile, ProcessDefinition definition, String variableName) {
        super(variableName, definition.getName());
        this.definitionFile = definitionFile;
        this.definition = definition;
    }

    public ProcessDefinition getProcessDefinition() {
        return definition;
    }

    public IFile getDefinitionFile() {
        return definitionFile;
    }

    @Override
    public IStatus run(final IProgressMonitor monitor) {
        getSearchResult().removeAll();
        return new VariableSearchVisitor(this).search(monitor);
    }
}
