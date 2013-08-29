package ru.runa.gpd.formeditor.ftl.bean;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.ui.views.properties.ComboBoxPropertyDescriptor;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import ru.runa.gpd.extension.VariableFormatRegistry;
import ru.runa.gpd.lang.model.Variable;

public class VariableParameter extends ComponentParameter<Integer> {
    private String variableTypeFilter;
    private List<Variable> availableValues;

    public void setVariableTypeFilter(String variableTypeFilter) {
        this.variableTypeFilter = variableTypeFilter;
    }

    @Override
    protected String convertValueToString() {
        Variable selected = availableValues.get(rawValue);

        boolean variableTypeChangedToNotSupported = variableTypeFilter != null && !VariableFormatRegistry.isApplicable(selected, variableTypeFilter);
        // boolean variableWasDeleted;
        if (variableTypeChangedToNotSupported) {
            // WYSIWYGPlugin.logWarning("", null);
            return "";
        }

        return selected.getName();
    }

    @Override
    public Integer getNullValue() {
        return -1;
    }

    @Override
    public PropertyDescriptor createPropertyDescriptor(int propertyId) {
        String typeName = param.getVariableTypeFilter();

        availableValues = new ArrayList<Variable>();
        List<String> labels = new LinkedList<String>();
        for (Variable variable : processDefinition.getVariables(true)) {
            if (typeName == null || VariableFormatRegistry.isApplicable(variable, typeName)) {
                availableValues.add(variable);
                labels.add(variable.getName());
            }
        } // TODO check variable access?

        return new ComboBoxPropertyDescriptor(propertyId, param.label, labels.toArray(new String[labels.size()]));
    }

    @Override
    protected Integer convertValueFromString(String valueStr) {
        String typeName = param.getVariableTypeFilter();
        int index = 0;

        int variableIndex = -1;
        if (valueStr.trim().isEmpty())
            return variableIndex;

        availableValues = new ArrayList<Variable>();
        for (Variable variable : processDefinition.getVariables(true)) {
            if (typeName == null || VariableFormatRegistry.isApplicable(variable, typeName)) {
                availableValues.add(variable);
                
                if (variableIndex == -1 && valueStr.equals(variable.getName())) {
                    variableIndex = index;
                }
                index++;
            }
        }
        return variableIndex;
    }
}
