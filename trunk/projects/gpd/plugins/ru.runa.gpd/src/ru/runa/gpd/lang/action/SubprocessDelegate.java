package ru.runa.gpd.lang.action;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.window.Window;

import ru.runa.gpd.lang.model.Subprocess;
import ru.runa.gpd.ui.dialog.SubprocessDialog;

public class SubprocessDelegate extends BaseActionDelegate {

    public void run(IAction action) {
        Subprocess subprocess = (Subprocess) selectedPart.getModel();
        openDetails(subprocess);
    }

    public void openDetails(Subprocess subprocess) {
        SubprocessDialog dialog = new SubprocessDialog(subprocess);
        if (dialog.open() != Window.CANCEL) {
            subprocess.setVariablesList(dialog.getSubprocessVariables());
            subprocess.setSubProcessName(dialog.getSubprocessName());
        }
    }
}
