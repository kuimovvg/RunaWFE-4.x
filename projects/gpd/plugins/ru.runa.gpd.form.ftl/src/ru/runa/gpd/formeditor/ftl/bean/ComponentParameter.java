package ru.runa.gpd.formeditor.ftl.bean;

import org.eclipse.ui.views.properties.PropertyDescriptor;

import ru.runa.gpd.formeditor.ftl.MethodTag.Param;
import ru.runa.gpd.lang.model.ProcessDefinition;

public abstract class ComponentParameter<T> {
    protected Param param;
    protected T rawValue;
    protected ProcessDefinition processDefinition;

    public Param getParam() {
        return param;
    }

    public void setParam(Param param) {
        this.param = param;
    }

    public void setProcessDefinition(ProcessDefinition processDefinition) {
        this.processDefinition = processDefinition;
    }

    public T getRawValue() {
        if (rawValue != null)
            return rawValue;
        return getNullValue();
    }

    public void setRawValue(T rawValue) {
        if (getNullValue().equals(rawValue))
            this.rawValue = null;
        else
            this.rawValue = rawValue;
    }

    public String getStringValue() {
        if (rawValue == null)
            return "";

        return convertValueToString();
    }
    
    public void initValue(String valueStr) {
        setRawValue(convertValueFromString(valueStr));
    }

    protected abstract String convertValueToString();
    
    protected abstract T convertValueFromString(String valueStr);

    public abstract T getNullValue();

    public abstract PropertyDescriptor createPropertyDescriptor(int propertyId);
}
