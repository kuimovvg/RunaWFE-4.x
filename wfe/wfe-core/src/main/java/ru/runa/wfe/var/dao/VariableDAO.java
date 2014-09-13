package ru.runa.wfe.var.dao;

import java.util.List;
import java.util.Map;

import ru.runa.wfe.commons.dao.GenericDAO;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.var.Variable;

import com.google.common.collect.Maps;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class VariableDAO extends GenericDAO<Variable> {

    public Variable<?> get(Process process, String name) {
        return findFirstOrNull("from Variable where process=? and name=?", process, name);
    }

    /**
     * @return all variable values.
     */
    public Map<String, Object> getAll(Process process) {
        Map<String, Object> variables = Maps.newHashMap();
        List<Variable<?>> list = getHibernateTemplate().find("from Variable where process=?", process);
        for (Variable<?> variable : list) {
            try {
                variables.put(variable.getName(), variable.getValue());
            } catch (Exception e) {
                log.error("Unable to revert " + variable + " in " + process, e);
            }
        }
        return variables;
    }

    public void deleteAll(Process process) {
        log.debug("deleting variables for process " + process.getId());
        getHibernateTemplate().bulkUpdate("delete from Variable where process=?", process);
    }

}
