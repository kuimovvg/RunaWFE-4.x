package ru.runa.gpd.property;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.DialogCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.ui.dialog.ChooseDateVariableDialog;

public class DateVariablePropertyDescriptor extends PropertyDescriptor {
    private final String noneItemValue;
    private final GraphElement element;

    public DateVariablePropertyDescriptor(Object id, String label, GraphElement element, String noneItemValue) {
        super(id, label);
        this.element = element;
        this.noneItemValue = noneItemValue;
    }

    @Override
    public CellEditor createPropertyEditor(Composite parent) {
        return new ChooseDateVariableDialogCellEditor(parent);
    }

    private class ChooseDateVariableDialogCellEditor extends DialogCellEditor {
        public ChooseDateVariableDialogCellEditor(Composite parent) {
            super(parent, SWT.NONE);
        }

        @Override
        protected Object openDialogBox(Control cellEditorWindow) {
            ChooseDateVariableDialog dialog = new ChooseDateVariableDialog(element.getProcessDefinition(), noneItemValue);
            return dialog.openDialog();
        }
    }
}
