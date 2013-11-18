package ru.runa.wfe.audit.dao;

import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

import ru.runa.wfe.audit.ProcessLog;
import ru.runa.wfe.audit.Severity;
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
     * @return process logs.
     */
    public List<ProcessLog> getAll(final Long processId, final List<Severity> severities) {
        return getHibernateTemplate().executeFind(new HibernateCallback<List<ProcessLog>>() {

            @Override
            public List<ProcessLog> doInHibernate(Session session) {
                Query query = session.createQuery("from ProcessLog where processId=:processId and severity in (:severities)");
                query.setParameter("processId", processId);
                query.setParameterList("severities", severities);
                return query.list();
            }
        });
    }

    /**
     * Deletes all process logs.
     */
    public void deleteAll(Long processId) {
        log.debug("deleting logs for process " + processId);
        getHibernateTemplate().bulkUpdate("delete from ProcessLog where processId=?", processId);
    }

    /**
     * Retrieves passed transitions for all Process's Tokens from process logs
     */
    public List<Transition> getPassedTransitions(ProcessDefinition processDefinition, Process process) {
        List<TransitionLog> transitionLogs = getHibernateTemplate().find("from TransitionLog where processId=?", process.getId());
        List<Transition> result = Lists.newArrayListWithExpectedSize(transitionLogs.size());
        for (TransitionLog log : transitionLogs) {
            result.add(log.getTransition(processDefinition));
        }
        return result;
    }

    // public List<String> getStartedSubprocessNodeIds(Process process) {
    // List<SubprocessStartLog> logs =
    // getHibernateTemplate().find("from SubprocessStartLog where processId=?",
    // process.getId());
    // List<String> result = Lists.newArrayListWithExpectedSize(logs.size());
    // for (SubprocessStartLog log : logs) {
    // result.add(log.getNodeId());
    // }
    // return result;
    // }

    public boolean isNodeEntered(Process process, String nodeId) {
        return getHibernateTemplate().find("from NodeEnterLog where processId=? and nodeId=?", process.getId(), nodeId).size() > 0;
    }

}
