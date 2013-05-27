package ru.runa.gpd.property;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.DialogCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.ui.dialog.UpdateVariableDialog;

public class FormatClassPropertyDescriptor extends PropertyDescriptor {
    private final Variable variable;

    public FormatClassPropertyDescriptor(Object id, String label, Variable variable) {
        super(id, label);
        this.variable = variable;
        setLabelProvider(new LocalizationLabelProvider());
    }

    @Override
    public CellEditor createPropertyEditor(Composite parent) {
        return new ChooseClassDialogCellEditor(parent);
    }

    private class ChooseClassDialogCellEditor extends DialogCellEditor {
        public ChooseClassDialogCellEditor(Composite parent) {
            super(parent, SWT.NONE);
        }

        @Override
        protected Object openDialogBox(Control cellEditorWindow) {
            UpdateVariableDialog dialog = new UpdateVariableDialog(variable.getProcessDefinition(), variable);
            if (dialog.open() == IDialogConstants.OK_ID) {
                return dialog.getTypeName();
            }
            return null;
        }

        @Override
        protected void updateContents(Object value) {
            if (getDefaultLabel() != null) {
                getDefaultLabel().setText(getLabelProvider().getText(value));
            }
        }
    }
}
