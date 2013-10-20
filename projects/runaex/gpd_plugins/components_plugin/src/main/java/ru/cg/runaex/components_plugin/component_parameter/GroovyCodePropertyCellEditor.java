package ru.cg.runaex.components_plugin.component_parameter;

import org.eclipse.jface.viewers.DialogCellEditor;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import ru.runa.gpd.extension.handler.GroovyActionHandlerProvider;

public class GroovyCodePropertyCellEditor extends DialogCellEditor {

    public GroovyCodePropertyCellEditor(Composite parent) {
        super(parent, SWT.NONE);
    }

    @Override
    protected String openDialogBox(Control cellEditorWindow) {
        String delegationConfiguration = (String) this.getValue();
        GroovyActionHandlerProvider.ConfigurationDialog dialog = new GroovyActionHandlerProvider.ConfigurationDialog(delegationConfiguration, ru.cg.runaex.components_plugin.util.VariableUtils.getVariables());
        dialog.open();
        String result = null;
        switch (dialog.getReturnCode()) {
        case Window.OK: {
            result = dialog.getResult();
            break;
        }
        case Window.CANCEL: {
            // Do nothing
            break;
        }
        }
        return result;
    }
}