package ru.runa.wf.dao.impl;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import ru.runa.bpm.graph.exe.ProcessInstance;
import ru.runa.bpm.graph.exe.StartedSubprocesses;
import ru.runa.bpm.graph.exe.Token;
import ru.runa.bpm.svc.save.SaveOperation;
import ru.runa.bpm.taskmgmt.exe.TaskInstance;
import ru.runa.wf.ProcessDefinitionInfo;
import ru.runa.wf.dao.TmpDAO;

public class TmpDAOImpl extends HibernateDaoSupport implements TmpDAO {

    @Override
    public <T> T get(Class<T> clazz, Long id) {
        return getHibernateTemplate().get(clazz, id);
    }

    @Override
    public void save(Object entity) {
        getHibernateTemplate().save(entity);
    }

    @Override
    public void delete(Object entity) {
        getHibernateTemplate().delete(entity);
    }

    @Override
    public void updateBPDefinitionInfo(final String definitionName, final List<String> processType) {
        getHibernateTemplate().execute(new HibernateCallback<ProcessDefinitionInfo>() {

            @Override
            public ProcessDefinitionInfo doInHibernate(Session session) throws HibernateException, SQLException {
                ProcessDefinitionInfo pInfo = (ProcessDefinitionInfo) session.createQuery(
                        "select pInfo from ru.runa.wf.ProcessDefinitionInfo as pInfo where pInfo.processName='" + definitionName + "'")
                        .uniqueResult();
                String[] types = processType.toArray(new String[processType.size()]);
                if (pInfo == null) {
                    pInfo = new ProcessDefinitionInfo(definitionName, types);
                } else {
                    pInfo.setProcessType(types);
                }
                session.saveOrUpdate(pInfo);
                return pInfo;
            }
        });
    }

    @Override
    public void deleteBPDefinitionInfo(final String definitionName) {
        getHibernateTemplate().execute(new HibernateCallback<Object>() {

            @Override
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                ProcessDefinitionInfo pInfo = (ProcessDefinitionInfo) session.createQuery(
                        "select pInfo from ru.runa.wf.ProcessDefinitionInfo as pInfo where pInfo.processName='" + definitionName + "'")
                        .uniqueResult();
                session.delete(pInfo);
                return null;
            }
        });
    }

    private List<SaveOperation> bpInstanceSaveOperations;

    @Required
    public void setBpInstanceSaveOperations(List<SaveOperation> bpInstanceSaveOperations) {
        this.bpInstanceSaveOperations = bpInstanceSaveOperations;
    }

    @Override
    public void saveProcessInstance(ProcessInstance instance) {
        // log.debug("executing custom save operations");
        for (SaveOperation saveOperation : bpInstanceSaveOperations) {
            saveOperation.save(getHibernateTemplate(), instance);
        }
    }

    @Override
    public List<TaskInstance> getProcessInstanceTasks(Long bpInstanceId) {
        return getHibernateTemplate().find("from ru.runa.bpm.taskmgmt.exe.TaskInstance where processInstance.id = ?", bpInstanceId);
    }

    @Override
    public List<Token> getProcessInstanceTokens(Long bpInstanceId) {
        return getHibernateTemplate().find("from ru.runa.bpm.graph.exe.Token where processInstance.id = ?", bpInstanceId);
    }

    @Override
    public List<TaskInstance> getTokenWithSameSwimlane(TaskInstance taskInstance) {
        return getHibernateTemplate().find(
                "from ru.runa.bpm.taskmgmt.exe.TaskInstance t where t.processInstance = ? and t.task.swimlane = ? and t.endDate is null",
                taskInstance.getProcessInstance(), taskInstance.getTask().getSwimlane());
    }

    @Override
    public List<ProcessInstance> getProcessInstancesForDefinitionName(String definitionName) {
        return getHibernateTemplate().find("from ru.runa.bpm.graph.exe.ProcessInstance where processDefinition.name = ?", definitionName);
    }

    @Override
    public List<ProcessInstance> getProcessInstancesForDefinitionVersion(String definitionName, Long version) {
        return getHibernateTemplate().find(
                "select p from ru.runa.bpm.graph.exe.ProcessInstance p where p.processDefinition.name = ? and p.processDefinition.version = ?",
                definitionName, version);
    }

    @Override
    public List<ProcessInstance> getProcessInstanceByDate(final Date startDate, final Date endDate, final boolean isFinishedOnly) {
        return getHibernateTemplate().executeFind(new HibernateCallback<List<ProcessInstance>>() {

            @Override
            public List<ProcessInstance> doInHibernate(Session session) throws HibernateException, SQLException {
                StringBuffer strQuery = new StringBuffer();
                strQuery.append("select p from ru.runa.bpm.graph.exe.ProcessInstance p where");
                if (startDate != null) {
                    strQuery.append(" p.startDate < :startDate");
                }
                if (endDate != null) {
                    if (startDate != null) {
                        strQuery.append(" and ");
                    }
                    strQuery.append(" p.endDate < :endDate");
                }
                if (startDate != null || endDate != null) {
                    if (isFinishedOnly) {
                        strQuery.append(" and p.endDate is not null ");
                    }
                } else {
                    return new ArrayList<ProcessInstance>();
                }

                Query query = session.createQuery(strQuery.toString());
                if (startDate != null) {
                    query.setParameter("startDate", startDate, Hibernate.TIMESTAMP);
                }
                if (endDate != null) {
                    query.setParameter("endDate", endDate, Hibernate.TIMESTAMP);
                }
                return query.list();
            }
        });
    }

    @Override
    public List<ProcessInstance> getProcessInstanceByStartDateInterval(final Date startDateFrom, final Date startDateTill,
            final boolean isFinishedOnly) {
        return getHibernateTemplate().executeFind(new HibernateCallback<List<ProcessInstance>>() {

            @Override
            public List<ProcessInstance> doInHibernate(Session session) throws HibernateException, SQLException {
                if ((startDateFrom == null) || (startDateTill == null)) {
                    return new ArrayList<ProcessInstance>();
                }

                StringBuffer strQuery = new StringBuffer();
                strQuery.append("select p from ru.runa.bpm.graph.exe.ProcessInstance p where");
                strQuery.append(" p.startDate > :startDateFrom");
                strQuery.append(" and ");
                strQuery.append(" p.startDate < :startDateTill");
                if (isFinishedOnly) {
                    strQuery.append(" and p.endDate is not null ");
                }
                Query query = session.createQuery(strQuery.toString());
                query.setParameter("startDateFrom", startDateFrom, Hibernate.TIMESTAMP);
                query.setParameter("startDateTill", startDateTill, Hibernate.TIMESTAMP);
                return query.list();
            }
        });
    }

    @Override
    public List<ProcessInstance> getProcessInstanceByIdInterval(final Long idFrom, final Long idTill, final boolean isFinishedOnly) {
        return getHibernateTemplate().executeFind(new HibernateCallback<List<ProcessInstance>>() {

            @Override
            public List<ProcessInstance> doInHibernate(Session session) throws HibernateException, SQLException {
                if (idFrom == null || idTill == null) {
                    return new ArrayList<ProcessInstance>();
                }
                StringBuffer strQuery = new StringBuffer();
                strQuery.append("select p from ru.runa.bpm.graph.exe.ProcessInstance p where");
                strQuery.append(" p.id >= :idFrom");
                strQuery.append(" and ");
                strQuery.append(" p.id <= :idTill");
                if (isFinishedOnly) {
                    strQuery.append(" and p.endDate is not null ");
                }
                Query query = session.createQuery(strQuery.toString());
                query.setParameter("idFrom", idFrom, Hibernate.LONG);
                query.setParameter("idTill", idTill, Hibernate.LONG);
                return query.list();
            }
        });
    }

    @Override
    public List<StartedSubprocesses> getRootSubprocesses(Long bpInstanceId) {
        return getHibernateTemplate().find("select s from ru.runa.bpm.graph.exe.StartedSubprocesses as s where s.subProcessInstance.id = ?",
                bpInstanceId);
    }

    @Override
    public List<StartedSubprocesses> getSubprocesses(Long bpInstanceId) {
        return getHibernateTemplate().find("select s from ru.runa.bpm.graph.exe.StartedSubprocesses as s where s.processInstance.id = ?",
                bpInstanceId);
    }

}
