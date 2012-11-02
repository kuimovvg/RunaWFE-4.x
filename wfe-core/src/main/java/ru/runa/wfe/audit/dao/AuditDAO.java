package ru.runa.wfe.audit.dao;

import java.util.List;

import ru.runa.wfe.audit.ProcessLog;
import ru.runa.wfe.audit.SystemLog;
import ru.runa.wfe.audit.TransitionLog;
import ru.runa.wfe.commons.dao.CommonDAO;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.lang.Transition;

import com.google.common.collect.Lists;

/**
 * DAO level interface for managing {@linkplain SystemLog}.
 * 
 * @author Konstantinov Aleksey 25.02.2012
 */
public class AuditDAO extends CommonDAO {

    /**
     * Save {@linkplain SystemLog}. Saving {@linkplain SystemLog} must not be saved before.
     * 
     * @param log
     *            {@linkplain SystemLog} to save.
     */
    public void create(SystemLog log) {
        getHibernateTemplate().save(log);
    }

    /**
     * Load all {@linkplain SystemLog}.
     * 
     * @return {@linkplain SystemLog} list.
     */
    public List<SystemLog> getAllSystemLogs() {
        return getHibernateTemplate().loadAll(SystemLog.class);
    }

    public void addProcessLog(ProcessLog processLog) {
        getHibernateTemplate().save(processLog);
    }

    public List<ProcessLog> getProcessLogs(Long processId) {
        return getHibernateTemplate().find("from ProcessLog where processId=? order by date asc", processId);
    }

    public void deleteProcessLogs(Long processId) {
        log.debug("deleting logs for process " + processId);
        List<ProcessLog> processLogs = getProcessLogs(processId);
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
