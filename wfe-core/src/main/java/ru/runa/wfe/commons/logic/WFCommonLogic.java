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
package ru.runa.wfe.commons.logic;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.security.auth.Subject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.audit.dao.ProcessLogDAO;
import ru.runa.wfe.definition.DefinitionDoesNotExistException;
import ru.runa.wfe.definition.DefinitionPermission;
import ru.runa.wfe.definition.dao.DeploymentDAO;
import ru.runa.wfe.definition.dao.ProcessDefinitionLoader;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.execution.ProcessPermission;
import ru.runa.wfe.execution.dao.NodeProcessDAO;
import ru.runa.wfe.execution.dao.ProcessDAO;
import ru.runa.wfe.form.Interaction;
import ru.runa.wfe.graph.image.GraphElementPresentationBuilder;
import ru.runa.wfe.graph.view.GraphElementPresentation;
import ru.runa.wfe.graph.view.GraphElementPresentationVisitor;
import ru.runa.wfe.job.dao.JobDAO;
import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.security.auth.SubjectPrincipalsHelper;
import ru.runa.wfe.ss.logic.SubstitutionLogic;
import ru.runa.wfe.task.Task;
import ru.runa.wfe.task.TaskDoesNotExistException;
import ru.runa.wfe.task.dao.TaskDAO;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.ExecutorDoesNotExistException;
import ru.runa.wfe.user.Group;
import ru.runa.wfe.validation.ValidatorContext;
import ru.runa.wfe.validation.ValidatorManager;
import ru.runa.wfe.validation.impl.ValidationException;
import ru.runa.wfe.var.IVariableProvider;
import ru.runa.wfe.var.dao.VariableDAO;

import com.google.common.base.Objects;
import com.google.common.collect.Sets;

/**
 * Created on 15.03.2005
 */
public class WFCommonLogic extends CommonLogic {
    private static final Log log = LogFactory.getLog(WFCommonLogic.class);

    @Autowired
    protected ProcessDefinitionLoader processDefinitionLoader;
    @Autowired
    protected SubstitutionLogic substitutionLogic;

    @Autowired
    protected DeploymentDAO deploymentDAO;
    @Autowired
    protected ProcessDAO processDAO;
    @Autowired
    protected NodeProcessDAO nodeProcessDAO;
    @Autowired
    protected TaskDAO taskDAO;
    @Autowired
    protected VariableDAO variableDAO;
    @Autowired
    protected ProcessLogDAO processLogDAO;
    @Autowired
    protected JobDAO jobDAO;

    public ProcessDefinition getDefinition(Long processDefinitionId) {
        return processDefinitionLoader.getDefinition(processDefinitionId);
    }

    public ProcessDefinition getDefinition(Process process) {
        return processDefinitionLoader.getDefinition(process);
    }

    public ProcessDefinition getDefinition(Task task) {
        return processDefinitionLoader.getDefinition(task);
    }

    protected ProcessDefinition getLatestDefinition(String definitionName) throws DefinitionDoesNotExistException {
        return processDefinitionLoader.getLatestDefinition(definitionName);
    }

    protected void validateVariables(ProcessDefinition processDefinition, String nodeId, IVariableProvider variableProvider)
            throws ValidationException {
        Interaction interaction = processDefinition.getInteractionNotNull(nodeId);
        if (interaction.getValidationData() != null) {
            InputStream is = new ByteArrayInputStream(interaction.getValidationData());
            ValidatorContext context = ValidatorManager.getInstance().validate(is, variableProvider);
            if (context.hasActionErrors() || context.hasFieldErrors()) {
                throw new ValidationException(context.getFieldErrors(), context.getGlobalErrors());
            }
        }
    }

    protected boolean canParticipateAsSubstitutor(Subject subject, Task task) throws AuthenticationException {
        Actor actor = SubjectPrincipalsHelper.getActor(subject);
        try {
            Set<Long> canSubIds = substitutionLogic.getSubstituted(actor);
            Set<Actor> canSub = new HashSet<Actor>();
            for (Long id : canSubIds) {
                canSub.add(executorDAO.getActor(id));
            }
            Executor taskExecutor = task.getExecutor();
            if (taskExecutor instanceof Actor) {
                return canSub.contains(taskExecutor);
            } else {
                for (Actor assignActor : getAssignedActors(task)) {
                    if (canSub.contains(assignActor)) {
                        return true;
                    }
                }
            }
        } catch (ExecutorDoesNotExistException e) {
        }
        return false;
    }

    protected void checkCanParticipate(Subject subject, Task task, Actor targetActor) throws AuthorizationException, TaskDoesNotExistException {
        Actor actor = SubjectPrincipalsHelper.getActor(subject);
        if (targetActor == null) {
            targetActor = actor;
        }
        Executor taskExecutor = task.getExecutor();
        if (taskExecutor == null) {
            throw new AuthorizationException("Unable to participate in unassigned task");
        }
        if (taskExecutor instanceof Actor) {
            if (Objects.equal(actor, taskExecutor)) {
                return;
            }
        } else {
            Set<Actor> groupActors = executorDAO.getGroupActors((Group) taskExecutor);
            if (groupActors.contains(actor)) {
                return;
            }
        }
        if (canParticipateAsSubstitutor(subject, task)) {
            return;
        }
        throw new AuthorizationException("Executor " + actor + " has no pemission to participate as " + targetActor + " in task " + task);
    }

    protected void checkReadToVariablesAllowed(Subject subject, Task task) throws AuthorizationException, TaskDoesNotExistException {
        if (isPermissionAllowed(subject, task.getProcess(), ProcessPermission.READ)) {
            return;
        }
        checkCanParticipate(subject, task, null);
    }

    protected Set<Actor> getAssignedActors(Task task) throws ExecutorDoesNotExistException {
        if (task.getExecutor() == null) {
            throw new InternalApplicationException("Unassigned tasks can't be in processing");
        }
        if (task.getExecutor() instanceof Actor) {
            return Sets.newHashSet((Actor) task.getExecutor());
        } else {
            return executorDAO.getGroupActors((Group) task.getExecutor());
        }
    }

    protected void deleteProcess(Process process) {
        log.debug("deleting process " + process);
        // delete subprocessees
        List<Process> subProcesses = nodeProcessDAO.getSubprocesses(process);
        for (Process subProcess : subProcesses) {
            log.debug("deleting sub process " + subProcess.getId());
            deleteProcess(subProcess);
        }
        processLogDAO.deleteAll(process.getId());
        jobDAO.deleteJobs(process);
        variableDAO.deleteAll(process);

        // delete started subprocesses
        log.debug("deleting started subprocesses for process " + process.getId());
        nodeProcessDAO.deleteByProcess(process.getId());
        processDAO.delete(process.getId());
    }

    /**
     * Loads graph presentation elements for process definition.
     * 
     * @param subject
     *            Current subject.
     * @param id
     *            Identity of process definition, which presentation elements
     *            must be loaded.
     * @param visitor
     *            Operation, which must be applied to loaded graph elements, or
     *            null, if nothing to apply.
     * @return List of graph presentation elements.
     */
    public List<GraphElementPresentation> getDefinitionGraphElements(Subject subject, Long id, GraphElementPresentationVisitor visitor) {
        try {
            ProcessDefinition definition = getDefinition(id);
            checkPermissionAllowed(subject, definition, DefinitionPermission.READ);
            List<GraphElementPresentation> result = GraphElementPresentationBuilder.createElements(definition);
            if (visitor != null) {
                for (GraphElementPresentation elementPresentation : result) {
                    elementPresentation.visit(visitor);
                }
            }
            return result;
        } catch (Exception e) {
            log.warn("Unable to draw diagram", e);
            throw new InternalApplicationException(e);
        }
    }

}
