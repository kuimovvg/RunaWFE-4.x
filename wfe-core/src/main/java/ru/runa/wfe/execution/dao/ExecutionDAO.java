package ru.runa.wfe.execution.dao;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Expression;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateCallback;

import ru.runa.wfe.audit.dao.AuditDAO;
import ru.runa.wfe.commons.dao.CommonDAO;
import ru.runa.wfe.execution.NodeProcess;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.execution.ProcessDoesNotExistException;
import ru.runa.wfe.execution.ProcessFilter;
import ru.runa.wfe.execution.Token;
import ru.runa.wfe.job.dao.JobDAO;
import ru.runa.wfe.var.Variable;
import ru.runa.wfe.var.dao.VariableDAO;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class ExecutionDAO extends CommonDAO {

    @Autowired
    AuditDAO auditDAO;
    @Autowired
    JobDAO jobDAO;
    @Autowired
    VariableDAO variableDAO;

    public void saveProcess(Process process) {
        getHibernateTemplate().save(process);
    }

    /**
     * @return process from the database by the identifier.\
     */
    public Process getProcessNotNull(Long processId) {
        Process process = get(Process.class, processId);
        if (process == null) {
            throw new ProcessDoesNotExistException(processId);
        }
        return process;
    }

    /**
     * gets a token from the database by the identifier.
     * 
     * @return the token or null in case the token doesn't exist.
     */
    // unused
    public Token getToken(Long tokenId) {
        return getHibernateTemplate().get(Token.class, tokenId);
    }

    /**
     * fetches all processes for the given process definition from the database. The returned list of processs is sorted start date, youngest first.
     */
    public List<Process> findAllProcesses(Long definitionId) {
        return getHibernateTemplate().find("from Process where definition.id=? order by startDate desc", definitionId);
    }

    public void deleteProcess(final Process process) {
        log.debug("deleting process " + process);
        getHibernateTemplate().execute(new HibernateCallback<Object>() {

            @Override
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                // delete the tokens and subprocessees
                List<Process> subProcesses = getSubprocesses(process);
                for (Process subProcess : subProcesses) {
                    log.debug("deleting sub process " + subProcess.getId());
                    deleteProcess(subProcess);
                }

                auditDAO.deleteProcessLogs(process.getId());
                jobDAO.deleteJobs(process);
                variableDAO.deleteVariables(process);

                // delete started subprocesses
                log.debug("deleting started subprocesses for process " + process.getId());
                session.createQuery("delete from NodeProcess where process.id=" + process.getId()).executeUpdate();

                log.debug("hibernate session delete for process " + process.getId());
                session.delete(process);
                session.flush();
                return null;
            }

        });
    }

    public List<Token> findAllActiveTokens() {
        return getHibernateTemplate().find("from Token where endDate is null");
    }

    public List<Process> getSubprocesses(Process process) {
        List<NodeProcess> nodeProcesses = getNodeProcesses(process.getId());
        List<Process> result = Lists.newArrayListWithExpectedSize(nodeProcesses.size());
        for (NodeProcess nodeProcess : nodeProcesses) {
            result.add(nodeProcess.getSubProcess());
        }
        return result;
    }

    public List<Process> getSubprocessesRecursive(Process process) {
        List<Process> result = getSubprocesses(process);
        for (Process subprocess : result) {
            result.addAll(getSubprocessesRecursive(subprocess));
        }
        return result;
    }

    public NodeProcess getNodeProcessByChild(Long processId) {
        return (NodeProcess) getFirstOrNull(getHibernateTemplate().find("from NodeProcess where subProcess.id = ?", processId));
    }

    public List<NodeProcess> getNodeProcesses(Long processId) {
        return getHibernateTemplate().find("from NodeProcess where process.id = ? order by id", processId);
    }

    public void saveNodeProcess(NodeProcess nodeProcess) {
        getHibernateTemplate().save(nodeProcess);
    }

    public Map<Long, Object> getVariableValueFromProcesses(final List<Long> processIds, final String variableName) {
        return getHibernateTemplate().execute(new HibernateCallback<Map<Long, Object>>() {

            @Override
            public Map<Long, Object> doInHibernate(Session session) throws HibernateException, SQLException {
                Map<Long, Object> result = Maps.newHashMap();
                if (!processIds.isEmpty()) {
                    for (int i = 0; i <= processIds.size() / 1000; ++i) {
                        int start = i * 1000;
                        int end = (i + 1) * 1000 > processIds.size() ? processIds.size() : (i + 1) * 1000;
                        Set<Long> requested = new HashSet<Long>(end - start);
                        for (int j = start; j < end; j++) {
                            requested.add(processIds.get(j));
                        }
                        if (requested.size() == 0) {
                            continue;
                        }
                        Criteria query = session.createCriteria(Variable.class);
                        query.add(Expression.in("process.id", requested));
                        query.add(Expression.eq("name", variableName));
                        List<Variable<?>> vars = query.list();
                        for (Variable<?> var : vars) { // TODO check permissions!
                            result.put(var.getProcess().getId(), var.getValue());
                        }
                    }
                }
                return result;
            }
        });
    }

    public List<Process> getProcesses(final ProcessFilter filter) {
        return getHibernateTemplate().executeFind(new HibernateCallback<List<Process>>() {

            @Override
            public List<Process> doInHibernate(Session session) throws HibernateException, SQLException {
                List<String> conditions = Lists.newArrayList();
                Map<String, Object> parameters = Maps.newHashMap();
                if (filter.getDefinitionName() != null) {
                    conditions.add("definition.name=:definitionName");
                    parameters.put("definitionName", filter.getDefinitionName());
                }
                if (filter.getDefinitionVersion() != null) {
                    conditions.add("definition.version=:definitionVersion");
                    parameters.put("definitionVersion", filter.getDefinitionVersion());
                }
                if (filter.getIdFrom() != null) {
                    conditions.add("id >= :idFrom");
                    parameters.put("idFrom", filter.getIdFrom());
                }
                if (filter.getIdTill() != null) {
                    conditions.add("id <= :idTo");
                    parameters.put("idTo", filter.getIdTill());
                }
                if (filter.getStartDateFrom() != null) {
                    conditions.add("startDate >= :startDateFrom");
                    parameters.put("startDateFrom", filter.getStartDateFrom());
                }
                if (filter.getStartDateTill() != null) {
                    conditions.add("startDate <= :startDateTo");
                    parameters.put("startDateTo", filter.getStartDateTill());
                }
                if (filter.getFinishedOnly() != null) {
                    if (filter.getFinishedOnly()) {
                        conditions.add("endDate is not null");
                    } else {
                        conditions.add("endDate is null");
                    }
                }
                if (filter.getEndDateFrom() != null) {
                    conditions.add("endDate >= :endDateFrom");
                    parameters.put("endDateFrom", filter.getEndDateFrom());
                }
                if (filter.getEndDateTill() != null) {
                    conditions.add("endDate <= :endDateTo");
                    parameters.put("endDateTo", filter.getEndDateTill());
                }
                if (conditions.size() == 0) {
                    throw new IllegalArgumentException("Filter should be specified");
                }
                String hql = "from Process where " + Joiner.on(" and ").join(conditions);
                Query query = session.createQuery(hql);
                for (Entry<String, Object> param : parameters.entrySet()) {
                    query.setParameter(param.getKey(), param.getValue());
                }
                return query.list();
            }
        });
    }

}
