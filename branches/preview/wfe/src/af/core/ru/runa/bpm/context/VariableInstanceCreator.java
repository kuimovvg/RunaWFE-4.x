package ru.runa.bpm.context;

import java.util.List;

import org.springframework.beans.factory.annotation.Required;

import ru.runa.InternalApplicationException;
import ru.runa.bpm.context.exe.JbpmType;
import ru.runa.bpm.context.exe.VariableInstance;
import ru.runa.bpm.context.exe.variableinstance.NullInstance;
import ru.runa.bpm.context.log.VariableCreateLog;
import ru.runa.bpm.graph.exe.Token;

public class VariableInstanceCreator {
    private List<JbpmType> types;

    @Required
    public void setTypes(List<JbpmType> types) {
        this.types = types;
    }

    public VariableInstance<?> create(Object value) {
        for (JbpmType type : types) {
            if (type.getMatcher().matches(value)) {
                try {
                    VariableInstance<?> variableInstance = type.getVariableInstanceClass().newInstance();
                    variableInstance.setConverter(type.getConverter());
                    return variableInstance;
                } catch (Exception e) {
                    throw new InternalApplicationException("Unable to create variable " + type.getVariableInstanceClass(), e);
                }
            }
        }
        throw new InternalApplicationException("No variable instance found for value " + value);
    }

    public VariableInstance<?> create(Token token, String name, Object value) {
        VariableInstance<?> variableInstance;
        if (value == null) {
            variableInstance = new NullInstance();
        } else {
            variableInstance = create(value);
        }
        variableInstance.setToken(token);
        variableInstance.setName(name);
        if (token != null) {
            variableInstance.setProcessInstance(token.getProcessInstance());
        }
        token.addLog(new VariableCreateLog(variableInstance));
        variableInstance.setValue(value);
        return variableInstance;
    }

}
