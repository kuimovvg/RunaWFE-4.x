package ru.runa.bpm.ui.properties;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.DialogCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import ru.runa.bpm.ui.common.model.GraphElement;
import ru.runa.bpm.ui.dialog.EscalationActionEditDialog;
import ru.runa.bpm.ui.jpdl3.model.TaskState;

public class EscalationActionPropertyDescriptor extends PropertyDescriptor {
    private final TaskState element;

    public EscalationActionPropertyDescriptor(Object id, String displayName, TaskState element) {
        super(id, displayName);
        this.element = element;
    }

    @Override
    public CellEditor createPropertyEditor(Composite parent) {
        return new TimerActionDialogCellEditor(parent);
    }

    private class TimerActionDialogCellEditor extends DialogCellEditor {

        public TimerActionDialogCellEditor(Composite parent) {
            super(parent, SWT.NONE);
        }

        @Override
        protected Object openDialogBox(Control cellEditorWindow) {
            EscalationActionEditDialog dialog = new EscalationActionEditDialog(((GraphElement) element).getProcessDefinition(), element.getEscalationAction());
            return dialog.openDialog();
        }
    }
}
