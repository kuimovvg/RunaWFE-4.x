package ru.runa.gpd.formeditor.ftl.bean;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import ru.runa.gpd.formeditor.ftl.MethodTag;
import ru.runa.gpd.formeditor.ftl.MethodTag.Param;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.ui.view.IRequiredPropertiesSource;
import ru.runa.gpd.util.EventSupport;

public class FtlComponent extends EventSupport implements IRequiredPropertiesSource {

    private ProcessDefinition processDefinition;
    private final MethodTag type;
    @SuppressWarnings("rawtypes")
    private final ArrayList<ComponentParameter> parameters;

    @SuppressWarnings("rawtypes")
    public FtlComponent(MethodTag type) {
        this.type = type;
        parameters = new ArrayList<ComponentParameter>(type.params.size());
        for (Param param : type.params) {
            parameters.add(ParameterFactory.createParameter(param));
        }
    }

    @SuppressWarnings("rawtypes")
    public void setProcessDefinition(ProcessDefinition processDefinition) {
        this.processDefinition = processDefinition;
        for (ComponentParameter parameter : parameters) {
            parameter.setProcessDefinition(processDefinition);
        }
    }

    public MethodTag getType() {
        return type;
    }

    @SuppressWarnings("rawtypes")
    public List<ComponentParameter> getParameters() {
        return parameters;
    }

    @SuppressWarnings("rawtypes")
    public void setRawParameters(List parameterValues) {
        for (int i = 0; i < parameterValues.size(); i++) {
            // TODO if file is from different version then may throw
            // IndexOutOfBoundException
            if (i >= parameters.size()) {
                ComponentParameter last = parameters.get(parameters.size() - 1);
                if (last instanceof RichComboParameter) {
                    String valueStr = parameterValues.get(i).toString();
                    valueStr = StringUtils.trimToNull(valueStr);
                    String previous = last.getStringValue();
                    valueStr = previous + "," + valueStr;
                    last.initValue(valueStr);
                    continue;
                }
            }
            if (i < parameters.size()) {
                // omit excess parameters
                ComponentParameter parameter = parameters.get(i);
                String valueStr = parameterValues.get(i).toString();
                valueStr = StringUtils.trimToNull(valueStr);
                parameter.initValue(valueStr);
            }
        }
    }

    @Override
    public Object getEditableValue() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IPropertyDescriptor[] getPropertyDescriptors() {
        IPropertyDescriptor[] descriptors = new IPropertyDescriptor[parameters.size()];
        for (int i = 0; i < parameters.size(); i++) {
            @SuppressWarnings("rawtypes")
            ComponentParameter parameter = parameters.get(i);
            PropertyDescriptor descriptor = parameter.createPropertyDescriptor(i);
            descriptor.setDescription(parameter.param.help);
            descriptors[i] = descriptor;
        }

        return descriptors;
    }

    @Override
    public Object getPropertyValue(Object propertyId) {
        int parameterIndex = (Integer) propertyId;
        Object value = parameters.get(parameterIndex).getRawValue();
        return value;
    }

    @Override
    public boolean isPropertyRequired(Object propertyId) {
        int parameterIndex = (Integer) propertyId;
        return parameters.get(parameterIndex).getParam().required;
    }

    @Override
    public boolean isPropertySet(Object propertyId) {
        int parameterIndex = (Integer) propertyId;
        return parameters.get(parameterIndex).getRawValue() != null; // TODO
    }

    @SuppressWarnings("unchecked")
    @Override
    public void resetPropertyValue(Object propertyId) {
        int parameterIndex = (Integer) propertyId;
        parameters.get(parameterIndex).setRawValue(null);
        firePropertyChange("", null, 1);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setPropertyValue(Object propertyId, Object value) {
        int parameterIndex = (Integer) propertyId;
        Object old = parameters.get(parameterIndex).getRawValue();
        parameters.get(parameterIndex).setRawValue(value);
        firePropertyChange(propertyId.toString(), old, value);

    }

    @Override
    public String toString() {
        return "FtlComponent [processDefinition=" + processDefinition + ", type=" + type + ", parameters=" + parameters + "]";
    }
}
