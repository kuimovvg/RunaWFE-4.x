package ru.runa.gpd.ui.dialog;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;

import ru.runa.gpd.Localization;
import ru.runa.gpd.extension.VariableFormatRegistry;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.Variable;

public class ChooseDateVariableDialog extends ChooseItemDialog {
    private final ProcessDefinition definition;
    private final String noneItemValue;

    public ChooseDateVariableDialog(ProcessDefinition definition, String noneItemValue) {
        super(Localization.getString("ChooseVariable.title"), Localization.getString("ChooseVariable.message"), false);
        this.definition = definition;
        this.noneItemValue = noneItemValue;
    }

    public String openDialog() {
        try {
            List<String> dateVariableNames = new ArrayList<String>();
            dateVariableNames.add(noneItemValue);
            for (Variable variable : definition.getVariables(false, Date.class.getName())) {
                dateVariableNames.add(variable.getName());
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
