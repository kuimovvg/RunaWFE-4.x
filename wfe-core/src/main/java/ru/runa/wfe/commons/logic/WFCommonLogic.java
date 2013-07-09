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

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.audit.dao.ProcessLogDAO;
import ru.runa.wfe.definition.dao.DeploymentDAO;
import ru.runa.wfe.definition.dao.ProcessDefinitionLoader;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.execution.ProcessPermission;
import ru.runa.wfe.execution.dao.NodeProcessDAO;
import ru.runa.wfe.execution.dao.TokenDAO;
import ru.runa.wfe.form.Interaction;
import ru.runa.wfe.graph.image.GraphElementPresentationBuilder;
import ru.runa.wfe.graph.view.GraphElementPresentation;
import ru.runa.wfe.graph.view.GraphElementPresentationVisitor;
import ru.runa.wfe.job.dao.JobDAO;
import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.ss.logic.SubstitutionLogic;
import ru.runa.wfe.task.Task;
import ru.runa.wfe.task.dao.TaskDAO;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.ExecutorDoesNotExistException;
import ru.runa.wfe.user.Group;
import ru.runa.wfe.user.User;
import ru.runa.wfe.validation.ValidationException;
import ru.runa.wfe.validation.ValidatorContext;
import ru.runa.wfe.validation.ValidatorManager;
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
    protected NodeProcessDAO nodeProcessDAO;
    @Autowired
    protected TaskDAO taskDAO;
    @Autowired
    protected VariableDAO variableDAO;
    @Autowired
    protected ProcessLogDAO processLogDAO;
    @Autowired
    protected JobDAO jobDAO;
    @Autowired
    protected TokenDAO tokenDAO;

    public ProcessDefinition getDefinition(Long processDefinitionId) {
        return processDefinitionLoader.getDefinition(processDefinitionId);
    }

    public ProcessDefinition getDefinition(Process process) {
        return processDefinitionLoader.getDefinition(process);
    }

    public ProcessDefinition getDefinition(Task task) {
        return processDefinitionLoader.getDefinition(task);
    }

    protected ProcessDefinition getLatestDefinition(String definitionName) {
        return processDefinitionLoader.getLatestDefinition(definitionName);
    }

    protected void validateVariables(User user, ProcessDefinition processDefinition, String nodeId, Map<String, Object> variables,
            IVariableProvider variableProvider) throws ValidationException {
        Interaction interaction = processDefinition.getInteractionNotNull(nodeId);
        if (interaction.getValidationData() != null) {
            ValidatorContext context = ValidatorManager.getInstance().validate(user, processDefinition, interaction.getValidationData(), variables,
                    variableProvider);
            if (context.hasGlobalErrors() || context.hasFieldErrors()) {
                throw new ValidationException(context.getFieldErrors(), context.getGlobalErrors());
            }
        }
    }

    private boolean canParticipateAsSubstitutor(Actor actor, Task task) {
        try {
            Set<Actor> substitutedActors = substitutionLogic.getSubstituted(actor);
            Executor taskExecutor = task.getExecutor();
            if (taskExecutor instanceof Actor) {
                return substitutedActors.contains(taskExecutor);
            } else {
                for (Actor assignedActor : getAssignedActors(task)) {
                    if (substitutedActors.contains(assignedActor)) {
                        return true;
                    }
                }
            }
        } catch (ExecutorDoesNotExistException e) {
            log.error("canParticipateAsSubstitutor: " + e);
        }
        return false;
    }

    protected void checkCanParticipate(Actor actor, Task task) {
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
            log.debug("Group " + groupActors + " does not contains interested " + actor);
        }
        if (canParticipateAsSubstitutor(actor, task)) {
            return;
        }
        throw new AuthorizationException(actor + " has no pemission to participate as " + taskExecutor + " in task " + task);
    }

    // TODO unused: variable permissions check
    protected void checkReadToVariablesAllowed(User user, Task task) {
        if (isPermissionAllowed(user, task.getProcess(), ProcessPermission.READ)) {
            return;
        }
        checkCanParticipate(user.getActor(), task);
    }

    private Set<Actor> getAssignedActors(Task task) {
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
        permissionDAO.deleteAllPermissions(process);
        List<Process> subProcesses = nodeProcessDAO.getSubprocesses(process);
        nodeProcessDAO.deleteByProcess(process);
        for (Process subProcess : subProcesses) {
            log.debug("deleting sub process " + subProcess.getId());
            deleteProcess(subProcess);
        }
        processLogDAO.deleteAll(process.getId());
        jobDAO.deleteAll(process);
        variableDAO.deleteAll(process);
        processDAO.delete(process);
    }

    /**
     * Loads graph presentation elements for process definition.
     * 
     * @param user
     *            Current user.
     * @param id
     *            Identity of process definition, which presentation elements
     *            must be loaded.
     * @param visitor
     *            Operation, which must be applied to loaded graph elements, or
     *            null, if nothing to apply.
     * @return List of graph presentation elements.
     */
    public List<GraphElementPresentation> getDefinitionGraphElements(User user, ProcessDefinition definition, GraphElementPresentationVisitor visitor) {
        List<GraphElementPresentation> result = GraphElementPresentationBuilder.createElements(definition);
        if (visitor != null) {
            for (GraphElementPresentation elementPresentation : result) {
                elementPresentation.visit(visitor);
            }
        }
        return result;
    }
}
