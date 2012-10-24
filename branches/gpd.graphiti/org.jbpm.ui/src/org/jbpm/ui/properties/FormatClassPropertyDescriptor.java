package org.jbpm.ui.properties;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.DialogCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.jbpm.ui.common.model.Variable;
import org.jbpm.ui.dialog.CreateVariableDialog;

public class FormatClassPropertyDescriptor extends PropertyDescriptor {

    private final Variable variable;

    public FormatClassPropertyDescriptor(Object id, String displayName, Variable variable) {
        super(id, displayName);
        this.variable = variable;
        setLabelProvider(new MappingLabelProvider());
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
            CreateVariableDialog dialog = new CreateVariableDialog(variable.getProcessDefinition(), variable);
            if (dialog.open() == IDialogConstants.OK_ID) {
                return dialog.getType();
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
