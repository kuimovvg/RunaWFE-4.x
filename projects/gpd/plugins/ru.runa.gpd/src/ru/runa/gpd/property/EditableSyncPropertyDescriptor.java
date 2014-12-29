package ru.runa.gpd.property;

import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.DialogCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import ru.runa.gpd.ui.dialog.EditableSyncDialog;
import ru.runa.gpd.ui.dialog.MultipleMapListDialog;

public class EditableSyncPropertyDescriptor extends PropertyDescriptor {

    private final String variableTypeFilter;

    private final List<String> values;

    private final Map<String, Boolean> mapValues;

    private String rawValue;

    public EditableSyncPropertyDescriptor(Object id, String displayName, String variableTypeFilter, String rawValue, List<String> values,
            Map<String, Boolean> mapValues) {
        super(id, displayName);
        this.variableTypeFilter = variableTypeFilter;
        this.values = values;
        this.mapValues = mapValues;
        this.rawValue = rawValue;
    }

    @Override
    public CellEditor createPropertyEditor(Composite parent) {
        return new EditableSyncCellEditor(parent);
    }

    public String getRawValue() {
        return rawValue;
    }

    public void setRawValue(String rawValue) {
        this.rawValue = rawValue;
    }

    public List<String> getValues() {
        return values;
    }

    public Map<String, Boolean> getMapValues() {
        return mapValues;
    }

    private class EditableSyncCellEditor extends DialogCellEditor {

        public EditableSyncCellEditor(Composite parent) {
            super(parent, SWT.NONE);
        }

        @Override
        protected Object openDialogBox(Control cellEditorWindow) {
            if (values != null) {
                EditableSyncDialog dialog = new EditableSyncDialog(variableTypeFilter, values, rawValue);
                Object result = dialog.openDialog();
                EditableSyncPropertyDescriptor.this.setRawValue((String) result);
                return result;
            } else {
                MultipleMapListDialog dialog = new MultipleMapListDialog(variableTypeFilter, mapValues);
                return dialog.openDialog();
            }
        }
    }

}
