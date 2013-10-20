package ru.runa.gpd.formeditor.ftl.bean;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import ru.runa.gpd.formeditor.ftl.MethodTag;
import ru.runa.gpd.formeditor.ftl.MethodTag.Param;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.ui.view.IRequiredPropertiesSource;

import org.apache.commons.lang.StringUtils;

public class FtlComponent implements IRequiredPropertiesSource {
    private ProcessDefinition processDefinition;
    private MethodTag type;
    private ArrayList<ComponentParameter> parameters;

    public FtlComponent(MethodTag type) {
        this.type = type;
        parameters = new ArrayList<ComponentParameter>(type.params.size());
        for (Param param : type.params) {
            parameters.add(ParameterFactory.createParameter(param));
        }
    }

    public void setProcessDefinition(ProcessDefinition processDefinition) {
        this.processDefinition = processDefinition;
        for (ComponentParameter parameter : parameters) {
            parameter.setProcessDefinition(processDefinition);
        }
    }

    public MethodTag getType() {
        return type;
    }

    public List<ComponentParameter> getParameters() {
        return parameters;
    }

    public void setRawParameters(List parameterValues) {
        for (int i = 0; i < parameterValues.size(); i++) {
            // TODO if file is from different version then may throw
            // IndexOutOfBoundException
            ComponentParameter parameter = parameters.get(i);
            String valueStr = parameterValues.get(i).toString();
            valueStr = StringUtils.trimToNull(valueStr);
            parameter.initValue(valueStr);
        }
    }

    public void setParameters(List<String> parameterValues) {
        for (int i = 0; i < parameterValues.size(); i++) {
            // TODO if file is from different version then may throw
            // IndexOutOfBoundException
            ComponentParameter parameter = parameters.get(i);
            parameter.initValue(parameterValues.get(i));
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

  public boolean isPropertyRequired(Object propertyId) {
    int parameterIndex = (Integer) propertyId;
    return parameters.get(parameterIndex).getParam().required;
  }

    @Override
    public boolean isPropertySet(Object propertyId) {
        int parameterIndex = (Integer) propertyId;
        return parameters.get(parameterIndex).getRawValue() != null; // TODO
    }

    @Override
    public void resetPropertyValue(Object propertyId) {
        int parameterIndex = (Integer) propertyId;
        parameters.get(parameterIndex).setRawValue(null);
    }

    @Override
    public void setPropertyValue(Object propertyId, Object value) {
        int parameterIndex = (Integer) propertyId;
        parameters.get(parameterIndex).setRawValue(value);
    }
}
