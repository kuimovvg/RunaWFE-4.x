package ru.runa.gpd.formeditor.ftl.bean;

import org.eclipse.ui.views.properties.ComboBoxPropertyDescriptor;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import ru.runa.gpd.formeditor.ftl.MethodTag.OptionalValue;

public class ComboBoxParameter extends ComponentParameter<Integer> {

    @Override
    protected String convertValueToString() {
        return param.optionalValues.get(rawValue).name;
    }

    @Override
    public Integer getNullValue() {
        return -1;
    }

    @Override
    public PropertyDescriptor createPropertyDescriptor(int propertyId) {
        String[] labelsArray = new String[param.optionalValues.size()];
        for (int j = 0; j < param.optionalValues.size(); j++) {
            OptionalValue value = param.optionalValues.get(j);
            labelsArray[j] = value.value;
        }
        return new ComboBoxPropertyDescriptor(propertyId, param.label, labelsArray);
    }

    @Override
    protected Integer convertValueFromString(String valueStr) {
        for (int i = 0; i < param.optionalValues.size(); i++) {
            OptionalValue option = param.optionalValues.get(i);
            if (option.name.equals(valueStr))
                return i;
        }
        return -1;
    }

}
