package ru.cg.runaex.components_plugin.database_property_editor;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import ru.cg.runaex.components.bean.component.part.DefaultValue;
import ru.cg.runaex.components_plugin.property_editor.require_rule.DefaultValueLabelProvider;

public class DefaultValuePropertyCellEditor extends LabelDialogCellEditor {

    public DefaultValuePropertyCellEditor(Composite parent) {
        super(parent);
    }

    @Override
    protected DefaultValue openDialogBox(Control cellEditorWindow) {
        SelectDefaultValueDialog dialog = new SelectDefaultValueDialog((DefaultValue) getValue());
        dialog.create();
        dialog.open();

        DefaultValue ref = null;

        switch (dialog.getReturnCode()) {
        case Window.OK: {
            ref = dialog.getDefaultValue();
            break;
        }
        case Window.CANCEL: {
            // Do nothing
            break;
        }
        }
        return ref;
    }

    @Override
    public ILabelProvider getLabelProvider() {
        return new DefaultValueLabelProvider();
    }

}
