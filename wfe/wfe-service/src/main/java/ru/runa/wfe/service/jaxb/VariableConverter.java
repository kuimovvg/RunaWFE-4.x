package ru.runa.wfe.service.jaxb;

import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.var.VariableDefinition;
import ru.runa.wfe.var.dto.WfVariable;
import ru.runa.wfe.var.format.FormatCommons;
import ru.runa.wfe.var.format.VariableFormat;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class VariableConverter {

    public static Variable marshal(VariableDefinition variableDefinition, Object value) {
        Variable variable = new Variable();
        variable.name = variableDefinition.getName();
        variable.scriptingName = variableDefinition.getScriptingName();
        VariableFormat variableFormat = FormatCommons.create(variableDefinition);
        variable.format = variableFormat.getName();
        variable.value = variableFormat.format(value);
        return variable;
    }
    

    public static List<Variable> marshal(List<WfVariable> variables) {
        List<Variable> result = Lists.newArrayListWithExpectedSize(variables.size());
        for (WfVariable variable : variables) {
            result.add(marshal(variable.getDefinition(), variable.getValue()));
        }
        return result;
    }

    public static List<Variable> marshalDefinitions(List<VariableDefinition> variableDefinitions) {
        List<Variable> result = Lists.newArrayListWithExpectedSize(variableDefinitions.size());
        for (VariableDefinition variableDefinition : variableDefinitions) {
            result.add(marshal(variableDefinition, null));
        }
        return result;
    }

    public static Object unmarshal(ProcessDefinition processDefinition, Variable variable) {
        try {
            VariableDefinition variableDefinition = processDefinition.getVariableNotNull(variable.name, true);
            Object value = FormatCommons.create(variableDefinition).parse(variable.value);
            return value;
        } catch (Exception e) {
            throw new InternalApplicationException("Unable to parse " + variable, e);
        }
    }

    public static Map<String, Object> unmarshal(ProcessDefinition processDefinition, List<Variable> variables) {
        Map<String, Object> map = Maps.newHashMap();
        if (variables != null) {
            for (Variable variable : variables) {
                map.put(variable.name, unmarshal(processDefinition, variable));
            }
        }
        return map;
    }

}
