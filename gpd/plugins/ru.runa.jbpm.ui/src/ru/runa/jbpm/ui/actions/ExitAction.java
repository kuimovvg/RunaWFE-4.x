package ru.runa.jbpm.ui.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.ui.PlatformUI;

public class ExitAction extends BaseActionDelegate {

	public void run(IAction action) {
		PlatformUI.getWorkbench().close();
	}

}
