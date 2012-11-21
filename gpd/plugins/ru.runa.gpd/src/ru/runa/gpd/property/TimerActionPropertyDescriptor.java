package ru.runa.gpd.property;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.DialogCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.ITimed;
import ru.runa.gpd.ui.dialog.TimerActionEditDialog;

public class TimerActionPropertyDescriptor extends PropertyDescriptor {
    private final ITimed element;

    public TimerActionPropertyDescriptor(Object id, String label, ITimed element) {
        super(id, label);
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
