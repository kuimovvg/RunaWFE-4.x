package ru.runa.bpm.ui.dialog;

import java.util.Collections;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import ru.runa.bpm.ui.resource.Messages;

public class ChooseVariableDialog extends ChooseItemDialog {
    private final List<String> variableNames;

    public ChooseVariableDialog(List<String> variableNames) {
        super(Messages.getString("ChooseVariable.title"), Messages.getString("ChooseVariable.message"), true);
        this.variableNames = variableNames;
    }

    public String openDialog() {
        try {
            Collections.sort(variableNames);
            setItems(variableNames);
            if (open() != IDialogConstants.CANCEL_ID) {
                return (String) getSelectedItem();
            }
        } catch (Exception e) {
            // ignore this and return null;
        }
        return null;

    }

}
