package ru.runa.bpm.ui.properties;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.DialogCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import ru.runa.bpm.ui.common.model.GraphElement;
import ru.runa.bpm.ui.common.model.ITimeOut;
import ru.runa.bpm.ui.dialog.DurationEditDialog;
import ru.runa.bpm.ui.resource.Messages;

public class TimeOutDurationPropertyDescriptor extends PropertyDescriptor {
    private final ITimeOut timeOut;

    public TimeOutDurationPropertyDescriptor(Object id, ITimeOut timeOut) {
        super(id, Messages.getString("timeout.property.duration"));
        this.timeOut = timeOut;
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
            DurationEditDialog dialog = new DurationEditDialog(((GraphElement) timeOut).getProcessDefinition(), timeOut.getTimeOutDuration());
            return dialog.openDialog();
        }
    }
}
