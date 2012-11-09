package ru.runa.wfe.audit.dao;

import java.util.List;

import ru.runa.wfe.audit.ProcessLog;
import ru.runa.wfe.audit.TransitionLog;
import ru.runa.wfe.commons.dao.GenericDAO;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.lang.Transition;

import com.google.common.collect.Lists;

/**
 * DAO for {@link ProcessLog}.
 * 
 * @author dofs
 * @since 4.0
 */
@SuppressWarnings("unchecked")
public class ProcessLogDAO extends GenericDAO<ProcessLog> {

    /**
     * @return process logs.
     */
    public List<ProcessLog> getAll(Long processId) {
        return getHibernateTemplate().find("from ProcessLog where processId=? order by id asc", processId);
    }

    /**
     * Deletes all process logs.
     */
    public void deleteAll(Long processId) {
        log.debug("deleting logs for process " + processId);
        List<ProcessLog> processLogs = getAll(processId);
        getHibernateTemplate().deleteAll(processLogs);
    }

    /**
     * Retrieves passed transitions for all Process's Tokens from process logs
     */
    public List<Transition> getPassedTransitions(final ProcessDefinition processDefinition, Process process) {
        List<TransitionLog> transitionLogs = getHibernateTemplate().find("from TransitionLog where processId=?", process.getId());
        List<Transition> result = Lists.newArrayListWithExpectedSize(transitionLogs.size());
        for (TransitionLog log : transitionLogs) {
            result.add(log.getTransition(processDefinition));
        }
        return result;
    }

}
