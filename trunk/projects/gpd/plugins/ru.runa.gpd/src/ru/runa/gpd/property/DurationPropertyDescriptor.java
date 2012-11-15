package ru.runa.gpd.property;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.DialogCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import ru.runa.gpd.Localization;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.ITimed;
import ru.runa.gpd.ui.dialog.DurationEditDialog;

public class DurationPropertyDescriptor extends PropertyDescriptor {
    private final ITimed timed;

    public DurationPropertyDescriptor(Object id, ITimed timed) {
        super(id, Localization.getString("property.duration"));
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
            DurationEditDialog dialog = new DurationEditDialog(((GraphElement) timed).getProcessDefinition(), timed.getDuration());
            return dialog.openDialog();
        }
    }
}
