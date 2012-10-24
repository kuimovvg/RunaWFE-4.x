package org.jbpm.ui.dialog;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.jbpm.ui.common.model.ProcessDefinition;
import org.jbpm.ui.common.model.Variable;
import org.jbpm.ui.resource.Messages;

public class ChooseDateVariableDialog extends ChooseItemDialog {
    private final ProcessDefinition definition;
    private final String noneItemValue;

    public ChooseDateVariableDialog(ProcessDefinition definition, String noneItemValue) {
        super(Messages.getString("ChooseVariable.title"), Messages.getString("ChooseVariable.message"), false);
        this.definition = definition;
        this.noneItemValue = noneItemValue;
    }

    public String openDialog() {
        try {
            List<String> dateVariableNames = new ArrayList<String>();
            dateVariableNames.add(noneItemValue);
            for (Variable variable : definition.getVariablesList()) {
                if ("ru.runa.wf.web.forms.format.DateTimeFormat".equals(variable.getFormat())
                        || "ru.runa.wf.web.forms.format.DateFormat".equals(variable.getFormat())) {
                    dateVariableNames.add(variable.getName());
                }
            }
            setItems(dateVariableNames);

            if (open() != IDialogConstants.CANCEL_ID) {
                return (String) getSelectedItem();
            }
        } catch (Exception e) {
            // ignore this and return null;
        }
        return null;
    }
}
