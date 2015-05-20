package ru.runa.gpd.formeditor.ftl.parameter;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;

import ru.runa.gpd.formeditor.ftl.Component;
import ru.runa.gpd.formeditor.ftl.ComponentParameter;
import ru.runa.gpd.formeditor.ftl.parameter.interfaces.IParameterChangeConsumer;
import ru.runa.gpd.formeditor.ftl.parameter.interfaces.IParameterChangeCustomer;
import ru.runa.gpd.formeditor.wysiwyg.FormEditor;
import ru.runa.gpd.lang.model.PropertyNames;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.lang.model.VariableUserType;
import ru.runa.gpd.ui.custom.LoggingSelectionAdapter;

import com.google.common.collect.Lists;

public class UserVariableFieldsComboParameter extends ComboParameter implements IParameterChangeConsumer {

    private Combo combo;
    private ComponentParameter parameter;
    private List<String> items;

    @Override
    protected List<String> getOptionLabels(ComponentParameter parameter) {
        return getOptions(null);
    }

    @Override
    protected List<String> getOptionValues(ComponentParameter parameter) {
        return getOptions(null);
    }

    private List<String> getOptions(VariableUserType type) {
        List<String> result = Lists.newArrayList();
        if (parameter == null) {
            return result;
        }
        if (type == null) {
            UserVariablesListComboParameter varListCombo = searchVarListCombo();
            if (varListCombo != null) {
                type = varListCombo.getSelectedVariableListGenericType();
            }
        }
        if (type == null) {
            return result;
        }
        List<Variable> attributes = type.getAttributes();
        for (Variable field : attributes) {
            result.add(field.getName());
        }
        return result;
    }

    private final UserVariablesListComboParameter searchVarListCombo() {
        UserVariablesListComboParameter result = null;
        try {
            for (int i = 1;; i++) {
                Component component = FormEditor.getCurrent().getComponentNotNull(i);
                List<ComponentParameter> parameters = component.getType().getParameters();
                if (!parameters.contains(parameter)) {
                    continue;
                }
                for (ComponentParameter tested : parameters) {
                    if (!(tested.getType() instanceof UserVariablesListComboParameter)) {
                        continue;
                    }
                    result = (UserVariablesListComboParameter) tested.getType();
                    break;
                }
                break;
            }
        } catch (RuntimeException e) {
        }
        return result;
    }

    @Override
    public Composite createEditor(Composite parent, ComponentParameter parameter, final Object oldValue, final PropertyChangeListener listener) {
        combo = new Combo(parent, SWT.READ_ONLY);
        combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        this.parameter = parameter;
        UserVariablesListComboParameter varListCombo = searchVarListCombo();
        if (varListCombo != null) {
            varListCombo.addParameterChangeListener(this);
        }
        if (oldValue != null && items != null) {
            for (String variableFieldName : items) {
                combo.add(variableFieldName);
            }
            combo.setText((String) oldValue);
        } else {
            rebuildComboSelection(null);
        }
        if (listener != null) {
            combo.addSelectionListener(new LoggingSelectionAdapter() {

                @Override
                protected void onSelection(SelectionEvent e) throws Exception {
                    listener.propertyChange(new PropertyChangeEvent(combo, PropertyNames.PROPERTY_VALUE, oldValue, combo.getText()));
                }
            });
        }
        return combo;
    }

    private void rebuildComboSelection(VariableUserType type) {
        items = getOptions(type);
        for (String variableFieldName : items) {
            combo.add(variableFieldName);
        }
    }

    @Override
    public void onParameterChange(IParameterChangeCustomer customer) {
        if (!(customer instanceof UserVariablesListComboParameter)) {
            return;
        }
        rebuildComboSelection(((UserVariablesListComboParameter) customer).getSelectedVariableListGenericType());
    }

}
