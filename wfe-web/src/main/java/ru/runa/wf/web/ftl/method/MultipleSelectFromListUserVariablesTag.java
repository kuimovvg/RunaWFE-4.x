package ru.runa.wf.web.ftl.method;

import java.util.Arrays;
import java.util.Map;

import ru.runa.wfe.commons.ftl.FtlTagVariableSubmissionHandler;
import ru.runa.wfe.var.VariableDefinition;
import ru.runa.wfe.var.format.FormatCommons;
import ru.runa.wfe.var.format.VariableFormat;

import com.google.common.collect.Maps;

public class MultipleSelectFromListUserVariablesTag extends AbstractListUserVariables implements FtlTagVariableSubmissionHandler {
    private static final long serialVersionUID = 1L;

    @Override
    protected Object executeTag() throws Exception {
        initFields();

        registerVariableHandler(dectVariableName);

        return ViewUtil.getActiveJsonTable(user, webHelper, variableProvider.getVariableNotNull(variableName),
                variableProvider.getVariableNotNull(dectVariableName), variableProvider.getProcessId(), sortField,
                displayMode == DisplayMode.MULTI_DIMENTIONAL_TABLE);
    }

    @Override
    public Map<String, ? extends Object> extractVariables(VariableDefinition variableDefinition, Map<String, ? extends Object> userInput,
            Map<String, String> formatErrorsForFields) throws Exception {
        Map<String, Object> result = Maps.newHashMap();
        if (!variableDefinition.getName().equals(dectVariableName) || !userInput.containsKey(dectVariableName)) {
            return result;
        }
        Object raw = userInput.get(dectVariableName);
        String json = null;
        VariableFormat format = FormatCommons.create(variableDefinition);
        if (!(raw instanceof String[])) {
            json = (String) raw;
        } else {
            json = Arrays.toString((String[]) raw);
        }
        result.put(variableDefinition.getName(), format.parse(json));
        return result;
    }
}
