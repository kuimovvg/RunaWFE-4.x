package ru.runa.gpd.ui.dialog;

import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.LabelProvider;

import ru.runa.gpd.Localization;
import ru.runa.gpd.lang.model.VariableUserType;

public class ChooseUserTypeDialog extends ChooseItemDialog {
    private final List<VariableUserType> userTypes;

    public ChooseUserTypeDialog(List<VariableUserType> userTypes) {
        super(Localization.getString("ChooseUserType.title"), Localization.getString("ChooseUserType.message"), true);
        this.userTypes = userTypes;
    }

    public VariableUserType openDialog() {
        try {
            setLabelProvider(new LabelProvider() {
                @Override
                public String getText(Object element) {
                    return ((VariableUserType) element).getName();
                }
            });
            // Collections.sort(userTypes);
            setItems(userTypes);
            if (open() != IDialogConstants.CANCEL_ID) {
                return (VariableUserType) getSelectedItem();
            }
        } catch (Exception e) {
            // ignore this and return null;
        }
        return null;

    }

}
