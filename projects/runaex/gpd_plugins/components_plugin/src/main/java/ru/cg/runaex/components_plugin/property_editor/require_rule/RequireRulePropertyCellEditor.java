package ru.cg.runaex.components_plugin.property_editor.require_rule;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import ru.cg.runaex.components.bean.component.part.RequireRuleComponentPart;
import ru.cg.runaex.components_plugin.database_property_editor.LabelDialogCellEditor;

public class RequireRulePropertyCellEditor extends LabelDialogCellEditor {

    public RequireRulePropertyCellEditor(Composite parent) {
        super(parent);
    }

    @Override
    protected RequireRuleComponentPart openDialogBox(Control cellEditorWindow) {
        RequireRuleComponentPart value = (RequireRuleComponentPart) getValue();
        RequireRuleEditDialog dialog = new RequireRuleEditDialog(value);
        dialog.open();
        RequireRuleComponentPart requireRule = null;

        switch (dialog.getReturnCode()) {
        case Window.OK: {
            requireRule = dialog.getRequireRule();
            break;
        }
        case Window.CANCEL: {
            // Do nothing
            break;
        }
        }
        return requireRule;
    }

    @Override
    public ILabelProvider getLabelProvider() {
        return new RequireRuleLabelProvider();
    }

}
