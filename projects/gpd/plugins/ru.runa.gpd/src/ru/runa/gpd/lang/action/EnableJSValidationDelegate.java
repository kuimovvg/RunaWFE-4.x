package ru.runa.gpd.lang.action;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;

import ru.runa.gpd.editor.gef.command.EnableJSValidationCommand;
import ru.runa.gpd.editor.gef.part.graph.FormNodeEditPart;
import ru.runa.gpd.lang.model.FormNode;

public class EnableJSValidationDelegate extends BaseActionDelegate {

    public void run(IAction action) {
        FormNode formNode = ((FormNodeEditPart) selectedPart).getModel();
        setJSValidation(formNode, action.isChecked());
    }

    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        super.selectionChanged(action, selection);
        if (selectedPart == null)
            return;
        FormNode formNode = ((FormNodeEditPart) selectedPart).getModel();
        action.setEnabled(formNode.hasFormValidation());
        action.setChecked(formNode.isUseJSValidation());
    }

    private void setJSValidation(FormNode formNode, boolean enabled) {
        EnableJSValidationCommand command = new EnableJSValidationCommand();
        command.setFormNode(formNode);
        command.setEnabled(enabled);
        executeCommand(command);
    }

}
