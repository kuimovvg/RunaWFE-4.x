package ru.runa.gpd.lang.model;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import ru.runa.gpd.Localization;
import ru.runa.gpd.ProcessCache;
import ru.runa.gpd.extension.VariableFormatArtifact;
import ru.runa.gpd.extension.VariableFormatRegistry;
import ru.runa.gpd.lang.ValidationError;
import ru.runa.gpd.util.VariableMapping;
import ru.runa.gpd.util.VariableUtils;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;

public class Subprocess extends Node implements Active {
    protected String subProcessName = "";
    protected List<VariableMapping> variableMappings = Lists.newArrayList();
    private boolean embedded;

    @Override
    public void validate(List<ValidationError> errors, IFile definitionFile) {
        super.validate(errors, definitionFile);
        if (subProcessName == null || subProcessName.length() == 0) {
            errors.add(ValidationError.createLocalizedError(this, "subprocess.empty"));
            return;
        }
        if (embedded) {
            if (getLeavingTransitions().size() != 1) {
                errors.add(ValidationError.createLocalizedError(this, "subprocess.embedded.required1leavingtransition"));
            }
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
            Variable processVariable = VariableUtils.getVariableByName(getProcessDefinition(), mapping.getProcessVariableName());
            if (processVariable == null) {
                errors.add(ValidationError.createLocalizedError(this, "subprocess.processVariableDoesNotExist", mapping.getProcessVariableName()));
                continue;
            }
            Variable subprocessVariable = VariableUtils.getVariableByName(subprocessDefinition, mapping.getSubprocessVariableName());
            if (subprocessVariable == null) {
                errors.add(ValidationError.createLocalizedError(this, "subprocess.subProcessVariableDoesNotExist", mapping.getSubprocessVariableName()));
                continue;
            }
            if (!isCompatibleVariables(processVariable, subprocessVariable)) {
                VariableFormatArtifact artifact1 = VariableFormatRegistry.getInstance().getArtifactNotNull(processVariable.getFormatClassName());
                VariableFormatArtifact artifact2 = VariableFormatRegistry.getInstance().getArtifactNotNull(subprocessVariable.getFormatClassName());
                errors.add(ValidationError.createLocalizedError(this, "subprocess.variableMappingIncompatibleTypes", 
                        processVariable.getName(), artifact1.getLabel(), subprocessVariable.getName(), artifact2.getLabel()));
            }
        }
    }

    protected boolean isCompatibleVariables(Variable variable1, Variable variable2) {
        if (Objects.equal(variable1.getUserType(), variable2.getUserType())) {
            return true;
        }
        if (VariableFormatRegistry.isAssignableFrom(variable1.getJavaClassName(), variable2.getJavaClassName())) {
            return true;
        }
        return VariableFormatRegistry.isAssignableFrom(variable2.getJavaClassName(), variable1.getJavaClassName());
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
    
    public SubprocessDefinition getEmbeddedSubprocess() {
         return getProcessDefinition().getEmbeddedSubprocessByName(getSubProcessName());
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
            return subProcessName;
        }
        return super.getPropertyValue(id);
    }

    public boolean isEmbedded() {
        return embedded;
    }
    
    public void setEmbedded(boolean embedded) {
        boolean old = this.embedded;
        this.embedded = embedded;
        firePropertyChange(PROPERTY_SUBPROCESS, old, this.embedded);
    }
}
