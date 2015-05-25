package ru.runa.gpd.formeditor.ftl.parameter;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;

import ru.runa.gpd.formeditor.ftl.ComponentParameter;
import ru.runa.gpd.formeditor.ftl.parameter.interfaces.IParameterChangeConsumer;
import ru.runa.gpd.formeditor.ftl.parameter.interfaces.IParameterChangeCustomer;
import ru.runa.gpd.formeditor.wysiwyg.FormEditor;
import ru.runa.gpd.lang.model.PropertyNames;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.lang.model.VariableUserType;
import ru.runa.gpd.ui.custom.LoggingSelectionAdapter;
import ru.runa.gpd.util.VariableUtils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class UserVariablesListComboParameter extends ComboParameter implements IParameterChangeCustomer {

    private List<IParameterChangeConsumer> consumers;
    private final Map<String, VariableUserType> variables = Maps.newHashMap();
    private Combo combo;

    @Override
    protected List<String> getOptionLabels(ComponentParameter parameter) {
        return getOptions(parameter);
    }

    @Override
    protected List<String> getOptionValues(ComponentParameter parameter) {
        return getOptions(parameter);
    }

    private List<String> getOptions(ComponentParameter parameter) {
        Map<String, Variable> vars = FormEditor.getCurrent().getVariables(parameter.getVariableTypeFilter());
        List<VariableUserType> userTypes = FormEditor.getCurrent().getUserVariablesTypes();
        for (Map.Entry<String, Variable> var : vars.entrySet()) {
            String typeName = VariableUtils.getListVariableComponent(var.getValue());
            if (typeName == null) {
                continue;
            }
            for (VariableUserType type : userTypes) {
                if (!type.getName().equals(typeName)) {
                    continue;
                }
                variables.put(var.getKey(), type);
                break;
            }
        }
        return Lists.newArrayList(variables.keySet());
    }

    public final VariableUserType getSelectedVariableListGenericType() {
        if (combo == null || combo.isDisposed() || combo.getText() == null || combo.getText().isEmpty()) {
            return null;
        }
        if (variables == null) {
            return null;
        }
        return variables.get(combo.getText());
    }

    @Override
    public Composite createEditor(Composite parent, ComponentParameter parameter, final Object oldValue, final PropertyChangeListener listener) {
        consumers = Lists.newArrayList();
        combo = new Combo(parent, SWT.READ_ONLY);
        combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        for (String variableName : getOptions(parameter)) {
            combo.add(variableName);
        }
        if (oldValue != null) {
            combo.setText((String) oldValue);
        }
        if (listener != null) {
            combo.addSelectionListener(new LoggingSelectionAdapter() {

                @Override
                protected void onSelection(SelectionEvent e) throws Exception {
                    listener.propertyChange(new PropertyChangeEvent(combo, PropertyNames.PROPERTY_VALUE, oldValue, combo.getText()));
                    if (consumers == null) {
                        return;
                    }
                    for (IParameterChangeConsumer consumer : consumers) {
                        consumer.onParameterChange(UserVariablesListComboParameter.this);
                    }
                }
            });
        }
        return combo;
    }

    @Override
    public void addParameterChangeListener(IParameterChangeConsumer consumer) {
        if (consumers != null) {
            consumers.add(consumer);
        }
    }

    @Override
    public void removeParameterChangeListener(IParameterChangeConsumer consumer) {
        if (consumers != null) {
            consumers.remove(consumer);
        }
    }

}
