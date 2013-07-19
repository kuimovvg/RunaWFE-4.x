package ru.runa.gpd.lang.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ui.views.properties.ComboBoxPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertyDescriptor;

import ru.runa.gpd.Localization;
import ru.runa.gpd.ProcessCache;
import ru.runa.gpd.extension.VariableFormatArtifact;
import ru.runa.gpd.extension.VariableFormatRegistry;
import ru.runa.gpd.util.VariableMapping;

import com.google.common.collect.Lists;

public class Subprocess extends Node implements Active {
    protected String subProcessName = "";
    protected List<VariableMapping> variableMappings = new ArrayList<VariableMapping>();

    @Override
    protected void validate() {
        super.validate();
        if (subProcessName == null || subProcessName.length() == 0) {
            addError("subprocess.empty");
            return;
        }
        ProcessDefinition subprocessDefinition = ProcessCache.getFirstProcessDefinition(subProcessName);
        if (subprocessDefinition == null) {
            addWarning("subprocess.notFound");
            return;
        }
        for (VariableMapping variableMapping : variableMappings) {
            if (VariableMapping.USAGE_MULTIINSTANCE_VARS.equals(variableMapping.getUsage())) {
                continue;
            }
            Variable processVariable = getProcessDefinition().getVariable(variableMapping.getProcessVariableName(), true);
            if (processVariable == null) {
                addError("subprocess.processVariableDoesNotExist", variableMapping.getProcessVariableName());
                continue;
            }
            Variable subprocessVariable = subprocessDefinition.getVariable(variableMapping.getSubprocessVariableName(), true);
            if (subprocessVariable == null) {
                addError("subprocess.subProcessVariableDoesNotExist", variableMapping.getSubprocessVariableName());
                continue;
            }
            if (!isCompatibleTypes(processVariable.getJavaClassName(), subprocessVariable.getJavaClassName())) {
                VariableFormatArtifact artifact1 = VariableFormatRegistry.getInstance().getArtifactNotNull(processVariable.getFormatClassName());
                VariableFormatArtifact artifact2 = VariableFormatRegistry.getInstance().getArtifactNotNull(subprocessVariable.getFormatClassName());
                addError("subprocess.variableMappingIncompatibleTypes", processVariable.getName(), artifact1.getLabel(), subprocessVariable.getName(), artifact2.getLabel());
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
        List<String> definitionNames = ProcessCache.getAllProcessDefinitionNames();
        String[] items = definitionNames.toArray(new String[definitionNames.size()]);
        list.add(new ComboBoxPropertyDescriptor(PROPERTY_SUBPROCESS, Localization.getString("Subprocess.Name"), items));
        return list;
    }

    @Override
    public Object getPropertyValue(Object id) {
        if (PROPERTY_SUBPROCESS.equals(id)) {
            return ProcessCache.getAllProcessDefinitionNames().indexOf(subProcessName);
        }
        return super.getPropertyValue(id);
    }

    @Override
    public void setPropertyValue(Object id, Object value) {
        if (PROPERTY_SUBPROCESS.equals(id)) {
            setSubProcessName(ProcessCache.getAllProcessDefinitionNames().get((Integer) value));
        } else {
            super.setPropertyValue(id, value);
        }
    }
}
