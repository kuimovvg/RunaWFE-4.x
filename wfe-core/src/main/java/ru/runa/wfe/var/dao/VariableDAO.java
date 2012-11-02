package ru.runa.wfe.var.dao;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import ru.runa.wfe.commons.dao.CommonDAO;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.execution.Swimlane;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.var.Variable;

import com.google.common.collect.Maps;

public class VariableDAO extends CommonDAO {

    @Autowired
    @Value(value = "${backwardCompatibility.variablesMode}")
    private boolean backwardCompatibilityVariablesMode;

    public Variable<?> getVariable(ru.runa.wfe.execution.Process process, String name) {
        return findFirstOrNull("from Variable where process=? and name=?", process, name);
    }

    /**
     * @return all variable values.
     */
    public Map<String, Object> getVariables(Process process) {
        Map<String, Object> variables = Maps.newHashMap();
        if (backwardCompatibilityVariablesMode) {
            // for compatibility
            for (Swimlane swimlane : process.getSwimlanes()) {
                Executor executor = swimlane.getExecutor();
                if (executor == null) {
                    continue;
                }
                variables.put(swimlane.getName(), executor);
            }
        }
        List<Variable<?>> list = getHibernateTemplate().find("from Variable where process=?", process);
        for (Variable<?> variable : list) {
            variables.put(variable.getName(), variable.getValue());
        }
        return variables;
    }

    public void deleteVariables(Process process) {
        log.debug("deleting variables for process " + process.getId());
        List<Variable<?>> variables = getHibernateTemplate().find("from Variable where process=?", process);
        getHibernateTemplate().deleteAll(variables);
    }

}
