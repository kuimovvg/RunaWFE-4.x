package ru.runa.gpd.lang.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ui.views.properties.IPropertyDescriptor;

import ru.runa.gpd.Localization;
import ru.runa.gpd.lang.ValidationError;
import ru.runa.gpd.property.DurationPropertyDescriptor;
import ru.runa.gpd.util.Duration;
import ru.runa.gpd.util.VariableMapping;

public class SendMessageNode extends Node implements Active {
    private List<VariableMapping> variablesList = new ArrayList<VariableMapping>();
    private Duration ttlDuration = new Duration("1 days");

    public Duration getTtlDuration() {
        return ttlDuration;
    }

    public void setTtlDuration(Duration ttlDuration) {
        this.ttlDuration = ttlDuration;
        firePropertyChange(PROPERTY_TTL, null, ttlDuration);
    }

    @Override
    public List<IPropertyDescriptor> getCustomPropertyDescriptors() {
        List<IPropertyDescriptor> list = super.getCustomPropertyDescriptors();
        list.add(new DurationPropertyDescriptor(PROPERTY_TTL, getProcessDefinition(), getTtlDuration(), Localization.getString("property.message.ttl")));
        return list;
    }

    @Override
    public Object getPropertyValue(Object id) {
        if (PROPERTY_TTL.equals(id)) {
            return ttlDuration;
        }
        return super.getPropertyValue(id);
    }

    @Override
    public void setPropertyValue(Object id, Object value) {
        if (PROPERTY_TTL.equals(id)) {
            setTtlDuration((Duration) value);
            return;
        }
        super.setPropertyValue(id, value);
    }

    @Override
    public void validate(List<ValidationError> errors) {
        super.validate(errors);
        int selectorRulesCount = 0;
        for (VariableMapping variableMapping : variablesList) {
            if (VariableMapping.USAGE_SELECTOR.equals(variableMapping.getUsage())) {
                selectorRulesCount++;
                continue;
            }
            String processVarName = variableMapping.getProcessVariableName();
            if (!getProcessDefinition().getVariableNames(true).contains(processVarName)) {
                errors.add(ValidationError.createLocalizedError(this, "message.processVariableDoesNotExist", processVarName));
                continue;
            }
        }
        if (selectorRulesCount == 0) {
            errors.add(ValidationError.createLocalizedWarning(this, "model.validation.message.selectorRulesEmpty"));
        }
    }

    public List<VariableMapping> getVariablesList() {
        List<VariableMapping> result = new ArrayList<VariableMapping>();
        result.addAll(variablesList);
        return result;
    }

    public void setVariablesList(List<VariableMapping> variablesList) {
        this.variablesList.clear();
        this.variablesList.addAll(variablesList);
        setDirty();
    }

    @Override
    protected boolean allowLeavingTransition(List<Transition> transitions) {
        return super.allowLeavingTransition(transitions) && transitions.size() == 0;
    }
}
