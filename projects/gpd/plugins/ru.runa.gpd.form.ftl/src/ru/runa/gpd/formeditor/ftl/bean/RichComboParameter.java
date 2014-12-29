package ru.runa.gpd.formeditor.ftl.bean;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.ui.views.properties.PropertyDescriptor;

import ru.runa.gpd.extension.VariableFormatRegistry;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.property.EditableSyncPropertyDescriptor;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class RichComboParameter extends ComponentParameter<String> {
    private List<Variable> availibleValues;

    @Override
    protected String convertValueToString() {
        return rawValue;
    }

    @Override
    public String getNullValue() {
        return "";
    }

    @Override
    public PropertyDescriptor createPropertyDescriptor(int propertyId) {
        String typeName = param.getVariableTypeFilter();

        availibleValues = new ArrayList<Variable>();
        List<String> labels = new LinkedList<String>();
        List<Variable> variables = processDefinition.getVariables(true, true);
        for (Variable variable : variables) {
            if (typeName == null
                    || (VariableFormatRegistry.isAssignableFrom(variable.getJavaClassName(), typeName) || VariableFormatRegistry.isAssignableFrom(
                            typeName, variable.getJavaClassName()))) {
                availibleValues.add(variable);
                labels.add(variable.getName());
            }
        } // TODO check variable access?

        List<String> selected = Lists.newLinkedList();

        String val = convertValueFromString(rawValue);

        if (!Strings.isNullOrEmpty(val)) {
            String[] vals = val.split(",");
            for (String string : vals) {
                selected.add(string.trim());
            }
        }

        if (typeName != null
                && (VariableFormatRegistry.isAssignableFrom(List.class.getName(), typeName) || VariableFormatRegistry.isAssignableFrom(
                        Map.class.getName(), typeName))) {
            Map<String, Boolean> map = Maps.<String, Boolean> newLinkedHashMap();

            for (String item : selected) {
                map.put(item, true);
            }
            for (String label : labels) {
                if (!map.containsKey(label)) {
                    map.put(label, false);
                }
            }
            return new EditableSyncPropertyDescriptor(propertyId, param.label, typeName, val, null, map);
        }
        return new EditableSyncPropertyDescriptor(propertyId, param.label, typeName, val, labels, null);
    }

    @Override
    protected String convertValueFromString(String valueStr) {
        if (valueStr == null || valueStr.trim().isEmpty()) {
            return getNullValue();
        }
        return valueStr;
    }
}
