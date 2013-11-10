package ru.runa.gpd.lang.model;

import java.util.List;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import ru.runa.gpd.Localization;
import ru.runa.gpd.ProcessCache;
import ru.runa.gpd.extension.VariableFormatArtifact;
import ru.runa.gpd.extension.VariableFormatRegistry;
import ru.runa.gpd.lang.ValidationError;
import ru.runa.gpd.util.VariableMapping;

import com.google.common.collect.Lists;

public class Subprocess extends Node implements Active {
    protected String subProcessName = "";
    protected List<VariableMapping> variableMappings = Lists.newArrayList();

    @Override
    public void validate(List<ValidationError> errors) {
        super.validate(errors);
        if (subProcessName == null || subProcessName.length() == 0) {
            errors.add(ValidationError.createLocalizedError(this, "subprocess.empty"));
            return;
        }
        ProcessDefinition subprocessDefinition = ProcessCache.getFirstProcessDefinition(subProcessName);
        if (subprocessDefinition == null) {
            errors.add(ValidationError.createLocalizedWarning(this, "subprocess.notFound"));
            return;
        }
        for (VariableMapping mapping : variableMappings) {
            if (VariableMapping.USAGE_MULTIINSTANCE_VARS.equals(mapping.getUsage())) {
                continue;
            }
            Variable processVariable = getProcessDefinition().getVariable(mapping.getProcessVariableName(), true);
            if (processVariable == null) {
                errors.add(ValidationError.createLocalizedError(this, "subprocess.processVariableDoesNotExist", mapping.getProcessVariableName()));
                continue;
            }
            Variable subprocessVariable = subprocessDefinition.getVariable(mapping.getSubprocessVariableName(), true);
            if (subprocessVariable == null) {
                errors.add(ValidationError.createLocalizedError(this, "subprocess.subProcessVariableDoesNotExist", mapping.getSubprocessVariableName()));
                continue;
            }
            if (!isCompatibleTypes(processVariable.getJavaClassName(), subprocessVariable.getJavaClassName())) {
                VariableFormatArtifact artifact1 = VariableFormatRegistry.getInstance().getArtifactNotNull(processVariable.getFormatClassName());
                VariableFormatArtifact artifact2 = VariableFormatRegistry.getInstance().getArtifactNotNull(subprocessVariable.getFormatClassName());
                errors.add(ValidationError.createLocalizedError(this, "subprocess.variableMappingIncompatibleTypes", 
                        processVariable.getName(), artifact1.getLabel(), subprocessVariable.getName(), artifact2.getLabel()));
            }
        }
    }

    protected boolean isCompatibleTypes(String javaClassName1, String javaClassName2) {
        return VariableFormatRegistry.isAssignableFrom(javaClassName1, javaClassName2)
                || VariableFormatRegistry.isAssignableFrom(javaClassName2, javaClassName1);
    }

    public List<VariableMapping> getVariableMappings() {
        return Lists.newArrayList(variableMappings);
    }

    public void setVariableMappings(List<VariableMapping> variablesList) {
        this.variableMappings.clear();
        this.variableMappings.addAll(variablesList);
        setDirty();
    }

    public void setSubProcessName(String subProcessName) {
        String old = this.subProcessName;
        this.subProcessName = subProcessName;
        firePropertyChange(PROPERTY_SUBPROCESS, old, this.subProcessName);
    }

    public String getSubProcessName() {
        return subProcessName;
    }

    @Override
    protected boolean allowLeavingTransition(List<Transition> transitions) {
        return super.allowLeavingTransition(transitions) && transitions.size() == 0;
    }

    @Override
    public List<IPropertyDescriptor> getCustomPropertyDescriptors() {
        List<IPropertyDescriptor> list = super.getCustomPropertyDescriptors();
        list.add(new PropertyDescriptor(PROPERTY_SUBPROCESS, Localization.getString("Subprocess.Name")));
        return list;
    }

    @Override
    public Object getPropertyValue(Object id) {
        if (PROPERTY_SUBPROCESS.equals(id)) {
            return ProcessCache.getAllProcessDefinitionNames().indexOf(subProcessName);
        }
        return super.getPropertyValue(id);
    }

}
