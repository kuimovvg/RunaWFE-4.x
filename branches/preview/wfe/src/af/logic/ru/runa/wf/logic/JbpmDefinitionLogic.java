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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.security.auth.Subject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import ru.runa.InternalApplicationException;
import ru.runa.af.ASystem;
import ru.runa.af.AuthenticationException;
import ru.runa.af.AuthorizationException;
import ru.runa.af.Permission;
import ru.runa.af.SecuredObject;
import ru.runa.af.authenticaion.SubjectPrincipalsHelper;
import ru.runa.af.dao.SystemLogDAO;
import ru.runa.af.log.ProcessDefinitionDeleteLog;
import ru.runa.af.presentation.BatchPresentation;
import ru.runa.af.presentation.BatchPresentationHibernateCompiler;
import ru.runa.bpm.graph.def.ArchievedProcessDefinition;
import ru.runa.bpm.graph.def.ExecutableProcessDefinition;
import ru.runa.bpm.graph.def.Transition;
import ru.runa.bpm.graph.exe.ProcessInstance;
import ru.runa.bpm.par.FileDataProvider;
import ru.runa.bpm.par.ProcessArchive;
import ru.runa.bpm.par.ProcessMetadataParser;
import ru.runa.bpm.taskmgmt.exe.TaskInstance;
import ru.runa.wf.ProcessDefinition;
import ru.runa.wf.ProcessDefinitionAlreadyExistsException;
import ru.runa.wf.ProcessDefinitionArchiveException;
import ru.runa.wf.ProcessDefinitionArchiveFormatException;
import ru.runa.wf.ProcessDefinitionDoesNotExistException;
import ru.runa.wf.ProcessDefinitionNameMismatchException;
import ru.runa.wf.ProcessDefinitionPermission;
import ru.runa.wf.ProcessInstanceDoesNotExistException;
import ru.runa.wf.SuperProcessInstanceExistsException;
import ru.runa.wf.TaskDoesNotExistException;
import ru.runa.wf.WorkflowSystemPermission;
import ru.runa.wf.form.Interaction;
import ru.runa.wf.form.VariableDefinition;
import ru.runa.wf.graph.GraphElementPresentation;
import ru.runa.wf.graph.SubprocessPermissionVisitor;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

/**
 * Created on 15.03.2005
 */
public class JbpmDefinitionLogic extends JbpmCommonLogic {
    private static final Log log = LogFactory.getLog(JbpmDefinitionLogic.class);
    @Autowired
    private SystemLogDAO systemLogDAO;

    public ProcessDefinition deployProcessDefinition(Subject subject, byte[] processArchiveBytes, List<String> processType)
            throws AuthenticationException, AuthorizationException, ProcessDefinitionAlreadyExistsException, ProcessDefinitionArchiveException,
            ProcessDefinitionDoesNotExistException {
        try {
            checkPermissionAllowed(subject, ASystem.SYSTEM, WorkflowSystemPermission.DEPLOY_DEFINITION);
            ExecutableProcessDefinition definition = parseProcessDefinition(processArchiveBytes);
            try {
                getLatestDefinition(definition.getName());
                throw new ProcessDefinitionAlreadyExistsException(definition.getName());
            } catch (ProcessDefinitionDoesNotExistException e) {
                // expected
            }
            processDefinitionDAO.deployDefinition(definition.getDBImpl(), null);
            tmpDAO.updateBPDefinitionInfo(definition.getName(), processType);
            ExecutableProcessDefinition newDefinition = getLatestDefinition(definition.getName());
            SecuredObject newDefinitionSO = securedObjectDAO.create(createProcessDefinitionStub(newDefinition));
            Collection<Permission> allPermissions = new ProcessDefinitionPermission().getAllPermissions();
            permissionDAO.setPermissions(SubjectPrincipalsHelper.getActor(subject), allPermissions, newDefinitionSO);
            ProcessDefinition definitionStub = createProcessDefinitionStub(newDefinition);
            setPrivelegedExecutorsPermissionsOnIdentifiable(definitionStub);
            log.debug("Deployed process definition " + definitionStub);
            return new ProcessDefinition(newDefinition);
        } catch (ProcessDefinitionArchiveException e) {
            log.error("Failed to deploy process definition", e);
            throw e;
        } catch (AuthenticationException e) {
            throw e;
        } catch (AuthorizationException e) {
            throw e;
        } catch (ProcessDefinitionDoesNotExistException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalApplicationException("Failed to deploy definition", e);
        }
    }

    public ProcessDefinition redeployProcessDefinition(Subject subject, Long definitionId, byte[] processArchiveBytes, List<String> processType)
            throws AuthenticationException, AuthorizationException, ProcessDefinitionDoesNotExistException, ProcessDefinitionArchiveException,
            ProcessDefinitionNameMismatchException {
        try {
            ExecutableProcessDefinition deployedDefinition = getDefinition(definitionId);
            ProcessDefinition definitionStub = createProcessDefinitionStub(deployedDefinition);
            checkPermissionAllowed(subject, definitionStub, ProcessDefinitionPermission.REDEPLOY_DEFINITION);
            if (processArchiveBytes == null) {
                tmpDAO.updateBPDefinitionInfo(deployedDefinition.getName(), processType);
                return new ProcessDefinition(deployedDefinition);
            }
            ExecutableProcessDefinition definition = parseProcessDefinition(processArchiveBytes);
            if (!deployedDefinition.getName().equals(definition.getName())) {
                throw new ProcessDefinitionNameMismatchException("Process archive contains definition for process " + definition.getName()
                        + " mismatch with deployed process name " + deployedDefinition.getName(), definition.getName(), deployedDefinition.getName());
            }
            processDefinitionDAO.deployDefinition(definition.getDBImpl(), deployedDefinition.getDBImpl());
            tmpDAO.updateBPDefinitionInfo(definition.getName(), processType);
            log.debug("Process definition " + definitionStub + " was successfully redeployed");
            return new ProcessDefinition(definition);
        } catch (ProcessDefinitionArchiveFormatException e) {
            log.error("Failed to redeploy process definition", e);
            throw e;
        } catch (AuthenticationException e) {
            throw e;
        } catch (AuthorizationException e) {
            throw e;
        } catch (ProcessDefinitionDoesNotExistException e) {
            log.error("Failed to redeploy process definition", e);
            throw e;
        } catch (ProcessDefinitionNameMismatchException e) {
            log.error("Failed to redeploy process definition", e);
            throw e;
        } catch (Exception e) {
            log.error("Failed to redeploy process definition", e);
            throw new ProcessDefinitionArchiveFormatException(e);
        }
    }

    public ProcessDefinition getLatestProcessDefinitionStub(Subject subject, String definitionName) throws AuthenticationException,
            AuthorizationException, ProcessDefinitionDoesNotExistException {
        ExecutableProcessDefinition definition = getLatestDefinition(definitionName);
        ProcessDefinition result = createProcessDefinitionStub(definition);
        checkPermissionAllowed(subject, result, Permission.READ);
        return result;
    }

    public ProcessDefinition getProcessDefinitionStub(Subject subject, Long definitionId) throws AuthenticationException, AuthorizationException,
            ProcessDefinitionDoesNotExistException {
        ProcessDefinition result = createProcessDefinitionStub(getDefinition(definitionId));
        checkPermissionAllowed(subject, result, Permission.READ);
        return result;
    }

    public ProcessDefinition getProcessDefinitionStubByProcessId(Subject subject, Long processInstanceId) throws AuthenticationException,
            AuthorizationException, ProcessInstanceDoesNotExistException {
        ProcessInstance processInstance = processExecutionDAO.getInstanceNotNull(processInstanceId);
        ExecutableProcessDefinition processDefinition = getDefinition(processInstance);
        ProcessDefinition result = createProcessDefinitionStub(processDefinition);
        checkPermissionAllowed(subject, result, Permission.READ);
        return result;
    }

    /**
     * Loads graph presentation elements for process definition and set readable
     * flag.
     * 
     * @param subject
     *            Current subject.
     * @param definitionId
     *            Identity of process definition, which presentation elements
     *            must be loaded.
     * @return List of graph presentation elements.
     */
    public List<GraphElementPresentation> getProcessDefinitionGraphElements(Subject subject, Long definitionId) throws AuthenticationException,
            AuthorizationException {
        SubprocessPermissionVisitor operation = new SubprocessPermissionVisitor(subject, processDefCache);
        return getProcessDefinitionGraphElements(subject, definitionId, operation);
    }

    public List<ProcessDefinition> getLatestProcessDefinitionStubs(Subject subject, BatchPresentation batchPresentation)
            throws AuthenticationException {
        List<Number> latestDefinitions = new BatchPresentationHibernateCompiler(batchPresentation).getIdentities(null, null, false);
        List<ProcessDefinition> result = Lists.newArrayListWithExpectedSize(latestDefinitions.size());
        for (Number definitionId : latestDefinitions) {
            try {
                result.add(createProcessDefinitionStub(getDefinition(definitionId.longValue())));
            } catch (ProcessDefinitionDoesNotExistException e) {
                log.warn(e.getMessage());
            }
        }
        return filterIdentifiable(subject, result, Permission.READ);
    }

    public List<ProcessDefinition> getProcessDefinitionHistory(Subject subject, String name) throws AuthenticationException {
        List<ArchievedProcessDefinition> definitionVersions = processDefinitionDAO.findAllDefinitionVersions(name);
        List<ProcessDefinition> result = Lists.newArrayListWithExpectedSize(definitionVersions.size());
        for (ArchievedProcessDefinition definition : definitionVersions) {
            result.add(createProcessDefinitionStub(definition));
        }
        return filterIdentifiable(subject, result, Permission.READ);
    }

    public void undeployProcessDefinition(Subject subject, String definitionName) throws AuthenticationException, AuthorizationException,
            ProcessDefinitionDoesNotExistException, SuperProcessInstanceExistsException {
        Preconditions.checkNotNull(definitionName, "definitionName must be specified.");
        // uncomment when type of column JBPM_DELEGATION.COLUMN CLASSNAME_ will
        // be changed to varchar
        // removeMultiInstanceArtifacts(daoHolder, subject, definitionName);
        ProcessDefinition definitionStub = createProcessDefinitionStub(getLatestDefinition(definitionName));
        checkPermissionAllowed(subject, definitionStub, ProcessDefinitionPermission.UNDEPLOY_DEFINITION);
        List<ProcessInstance> instances = tmpDAO.getProcessInstancesForDefinitionName(definitionName);
        for (ProcessInstance instance : instances) {
            if (instance.getSuperProcessToken() != null) {
                throw new SuperProcessInstanceExistsException(definitionName, instance.getSuperProcessToken().getProcessInstance()
                        .getProcessDefinition().getName());
            }
            prepareProcessRemoval(instance);
        }
        executorDAO.removeOpenTasks(definitionName);
        securedObjectDAO.remove(definitionStub);
        deleteProcessDefinitionsByName(subject, definitionName);
        tmpDAO.deleteBPDefinitionInfo(definitionName);
        log.debug("Process definition " + definitionStub + " was undeployed");
    }

    /**
     * Removes multi instance artifacts. It must be removed, or foreign key
     * constraint violation occurred.
     * 
     * @param daoHolder
     *            Contains methods to get different DAO objects.
     * @param subject
     *            Process undeploy requester subject.
     * @param definitionName
     *            Undeploing process definition name.
     */
    // private void removeMultiInstanceArtifacts(Subject subject, String
    // definitionName) throws AuthenticationException {
    // Session session = daoHolder.getJbpmContext(subject).getSession();
    // String definitionSelect = "(select id from " +
    // ProcessDefinition.class.getName() + " as pd where pd.name=:definition)";
    // String actionNames =
    // "(action.name='InternalGetActorsByGroupActionHandlerName' or action.name='InternalGetActorsByRelationActionHandlerName')";
    // String delegationNames =
    // "(action.className='ru.runa.wf.InternalGetActorsByGroupActionHandler' or action.className='ru.runa.wf.InternalGetActorsByRelationActionHandler')";
    // Query query = session.createQuery("delete from " + Action.class.getName()
    // + " as action where action.processDefinition.id in "
    // + definitionSelect + " and " + actionNames);
    // query.setParameter("definition", definitionName);
    // query.executeUpdate();
    // query = session.createQuery("delete from " + Delegation.class.getName() +
    // " as action where action.processDefinition.id in "
    // + definitionSelect + " and " + delegationNames);
    // query.setParameter("definition", definitionName);
    // query.executeUpdate();
    // session.flush();
    // }
    /**
     * Deletes all Process Definitions from the database
     */
    private void deleteProcessDefinitionsByName(Subject subject, String definitionName) throws AuthenticationException {
        List<ArchievedProcessDefinition> definitions = processDefinitionDAO.findAllDefinitionVersions(definitionName);
        for (ArchievedProcessDefinition definition : definitions) {
            // delete all the process instances of this definition
            List<ProcessInstance> instances = processExecutionDAO.findAllProcessInstances(definition.getId());
            for (ProcessInstance processInstance : instances) {
                processExecutionDAO.deleteInstance(processInstance);
            }
            processDefinitionDAO.deleteDefinition(definition);
            systemLogDAO.create(new ProcessDefinitionDeleteLog(SubjectPrincipalsHelper.getActor(subject).getCode(), definitionName, definition
                    .getVersion()));
        }
    }

    public Interaction getInteraction(Subject subject, Long taskId, String taskName) throws AuthorizationException, AuthenticationException,
            TaskDoesNotExistException {
        try {
            TaskInstance taskInstance = getTaskWithName(taskId, taskName);
            ExecutableProcessDefinition definition = getDefinition(taskInstance);
            ProcessDefinition definitionStub = createProcessDefinitionStub(definition);
            if (!isPermissionAllowed(subject, definitionStub, ProcessDefinitionPermission.READ)) {
                checkCanParticipate(subject, taskInstance, null);
            }
            return definition.getInteractionNotNull(taskName);
        } catch (ProcessDefinitionDoesNotExistException e) {
            throw new InternalApplicationException(e);
        }
    }

    public List<String> getOutputTransitionNames(Subject subject, Long definitionId, Long taskId) throws AuthenticationException,
            TaskDoesNotExistException {
        try {
            List<Transition> transitions;
            if (definitionId != null) {
                transitions = getDefinition(definitionId).getStartStateNotNull().getLeavingTransitions();
            } else {
                TaskInstance taskInstance = taskDAO.getTaskInstanceNotNull(taskId);
                transitions = taskInstance.getTask().getNode().getLeavingTransitions();
            }
            List<String> result = new ArrayList<String>();
            for (Transition transition : transitions) {
                result.add(transition.getName());
            }
            return result;
        } catch (ProcessDefinitionDoesNotExistException e) {
            throw new InternalApplicationException(e);
        }
    }

    public byte[] getFile(Subject subject, Long definitionId, String fileName) throws AuthenticationException,
            ProcessDefinitionDoesNotExistException, AuthorizationException {
        ExecutableProcessDefinition definition = getDefinition(definitionId);
        if (!ProcessArchive.UNSECURED_FILE_NAMES.contains(fileName)) {
            ProcessDefinition definitionStub = createProcessDefinitionStub(definition);
            checkPermissionAllowed(subject, definitionStub, ProcessDefinitionPermission.READ);
        }
        return definition.getFileBytes(fileName);
    }

    public Interaction getStartInteraction(Subject subject, Long definitionId) throws AuthorizationException, AuthenticationException,
            ProcessDefinitionDoesNotExistException {
        ExecutableProcessDefinition definition = getDefinition(definitionId);
        ProcessDefinition definitionStub = createProcessDefinitionStub(definition);
        checkPermissionAllowed(subject, definitionStub, ProcessDefinitionPermission.READ);
        Interaction interaction = definition.getInteractionNotNull(definition.getStartStateNotNull().getName());
        Map<String, Object> defaultValues = definition.getDefaultVariableValues();
        for (Map.Entry<String, Object> entry : defaultValues.entrySet()) {
            interaction.getDefaultVariableValues().put(entry.getKey(), entry.getValue());
        }
        return interaction;
    }

    public static final String DEFINITION_NAME_SWIMLANE_NAME_SEPARATOR = ".";

    public Set<String> getAllSwimlanesNamesForAllProcessDefinition(Subject subject) throws AuthenticationException {
        Collection<ExecutableProcessDefinition> definitionsCollection = processDefCache.getLatestProcessDefinitions();
        List<ProcessDefinition> latestDefinitionStubs = Lists.newArrayListWithExpectedSize(definitionsCollection.size());
        for (ExecutableProcessDefinition definition : definitionsCollection) {
            latestDefinitionStubs.add(createProcessDefinitionStub(definition));
        }
        latestDefinitionStubs = filterIdentifiable(subject, latestDefinitionStubs, ProcessDefinitionPermission.READ);
        Set<String> definitionSwimlaneNameSet = new TreeSet<String>();
        for (ProcessDefinition processDefinition : latestDefinitionStubs) {
            definitionSwimlaneNameSet.addAll(processDefCache.getSwimlaneNamesForDefinition(processDefinition.getName()));
        }
        return definitionSwimlaneNameSet;
    }

    public Map<String, String> getOrgFunctionFriendlyNamesMapping(Subject subject, Long definitionId) throws ProcessDefinitionDoesNotExistException,
            AuthenticationException, AuthorizationException {
        try {
            ExecutableProcessDefinition definition = getDefinition(definitionId);
            ProcessDefinition definitionStub = createProcessDefinitionStub(definition);
            checkPermissionAllowed(subject, definitionStub, ProcessDefinitionPermission.READ);
            byte[] data = definition.getFileBytes(FileDataProvider.ORGFUNCTIONS_XML_FILE_NAME);
            if (data != null) {
                return ProcessMetadataParser.parseOrgFunctionMappings(data);
            }
        } catch (Exception e) {
            log.warn("getOrgFunctionFriendlyNamesMapping", e);
        }
        return new HashMap<String, String>();
    }

    public List<VariableDefinition> getProcessDefinitionVariables(Subject subject, Long definitionId) throws ProcessDefinitionDoesNotExistException,
            AuthenticationException, AuthorizationException {
        ExecutableProcessDefinition definition = getDefinition(definitionId);
        // TODO BSHActionHandler uses this method
        // ProcessDefinitionDescriptor definitionStub =
        // createProcessDefinitionStub(definition);
        // checkPermissionAllowed(daoHolder, subject, definitionStub,
        // ProcessDefinitionPermission.READ);
        return Lists.newArrayList(definition.getVariables().values());
    }

    private ExecutableProcessDefinition parseProcessDefinition(byte[] data) {
        ArchievedProcessDefinition processDefinitionDBImpl = new ArchievedProcessDefinition();
        processDefinitionDBImpl.setParFile(data);
        ProcessArchive archive = new ProcessArchive(data);
        return archive.parseProcessDefinition(processDefinitionDBImpl);
    }

}
