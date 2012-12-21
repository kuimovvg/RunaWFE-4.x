package ru.runa.gpd.property;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.DialogCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import ru.runa.gpd.Localization;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.ui.dialog.DurationEditDialog;

public class DefaultTaskDueDatePropertyDescriptor extends PropertyDescriptor {
    private final ProcessDefinition definition;

    public DefaultTaskDueDatePropertyDescriptor(Object id, ProcessDefinition definition) {
        super(id, Localization.getString("default.task.duedate"));
        this.definition = definition;
    }

    @Override
    public CellEditor createPropertyEditor(Composite parent) {
        return new TimeOutDurationDialogCellEditor(parent);
    }

    private class TimeOutDurationDialogCellEditor extends DialogCellEditor {
        public TimeOutDurationDialogCellEditor(Composite parent) {
            super(parent, SWT.NONE);
        }

        @Override
        protected Object openDialogBox(Control cellEditorWindow) {
            DurationEditDialog dialog = new DurationEditDialog(definition, definition.getDefaultTaskTimeoutDelay());
            return dialog.openDialog();
        }
    }
}
