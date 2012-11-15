package ru.runa.gpd.lang.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ui.views.properties.ComboBoxPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertyDescriptor;

import ru.runa.gpd.Localization;
import ru.runa.gpd.ProcessCache;
import ru.runa.gpd.util.VariableMapping;
import ru.runa.gpd.validation.FormatMapping;
import ru.runa.gpd.validation.FormatMappingParser;
import ru.runa.wfe.var.format.StringFormat;

public class Subprocess extends DescribableNode implements Active {
    protected String subProcessName = "";
    protected List<VariableMapping> variablesList = new ArrayList<VariableMapping>();

    @Override
    protected void validate() {
        super.validate();
        if (subProcessName == null || subProcessName.length() == 0) {
            addError("subprocess.empty");
            return;
        }
        ProcessDefinition definition = ProcessCache.getProcessDefinition(subProcessName);
        if (definition == null) {
            addWarning("subprocess.notFound");
            return;
        }
        List<Variable> subProcessVariables = definition.getVariablesList();
        List<Variable> processVariables = getProcessDefinition().getVariablesList();
        for (VariableMapping variableMapping : variablesList) {
            if (VariableMapping.USAGE_MULTIINSTANCE_VARS.equals(variableMapping.getUsage())) {
                continue;
            }
            String processVarName = variableMapping.getProcessVariable();
            String processVarFormat = getVariableFormat(processVarName, processVariables, getProcessDefinition());
            if (processVarFormat == null) {
                addError("subprocess.processVariableDoesNotExist", processVarName);
                continue;
            }
            String subProcessVarName = variableMapping.getSubprocessVariable();
            String subProcessVarFormat = getVariableFormat(subProcessVarName, subProcessVariables, definition);
            if (subProcessVarFormat == null) {
                addError("subprocess.subProcessVariableDoesNotExist", subProcessVarName);
                continue;
            }
            if (!isCompatibleTypes(processVarFormat, subProcessVarFormat)) {
                addError("subprocess.variableMappingIncompatibleTypes", processVarName, processVarFormat, subProcessVarName, subProcessVarFormat);
            }
        }
    }

    protected boolean isCompatibleTypes(String processVarFormat, String subProcessVarFormat) {
        FormatMapping mapping1 = FormatMappingParser.getFormatMappings().get(processVarFormat);
        String processVarType = mapping1 != null ? mapping1.getJavaType() : Object.class.getName();
        FormatMapping mapping2 = FormatMappingParser.getFormatMappings().get(subProcessVarFormat);
        String subProcessVarType = mapping2 != null ? mapping2.getJavaType() : Object.class.getName();
        return processVarType.equals(subProcessVarType);
    }

    private String getVariableFormat(String varName, List<Variable> variables, ProcessDefinition definition) {
        String processVarFormat = null;
        for (Variable processVariable : variables) {
            if (processVariable.getName().equals(varName)) {
                processVarFormat = processVariable.getFormat();
                break;
            }
        }
        if (processVarFormat == null && definition.getSwimlaneByName(varName) != null) {
            processVarFormat = StringFormat.class.getName();
        }
        return processVarFormat;
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

    public void setSubProcessName(String subProcessName) {
        String old = this.subProcessName;
        this.subProcessName = subProcessName;
        firePropertyChange(PROPERTY_SUBPROCESS, old, this.subProcessName);
    }

    public String getSubProcessName() {
        return subProcessName;
    }

    @Override
    protected boolean allowLeavingTransition(Node target, List<Transition> transitions) {
        return super.allowLeavingTransition(target, transitions) && transitions.size() == 0;
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
