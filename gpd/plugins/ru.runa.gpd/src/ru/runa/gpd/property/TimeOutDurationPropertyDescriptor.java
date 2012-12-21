package ru.runa.gpd.property;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.DialogCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import ru.runa.gpd.Localization;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.ITimeOut;
import ru.runa.gpd.ui.dialog.DurationEditDialog;

public class TimeOutDurationPropertyDescriptor extends PropertyDescriptor {
    private final ITimeOut timeOut;

    public TimeOutDurationPropertyDescriptor(Object id, ITimeOut timeOut) {
        super(id, Localization.getString("timeout.property.duration"));
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
            DurationEditDialog dialog = new DurationEditDialog(((GraphElement) timeOut).getProcessDefinition(), timeOut.getTimeOutDelay());
            return dialog.openDialog();
        }
    }
}
