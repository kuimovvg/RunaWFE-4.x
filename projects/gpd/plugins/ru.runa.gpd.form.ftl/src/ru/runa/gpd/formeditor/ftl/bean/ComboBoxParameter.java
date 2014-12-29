package ru.runa.gpd.formeditor.ftl.bean;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ui.views.properties.ComboBoxPropertyDescriptor;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import ru.runa.gpd.extension.VariableFormatRegistry;
import ru.runa.gpd.formeditor.ftl.MethodTag.OptionalValue;
import ru.runa.gpd.lang.model.Variable;

import com.google.common.collect.Lists;

public class ComboBoxParameter extends ComponentParameter<Integer> {

    private List<Variable> availableValues;

    @Override
    protected String convertValueToString() {
        if (isContainer()) {
            Variable selected = availableValues.get(rawValue);
            String variableTypeFilter = param.optionalValues.get(0).filterType;

            boolean variableTypeChangedToNotSupported = variableTypeFilter != null
                    && !(VariableFormatRegistry.isApplicable(selected, variableTypeFilter) || VariableFormatRegistry.isAssignableFrom(
                            selected.getJavaClassName(), variableTypeFilter));
            if (variableTypeChangedToNotSupported) {
                return "";
            }
            return selected.getName();
        } else {
            return param.optionalValues.get(rawValue).name;
        }
    }

    @Override
    public Integer getNullValue() {
        return -1;
    }

    @Override
    public PropertyDescriptor createPropertyDescriptor(int propertyId) {
        String[] labelsArray = null;
        availableValues = Lists.newArrayList();
        if (isContainer()) {
            String filterType = param.optionalValues.get(0).filterType;
            for (Variable variable : processDefinition.getVariables(true, true)) {
                if (filterType == null
                        || (VariableFormatRegistry.isApplicable(variable, filterType) || VariableFormatRegistry.isAssignableFrom(
                                variable.getJavaClassName(), filterType))) {
                    availableValues.add(variable);
                }
            }
            labelsArray = new String[availableValues.size()];
            for (int i = 0; i < labelsArray.length; i++) {
                labelsArray[i] = availableValues.get(i).getName();
            }
        } else {
            labelsArray = new String[param.optionalValues.size()];
            for (int j = 0; j < param.optionalValues.size(); j++) {
                OptionalValue value = param.optionalValues.get(j);
                labelsArray[j] = value.value;
            }
        }
        return new ComboBoxPropertyDescriptor(propertyId, param.label, labelsArray);
    }

    @Override
    protected Integer convertValueFromString(String valueStr) {
        if (isContainer()) {
            String typeName = param.optionalValues.get(0).filterType;
            int index = 0;

            int variableIndex = -1;
            if (valueStr == null || valueStr.trim().isEmpty()) {
                return variableIndex;
            }

            availableValues = new ArrayList<Variable>();
            for (Variable variable : processDefinition.getVariables(true, true)) {
                if (typeName == null
                        || (VariableFormatRegistry.isApplicable(variable, typeName) || VariableFormatRegistry.isAssignableFrom(
                                variable.getJavaClassName(), typeName))) {
                    availableValues.add(variable);

                    if (variableIndex == -1 && valueStr.equals(variable.getName())) {
                        variableIndex = index;
                    }
                    index++;
                }
            }
            return variableIndex;
        } else {
            for (int i = 0; i < param.optionalValues.size(); i++) {
                OptionalValue option = param.optionalValues.get(i);
                if (option.name.equals(valueStr)) {
                    return i;
                }
            }
        }
        return -1;
    }

    private boolean isContainer() {
        boolean isContainer = false;
        if (param.optionalValues != null && param.optionalValues.size() > 0) {
            isContainer = param.optionalValues.get(0).container;
        }
        return isContainer;
    }

}
