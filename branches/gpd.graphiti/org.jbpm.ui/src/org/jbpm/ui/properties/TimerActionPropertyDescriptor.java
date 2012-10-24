package org.jbpm.ui.properties;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.DialogCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.jbpm.ui.common.model.GraphElement;
import org.jbpm.ui.common.model.ITimed;
import org.jbpm.ui.dialog.TimerActionEditDialog;

public class TimerActionPropertyDescriptor extends PropertyDescriptor {
    private final ITimed element;

    public TimerActionPropertyDescriptor(Object id, String displayName, ITimed element) {
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
            TimerActionEditDialog dialog = new TimerActionEditDialog(((GraphElement) element).getProcessDefinition(), element.getTimerAction());
            return dialog.openDialog();
        }
    }
}
