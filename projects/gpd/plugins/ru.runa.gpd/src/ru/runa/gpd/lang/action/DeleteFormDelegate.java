package ru.runa.gpd.lang.action;

import org.eclipse.jface.action.IAction;

import ru.runa.gpd.Localization;
import ru.runa.gpd.lang.model.FormNode;
import ru.runa.gpd.ui.custom.Dialogs;

public class DeleteFormDelegate extends BaseModelActionDelegate {
    @Override
    public void run(IAction action) {
        if (Dialogs.confirm(Localization.getString("Form.WillBeDeleted"))) {
            FormNode formNode = getSelection();
            formNode.setFormFileName(FormNode.EMPTY);
            formNode.setValidationFileName(FormNode.EMPTY);
            formNode.setScriptFileName(FormNode.EMPTY);
        }
    }
}
