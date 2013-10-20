package ru.cg.runaex.components_plugin.property_editor.require_rule;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import ru.cg.runaex.components.bean.component.part.GroovyRuleComponentPart;
import ru.cg.runaex.components_plugin.database_property_editor.LabelDialogCellEditor;
import ru.cg.runaex.components_plugin.util.VariableUtils;
import ru.runa.gpd.extension.handler.GroovyActionHandlerProvider;

public class GroovyRulePropertyCellEditor extends LabelDialogCellEditor {

    public GroovyRulePropertyCellEditor(Composite parent) {
        super(parent);
    }

    @Override
    protected GroovyRuleComponentPart openDialogBox(Control cellEditorWindow) {
        GroovyRuleComponentPart groovyRule = (GroovyRuleComponentPart) getValue();
        GroovyActionHandlerProvider.ConfigurationDialog dialog = new GroovyActionHandlerProvider.ConfigurationDialog(groovyRule.getGroovyScript(), VariableUtils.getVariables());
        dialog.open();
        GroovyRuleComponentPart newGroovyRule = null;
        switch (dialog.getReturnCode()) {
        case Window.OK: {
            newGroovyRule = new GroovyRuleComponentPart(dialog.getResult());
            break;
        }
        case Window.CANCEL: {
            // Do nothing
            break;
        }
        }
        return newGroovyRule;
    }

    @Override
    public ILabelProvider getLabelProvider() {
        return new GroovyRuleLabelProvider();
    }

}
