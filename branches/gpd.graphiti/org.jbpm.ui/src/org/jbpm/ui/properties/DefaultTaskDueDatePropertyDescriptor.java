package org.jbpm.ui.properties;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.DialogCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import org.jbpm.ui.common.model.ProcessDefinition;
import org.jbpm.ui.dialog.DurationEditDialog;
import org.jbpm.ui.resource.Messages;
import org.jbpm.ui.util.TimerDuration;

public class DefaultTaskDueDatePropertyDescriptor extends PropertyDescriptor {
    private final ProcessDefinition def;

    public DefaultTaskDueDatePropertyDescriptor(Object id, ProcessDefinition def) {
        super(id, Messages.getString("default.task.duedate"));
        this.def = def;
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
            DurationEditDialog dialog = new DurationEditDialog(def, def.getDefaultTaskDuration());
            TimerDuration res = (TimerDuration)dialog.openDialog();
            return res;
        }
    }
}
