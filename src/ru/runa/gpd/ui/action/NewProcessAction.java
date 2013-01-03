package ru.runa.gpd.ui.action;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;

import ru.runa.gpd.util.ProjectFinder;
import ru.runa.gpd.util.WorkspaceOperations;

public class NewProcessAction extends BaseActionDelegate {
    @Override
    public void run(IAction action) {
        WorkspaceOperations.createNewProcessDefinition(getStructuredSelection());
    }

    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        action.setEnabled(ProjectFinder.getAllProjects().length > 0 && !isBotStructuredSelection(selection));
    }
}
