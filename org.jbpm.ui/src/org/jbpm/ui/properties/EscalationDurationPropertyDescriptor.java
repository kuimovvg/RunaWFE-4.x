package org.jbpm.ui.properties;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.DialogCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.jbpm.ui.common.model.GraphElement;
import org.jbpm.ui.dialog.DurationEditDialog;
import org.jbpm.ui.jpdl3.model.TaskState;
import org.jbpm.ui.resource.Messages;

public class EscalationDurationPropertyDescriptor extends PropertyDescriptor {
    private final TaskState timed;

    public EscalationDurationPropertyDescriptor(Object id, TaskState timed) {
        super(id, Messages.getString("escalation.duration"));
        this.timed = timed;
    }
    
    @Override
    public CellEditor createPropertyEditor(Composite parent) {
        return new DurationDialogCellEditor(parent);
    }

    private class DurationDialogCellEditor extends DialogCellEditor {

        public DurationDialogCellEditor(Composite parent) {
            super(parent, SWT.NONE);
        }

        @Override
        protected Object openDialogBox(Control cellEditorWindow) {
            DurationEditDialog dialog = new DurationEditDialog(((GraphElement) timed).getProcessDefinition(), timed.getEscalationTime());
            return dialog.openDialog();
        }
    }
}
