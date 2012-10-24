package ru.runa.jbpm.ui.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.ui.internal.dialogs.AboutDialog;

public class HelpAboutAction extends BaseActionDelegate {

	public void run(IAction action) {
		if (window != null) {
			new AboutDialog(window.getShell()).open();
        }
	}

}
