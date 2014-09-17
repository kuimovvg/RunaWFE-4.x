package ru.runa.gpd.ui.dialog;

import java.util.Collections;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.LabelProvider;

import ru.runa.gpd.Localization;
import ru.runa.gpd.lang.model.Variable;

import com.google.common.collect.Lists;

public class ChooseVariableDialog extends ChooseItemDialog {
    private final List<Variable> variables = Lists.newArrayList();

    public ChooseVariableDialog(List<Variable> variables) {
        super(Localization.getString("ChooseVariable.title"), Localization.getString("ChooseVariable.message"), true);
        this.variables.addAll(variables);
    }

    public Variable openDialog() {
        try {
            setLabelProvider(new LabelProvider() {
                @Override
                public String getText(Object element) {
                    return ((Variable) element).getName();
                }
            });
            Collections.sort(variables);
            setItems(variables);
            if (open() != IDialogConstants.CANCEL_ID) {
                return (Variable) getSelectedItem();
            }
        } catch (Exception e) {
            // ignore this and return null;
        }
        return null;

    }

}
