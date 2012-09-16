/*
 * This file is part of the RUNA WFE project.
 * 
 * This program is free software; you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation; version 2.1 
 * of the License. 
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the 
 * GNU Lesser General Public License for more details. 
 * 
 * You should have received a copy of the GNU Lesser General Public License 
 * along with this program; if not, write to the Free Software 
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package ru.runa.wf.logic;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.security.auth.Subject;

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.Rule;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Expression;
import org.springframework.beans.factory.annotation.Autowired;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import ru.runa.InternalApplicationException;
import ru.runa.af.ASystem;
import ru.runa.af.Actor;
import ru.runa.af.ActorPermission;
import ru.runa.af.AuthenticationException;
import ru.runa.af.AuthorizationException;
import ru.runa.af.Executor;
import ru.runa.af.ExecutorOpenTask;
import ru.runa.af.ExecutorOutOfDateException;
import ru.runa.af.ExecutorPermission;
import ru.runa.af.Group;
import ru.runa.af.Identifiable;
import ru.runa.af.Permission;
import ru.runa.af.SecuredObject;
import ru.runa.af.SecuredObjectAlreadyExistsException;
import ru.runa.af.SecuredObjectOutOfDateException;
import ru.runa.af.Substitution;
import ru.runa.af.SubstitutionCriteria;
import ru.runa.af.SystemExecutors;
import ru.runa.af.SystemPermission;
import ru.runa.af.TerminatorSubstitution;
import ru.runa.af.UnapplicablePermissionException;
import ru.runa.af.authenticaion.SubjectPrincipalsHelper;
import ru.runa.af.log.SystemLog;
import ru.runa.af.logic.AuthenticationLogic;
import ru.runa.af.presentation.AFProfileStrategy;
import ru.runa.af.presentation.BatchPresentation;
import ru.runa.af.presentation.BatchPresentationHibernateCompiler;
import ru.runa.bpm.context.exe.VariableInstance;
import ru.runa.bpm.context.log.VariableCreateLog;
import ru.runa.bpm.context.log.VariableUpdateLog;
import ru.runa.bpm.db.JobDAO;
import ru.runa.bpm.db.LoggingDAO;
import ru.runa.bpm.graph.def.Action;
import ru.runa.bpm.graph.def.ExecutableProcessDefinition;
import ru.runa.bpm.graph.def.Transition;
import ru.runa.bpm.graph.exe.ExecutionContext;
import ru.runa.bpm.graph.exe.PassedTransition;
import ru.runa.bpm.graph.exe.ProcessInstance;
import ru.runa.bpm.graph.exe.StartedSubprocesses;
import ru.runa.bpm.graph.exe.Token;
import ru.runa.bpm.graph.log.NodeLog;
import ru.runa.bpm.instantiation.Delegation;
import ru.runa.bpm.job.Job;
import ru.runa.bpm.logging.log.ProcessLog;
import ru.runa.bpm.par.FileDataProvider;
import ru.runa.bpm.taskmgmt.def.AssignmentHandler;
import ru.runa.bpm.taskmgmt.def.Swimlane;
import ru.runa.bpm.taskmgmt.exe.TaskInstance;
import ru.runa.bpm.taskmgmt.log.TaskEndLog;
import ru.runa.commons.InfoHolder;
import ru.runa.commons.JBPMLazyLoaderHelper;
import ru.runa.commons.SubjectPrincipalHolder;
import ru.runa.commons.email.EmailResources;
import ru.runa.commons.email.EmailSender;
import ru.runa.commons.hibernate.HibernateSessionFactory;
import ru.runa.wf.ActorStub;
import ru.runa.wf.LogPresentationBuilder;
import ru.runa.wf.ProcessDefinition;
import ru.runa.wf.ProcessDefinitionDoesNotExistException;
import ru.runa.wf.ProcessDefinitionFileDoesNotExistException;
import ru.runa.wf.ProcessDefinitionPermission;
import ru.runa.wf.ProcessInstanceDoesNotExistException;
import ru.runa.wf.ProcessInstancePermission;
import ru.runa.wf.ProcessInstanceStub;
import ru.runa.wf.SwimlaneStub;
import ru.runa.wf.TaskAlreadyAcceptedException;
import ru.runa.wf.TaskDoesNotExistException;
import ru.runa.wf.TaskStub;
import ru.runa.wf.TaskStubFactory;
import ru.runa.wf.caches.TaskCache;
import ru.runa.wf.caches.TaskCacheCtrl;
import ru.runa.wf.form.Interaction;
import ru.runa.wf.form.VariablesValidationException;
import ru.runa.wf.graph.GraphConverter;
import ru.runa.wf.graph.GraphElementPresentation;
import ru.runa.wf.graph.StartedSubprocessesVisitor;

import com.google.common.base.Objects;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Created on 15.03.2005
 * 
 */
public class JbpmExecutionLogic extends JbpmCommonLogic {
    private static final Log log = LogFactory.getLog(JbpmExecutionLogic.class);
    private static final String EmailPropertyFile = "emailTaskNotifier";
    protected TaskCache taskCache = TaskCacheCtrl.getInstance();
    @Autowired
    private JobDAO jobDAO;
    @Autowired
    private LoggingDAO loggingDAO;
    @Autowired
    private TaskStubFactory taskStubFactory;
    @Autowired
    private AuthenticationLogic authenticationLogic;

    public List<TaskInstance> getProcessInstanceTasks(Subject subject, Long processInstanceId) throws AuthenticationException {
        // TODO lazy loading
        List<TaskInstance> result = tmpDAO.getProcessInstanceTasks(processInstanceId);
        for (TaskInstance task : result) {
            JBPMLazyLoaderHelper.forceLoading(task);
        }
        return tmpDAO.getProcessInstanceTasks(processInstanceId);
    }

    public Date getLastChangeDate() {
        return taskCache.getLastChangeDate();
    }

    public void cancelProcessInstance(Subject subject, Long processInstanceId) throws AuthorizationException, AuthenticationException,
            ProcessInstanceDoesNotExistException {
        Actor actor = SubjectPrincipalsHelper.getActor(subject);
        ProcessInstance processInstance = processExecutionDAO.getInstanceNotNull(processInstanceId);
        ExecutableProcessDefinition processDefinition = getDefinition(processInstance);
        ExecutionContext executionContext = new ExecutionContext(processDefinition, processInstance);
        ProcessInstanceStub instanceStub = createProcessInstanceStub(processInstance);
        checkPermissionAllowed(subject, instanceStub, ProcessInstancePermission.CANCEL_INSTANCE);
        processInstance.end(executionContext);
        for (Object taskObj : taskDAO.findTaskInstancesByProcessInstance(processInstance)) {
            TaskInstance task = (TaskInstance) taskObj;
            task.setActorId(executionContext, Long.toString(actor.getCode()));
            executorDAO.removeOpenTask(task);
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Process instance ").append(instanceStub).append(" was cancelled by ").append(SubjectPrincipalsHelper.getActor(subject).getName());
        log.info(sb.toString());
        tmpDAO.saveProcessInstance(processInstance);
    }

    public List<Job> getActiveJobs(Subject subject, Long taskInstanceId) throws AuthenticationException {
        return jobDAO.findJobsByToken(tmpDAO.get(TaskInstance.class, taskInstanceId).getToken());
    }

    public void completeTask(Subject subject, Long taskId, String taskName, Long actorId, Map<String, Object> variables, String transitionName)
            throws AuthorizationException, AuthenticationException, TaskDoesNotExistException, ExecutorOutOfDateException,
            VariablesValidationException {
        SubjectPrincipalHolder.actorIds.set(SubjectPrincipalsHelper.getActor(subject).getCode().toString());
        TaskInstance taskInstance = getTaskWithName(taskId, taskName);
        ExecutableProcessDefinition processDefinition = getDefinition(taskInstance);
        ExecutionContext executionContext = new ExecutionContext(processDefinition, taskInstance.getToken());
        if (variables == null) {
            variables = new HashMap<String, Object>();
        }
        Actor actor = executorDAO.getActor(actorId);
        checkCanParticipate(subject, taskInstance, actor);
        actor = checkPermissionsOnExecutor(subject, actor, ActorPermission.READ);
        String swimlaneName = taskInstance.getSwimlaneInstance().getName();
        String oldExecutorId = taskInstance.getSwimlaneInstance().getAssignedActorId();
        // FIXME dirty hack to assign Bayens swimlane variable and swimlane
        // actorId
        String targetActorStringId = String.valueOf(actor.getCode());
        List<TaskInstance> result = tmpDAO.getTokenWithSameSwimlane(taskInstance);
        // to register substitutor in logs
        taskInstance.setActorId(executionContext, String.valueOf(SubjectPrincipalsHelper.getActor(subject).getCode()), false);
        for (TaskInstance instance : result) {
            instance.setActorId(executionContext, targetActorStringId);
        }
        removeTemporaryGroup(oldExecutorId);
        variables.put(swimlaneName, targetActorStringId);
        String stateName = taskInstance.getName();
        Map<String, Object> variablesToValidate = new HashMap<String, Object>();
        variablesToValidate.put("transition", transitionName);
        variablesToValidate.putAll(variables);
        validateVariables(processDefinition, taskName, variablesToValidate);
        taskInstance.setVariables(executionContext, variables);
        if (transitionName == null) {
            List<Transition> transitions = taskInstance.getTask().getNode().getLeavingTransitions();
            for (Transition transition : transitions) {
                if (transition.getName() != null && !transition.getName().equals("time-out-transition")) {
                    transitionName = transition.getName();
                    break;
                }
            }
        }
        if (transitionName != null) {
            taskInstance.end(executionContext, transitionName);
        } else {
            taskInstance.end(executionContext);
        }
        executorDAO.removeOpenTask(taskInstance);
        StringBuilder sb = new StringBuilder();
        sb.append("Task ").append(stateName).append(" was done by ").append(SubjectPrincipalsHelper.getActor(subject).getName());
        sb.append(" in process instance of ").append(taskInstance.getProcessInstance());
        log.info(sb.toString());
        try {
            sendEmail(taskInstance.getProcessInstance().getTaskMgmtInstance().getUnfinishedTasks(taskInstance.getToken()));
        } catch (Exception e) {
            log.info("Cant't send email", e);
        }
        tmpDAO.saveProcessInstance(taskInstance.getProcessInstance());
    }

    public int getAllProcessInstanceStubsCount(Subject subject, BatchPresentation batchPresentation) throws InternalApplicationException,
            AuthenticationException {
        return getPersistentObjectCount(subject, batchPresentation, ProcessInstancePermission.READ, PROCESS_INSTANCE_STUB_CLASSES);
    }

    private static final Class<? extends Identifiable>[] PROCESS_INSTANCE_STUB_CLASSES = new Class[] { ProcessInstanceStub.class };

    public List<ProcessInstanceStub> getProcessInstanceStubs(Subject subject, BatchPresentation batchPresentation) throws AuthenticationException {
        // Uncomment for WFDEMO (default ordering in process instances is
        // decrase time start)
        /*
         * if(batchPresentation.isDefault()){
         * batchPresentation.setFieldsToSort(new int[]{2}, new
         * boolean[]{false}); }
         */
        List<ProcessInstance> jbpmProcessInstancesList = getPersistentObjects(subject, batchPresentation, ProcessInstancePermission.READ,
                PROCESS_INSTANCE_STUB_CLASSES, true);
        return getProcessInstaceStubs(jbpmProcessInstancesList);
    }

    public List<ProcessInstanceStub> getProcessInstanceStubsForDefinitionName(Subject subject, String processDefinitionName)
            throws AuthenticationException {
        List<ProcessInstance> jbpmProcessInstancesList = tmpDAO.getProcessInstancesForDefinitionName(processDefinitionName);
        return filterInstances(subject, jbpmProcessInstancesList);
    }

    public ProcessDefinition getProcessDefinition(Subject subject, Long taskId) throws AuthenticationException, AuthorizationException,
            TaskDoesNotExistException {
        ExecutableProcessDefinition processDefinition = getDefinition(taskDAO.getTaskInstanceNotNull(taskId));
        ProcessDefinition definitionStub = createProcessDefinitionStub(processDefinition);
        checkPermissionAllowed(subject, definitionStub, Permission.READ);
        return definitionStub;
    }

    public ProcessInstanceStub getProcessInstanceStub(Subject subject, Long id) throws AuthorizationException, AuthenticationException,
            ProcessInstanceDoesNotExistException {
        ProcessInstance processInstance = processExecutionDAO.getInstanceNotNull(id);
        ProcessInstanceStub result = createProcessInstanceStub(processInstance);
        checkPermissionAllowed(subject, result, Permission.READ);
        return result;
    }

    public ProcessInstanceStub getSuperProcessInstanceStub(Subject subject, Long id) throws AuthenticationException,
            ProcessInstanceDoesNotExistException {
        ProcessInstance processInstance = processExecutionDAO.getInstanceNotNull(id);
        Token superToken = processInstance.getSuperProcessToken();
        if (superToken == null) {
            return null;
        }
        ProcessInstance parentProcessInstance = superToken.getProcessInstance();
        return createProcessInstanceStub(parentProcessInstance);
    }

    private void sendEmail(Collection<TaskInstance> taskInstances) throws Exception {
        EmailResources resources = new EmailResources(EmailPropertyFile);
        if (resources.isEmailNotifySending()) {
            Exception ex = null;
            Iterator<TaskInstance> iter = taskInstances.iterator();
            while (iter.hasNext()) {
                TaskInstance taskInstance = iter.next();
                Map<String, Object> tokenVariables = null;
                try {
                    tokenVariables = taskInstance.getVariables();
                    Set<Object> param = new HashSet<Object>();
                    param.add(taskInstance.getAssignedActorId());
                    param.add(taskInstance);
                    tokenVariables.put("__currentTokenExecutor__", param);
                    Class<? extends EmailSender> clazz = (Class<? extends EmailSender>) Class.forName(resources.getEmailSenderClassName());
                    EmailSender sender = clazz.newInstance();

                    Subject subjectMail = authenticationLogic.authenticate(resources.getWFUser(), resources.getWFUserPass());
                    sender.sendMessage(resources, subjectMail, tokenVariables, taskInstance.getId(), taskInstance.getTask().getName());
                } catch (Exception e) {
                    ex = e;
                } finally {
                    if (tokenVariables != null) {
                        tokenVariables.remove("__currentTokenExecutor__");
                    }
                }
            }
            if (ex != null) {
                throw ex;
            }
            log.info("Emails successfully sent.");
        }
    }

    private Set<Executor> getActorsToGetTasks(Actor actor, boolean inactiveGroup) throws InternalApplicationException, ExecutorOutOfDateException {
        Set<Executor> executors = new HashSet<Executor>();
        executors.add(actor);
        Set<Group> upperGroups = executorDAO.getExecutorParentsAll(actor);
        if (!inactiveGroup) {
            executors.addAll(upperGroups);
        } else {
            for (Group gr : upperGroups) {
                if (!hasActiveActorInGroup(gr)) {
                    executors.add(gr);
                }
            }
        }
        return executors;
    }

    private Set<Executor> getSubstitutableExecutors(Actor actor) throws ExecutorOutOfDateException {
        Set<Executor> executorsToGetTasks = getActorsToGetTasks(actor, false);
        Set<Executor> executorsToGetTasksSub = new HashSet<Executor>();
        executorsToGetTasksSub.addAll(executorsToGetTasks);
        for (Long substitutedActor : substitutionLogic.getSubstituted(actor)) {
            executorsToGetTasksSub.addAll(getActorsToGetTasks(executorDAO.getActor(substitutedActor), true));
        }
        return executorsToGetTasksSub;
    }

    Executor getAssignedExecutor(String assignedValue) throws ExecutorOutOfDateException {
        if (assignedValue == null || assignedValue.equals(InfoHolder.UNASSIGNED_SWIMLANE_VALUE)) {
            return null;
        }
        if (assignedValue.charAt(0) == 'G') {
            return executorDAO.getGroup(Long.parseLong(assignedValue.substring(1)));
        } else {
            return executorDAO.getActorByCode(Long.parseLong(assignedValue));
        }
    }

    private boolean isCanSubToken(TaskInstance token, Actor asActor, Actor substitutorActor) throws ExecutorOutOfDateException {
        TreeMap<Substitution, Set<Long>> mapOfSubstitionRule = substitutionLogic.getSubstitutors(asActor);
        for (Map.Entry<Substitution, Set<Long>> substitutionRule : mapOfSubstitionRule.entrySet()) {
            Substitution substitution = substitutionRule.getKey();
            SubstitutionCriteria criteria = substitution.getCriteria();
            Set<Long> substitutors = substitutionRule.getValue();
            if (substitution instanceof TerminatorSubstitution) {
                if (criteria == null || criteria.isSatisfied(token, asActor, substitutorActor)) {
                    return false;
                }
                continue;
            }
            boolean canISubstitute = false;
            boolean substitutionApplies = false;
            for (Long actorId : substitutors) {
                Actor actor = executorDAO.getActor(actorId);
                if (actor.isActive() && (criteria == null || criteria.isSatisfied(token, asActor, actor))) {
                    substitutionApplies = true;
                }
                if (Objects.equal(actor.getCode(), substitutorActor.getCode())) {
                    canISubstitute = true;
                }
            }
            if (!substitutionApplies) {
                continue;
            }
            return canISubstitute;
        }
        return false;
    }

    private List<TaskInstance> getAllTaskInstances(BatchPresentation batchPresentation, Set<Executor> executors) {
        ArrayList<String> executorsId = new ArrayList<String>();
        for (Executor ex : executors) {
            if (ex instanceof Actor) {
                executorsId.add(Long.toString(((Actor) ex).getCode()));
            } else {
                executorsId.add("G" + Long.toString(ex.getId()));
            }
        }
        return new BatchPresentationHibernateCompiler(batchPresentation).getBatch(executorsId, "assignedActorId", false);
    }

    public TaskStub getTask(Subject subject, Long taskId) throws AuthenticationException {
        TaskInstance taskInstance = taskDAO.getTaskInstanceNotNull(taskId);
        return taskStubFactory.create(taskInstance, SubjectPrincipalsHelper.getActor(subject), null);
    }

    public void checkUnassignedTasks() {
        List<TaskInstance> unassignedTasks = taskDAO.findTaskInstances(InfoHolder.UNASSIGNED_SWIMLANE_VALUE);
        if (unassignedTasks != null) {
            for (TaskInstance task : unassignedTasks) {
                if (task.getProcessInstance().getEndDate() != null) {
                    continue;
                }
                try {
                    if (task.getSwimlaneInstance() == null) {
                        continue;
                    }
                    Delegation delegate = task.getSwimlaneInstance().getSwimlane().getDelegation();
                    if (delegate == null) {
                        continue;
                    }
                    AssignmentHandler handler = null;
                    {
                        Object obj = delegate.getInstance();
                        if (!(obj instanceof AssignmentHandler)) {
                            continue;
                        }
                        handler = (AssignmentHandler) obj;
                    }
                    ExecutableProcessDefinition processDefinition = getDefinition(task);
                    ExecutionContext current = new ExecutionContext(processDefinition, task.getToken());
                    current.setTaskInstance(task);
                    handler.assign(task, current);
                } catch (Exception e) {
                    log.warn(e);
                }
            }
        }
    }

    public void removeUnnecessaryTimers() {
        Session session = null;
        try { // TODO move to DAO
            session = HibernateSessionFactory.openSession();
            List<Long> ids = session.createQuery("select j.id from ru.runa.bpm.job.Job as j where j.processInstance.endDate is not null").list();
            if (!ids.isEmpty()) {
                Query query = session.createQuery("delete from ru.runa.bpm.job.Job where id in (:ids)");
                query.setParameterList("ids", ids);
                query.executeUpdate();
            }
        } finally {
            if (session != null) {
                HibernateSessionFactory.closeSession(true);
            }
        }
    }

    protected Set<String> parseIgnoredSubstitutions(byte[] bytes) throws IOException, SAXException {
        Digester digester = new Digester();
        digester.push(new HashSet<String>());
        digester.addRule("ignoreSubstitutions/task", new Rule() {
            @Override
            public void begin(String namespace, String name, Attributes attributes) throws Exception {
                String taskName = attributes.getValue("name");
                ((Set<String>) getDigester().peek()).add(taskName);
                super.begin(namespace, name, attributes);
            }
        });
        return (Set<String>) digester.parse(new ByteArrayInputStream(bytes));
    }

    private boolean isSubstitutionEnabled(TaskInstance task) throws ProcessDefinitionDoesNotExistException,
            ProcessDefinitionFileDoesNotExistException, IOException, SAXException {
        ExecutableProcessDefinition definition = getDefinition(task);
        byte[] file = definition.getFileBytes(FileDataProvider.SUBSTITUTION_EXCEPTIONS_FILE_NAME);
        if (file == null) {
            return true;
        }
        return !parseIgnoredSubstitutions(file).contains(task.getName());
    }

    public List<TaskStub> getTasks(Subject subject, BatchPresentation batchPresentation) throws AuthenticationException {
        try {
            Actor actor = SubjectPrincipalsHelper.getActor(subject);
            List<TaskStub> retVal = taskCache.getTasks(actor.getId(), batchPresentation);
            if (retVal != null) {
                return retVal;
            }
            int cacheVersion = taskCache.getCacheVersion();
            List<TaskStub> taskStubList = new ArrayList<TaskStub>();
            Set<Executor> executorsToGetTasks = getActorsToGetTasks(actor, false);
            Set<Executor> executorsToGetTasksSub = getSubstitutableExecutors(actor);
            List<TaskInstance> tasks = getAllTaskInstances(batchPresentation, executorsToGetTasksSub);
            // List<Date> deadlines = tasks.isEmpty() ? new ArrayList<Date>() :
            // getTasksDeadline(jbpmContext, tasks);
            for (TaskInstance task : tasks) {
                Executor assignedEx = getAssignedExecutor(task.getAssignedActorId());
                ExecutableProcessDefinition processDefinition = getDefinition(task);
                Interaction interaction = processDefinition.getInteraction(task.getName());
                String formType = (interaction == null ? null : interaction.getType());
                if (executorsToGetTasks.contains(assignedEx)) {
                    // Our task - get.
                    taskStubList.add(taskStubFactory.create(task, actor, formType));
                    continue;
                }
                if (!isSubstitutionEnabled(task)) {
                    continue;
                }
                // Task to substitute (may be)
                if (assignedEx instanceof Actor) {
                    if (isCanSubToken(task, (Actor) assignedEx, actor)) {
                        taskStubList.add(taskStubFactory.create(task, (Actor) assignedEx, formType));
                    }
                } else {
                    for (Actor actInGroup : executorDAO.getGroupActors((Group) assignedEx)) {
                        if (isCanSubToken(task, actInGroup, actor)) {
                            taskStubList.add(taskStubFactory.create(task, actInGroup, formType));
                            break;
                        }
                    }
                }
            }
            retVal = taskStubList;
            List<ExecutorOpenTask> openTasks = executorDAO.getOpenedTasksByExecutor(actor);
            for (TaskStub taskStub : retVal) {
                boolean found = false;
                for (ExecutorOpenTask openTask : openTasks) {
                    if (openTask.getTaskInstance().getId() == taskStub.getId()) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    taskStub.setFirstOpen(true);
                }
            }
            taskCache.setTasks(cacheVersion, actor.getId(), batchPresentation, retVal);
            return retVal;
        } catch (Exception e) {
            Throwables.propagateIfInstanceOf(e, InternalApplicationException.class);
            Throwables.propagateIfInstanceOf(e, AuthenticationException.class);
            throw Throwables.propagate(e);
        }
    }

    public List<String> getVariableNames(Subject subject, Long taskId) throws AuthorizationException, AuthenticationException,
            TaskDoesNotExistException {
        Map<String, Object> variables = getVariables(subject, taskId);
        return Lists.newArrayList(variables.keySet());
    }

    public Long startProcessInstance(Subject subject, String definitionName) throws AuthorizationException, AuthenticationException,
            ProcessDefinitionDoesNotExistException, VariablesValidationException {
        return startProcessInstanceInternal(subject, definitionName, new HashMap<String, Object>());
    }

    public Long startProcessInstance(Subject subject, String definitionName, Map<String, Object> variablesMap) throws AuthorizationException,
            AuthenticationException, ProcessDefinitionDoesNotExistException, VariablesValidationException {
        if (variablesMap == null) {
            throw new IllegalArgumentException("Variables map must be not null");
        }
        return startProcessInstanceInternal(subject, definitionName, variablesMap);
    }

    private List<ProcessInstanceStub> filterInstances(Subject subject, List<ProcessInstance> jbpmProcessInstancesList) throws AuthenticationException {
        List<ProcessInstanceStub> result = getProcessInstaceStubs(jbpmProcessInstancesList);
        result = filterIdentifiable(subject, result, ProcessInstancePermission.READ);
        return result;
    }

    private List<ProcessInstanceStub> getProcessInstaceStubs(List<ProcessInstance> processInstances) {
        List<ProcessInstanceStub> result = Lists.newArrayListWithExpectedSize(processInstances.size());
        for (ProcessInstance processInstance : processInstances) {
            result.add(createProcessInstanceStub(processInstance));
        }
        return result;
    }

    private void validateVariables(ExecutableProcessDefinition processDefinition, String stateName, Map<String, ?> variables)
            throws VariablesValidationException {
        Interaction interaction = processDefinition.getInteractionNotNull(stateName);
        if (interaction.getValidationData() != null) {
            String key = processDefinition.getName() + "_" + stateName;
            FormValidator.validate(key, interaction.getValidationData(), variables);
        }
    }

    private Long startProcessInstanceInternal(Subject subject, String definitionName, Map<String, Object> variablesMap)
            throws AuthorizationException, AuthenticationException, ProcessDefinitionDoesNotExistException, VariablesValidationException {
        SubjectPrincipalHolder.actorIds.set(SubjectPrincipalsHelper.getActor(subject).getCode().toString());
        if (variablesMap == null) {
            variablesMap = new HashMap<String, Object>();
        }
        ProcessInstance processInstance = null;
        Long returnId = null;
        try {
            ExecutableProcessDefinition processDefinition = getLatestDefinition(definitionName);
            ProcessDefinition definitionStub = createProcessDefinitionStub(processDefinition);
            checkPermissionAllowed(subject, definitionStub, ProcessDefinitionPermission.START_PROCESS);
            Map<String, Object> defaultValues = processDefinition.getDefaultVariableValues();
            for (Map.Entry<String, Object> entry : defaultValues.entrySet()) {
                if (!variablesMap.containsKey(entry.getKey())) {
                    variablesMap.put(entry.getKey(), entry.getValue());
                }
            }
            validateVariables(processDefinition, processDefinition.getStartStateNotNull().getName(), variablesMap);
            String transitionName = (String) variablesMap.remove("startTransition");
            processInstance = processDefinition.createProcessInstance(variablesMap);
            tmpDAO.save(processInstance);
            ExecutionContext executionContext = new ExecutionContext(processDefinition, processInstance);
            ProcessInstanceStub instanceStub = createInstanceStubGrantPermissions(subject, processInstance, definitionStub);
            // TODO: some hack to assign swimlane at start state.
            TaskInstance startTask = processInstance.getTaskMgmtInstance().createStartTaskInstance(executionContext,
                    SubjectPrincipalsHelper.getActor(subject).getCode());
            if (startTask != null) {
                startTask.setVariables(executionContext, variablesMap); // if
                                                                        // start
                                                                        // role
                                                                        // was
                // already assigned -
                // restore assignment
                // TODO remove cancellation
                if (transitionName != null) {
                    startTask.cancel(executionContext, transitionName);
                } else {
                    startTask.cancel(executionContext);
                }
            } else if (processInstance.getEndDate() == null && processInstance.getRootToken().getNode() != null) {
                processInstance.signal(executionContext);
            }
            log.info("Process instance [name=" + definitionName + "] was successfully started");
            returnId = instanceStub.getId();
            tmpDAO.saveProcessInstance(processInstance);
        } catch (Exception e) {
            Throwables.propagateIfInstanceOf(e, InternalApplicationException.class);
            Throwables.propagateIfInstanceOf(e, AuthorizationException.class);
            Throwables.propagateIfInstanceOf(e, AuthenticationException.class);
            Throwables.propagateIfInstanceOf(e, ProcessDefinitionDoesNotExistException.class);
            Throwables.propagateIfInstanceOf(e, VariablesValidationException.class);
            Throwables.propagate(e);
        }
        try {
            if (processInstance != null) {
                sendEmail(processInstance.getTaskMgmtInstance().getUnfinishedTasks(processInstance.getRootToken()));
            }
        } catch (Exception e) {
            log.info("Cant't send email", e);
        }
        return returnId;
    }

    /**
     * Creates {@link ProcessInstanceStub} for given {@link ProcessInstance} and
     * grant permissions according to {@link ExecutableProcessDefinition}
     * permissions.
     * 
     * @param subject
     *            Subject for actor, which starts process instance.
     * @param executorDAO
     *            DAO to manipulate {@link Executor}'s and {@link Permission}'s.
     * @param securedObjectDAO
     *            DAO to manipulate {@link SecuredObject}.
     * @param processInstance
     *            {@link ProcessInstance}, which receives permissions.
     * @param definitionStub
     *            {@link ProcessDefinition}, corresponding to
     *            {@link ProcessInstance}, which receives permissions.
     * @return {@link ProcessInstanceStub} for given {@link ProcessInstance}.
     */
    private ProcessInstanceStub createInstanceStubGrantPermissions(Subject subject, ProcessInstance processInstance, ProcessDefinition definitionStub)
            throws SecuredObjectAlreadyExistsException, SecuredObjectOutOfDateException, ExecutorOutOfDateException, UnapplicablePermissionException,
            AuthenticationException {
        ProcessInstanceStub instanceStub = createProcessInstanceStub(processInstance);
        SecuredObject definitionSecuredObject = securedObjectDAO.get(definitionStub);
        SecuredObject instanceSO = securedObjectDAO.create(instanceStub);
        List<Executor> executorsWithPermission = securedObjectDAO.getExecutorsWithPermission(definitionStub,
                AFProfileStrategy.EXECUTOR_DEAFAULT_BATCH_PRESENTATOIN_FACTORY.getDefaultBatchPresentation());
        for (Executor executor : executorsWithPermission) {
            Set<Permission> instancePermissions = InstancePermissionsHelper.getInstancePermissions(executor, definitionSecuredObject, permissionDAO);
            permissionDAO.setPermissions(executor, instancePermissions, instanceSO);
        }
        setPrivelegedExecutorsPermissionsOnIdentifiable(instanceStub);
        Actor processStarter = SubjectPrincipalsHelper.getActor(subject);
        List<Permission> permissions = permissionDAO.getPermissions(processStarter, instanceSO);
        Executor systemProcessStarter = executorDAO.getExecutor(SystemExecutors.PROCESS_STARTER_NAME);
        Set<Permission> systemStarterPermissions = InstancePermissionsHelper.getInstancePermissions(systemProcessStarter, definitionSecuredObject,
                permissionDAO);
        permissionDAO.setPermissions(processStarter, Permission.mergePermissions(permissions, systemStarterPermissions), instanceSO);
        return instanceStub;
    }

    public Map<String, Object> getInstanceVariables(Subject subject, Long instanceId) throws AuthorizationException, AuthenticationException,
            ProcessInstanceDoesNotExistException {
        ProcessInstance processInstance = processExecutionDAO.getInstanceNotNull(instanceId);
        ProcessInstanceStub instance = createProcessInstanceStub(processInstance);
        checkPermissionAllowed(subject, instance, ProcessInstancePermission.READ);
        Map<String, Object> variables = processInstance.getContextInstance().getVariables();
        for (Object swimlaneName : processInstance.getTaskMgmtInstance().getSwimlaneInstances().keySet()) {
            variables.remove(swimlaneName);
        }
        return variables;
    }

    public Map<String, Object> getVariables(Subject subject, Long taskId) throws AuthorizationException, AuthenticationException,
            TaskDoesNotExistException {
        TaskInstance taskInstance = taskDAO.getTaskInstanceNotNull(taskId);
        checkReadToVariablesAllowed(subject, taskInstance);
        return taskInstance.getVariables();
    }

    private Collection<Long> getProcessDefinitionsForInstances(List<Long> instanceIds) {
        Set<Long> result = new HashSet<Long>();
        Session session = HibernateSessionFactory.getSession();
        for (int i = 0; i <= instanceIds.size() / 1000; ++i) {
            int start = i * 1000;
            int end = (i + 1) * 1000 > instanceIds.size() ? instanceIds.size() : (i + 1) * 1000;
            Set<Long> requested = new HashSet<Long>(end - start);
            for (int j = start; j < end; j++) {
                requested.add(instanceIds.get(j));
            }
            Query query = session.createQuery("select instance.processDefinition.id from " + ProcessInstance.class.getName()
                    + " as instance where instance.id in (:ids)");
            query.setParameterList("ids", requested);
            result.addAll(query.list());
        }
        return result;
    }

    private Map<Long, VariableInstance<?>> getPublicVariables(List<Long> instanceIds, String variableName) {
        try {
            Map<Long, VariableInstance<?>> result = new HashMap<Long, VariableInstance<?>>();
            Collection<ExecutableProcessDefinition> definitions = new ArrayList<ExecutableProcessDefinition>();
            for (Long definitionId : getProcessDefinitionsForInstances(instanceIds)) {
                ExecutableProcessDefinition definition = getDefinition(definitionId);
                if (definition.isVariablePublic(variableName)) {
                    definitions.add(definition);
                }
            }
            if (definitions.isEmpty()) {
                return result;
            }
            List<VariableInstance<?>> vars = Lists.newArrayList();
            for (int i = 0; i <= instanceIds.size() / 1000; ++i) {
                try {
                    int start = i * 1000;
                    int end = (i + 1) * 1000 > instanceIds.size() ? instanceIds.size() : (i + 1) * 1000;
                    Set<Long> requested = new HashSet<Long>(end - start);
                    for (int j = start; j < end; j++) {
                        requested.add(instanceIds.get(j));
                    }
                    Query query = HibernateSessionFactory.getSession().createQuery(
                            "Select var from " + VariableInstance.class.getName() + " as var where var.name='" + variableName
                                    + "' and var.processInstance.id in (:ids) and var.processInstance.processDefinition in (:pids)");
                    query.setParameterList("ids", requested);
                    query.setParameterList("pids", definitions);
                    vars.addAll(query.list());
                } catch (Exception e) {
                    log.error("Error in variable getter", e);
                }
            }
            for (VariableInstance<?> var : vars) {
                result.put(var.getProcessInstance().getId(), var);
            }
            return result;
        } catch (ProcessDefinitionDoesNotExistException e) {
            throw new InternalApplicationException(e);
        }
    }

    public Object getVariable(Subject subject, Long taskId, String variableName) throws AuthorizationException, AuthenticationException,
            TaskDoesNotExistException {
        try {
            TaskInstance taskInstance = taskDAO.getTaskInstanceNotNull(taskId);
            ExecutableProcessDefinition definition = getDefinition(taskInstance);
            if (!definition.isVariablePublic(variableName)) {
                checkReadToVariablesAllowed(subject, taskInstance);
            }
            return taskInstance.getVariable(variableName);
        } catch (ProcessDefinitionDoesNotExistException e) {
            throw new InternalApplicationException(e);
        }
    }

    public List<Object> getVariable(Subject subject, List<Long> processIds, String variableName) throws AuthenticationException {
        List<Object> result = Lists.newArrayListWithExpectedSize(processIds.size());
        List<Identifiable> idents = Lists.newArrayListWithExpectedSize(processIds.size());
        for (final Long processId : processIds) {
            idents.add(new Identifiable() {

                @Override
                public int identifiableType() {
                    return ProcessInstanceStub.class.getName().hashCode();
                }

                @Override
                public Long getId() {
                    return processId;
                }
            });
        }
        idents = filterIdentifiable(subject, idents, ProcessInstancePermission.READ);
        List<Long> readableProcesses = new ArrayList<Long>();
        for (Identifiable identifiable : idents) {
            readableProcesses.add(identifiable.getId());
        }
        Session session = HibernateSessionFactory.getSession();
        List<VariableInstance<?>> vars = Lists.newArrayList();
        if (!readableProcesses.isEmpty()) {
            for (int i = 0; i <= readableProcesses.size() / 1000; ++i) {
                try {
                    int start = i * 1000;
                    int end = (i + 1) * 1000 > readableProcesses.size() ? readableProcesses.size() : (i + 1) * 1000;
                    Set<Long> requested = new HashSet<Long>(end - start);
                    for (int j = start; j < end; j++) {
                        requested.add(readableProcesses.get(j));
                    }
                    Criteria query = session.createCriteria(VariableInstance.class);
                    query.add(Expression.in("processInstance.id", requested));
                    query.add(Expression.eq("name", variableName));
                    vars.addAll(query.list());
                } catch (Exception e) {
                    log.error("Error in variable getter", e);
                }
            }
        }
        Map<Long, VariableInstance<?>> vMap = new HashMap<Long, VariableInstance<?>>();
        for (VariableInstance<?> var : vars) {
            vMap.put(var.getProcessInstance().getId(), var);
        }
        vMap.putAll(getPublicVariables(processIds, variableName));
        for (int i = 0; i < processIds.size(); ++i) {
            VariableInstance<?> variableInstance = vMap.get(processIds.get(i));
            result.add(variableInstance == null ? null : variableInstance.getValue());
        }
        return result;
    }

    public void updateVariables(Subject subject, Long taskInstanceId, Map<String, Object> variables) throws AuthorizationException,
            AuthenticationException, TaskDoesNotExistException {
        Actor actor = SubjectPrincipalsHelper.getActor(subject);
        TaskInstance taskInstance = taskDAO.getTaskInstanceNotNull(taskInstanceId);
        ExecutableProcessDefinition processDefinition = getDefinition(taskInstance);
        ExecutionContext executionContext = new ExecutionContext(processDefinition, taskInstance.getToken());
        checkCanParticipate(subject, taskInstance, actor);
        Map<String, Object> currentVariables = taskInstance.getVariables();
        currentVariables.putAll(variables);
        taskInstance.setVariables(executionContext, currentVariables);
    }

    public void removeVariable(Subject subject, Long taskInstanceId, String variableName) throws AuthorizationException, AuthenticationException,
            TaskDoesNotExistException {
        Actor actor = SubjectPrincipalsHelper.getActor(subject);
        TaskInstance taskInstance = taskDAO.getTaskInstanceNotNull(taskInstanceId);
        checkCanParticipate(subject, taskInstance, actor);
        taskInstance.deleteVariable(variableName);
    }

    public List<SwimlaneStub> getSwimlanes(Subject subject, Long instanceId) throws AuthorizationException, AuthenticationException,
            ProcessInstanceDoesNotExistException {
        ProcessInstance processInstance = processExecutionDAO.getInstanceNotNull(instanceId);
        ExecutableProcessDefinition processDefinition = getDefinition(processInstance);
        ProcessInstanceStub instance = createProcessInstanceStub(processInstance);
        checkPermissionAllowed(subject, instance, ProcessInstancePermission.READ);
        Actor performer = SubjectPrincipalsHelper.getActor(subject);
        Map<String, Swimlane> swimlaneMap = processDefinition.getSwimlanes();
        List<SwimlaneStub> swimlaneStubs = Lists.newArrayListWithExpectedSize(swimlaneMap.size());
        for (Map.Entry<String, Swimlane> entry : swimlaneMap.entrySet()) {
            String swimlaneName = entry.getKey();
            Swimlane swimlane = entry.getValue();
            // String swimlaneAssignment = (String) variables.get(swimlaneName);
            String swimlaneAssignment = (String) processInstance.getContextInstance().getVariable(swimlaneName);
            if (swimlaneAssignment == null || InfoHolder.UNASSIGNED_SWIMLANE_VALUE.equals(swimlaneAssignment)) {
                swimlaneStubs.add(new SwimlaneStub(swimlane, instanceId, null));
            } else {
                Executor actor = null;
                try {
                    if (swimlaneAssignment.charAt(0) != 'G') {
                        Long code = Long.parseLong(swimlaneAssignment);
                        actor = executorDAO.getActorByCode(code);
                    } else {
                        Long id = Long.parseLong(swimlaneAssignment.substring(1));
                        actor = executorDAO.getGroup(id);
                    }
                    if (!permissionDAO.isAllowed(performer, ExecutorPermission.READ, securedObjectDAO.get(actor))) {
                        actor = ActorStub.UNAUTHORIZED_ACTOR_STUB;
                    }
                } catch (Exception e) {
                    log.warn("Unable to get swimlane participant for " + swimlaneName + " = " + swimlaneAssignment, e);
                    actor = ActorStub.NOT_EXISTING_ACTOR_STUB;
                }
                swimlaneStubs.add(new SwimlaneStub(swimlane, instanceId, actor));
            }
        }
        return swimlaneStubs;
    }

    public Map<String, List<Executor>> getSwimlaneExecutorMap(Subject subject, Long instanceId, Long swimlaneId) throws AuthorizationException,
            AuthenticationException, ProcessInstanceDoesNotExistException, ExecutorOutOfDateException {
        Map<String, List<Executor>> result = Maps.newHashMap();
        ProcessInstance processInstance = processExecutionDAO.getInstanceNotNull(instanceId);
        ProcessInstanceStub processInstanceStub = createProcessInstanceStub(processInstance);
        checkPermissionAllowed(subject, processInstanceStub, ProcessInstancePermission.READ);
        Collection<TaskInstance> activeTasksCollection = new HashSet<TaskInstance>();
        fillProcessInstanceActiveTokensSet(activeTasksCollection, processInstance.getRootToken(), swimlaneId);
        Map<TaskInstance, Actor> tokenAssingedActorMap = getTokenAssingedActorMap(activeTasksCollection);
        for (TaskInstance task : activeTasksCollection) {
            if (task.getAssignedActorId() == null || task.getAssignedActorId().compareTo(InfoHolder.UNASSIGNED_SWIMLANE_VALUE) == 0) {
                List<Executor> executors = Lists.newArrayList((Executor) tokenAssingedActorMap.get(task));
                executors = filterIdentifiable(subject, executors, ExecutorPermission.READ);
                result.put(task.getName(), executors);
            } else {
                List<Executor> executors = new ArrayList<Executor>();
                String strAssignedActor = task.getAssignedActorId();
                if (strAssignedActor.charAt(0) != 'G') {
                    Long code = Long.parseLong(strAssignedActor);
                    executors.add(executorDAO.getActorByCode(code));
                } else {
                    Long id = Long.parseLong(strAssignedActor.substring(1));
                    executors.add(executorDAO.getGroup(id));
                }
                executors = filterIdentifiable(subject, executors, ExecutorPermission.READ);
                result.put(task.getName(), executors);
            }
        }
        return result;
    }

    private static final String GRAPH_IMAGE_FILE_NAME_OLD = "graph.gif";
    private static final String GRAPH_IMAGE_FILE_NAME_NEW = "processimage.jpg";

    public byte[] getProcessInstanceDiagram(Subject subject, Long instanceId, Long taskId, Long childProcessId) throws AuthorizationException,
            AuthenticationException, ProcessInstanceDoesNotExistException {
        try {
            ProcessInstance processInstance = processExecutionDAO.getInstanceNotNull(instanceId);
            ProcessInstanceStub processInstanceStub = createProcessInstanceStub(processInstance);
            checkPermissionAllowed(subject, processInstanceStub, ProcessInstancePermission.READ);
            ExecutableProcessDefinition processDefinition = getDefinition(processInstance);
            byte[] graphBytes = getGraphImageBytes(processDefinition);
            TaskInstance taskInstance = null;
            if (taskId != null) {
                taskInstance = taskDAO.getTaskInstanceNotNull(taskId);
            }
            Token token = taskInstance == null ? null : taskInstance.getToken();
            while (token != null && token.getProcessInstance().getId() != processInstance.getId()) {
                token = token.getProcessInstance().getSuperProcessToken();
            }
            GraphConverter converter = new GraphConverter(processDefinition);
            converter.setPassedTransitions(getPassedTransitions(processInstance.getId()));
            converter.setFailedActions(/*
                                        * getFailedActions(jbpmContext.
                                        * getLoggingSession(),
                                        * processInstance.getId())
                                        */new ArrayList<Action>());
            converter.setActiveToken(token);
            return converter.createDiagram(graphBytes, processInstance, childProcessId);
        } catch (Exception e) {
            throw new InternalApplicationException("Unable to draw diagram", e);
        }
    }

    public byte[] getProcessInstanceHistoryDiagram(Subject subject, Long instanceId, Long taskId) throws AuthorizationException,
            AuthenticationException, ProcessInstanceDoesNotExistException {
        try {
            ProcessInstance processInstance = processExecutionDAO.getInstanceNotNull(instanceId);
            ProcessInstanceStub processInstanceStub = createProcessInstanceStub(processInstance);
            checkPermissionAllowed(subject, processInstanceStub, ProcessInstancePermission.READ);
            ExecutableProcessDefinition processDefinition = getDefinition(processInstance);
            byte[] graphBytes = getGraphImageBytes(processDefinition);
            TaskInstance taskInstance = taskDAO.getTaskInstanceNotNull(taskId);
            Token token = taskInstance == null ? null : taskInstance.getToken();
            while (token != null && token.getProcessInstance().getId() != processInstance.getId()) {
                token = token.getProcessInstance().getSuperProcessToken();
            }
            Object logs = getInvocationLogs(subject, instanceId, null);
            GraphConverter converter = new GraphConverter(processDefinition);
            return converter.createHistoryDiagram(graphBytes, processInstance, (List<ProcessLog>) logs);
        } catch (Exception e) {
            throw new InternalApplicationException("Unable to draw diagram", e);
        }
    }

    public List<GraphElementPresentation> getProcessInstanceUIHistoryData(Subject subject, Long instanceId, Long taskId)
            throws AuthorizationException, AuthenticationException, ProcessInstanceDoesNotExistException {
        try {
            ProcessInstance processInstance = processExecutionDAO.getInstanceNotNull(instanceId);
            ProcessInstanceStub processInstanceStub = createProcessInstanceStub(processInstance);
            checkPermissionAllowed(subject, processInstanceStub, ProcessInstancePermission.READ);
            ExecutableProcessDefinition processDefinition = getDefinition(processInstance);
            byte[] graphBytes = getGraphImageBytes(processDefinition);
            TaskInstance taskInstance = taskDAO.getTaskInstanceNotNull(taskId);
            Token token = taskInstance == null ? null : taskInstance.getToken();
            while (token != null && token.getProcessInstance().getId() != processInstance.getId()) {
                token = token.getProcessInstance().getSuperProcessToken();
            }
            Object logs = getInvocationLogs(subject, instanceId, null);
            GraphConverter converter = new GraphConverter(processDefinition);
            List<Token> processInstanceTokens = tmpDAO.getProcessInstanceTokens(processInstance.getId());
            List<GraphElementPresentation> logElements = converter.getProcessInstanceUIHistoryData(subject, graphBytes, processInstance,
                    processInstanceTokens, (List<ProcessLog>) logs);
            return logElements;
        } catch (Exception e) {
            throw new InternalApplicationException("Unable to draw diagram", e);
        }
    }

    private byte[] getGraphImageBytes(ExecutableProcessDefinition processDefinition) {
        byte[] graphBytes = processDefinition.getFileBytes(GRAPH_IMAGE_FILE_NAME_NEW);
        if (graphBytes == null) {
            graphBytes = processDefinition.getFileBytes(GRAPH_IMAGE_FILE_NAME_OLD);
        }
        if (graphBytes == null) {
            throw new NullPointerException("Neither " + GRAPH_IMAGE_FILE_NAME_NEW + " and " + GRAPH_IMAGE_FILE_NAME_OLD + " not found in process");
        }
        return graphBytes;
    }

    /**
     * Loads graph presentation elements for process definition and set identity
     * of started subprocesses.
     * 
     * @param subject
     *            Current subject.
     * @param definitionId
     *            Identity of process definition, which presentation elements
     *            must be loaded.
     * @return List of graph presentation elements.
     */
    public List<GraphElementPresentation> getProcessInstanceGraphElements(Subject subject, Long instanceId) throws AuthenticationException,
            AuthorizationException {
        try {
            ProcessInstance processInstance = processExecutionDAO.getInstanceNotNull(instanceId);
            List<StartedSubprocesses> subProcesses = tmpDAO.getSubprocesses(instanceId);
            StartedSubprocessesVisitor operation = new StartedSubprocessesVisitor(subject, subProcesses);
            return getProcessDefinitionGraphElements(subject, processInstance.getProcessDefinition().getId(), operation);
        } catch (ProcessInstanceDoesNotExistException e) {
            log.warn("Unable to draw diagram", e);
            throw new InternalApplicationException(e);
        }
    }

    private void fillProcessInstanceActiveTokensSet(Collection<TaskInstance> activeTokenSet, Token token, Long swimlaneId) {
        Collection<TaskInstance> forToken = token.getProcessInstance().getTaskMgmtInstance().getUnfinishedTasks(token);
        for (TaskInstance task : forToken) {
            if (task.getSwimlaneInstance() != null && task.getSwimlaneInstance().getSwimlane().getId() == swimlaneId
                    && !InfoHolder.UNASSIGNED_SWIMLANE_VALUE.equals(task.getAssignedActorId())) {
                activeTokenSet.add(task);
            }
        }
        for (Token currentToken : token.getActiveChildren().values()) {
            Set<TaskInstance> tmp = new HashSet<TaskInstance>();
            fillProcessInstanceActiveTokensSet(tmp, currentToken, swimlaneId);
            activeTokenSet.addAll(tmp);
        }
    }

    private void removeTemporaryGroup(String executorId) throws ExecutorOutOfDateException {
        if (executorId == null || !executorId.startsWith("G")) {
            return;
        }
        Long groupId = Long.parseLong(executorId.substring(1));
        Group group = executorDAO.getGroup(groupId);
        if (group.isTemporary()) {
            executorDAO.remove(group);
        }
    }

    public void assignTask(Subject subject, Long taskId, String taskName, Long actorId) throws AuthenticationException, TaskAlreadyAcceptedException,
            ExecutorOutOfDateException {
        try {
            // reassign the actor for the token
            Actor actor = executorDAO.getActor(actorId);
            String newAssignedValue = String.valueOf(actor.getCode());
            // check assigned executor for the task
            TaskInstance taskInstance = getTaskWithName(taskId, taskName);
            if (taskInstance == null || taskInstance.getAssignedActorId() == null || !taskInstance.getAssignedActorId().startsWith("G")) {
                throw new TaskAlreadyAcceptedException(taskName);
            }
            ExecutableProcessDefinition processDefinition = getDefinition(taskInstance);
            List<TaskInstance> tasks = tmpDAO.getTokenWithSameSwimlane(taskInstance);
            String oldAssignValue = taskInstance.getAssignedActorId();
            for (TaskInstance task : tasks) {
                ExecutionContext executionContext = new ExecutionContext(processDefinition, taskInstance.getToken());
                task.setActorId(executionContext, newAssignedValue);
            }
            removeTemporaryGroup(oldAssignValue);
        } catch (TaskDoesNotExistException e) {
            throw new TaskAlreadyAcceptedException(taskName, e);
        }
    }

    public Object getInvocationLogs(Subject subject, Long processInstanceId, LogPresentationBuilder builder) throws Exception {
        Map<Token, List<ProcessLog>> logsMap = loggingDAO.findLogsByProcessInstance(processInstanceId);
        List<ProcessLog> logs = new ArrayList<ProcessLog>();
        for (List<ProcessLog> processList : logsMap.values()) {
            logs.addAll(processList);
        }
        Collections.sort(logs, new LogsComparator());
        for (int i = logs.size() - 1; i > 0; --i) { // VariableUpdate/VariableCreate
                                                    // logs must be after
                                                    // taskEnd
            if (logs.get(i) instanceof TaskEndLog && (logs.get(i - 1) instanceof VariableCreateLog || logs.get(i - 1) instanceof VariableUpdateLog)) {
                ProcessLog tmp = logs.get(i);
                logs.set(i, logs.get(i - 1));
                logs.set(i - 1, tmp);
            }
        }
        if (builder != null) {
            return builder.processLogs(logs);
        } else {
            return logs;
        }
    }

    public void createOpenTask(Subject subject, BatchPresentation batchPresentation, Long taskId) throws AuthenticationException,
            TaskDoesNotExistException {
        Actor actor = SubjectPrincipalsHelper.getActor(subject);
        TaskInstance taskInstance = taskDAO.getTaskInstanceNotNull(taskId);
        ExecutorOpenTask executorOpenTask = new ExecutorOpenTask(actor, taskInstance);
        executorDAO.createOpenTask(executorOpenTask);
    }

    /**
     * Load system logs according to {@link BatchPresentation}.
     * 
     * @param subject
     *            Requester subject.
     * @param batchPresentation
     *            {@link BatchPresentation} to load logs.
     * @return Loaded system logs.
     */
    public List<SystemLog> getSystemLogs(Subject subject, BatchPresentation batchPresentation) throws AuthorizationException, AuthenticationException {
        checkPermissionAllowed(subject, ASystem.SYSTEM, SystemPermission.READ);
        return new BatchPresentationHibernateCompiler(batchPresentation).getBatch(true);
    }

    /**
     * Load system logs count according to {@link BatchPresentation}.
     * 
     * @param subject
     *            Requester subject.
     * @param batchPresentation
     *            {@link BatchPresentation} to load logs count.
     * @return System logs count.
     */
    public int getSystemLogsCount(Subject subject, BatchPresentation batchPresentation) throws AuthorizationException, AuthenticationException {
        checkPermissionAllowed(subject, ASystem.SYSTEM, SystemPermission.READ);
        return new BatchPresentationHibernateCompiler(batchPresentation).getCount();
    }

    static class LogsComparator implements Comparator<ProcessLog> {
        @Override
        public int compare(ProcessLog o1, ProcessLog o2) {
            Date date1 = o1 instanceof NodeLog ? ((NodeLog) o1).getEnter() : o1.getDate();
            Date date2 = o2 instanceof NodeLog ? ((NodeLog) o2).getEnter() : o2.getDate();
            int result = date1.compareTo(date2);
            if (result == 0) {
                return new Long(o1.getId()).compareTo(o2.getId());
            } else {
                return result;
            }
        }
    }

    /**
     * Retrieves passed transitions for all ProcessInstance's Tokens from
     * process logs
     * 
     * @param loggingDAO
     * @param processId
     * @return
     */
    public List<Transition> getPassedTransitions(Long processId) {
        Session session = null;
        try { // FIXME
            session = HibernateSessionFactory.openSession();
            Criteria criteria = session.createCriteria(PassedTransition.class);
            criteria.add(Expression.eq("processInstance.id", processId));
            List<PassedTransition> passed = criteria.list();
            List<Transition> result = new ArrayList<Transition>();
            for (PassedTransition passedTr : passed) {
                result.add(passedTr.getTransition());
            }
            return result;
        } finally {
            if (session != null) {
                HibernateSessionFactory.closeSession(true);
            }
        }
    }
}
