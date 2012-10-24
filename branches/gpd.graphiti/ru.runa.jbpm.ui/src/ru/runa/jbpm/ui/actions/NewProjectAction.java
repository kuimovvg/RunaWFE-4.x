package ru.runa.jbpm.ui.actions;

import org.eclipse.jface.action.IAction;
import org.jbpm.ui.util.WorkspaceOperations;

public class NewProjectAction extends BaseActionDelegate {

	public void run(IAction action) {
	    WorkspaceOperations.createNewProject();
	}

}
