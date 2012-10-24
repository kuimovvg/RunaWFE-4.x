package ru.runa.jbpm.ui.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.jbpm.ui.DesignerLogger;

public abstract class OpenViewBaseAction extends BaseActionDelegate {

    protected abstract String getViewId();

    public void run(IAction action) {
        try {
            PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(getViewId(), null, IWorkbenchPage.VIEW_VISIBLE);
        } catch (PartInitException e) {
            DesignerLogger.logError(e);
        }
    }

}
