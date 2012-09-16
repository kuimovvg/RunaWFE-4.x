package ru.runa.bpm.db;

import java.sql.SQLException;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

import ru.runa.bpm.context.exe.TokenVariableMap;
import ru.runa.bpm.context.exe.VariableInstance;
import ru.runa.bpm.graph.exe.ProcessInstance;
import ru.runa.bpm.graph.exe.Token;
import ru.runa.bpm.logging.log.ProcessLog;
import ru.runa.bpm.taskmgmt.exe.TaskInstance;
import ru.runa.wf.ProcessInstanceDoesNotExistException;

public class ProcessExecutionDAO extends CommonDAO {

    /**
     * gets a process instance from the database by the identifier. This method
     * returns null in case the given process instance doesn't exist.
     */
    public ProcessInstance getInstanceNotNull(Long processInstanceId) {
        ProcessInstance processInstance = get(ProcessInstance.class, processInstanceId);
        if (processInstance == null) {
            throw new ProcessInstanceDoesNotExistException(processInstanceId);
        }
        return processInstance;
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
     * fetches all processInstances for the given process definition from the
     * database. The returned list of process instances is sorted start date,
     * youngest first.
     */
    public List<ProcessInstance> findAllProcessInstances(Long definitionId) {
        return getHibernateTemplate().find("from ru.runa.bpm.graph.exe.ProcessInstance where processDefinition.id=? order by startDate desc",
                definitionId);
    }

    public void deleteInstance(final ProcessInstance processInstance) {
        log.debug("deleting process instance " + processInstance);
        getHibernateTemplate().execute(new HibernateCallback<Object>() {

            @Override
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                // delete the logs
                log.debug("deleting logs for process instance " + processInstance.getId());
                Query query = session.createQuery("from ru.runa.bpm.logging.log.ProcessLog l where l.token.processInstance=?");
                query.setParameter(0, processInstance);
                List<ProcessLog> logs = query.list();
                for (ProcessLog processLog : logs) {
                    session.delete(processLog);
                }
                session.flush();

                // jobs
                log.debug("deleting jobs for process instance " + processInstance.getId());
                query = session.createQuery("delete from ru.runa.bpm.job.Job job where job.processInstance = :processInstance");
                query.setEntity("processInstance", processInstance);
                query.executeUpdate();
                session.flush();

                // tasks
                query = session
                        .createQuery("from ru.runa.bpm.taskmgmt.exe.TaskInstance where taskMgmtInstance.processInstance=? or processInstance=?");
                query.setParameter(0, processInstance);
                query.setParameter(1, processInstance);
                List<TaskInstance> taskInstances = query.list();

                if ((taskInstances != null) && (!taskInstances.isEmpty())) {
                    log.debug("deleting tasks " + taskInstances + " for process instance " + processInstance.getId());
                    for (TaskInstance taskInstance : taskInstances) {
                        session.delete(taskInstance);
                        // query =
                        // session.getNamedQuery("delete from ru.runa.bpm.taskmgmt.exe.TaskInstance t where t.id = :id");
                        // query.setLong("id", taskInstance.getId());
                        // query.executeUpdate();
                    }
                }
                session.flush();

                // missed variables
                if (processInstance.getRootToken().getProcessInstance() == null) {
                    query = session
                            .createQuery("select v from ru.runa.bpm.context.exe.VariableInstance v where v.processInstance = :processInstance");
                    query.setEntity("processInstance", processInstance);
                    List<VariableInstance<?>> variableInstances = query.list();
                    log.debug("deleting variables " + variableInstances + " for process instance " + processInstance.getId());
                    for (VariableInstance<?> variableInstance : variableInstances) {
                        session.delete(variableInstance);
                    }
                    query = session.createQuery("select m from ru.runa.bpm.context.exe.TokenVariableMap m where m.token = :token");
                    query.setEntity("token", processInstance.getRootToken());
                    List<TokenVariableMap> TokenVariableMaps = query.list();
                    for (TokenVariableMap tokenVariableMap : TokenVariableMaps) {
                        session.delete(tokenVariableMap);
                    }
                    session.flush();
                }

                // delete passed transitions
                log.debug("deleting passed transitions for process instance " + processInstance.getId());
                session.createQuery("delete from ru.runa.bpm.graph.exe.PassedTransition where processInstance.id=" + processInstance.getId())
                        .executeUpdate();

                // delete started subprocesses
                log.debug("deleting started subprocesses for process instance " + processInstance.getId());
                session.createQuery("delete from ru.runa.bpm.graph.exe.StartedSubprocesses where processInstance.id=" + processInstance.getId())
                        .executeUpdate();

                // delete the tokens and subprocess instances
                log.debug("deleting subprocesses for process instance " + processInstance.getId());
                query = session
                        .createQuery("from ru.runa.bpm.graph.exe.ProcessInstance where superProcessToken!=null and superProcessToken.processInstance=? order by startDate desc");
                query.setParameter(0, processInstance);
                List<ProcessInstance> subProcessInstances = query.list();
                if (subProcessInstances == null || subProcessInstances.isEmpty()) {
                    log.debug("no subprocesses to delete for process instance " + processInstance.getId());
                } else {
                    for (ProcessInstance subProcessInstance : subProcessInstances) {
                        subProcessInstance.getSuperProcessToken().setSubProcessInstance(null);
                        subProcessInstance.setSuperProcessToken(null);
                        log.debug("deleting sub process " + subProcessInstance.getId());
                        deleteInstance(subProcessInstance);
                    }
                    session.flush();
                }

                // null out the parent process token
                Token superProcessToken = processInstance.getSuperProcessToken();
                if (superProcessToken != null) {
                    log.debug("nulling property subProcessInstance in superProcessToken " + superProcessToken.getId()
                            + " which is referencing the process instance " + processInstance.getId() + " which is being deleted");
                    superProcessToken.setSubProcessInstance(null);
                }

                // add the process instance
                log.debug("hibernate session delete for process instance " + processInstance.getId());
                session.delete(processInstance);
                session.flush();
                return null;
            }

        });
    }

    public List<Token> findAllActiveReceiveMessageTokens() {
        return getHibernateTemplate()
                .find("select token from ru.runa.bpm.graph.node.ReceiveMessage node, ru.runa.bpm.graph.exe.Token token where token.node = node and token.endDate is null");
    }
}
