package ru.cg.runaex.components_plugin.database_property_editor;

import org.eclipse.jface.viewers.DialogCellEditor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public abstract class LabelDialogCellEditor extends DialogCellEditor {
    
    protected LabelDialogCellEditor(Composite parent) {
        super(parent, SWT.NONE);
    }

    @Override
    protected Object openDialogBox(Control arg0) {
        return null;
    }

    protected void updateContents(Object value) {
        if (getDefaultLabel() == null) {
            return;
        }

        String text = "";
        if (value != null) {
            if (getLabelProvider() != null) {
                text = getLabelProvider().getText(value);
                getDefaultLabel().setText(text);
            } else {
                super.updateContents(value);
            }
        }
    }

    public abstract ILabelProvider getLabelProvider();

}
