package ru.runa.wf.dao.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.ReplicationMode;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.criterion.Expression;
import org.hibernate.proxy.HibernateProxy;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import ru.runa.InternalApplicationException;
import ru.runa.af.Actor;
import ru.runa.af.Executor;
import ru.runa.af.SecuredObject;
import ru.runa.af.dao.impl.ExecutorGroupRelation;
import ru.runa.af.dao.impl.HibernateSessionFactory;
import ru.runa.af.dao.impl.PermissionMapping;
import ru.runa.bpm.graph.def.ExecutableProcessDefinition;
import ru.runa.bpm.graph.def.Node;
import ru.runa.bpm.graph.exe.PassedTransition;
import ru.runa.bpm.graph.exe.ProcessInstance;
import ru.runa.bpm.graph.exe.StartedSubprocesses;
import ru.runa.bpm.graph.exe.Token;
import ru.runa.bpm.graph.node.MultiInstanceState;
import ru.runa.bpm.graph.node.ProcessState;
import ru.runa.bpm.logging.log.ProcessLog;
import ru.runa.commons.JBPMLazyLoaderHelper;
import ru.runa.wf.ProcessDefinition;
import ru.runa.wf.ProcessDefinitionInfo;
import ru.runa.wf.ProcessInstanceStub;
import ru.runa.wf.dao.ArchiveDAO;

public class ArchiveDAOHibernateImpl extends HibernateDaoSupport implements ArchiveDAO {
    private static final String EXT_ID_PROPERTY_NAME = "extId";
    private static final String TYPE_PROPERTY_NAME = "type";

    @Override
    public void initSessionTransferToArchive() {
        HibernateSessionFactory.openSession();
        HibernateSessionFactory.openArchiveSession();
    }

    @Override
    public void initSessionTransferFromArchive() {
        HibernateSessionFactory.openSession(true);
        HibernateSessionFactory.openArchiveSession();
    }

    public void close(boolean commitTransaction) {
        HibernateSessionFactory.closeSession(commitTransaction);
        HibernateSessionFactory.closeArchiveSession(commitTransaction);
    }

    @Override
    public void copyProcessInstanceInArchive(ProcessInstance instance) {
        Session archiveSession = HibernateSessionFactory.getArchiveSession();
        Session session = HibernateSessionFactory.getSession();
        try {
            copyProcessInstance(session, archiveSession, instance);
        } catch (HibernateException e) {
            throw new InternalApplicationException(e);
        }
    }

    @Override
    public void copyProcessDefinitionInArchive(Long processDefinitionId) {
        Session archiveSession = HibernateSessionFactory.getArchiveSession();
        Session session = HibernateSessionFactory.getSession();
        try {
            copyProcessDefinition(session, archiveSession, processDefinitionId, true);
        } catch (HibernateException e) {
            throw new InternalApplicationException(e);
        }
    }

    @Override
    public void copyProcessInstanceFromArchive(ProcessInstance instance) {
        Session archiveSession = HibernateSessionFactory.getArchiveSession();
        Session session = HibernateSessionFactory.getSession();
        try {
            copyProcessInstance(archiveSession, session, instance);
        } catch (HibernateException e) {
            throw new InternalApplicationException(e);
        }
    }

    @Override
    public void copyProcessDefinitionFromArchive(Long processDefinitionId) {
        Session archiveSession = HibernateSessionFactory.getArchiveSession();
        Session session = HibernateSessionFactory.getSession();
        try {
            copyProcessDefinition(archiveSession, session, processDefinitionId, true);
        } catch (HibernateException e) {
            throw new InternalApplicationException(e);
        }
    }

    @Override
    public ProcessInstance getProcessInstance(Long processInstanceId) {
        Session archiveSession = HibernateSessionFactory.getArchiveSession();
        Criteria criteria = archiveSession.createCriteria(ProcessInstance.class);
        criteria.add(Expression.eq("id", processInstanceId));
        return (ProcessInstance) criteria.uniqueResult();
    }

    @Override
    public Session getArchiveSession() {
        return HibernateSessionFactory.getArchiveSession();
    }

    /**
     * Copies process instance from one database to other. Also copies required
     * process definition. Set permission for system actors to copied objects.
     * 
     * @param srcSession
     *            Source hibernate session (Objects copied from this session).
     * @param targetSession
     *            Target hibernate session (Objects copied to this session).
     * @param instance
     *            Process instance to copy.
     */
    private void copyProcessInstance(Session srcSession, Session targetSession, ProcessInstance instance) {
        // copyProcessDefinition(srcSession, targetSession,
        // instance.getProcessDefinition().getId(), false);
        // ArchivingContext context = new ArchivingContext(srcSession,
        // targetSession, "xxx: spring", "xxx: spring");
        // Long processId = instance.getId();
        //
        // List<Executor> replicActors = new ArrayList<Executor>();
        // List<SecuredObject> replicSecuredObjects = new
        // ArrayList<SecuredObject>();
        // List<ExecutorGroupRelation> replicRelations = new
        // ArrayList<ExecutorGroupRelation>();
        // List<PermissionMapping> replicPermissionMappings = new
        // ArrayList<PermissionMapping>();
        //
        // loadProcessInstanceAndArchive(context, processId, replicActors,
        // replicSecuredObjects, replicRelations, replicPermissionMappings);
        // loadPassedTransitionsAndArchive(context, processId);
        // loadExecutorsAndArchive(context, replicActors, replicRelations);
        // loadPermissionMappingsAndArchive(context, replicSecuredObjects,
        // replicPermissionMappings);
        // loadLogsAndArchive(context, processId);
        // loadSubprocessesAndArchive(context, processId);
        //
        // Criteria criteria = srcSession.createCriteria(ProcessInstance.class);
        // criteria.add(Expression.eq("id", processId));
        // ProcessInstance instFull = (ProcessInstance)
        // JBPMLazyLoaderHelper.forceLoading(criteria.uniqueResult());
        // prepareReplication(context);
        // targetSession.replicate(instFull, ReplicationMode.OVERWRITE);
        // finishReplication(context);
    }

    /**
     * Load logs from source database and archive it.
     * 
     * @param context
     *            Archiving context.
     * @param processId
     *            Archiving process instance id.
     */
    private void loadLogsAndArchive(ArchivingContext context, Long processId) {
        List<ProcessLog> eqLogs = loadProcessLogs(context.getSrcSession(), processId);
        prepareReplication(context);
        for (ProcessLog log : eqLogs) {
            context.getTargetSession().replicate(log, ReplicationMode.OVERWRITE);
        }
        finishReplication(context);
    }

    /**
     * Loads permissions and secured objects from source database and archive
     * it.
     * 
     * @param context
     *            Archiving context.
     * @param replicSecuredObjects
     *            Secured objects to replicate into archive.
     * @param replicPermissionMappings
     *            Permission mappings into replicate to archive.
     */
    private void loadPermissionMappingsAndArchive(ArchivingContext context, List<SecuredObject> replicSecuredObjects,
            List<PermissionMapping> replicPermissionMappings) {
        prepareReplication(context);
        for (SecuredObject securObject : replicSecuredObjects) {
            context.getTargetSession().replicate(securObject, ReplicationMode.OVERWRITE);
        }
        context.getTargetSession().flush();
        for (PermissionMapping permissionMapping : replicPermissionMappings) {
            context.getTargetSession().replicate(permissionMapping, ReplicationMode.OVERWRITE);
        }
        finishReplication(context);
    }

    /**
     * Loads executors and group relations and archive it.
     * 
     * @param context
     *            Archiving context.
     * @param replicActors
     *            Executors to replicate into archive.
     * @param replicRelations
     *            Executor relations to replicate into archive
     */
    private void loadExecutorsAndArchive(ArchivingContext context, List<Executor> replicActors, List<ExecutorGroupRelation> replicRelations) {
        prepareReplication(context);
        Session targetSession = context.getTargetSession();
        for (Executor actor : replicActors) {
            if (actor instanceof Actor) {
                prepareActorReplication(context.getTargetSession(), (Actor) actor);
            } else {
                prepareExecutorReplication(context.getTargetSession(), actor);
            }
            targetSession.replicate(actor, ReplicationMode.OVERWRITE);
        }
        targetSession.flush();
        for (ExecutorGroupRelation relation : replicRelations) {
            prepareGroupRelationReplication(context.getTargetSession(), relation);
            targetSession.replicate(relation, ReplicationMode.OVERWRITE);
        }
        finishReplication(context);
    }

    /**
     * Prepare group relation for replicate. Remove old group relation with same
     * group/executor if it exist.
     * 
     * @param session
     *            Hibernate session for archive database.
     * @param relation
     *            Replicated group relation.
     */
    private void prepareGroupRelationReplication(Session session, ExecutorGroupRelation relation) {
        Criteria criteria = session.createCriteria(ExecutorGroupRelation.class);
        criteria.add(Expression.eq("group", relation.getGroup()));
        criteria.add(Expression.eq("executor", relation.getExecutor()));
        criteria.add(Expression.ne("id", relation.getId()));
        ExecutorGroupRelation existingOther = (ExecutorGroupRelation) criteria.uniqueResult();
        if (existingOther == null) {
            return;
        }
        session.createQuery("delete from " + ExecutorGroupRelation.class.getName() + " as rel where rel.id=" + existingOther.getId()).executeUpdate();
    }

    /**
     * Prepare actor replication. If archive contains actor with same code it
     * will be removed. Executor with same name will be renamed.
     * 
     * @param session
     *            Hibernate session for archive database.
     * @param actor
     *            Replicated actor.
     */
    private void prepareActorReplication(Session session, Actor actor) {
        Criteria criteria = session.createCriteria(Actor.class);
        criteria.add(Expression.eq("code", actor.getCode()));
        criteria.add(Expression.ne("id", actor.getId()));
        Executor existingOther = (Executor) criteria.uniqueResult();
        if (existingOther == null) {
            prepareExecutorReplication(session, actor);
            return;
        }
        session.createQuery("delete from " + ExecutorGroupRelation.class.getName() + " as rel where rel.executor.id=" + existingOther.getId())
                .executeUpdate();
        session.createQuery("delete from " + Executor.class.getName() + " as ex where ex.id=" + existingOther.getId()).executeUpdate();
        session.flush();
        prepareExecutorReplication(session, actor);
    }

    /**
     * Prepare executor replication. Executor with same name will be renamed.
     * 
     * @param session
     *            Hibernate session for archive database.
     * @param executor
     *            Replicated executor.
     */
    private void prepareExecutorReplication(Session session, Executor executor) {
        Criteria criteria = session.createCriteria(Executor.class);
        criteria.add(Expression.eq("name", executor.getName()));
        criteria.add(Expression.ne("id", executor.getId()));
        Executor existingOther = (Executor) criteria.uniqueResult();
        if (existingOther == null) {
            return;
        }
        for (int i = 0; i < 1000; ++i) {
            Criteria searchCriteria = session.createCriteria(Executor.class);
            String newName = executor.getName() + "_archive__" + i;
            searchCriteria.add(Expression.eq("name", newName));
            if (searchCriteria.uniqueResult() != null) {
                continue;
            }
            session.createQuery("update " + Executor.class.getName() + " set name='" + newName + "' where id=" + existingOther.getId())
                    .executeUpdate();
            session.flush();
            return;
        }
        throw new InternalApplicationException("Can't rename existing executor with name " + existingOther.getName());
    }

    /**
     * Loads passed transitions and archive it.
     * 
     * @param context
     *            Archiving context.
     * @param processId
     *            Archiving process instance id.
     */
    private void loadPassedTransitionsAndArchive(ArchivingContext context, Long processId) {
        Criteria passTrCriteria = context.getSrcSession().createCriteria(PassedTransition.class);
        passTrCriteria.add(Expression.eq("processInstance.id", processId));
        List<PassedTransition> passed = (List<PassedTransition>) JBPMLazyLoaderHelper.forceLoading(passTrCriteria.list());
        prepareReplication(context);
        for (PassedTransition passTr : passed) {
            context.getTargetSession().replicate(passTr, ReplicationMode.OVERWRITE);
        }
        finishReplication(context);
    }

    /**
     * Loads subprocesses and archive it.
     * 
     * @param context
     *            Archiving context.
     * @param processId
     *            Archiving process instance id.
     */
    private void loadSubprocessesAndArchive(ArchivingContext context, Long processId) {
        Criteria criteria = context.getSrcSession().createCriteria(StartedSubprocesses.class);
        criteria.add(Expression.eq("processInstance.id", processId));
        List<StartedSubprocesses> subprocesses = (List<StartedSubprocesses>) JBPMLazyLoaderHelper.forceLoading(criteria.list());
        if (subprocesses == null) {
            return;
        }
        for (StartedSubprocesses subProces : subprocesses) {
            copyProcessInstance(context.getSrcSession(), context.getTargetSession(), subProces.getSubProcessInstance());
        }
        prepareReplication(context);
        for (StartedSubprocesses subProcess : subprocesses) {
            context.getTargetSession().replicate(subProcess, ReplicationMode.OVERWRITE);
        }
        finishReplication(context);
    }

    /**
     * Loads process instance and swimlane information.
     * 
     * @param session
     *            Hibernate session to load from database.
     * @param context
     *            Archiving context.
     * @param processId
     *            Archiving process instance id.
     * @param replicActors
     *            Actors to replicate into archiving database.
     * @param replicSecuredObjects
     *            Secured objects to replicate into archiving database.
     * @param replicRelations
     *            Executor relations to replicate into archiving database.
     * @param replicPermissionMappings
     *            Permission mappings to replicate into archiving database.
     * @return Loaded process instance.
     */
    private void loadProcessInstanceAndArchive(ArchivingContext context, Long processId, List<Executor> replicActors,
            List<SecuredObject> replicSecuredObjects, List<ExecutorGroupRelation> replicRelations, List<PermissionMapping> replicPermissionMappings) {
        Criteria criteria = context.getSrcSession().createCriteria(ProcessInstance.class);
        criteria.add(Expression.eq("id", processId));
        ProcessInstance inst = (ProcessInstance) JBPMLazyLoaderHelper.forceLoading(criteria.uniqueResult());

        Criteria securedCriteria = context.getSrcSession().createCriteria(SecuredObject.class);
        securedCriteria.add(Expression.eq(EXT_ID_PROPERTY_NAME, new Long(processId)));
        securedCriteria.add(Expression.eq(TYPE_PROPERTY_NAME, new Integer(ProcessInstanceStub.class.getName().hashCode())));
        SecuredObject securedObject = (SecuredObject) JBPMLazyLoaderHelper.forceLoading(securedCriteria.uniqueResult());
        replicSecuredObjects.add(securedObject);

        replicPermissionMappings.addAll(context.loadPermissions(securedObject));
        loadSwimlaneInfo(context, inst, replicPermissionMappings, replicActors, replicSecuredObjects, replicRelations);

        inst.getRootToken().setSubProcessMultiInstance(null);
        inst.getRootToken().setSubProcessInstance(null);
        Token rootToken = inst.getRootToken();
        clearChild(rootToken);

        if (inst.getSuperProcessToken() != null) {
            inst.setSuperProcessToken(null);
        }

        prepareReplication(context);
        context.getTargetSession().replicate(inst, ReplicationMode.OVERWRITE);
        finishReplication(context);
    }

    /**
     * Preparing archiving process for objects replication. Clears target and
     * source sessions; flushes target session.
     * 
     * @param context
     *            Archiving process context.
     */
    private static void prepareReplication(ArchivingContext context) {
        context.getSrcSession().clear();
        context.getTargetSession().flush();
        context.getTargetSession().clear();
    }

    /**
     * Finishing objects replication.
     * 
     * @param context
     *            Archiving process context.
     */
    private static void finishReplication(ArchivingContext context) {
        context.getTargetSession().flush();
        context.getTargetSession().clear();
    }

    /**
     * Loads information for archiving from process swimlanes.
     * 
     * @param session
     *            Hibernate session to load data from database.
     * @param context
     *            Archiving context.
     * @param inst
     *            Process instance to archive.
     * @param addedPermissionMapping
     *            Permission mappings to replicate into archiving database.
     * @param addedActors
     *            Actors to replicate into archiving database.
     * @param addedSecuredObjects
     *            Secured objects to replicate into archiving database.
     * @param addedRelations
     *            Executor relations to replicate into archiving database.
     */
    private void loadSwimlaneInfo(ArchivingContext context, ProcessInstance inst, List<PermissionMapping> addedPermissionMapping,
            List<Executor> addedActors, List<SecuredObject> addedSecuredObjects, List<ExecutorGroupRelation> addedRelations) {
        // Session session = context.getSrcSession();
        // Map swimlaneMap = inst.getProcessDefinition().getSwimlanes();
        // for (Object swimlane : swimlaneMap.entrySet()) {
        // String swimlaneName = (String) ((Map.Entry) swimlane).getKey();
        // String swimlaneAssignment = (String)
        // inst.getContextInstance().getVariable(swimlaneName);
        // if (Strings.isNullOrEmpty(swimlaneAssignment)) {
        // continue;
        // }
        // if (swimlaneAssignment.charAt(0) != 'G') {
        // Long code = Long.parseLong(swimlaneAssignment);
        // Criteria addActorCriteria = session.createCriteria(Actor.class);
        // addActorCriteria.add(Expression.eq("code", new Long(code)));
        // Actor actor = (Actor) addActorCriteria.uniqueResult();
        // if (actor != null) {
        // addExecutorToArchive(context, addedPermissionMapping, addedActors,
        // addedSecuredObjects, actor);
        // Criteria relationGrCriteria =
        // session.createCriteria(ExecutorGroupRelation.class);
        // relationGrCriteria.add(Expression.eq("executor", actor));
        // List<ExecutorGroupRelation> relations = relationGrCriteria.list();
        // addedRelations.addAll(relations);
        // for (ExecutorGroupRelation relation : relations) {
        // addExecutorToArchive(context, addedPermissionMapping, addedActors,
        // addedSecuredObjects, relation.getGroup());
        // }
        // }
        // } else {
        // Long id = Long.parseLong(swimlaneAssignment.substring(1));
        // Criteria addGroupCriteria = session.createCriteria(Group.class);
        // addGroupCriteria.add(Expression.eq("id", id));
        // Group group = (Group) addGroupCriteria.uniqueResult();
        // if (group != null) {
        // addExecutorToArchive(context, addedPermissionMapping, addedActors,
        // addedSecuredObjects, group);
        // }
        // }
        // }
    }

    /**
     * Add executor and executor permission mappings to replicate into archive.
     * 
     * @param context
     *            Archiving context.
     * @param adddedPermissionMappings
     *            Permission mappings to replicate into archive.
     * @param addedActors
     *            Actors to replicate into archive.
     * @param addedSecuredObjects
     *            Secured objects to replicate into archive.
     * @param executor
     *            Executor, which must be replicated into archive.
     */
    private void addExecutorToArchive(ArchivingContext context, List<PermissionMapping> adddedPermissionMappings, List<Executor> addedActors,
            List<SecuredObject> addedSecuredObjects, Executor executor) {
        addedActors.add(executor);
        Criteria localUserSecuredCriteria = context.getSrcSession().createCriteria(SecuredObject.class);
        localUserSecuredCriteria.add(Expression.eq(EXT_ID_PROPERTY_NAME, new Long(executor.getId())));
        localUserSecuredCriteria.add(Expression.eq(TYPE_PROPERTY_NAME, new Integer(executor.getClass().getName().hashCode())));
        SecuredObject localSecuredObject = (SecuredObject) localUserSecuredCriteria.uniqueResult();
        localSecuredObject = (SecuredObject) JBPMLazyLoaderHelper.forceLoading(localSecuredObject);
        addedSecuredObjects.add(localSecuredObject);
        adddedPermissionMappings.addAll(context.loadPermissions(localSecuredObject));
    }

    /**
     * Loads process instance logs from JBPM_LOG or JRPM_LOG_NN tables.
     * 
     * @param session
     *            Hibernate session to load process logs.
     * @param processId
     *            Process instance id for loading logs.
     * @return Loaded logs or empty collection if loading failed.
     */
    private List<ProcessLog> loadProcessLogs(Session session, Long processId) {
        Set<ProcessLog> logs = tryLoadProcessLogsFromTable(session, processId, "JBPM_LOG");
        if (logs.isEmpty()) {
            logs = tryLoadProcessLogsFromTable(session, processId, "JBPM_LOG_" + ((processId / 1000) + 1));
        }
        List<ProcessLog> eqLogs = new ArrayList<ProcessLog>();
        eqLogs.addAll(logs);
        Collections.sort(eqLogs, new LogsComparator());
        return eqLogs;
    }

    /**
     * Try's to loads logs for process instance from table.
     * 
     * @param session
     *            Hibernate session to load process logs.
     * @param processId
     *            Process instance id for loading logs.
     * @param tblName
     *            Table name, from which logs is loaded.
     * @return Loaded logs or empty collection if loading failed.
     */
    private Set<ProcessLog> tryLoadProcessLogsFromTable(Session session, Long processId, String tblName) {
        try {
            StringBuilder queryString = new StringBuilder();
            queryString.append("select ").append(tblName).append(".* from ").append(tblName).append(" left join JBPM_TOKEN on ").append(tblName);
            queryString.append(".TOKEN_=JBPM_TOKEN.ID_ left join JBPM_PROCESSINSTANCE on JBPM_TOKEN.PROCESSINSTANCE_=JBPM_PROCESSINSTANCE.ID_ ");
            queryString.append("where JBPM_PROCESSINSTANCE.ID_=").append(processId);
            SQLQuery query = session.createSQLQuery(queryString.toString());
            query.addEntity(ProcessLog.class);
            List<ProcessLog> loaded = query.list();
            Set<ProcessLog> logs = new HashSet<ProcessLog>();
            for (ProcessLog log : loaded) {
                logs.add((ProcessLog) JBPMLazyLoaderHelper.forceLoading(log));
            }
            return logs;
        } catch (Throwable e) {
            return new HashSet<ProcessLog>();
        }
    }

    /**
     * Copy process definition to archive.
     * 
     * @param srcSession
     *            Source hibernate session.
     * @param targetSession
     *            Target hibernate session.
     * @param processDefinitionId
     *            Process definition id.
     * @param includeSubProc
     *            Flag, equals true, if subprocess (used in multiinstance or
     *            subprocess elements) must be also copied; false otherwise.
     */
    private void copyProcessDefinition(Session srcSession, Session targetSession, Long processDefinitionId, boolean includeSubProc) {
        ArchivingContext context = new ArchivingContext(srcSession, targetSession, "xxx: spring", "xxx: spring");
        ExecutableProcessDefinition def = loadProcessDefintionAndArchive(context, processDefinitionId);
        loadDefinitionPermissionsAndArchive(context, def);
        if (includeSubProc) {
            copyDefinitionSubprocesses(context, def);
        }
    }

    /**
     * Copies process definitions, used in process (in subprocess or
     * multiinstance elements) to archive.
     * 
     * @param context
     *            Archiving process context.
     * @param def
     *            Archiving process definition.
     */
    private void copyDefinitionSubprocesses(ArchivingContext context, ExecutableProcessDefinition def) {
        // List<ExecutableProcessDefinition> subDefinitions = new
        // ArrayList<ExecutableProcessDefinition>();
        // Set<String> subProcessDefinitionNames =
        // getDefinitionSubprocesses(def.getNodes());
        // for (String subProcessDefinitionName : subProcessDefinitionNames) {
        // Query query = context.getSrcSession().createQuery(
        // "select pd from ArchievedProcessDefinition as pd where pd.name = :name order by pd.version desc");
        // query.setParameter("name", subProcessDefinitionName);
        // List<ExecutableProcessDefinition> definitions = query.list();
        // if (definitions != null && definitions.size() > 0) {
        // ExecutableProcessDefinition processDefinition = definitions.get(0);
        // subDefinitions.add(processDefinition);
        // }
        // }

        // for (ProcessDefinitionGraphImpl subDefinition : subDefinitions) {
        // copyProcessDefinition(context.getSrcSession(),
        // context.getTargetSession(), subDefinition.getId(), true);
        // }
    }

    /**
     * Creates permission for archived process.
     * 
     * @param context
     *            Archiving process context.
     * @param def
     *            Archiving process definition.
     */
    private void loadDefinitionPermissionsAndArchive(ArchivingContext context, ExecutableProcessDefinition def) {
        Criteria securedCriteria = context.getSrcSession().createCriteria(SecuredObject.class);
        securedCriteria.add(Expression.eq(EXT_ID_PROPERTY_NAME, new Long(def.getName().hashCode())));
        securedCriteria.add(Expression.eq(TYPE_PROPERTY_NAME, new Integer(ProcessDefinition.class.getName().hashCode())));
        SecuredObject securedObject = (SecuredObject) JBPMLazyLoaderHelper.forceLoading(securedCriteria.uniqueResult());
        List<PermissionMapping> permissionMappings = context.loadPermissions(securedObject);
        prepareReplication(context);
        context.getTargetSession().replicate(securedObject, ReplicationMode.OVERWRITE);
        context.getTargetSession().flush();
        for (PermissionMapping mapping : permissionMappings) {
            context.getTargetSession().replicate(mapping, ReplicationMode.OVERWRITE);
        }
        finishReplication(context);
    }

    /**
     * Loads process definition and archive it.
     * 
     * @param context
     *            Archiving process context.
     * @param def
     *            Archiving process definition.
     * @return Returns archived process definition.
     */
    private ExecutableProcessDefinition loadProcessDefintionAndArchive(ArchivingContext context, Long processDefinitionId) {
        Criteria criteria = context.getSrcSession().createCriteria(ExecutableProcessDefinition.class);
        criteria.add(Expression.eq("id", processDefinitionId));
        ExecutableProcessDefinition def = (ExecutableProcessDefinition) JBPMLazyLoaderHelper.forceLoading(criteria.uniqueResult());
        ProcessDefinitionInfo pInfo = (ProcessDefinitionInfo) context.getSrcSession()
                .createQuery("select pInfo from ru.runa.wf.ProcessDefinitionInfo as pInfo where pInfo.processName='" + def.getName() + "'")
                .uniqueResult();
        prepareReplication(context);
        context.getTargetSession().replicate(def, ReplicationMode.OVERWRITE);
        context.getTargetSession().replicate(pInfo, ReplicationMode.OVERWRITE);
        finishReplication(context);
        return def;
    }

    /**
     * Check all nodes and returns process definitions names, used in process
     * (subprocess and multiinstance elements).
     * 
     * @param definitionNodes
     *            List of process definition nodes.
     * @return Returns process definitions names.
     */
    private Set<String> getDefinitionSubprocesses(List<Node> definitionNodes) {
        HashSet<String> processes = new HashSet<String>();
        for (Node node : definitionNodes) {
            if (node.getNodeType() == Node.NodeType.SubProcess) {
                if (node instanceof HibernateProxy) {
                    node = (Node) ((HibernateProxy) node).getHibernateLazyInitializer().getImplementation();
                }
                String subDefinitionName = ((ProcessState) node).getSubProcessName();
                processes.add(subDefinitionName);
            }
            if (node.getNodeType() == Node.NodeType.MultiInstance) {
                if (node instanceof HibernateProxy) {
                    node = (Node) ((HibernateProxy) node).getHibernateLazyInitializer().getImplementation();
                }
                String subDefinitionName = ((MultiInstanceState) node).getSubProcessName();
                processes.add(subDefinitionName);
            }
        }
        return processes;
    }

    private void clearChild(Token rootToken) {
        if (rootToken.getChildren() == null) {
            return;
        }
        for (java.util.Iterator<Entry<String, Token>> iterator = rootToken.getChildren().entrySet().iterator(); iterator.hasNext();) {
            Token token = iterator.next().getValue();
            clearChild(token);
            token.setProcessInstance(null);
            token.setSubProcessInstance(null);
            token.setSubProcessMultiInstance(null);
        }
    }

    static class LogsComparator implements Comparator<ProcessLog> {

        @Override
        public int compare(ProcessLog o1, ProcessLog o2) {
            return new Long(o1.getId()).compareTo(new Long(o2.getId()));
        }
    }
}
