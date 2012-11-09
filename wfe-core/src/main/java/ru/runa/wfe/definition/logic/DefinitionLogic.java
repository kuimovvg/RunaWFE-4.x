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
package ru.runa.wfe.definition.logic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.security.auth.Subject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.audit.ProcessDefinitionDeleteLog;
import ru.runa.wfe.audit.dao.SystemLogDAO;
import ru.runa.wfe.commons.logic.WFCommonLogic;
import ru.runa.wfe.definition.DefinitionAlreadyExistException;
import ru.runa.wfe.definition.DefinitionArchiveFormatException;
import ru.runa.wfe.definition.DefinitionDoesNotExistException;
import ru.runa.wfe.definition.DefinitionNameMismatchException;
import ru.runa.wfe.definition.DefinitionPermission;
import ru.runa.wfe.definition.Deployment;
import ru.runa.wfe.definition.Language;
import ru.runa.wfe.definition.WorkflowSystemPermission;
import ru.runa.wfe.definition.dto.WfDefinition;
import ru.runa.wfe.definition.jpdl.JpdlProcessArchive;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.execution.ProcessDoesNotExistException;
import ru.runa.wfe.execution.ProcessFilter;
import ru.runa.wfe.execution.SuperProcessExistsException;
import ru.runa.wfe.form.Interaction;
import ru.runa.wfe.graph.image.SubprocessPermissionVisitor;
import ru.runa.wfe.graph.view.GraphElementPresentation;
import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.lang.Transition;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.hibernate.BatchPresentationHibernateCompiler;
import ru.runa.wfe.security.ASystem;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.auth.SubjectPrincipalsHelper;
import ru.runa.wfe.task.Task;
import ru.runa.wfe.task.TaskDoesNotExistException;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.var.VariableDefinition;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

/**
 * Created on 15.03.2005
 */
public class DefinitionLogic extends WFCommonLogic {
    private static final Log log = LogFactory.getLog(DefinitionLogic.class);
    @Autowired
    private SystemLogDAO systemLogDAO;

    public WfDefinition deployProcessDefinition(Subject subject, byte[] processArchiveBytes, List<String> processType)
            throws DefinitionAlreadyExistException, DefinitionArchiveFormatException, DefinitionDoesNotExistException {
        checkPermissionAllowed(subject, ASystem.INSTANCE, WorkflowSystemPermission.DEPLOY_DEFINITION);
        ProcessDefinition definition = parseProcessDefinition(processArchiveBytes);
        try {
            getLatestDefinition(definition.getName());
            throw new DefinitionAlreadyExistException(definition.getName());
        } catch (DefinitionDoesNotExistException e) {
            // expected
        }
        definition.getDBImpl().setCategories(processType);
        deploymentDAO.deploy(definition.getDBImpl(), null);
        Collection<Permission> allPermissions = new DefinitionPermission().getAllPermissions();
        permissionDAO.setPermissions(SubjectPrincipalsHelper.getActor(subject), allPermissions, definition);
        log.debug("Deployed process definition " + definition);
        return new WfDefinition(definition);
    }

    public WfDefinition redeployProcessDefinition(Subject subject, Long definitionId, byte[] processArchiveBytes, List<String> processType)
            throws DefinitionDoesNotExistException, DefinitionArchiveFormatException, DefinitionNameMismatchException {
        ProcessDefinition deployedDefinition = getDefinition(definitionId);
        checkPermissionAllowed(subject, deployedDefinition, DefinitionPermission.REDEPLOY_DEFINITION);
        if (processArchiveBytes == null) {
            // update only categories
            deployedDefinition.getDBImpl().setCategories(processType);
            deploymentDAO.update(deployedDefinition.getDBImpl());
            return new WfDefinition(deployedDefinition);
        }
        ProcessDefinition definition = parseProcessDefinition(processArchiveBytes);
        if (!deployedDefinition.getName().equals(definition.getName())) {
            throw new DefinitionNameMismatchException("Process archive contains definition for process " + definition.getName()
                    + " mismatch with deployed process name " + deployedDefinition.getName(), definition.getName(), deployedDefinition.getName());
        }
        definition.getDBImpl().setCategories(processType);
        deploymentDAO.deploy(definition.getDBImpl(), deployedDefinition.getDBImpl());
        for (Executor executor : permissionDAO.getExecutorsWithPermission(deployedDefinition)) {
            List<Permission> permissions = permissionDAO.getOwnPermissions(executor, deployedDefinition);
            permissionDAO.setPermissions(executor, permissions, definition);
        }
        log.debug("Process definition " + deployedDefinition + " was successfully redeployed");
        return new WfDefinition(definition);
    }

    public WfDefinition getLatestProcessDefinition(Subject subject, String definitionName) throws AuthenticationException, AuthorizationException,
            DefinitionDoesNotExistException {
        ProcessDefinition definition = getLatestDefinition(definitionName);
        checkPermissionAllowed(subject, definition, Permission.READ);
        return new WfDefinition(definition);
    }

    public WfDefinition getProcessDefinition(Subject subject, Long definitionId) throws AuthenticationException, AuthorizationException,
            DefinitionDoesNotExistException {
        ProcessDefinition processDefinition = getDefinition(definitionId);
        checkPermissionAllowed(subject, processDefinition, Permission.READ);
        return new WfDefinition(processDefinition);
    }

    public WfDefinition getProcessDefinitionByProcessId(Subject subject, Long processId) throws AuthenticationException, AuthorizationException,
            ProcessDoesNotExistException {
        Process process = processDAO.getNotNull(processId);
        ProcessDefinition processDefinition = getDefinition(process);
        checkPermissionAllowed(subject, processDefinition, Permission.READ);
        return new WfDefinition(processDefinition);
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
        SubprocessPermissionVisitor operation = new SubprocessPermissionVisitor(subject, processDefinitionLoader);
        return getDefinitionGraphElements(subject, definitionId, operation);
    }

    public List<WfDefinition> getLatestProcessDefinitions(Subject subject, BatchPresentation batchPresentation) throws AuthenticationException {
        List<Number> latestDefinitions = new BatchPresentationHibernateCompiler(batchPresentation).getIdentities(null, null, false);
        List<WfDefinition> result = Lists.newArrayListWithExpectedSize(latestDefinitions.size());
        for (Number definitionId : latestDefinitions) {
            ProcessDefinition processDefinition = getDefinition(definitionId.longValue());
            if (isPermissionAllowed(subject, processDefinition, Permission.READ)) {
                result.add(new WfDefinition(processDefinition));
            }
        }
        return result;
    }

    public List<WfDefinition> getProcessDefinitionHistory(Subject subject, String name) throws AuthenticationException {
        List<Deployment> deploymentVersions = deploymentDAO.findAllDeploymentVersions(name);
        List<WfDefinition> result = Lists.newArrayListWithExpectedSize(deploymentVersions.size());
        for (Deployment deployment : deploymentVersions) {
            if (isPermissionAllowed(subject, deployment, Permission.READ)) {
                result.add(new WfDefinition(deployment));
            }
        }
        return result;
    }

    public void undeployProcessDefinition(Subject subject, String definitionName) throws AuthenticationException, AuthorizationException,
            DefinitionDoesNotExistException, SuperProcessExistsException {
        Preconditions.checkNotNull(definitionName, "definitionName must be specified.");
        ProcessDefinition processDefinition = getLatestDefinition(definitionName);
        checkPermissionAllowed(subject, processDefinition, DefinitionPermission.UNDEPLOY_DEFINITION);
        ProcessFilter filter = new ProcessFilter();
        filter.setDefinitionName(definitionName);
        List<Process> processes = processDAO.getProcesses(filter);
        for (Process process : processes) {
            if (nodeProcessDAO.getNodeProcessByChild(process.getId()) != null) {
                throw new SuperProcessExistsException(definitionName, nodeProcessDAO.getNodeProcessByChild(process.getId()).getProcess()
                        .getDefinition().getName());
            }
        }
        deleteProcessDefinitionsByName(subject, definitionName);
        log.debug("Process definition " + processDefinition + " was undeployed");
    }

    /**
     * Deletes all Process Definitions from the database
     */
    private void deleteProcessDefinitionsByName(Subject subject, String definitionName) throws AuthenticationException {
        Long actorId = SubjectPrincipalsHelper.getActor(subject).getId();
        List<Deployment> deployments = deploymentDAO.findAllDeploymentVersions(definitionName);
        for (Deployment deployment : deployments) {
            // delete all the processes of this definition
            List<Process> processes = processDAO.findAllProcesses(deployment.getId());
            for (Process process : processes) {
                deleteProcess(process);
            }
            deploymentDAO.delete(deployment);
            systemLogDAO.create(new ProcessDefinitionDeleteLog(actorId, definitionName, deployment.getVersion()));
        }
    }

    public Interaction getInteraction(Subject subject, Long taskId) throws AuthorizationException, AuthenticationException, TaskDoesNotExistException {
        try {
            Task task = taskDAO.getNotNull(taskId);
            ProcessDefinition definition = getDefinition(task);
            if (!isPermissionAllowed(subject, definition, DefinitionPermission.READ)) {
                checkCanParticipate(subject, task, null);
            }
            return definition.getInteractionNotNull(task.getName());
        } catch (DefinitionDoesNotExistException e) {
            throw new InternalApplicationException(e);
        }
    }

    public List<String> getOutputTransitionNames(Subject subject, Long definitionId, Long taskId) throws AuthenticationException,
            TaskDoesNotExistException {
        try {
            List<Transition> transitions;
            if (definitionId != null) {
                ProcessDefinition processDefinition = getDefinition(definitionId);
                transitions = processDefinition.getStartStateNotNull().getLeavingTransitions();
            } else {
                Task task = taskDAO.getNotNull(taskId);
                ProcessDefinition processDefinition = getDefinition(task);
                transitions = task.getTask(processDefinition).getNode().getLeavingTransitions();
            }
            List<String> result = new ArrayList<String>();
            for (Transition transition : transitions) {
                result.add(transition.getName());
            }
            return result;
        } catch (DefinitionDoesNotExistException e) {
            throw new InternalApplicationException(e);
        }
    }

    public byte[] getFile(Subject subject, Long definitionId, String fileName) throws AuthenticationException, DefinitionDoesNotExistException,
            AuthorizationException {
        ProcessDefinition definition = getDefinition(definitionId);
        if (!JpdlProcessArchive.UNSECURED_FILE_NAMES.contains(fileName)) {
            checkPermissionAllowed(subject, definition, DefinitionPermission.READ);
        }
        if (JpdlProcessArchive.PAR_FILE.equals(fileName)) {
            return definition.getDBImpl().getContent();
        }
        return definition.getFileData(fileName);
    }

    public Interaction getStartInteraction(Subject subject, Long definitionId) throws AuthorizationException, AuthenticationException,
            DefinitionDoesNotExistException {
        ProcessDefinition definition = getDefinition(definitionId);
        checkPermissionAllowed(subject, definition, DefinitionPermission.READ);
        Interaction interaction = definition.getInteractionNotNull(definition.getStartStateNotNull().getName());
        Map<String, Object> defaultValues = definition.getDefaultVariableValues();
        for (Map.Entry<String, Object> entry : defaultValues.entrySet()) {
            interaction.getDefaultVariableValues().put(entry.getKey(), entry.getValue());
        }
        return interaction;
    }

    private static final String DEFINITION_NAME_SWIMLANE_NAME_SEPARATOR = ".";

    public Set<String> getAllSwimlaneNamesForAllProcessDefinition(Subject subject) throws AuthenticationException {
        Set<String> result = new TreeSet<String>();
        for (ProcessDefinition definition : processDefinitionLoader.getLatestProcessDefinitions()) {
            if (isPermissionAllowed(subject, definition, DefinitionPermission.READ)) {
                for (String swimlaneName : definition.getSwimlanes().keySet()) {
                    result.add(definition.getName() + DEFINITION_NAME_SWIMLANE_NAME_SEPARATOR + swimlaneName);
                }
            }
        }
        return result;
    }

    public List<VariableDefinition> getProcessDefinitionVariables(Subject subject, Long definitionId) throws DefinitionDoesNotExistException,
            AuthenticationException, AuthorizationException {
        ProcessDefinition definition = getDefinition(definitionId);
        checkPermissionAllowed(subject, definition, DefinitionPermission.READ);
        return definition.getVariables();
    }

    private ProcessDefinition parseProcessDefinition(byte[] data) {
        Deployment parDeployment = new Deployment();
        parDeployment.setLanguage(Language.JPDL);
        parDeployment.setContent(data);
        JpdlProcessArchive archive = new JpdlProcessArchive(parDeployment);
        return archive.parseProcessDefinition();
    }

}
