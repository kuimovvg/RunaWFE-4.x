package ru.runa.wfe.task;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import ru.runa.wfe.definition.dao.ProcessDefinitionLoader;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.hibernate.BatchPresentationHibernateCompiler;
import ru.runa.wfe.ss.Substitution;
import ru.runa.wfe.ss.SubstitutionCriteria;
import ru.runa.wfe.ss.TerminatorSubstitution;
import ru.runa.wfe.ss.logic.SubstitutionLogic;
import ru.runa.wfe.task.cache.TaskCache;
import ru.runa.wfe.task.cache.TaskCacheCtrl;
import ru.runa.wfe.task.dao.TaskDAO;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.task.dto.WfTaskFactory;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.Group;
import ru.runa.wfe.user.dao.ExecutorDAO;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * Task list builder component.
 * 
 * @author Dofs
 * @since 4.0
 */
public class TasklistBuilder {
    private static final Log log = LogFactory.getLog(TasklistBuilder.class);
    private TaskCache taskCache = TaskCacheCtrl.getInstance();
    @Autowired
    private WfTaskFactory taskObjectFactory;
    @Autowired
    private ExecutorDAO executorDAO;
    @Autowired
    private SubstitutionLogic substitutionLogic;
    @Autowired
    private ProcessDefinitionLoader processDefinitionLoader;
    @Autowired
    private TaskDAO taskDAO;

    public List<WfTask> getTasks(Actor actor, BatchPresentation batchPresentation) {
        Preconditions.checkNotNull(batchPresentation, "batchPresentation");
        List<WfTask> result = taskCache.getTasks(actor.getId(), batchPresentation);
        if (result != null) {
            return result;
        }
        result = Lists.newArrayList();
        Set<Executor> executorsToGetTasksByMembership = getExecutorsToGetTasks(actor, false);
        Set<Executor> executorsToGetTasks = Sets.newHashSet(executorsToGetTasksByMembership);
        Set<Actor> substitutedActors = substitutionLogic.getSubstituted(actor);
        log.debug("Building tasklist for " + actor + " with BP '" + batchPresentation.getName() + "' with substituted: " + substitutedActors);
        for (Actor substitutedActor : substitutedActors) {
            executorsToGetTasks.addAll(getExecutorsToGetTasks(substitutedActor, true));
        }
        List<Task> tasks = new BatchPresentationHibernateCompiler(batchPresentation).getBatch(executorsToGetTasks, "executor", false);
        for (Task task : tasks) {
            try {
                Executor taskExecutor = task.getExecutor();
                ProcessDefinition processDefinition = processDefinitionLoader.getDefinition(task.getProcess());
                if (executorsToGetTasksByMembership.contains(taskExecutor)) {
                    log.debug(task + " is acquired by membership rules");
                    result.add(taskObjectFactory.create(task, actor, false));
                    continue;
                }
                if (processDefinition.ignoreSubsitutionRulesForTask(task)) {
                    log.debug(task + " is ignored due to ignore subsitution rule");
                    continue;
                }
                ExecutionContext executionContext = new ExecutionContext(processDefinition, task);
                log.debug("Whether " + task + " should be acquired by substitution rules?");
                boolean firstOpen = !task.getOpenedByExecutorIds().contains(actor.getId());
                if (taskExecutor instanceof Actor) {
                    if (isTaskAcceptableBySubstitutionRules(executionContext, task, (Actor) taskExecutor, actor)) {
                        log.debug(task + " is acquired by substitution rules [by actor]");
                        result.add(taskObjectFactory.create(task, (Actor) taskExecutor, true, firstOpen));
                    }
                } else {
                    for (Actor groupActor : executorDAO.getGroupActors((Group) taskExecutor)) {
                        if (isTaskAcceptableBySubstitutionRules(executionContext, task, groupActor, actor)) {
                            log.debug(task + " is acquired by substitution rules [by group]");
                            result.add(taskObjectFactory.create(task, groupActor, true, firstOpen));
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                if (taskDAO.get(task.getId()) == null) {
                    log.debug(task + " has been completed", e);
                    continue;
                }
                log.error("Unable to build " + task, e);
            }
        }
        taskCache.setTasks(taskCache.getCacheVersion(), actor.getId(), batchPresentation, result);
        return result;
    }

    private Set<Executor> getExecutorsToGetTasks(Actor actor, boolean addOnlyInactiveGroups) {
        Set<Executor> executors = new HashSet<Executor>();
        executors.add(actor);
        Set<Group> upperGroups = executorDAO.getExecutorParentsAll(actor);
        if (addOnlyInactiveGroups) {
            for (Group group : upperGroups) {
                if (!hasActiveActorInGroup(group)) {
                    executors.add(group);
                }
            }
        } else {
            executors.addAll(upperGroups);
        }
        return executors;
    }

    private boolean isTaskAcceptableBySubstitutionRules(ExecutionContext executionContext, Task task, Actor assignedActor, Actor substitutorActor) {
        TreeMap<Substitution, Set<Actor>> mapOfSubstitionRule = substitutionLogic.getSubstitutors(assignedActor);
        for (Map.Entry<Substitution, Set<Actor>> substitutionRule : mapOfSubstitionRule.entrySet()) {
            Substitution substitution = substitutionRule.getKey();
            SubstitutionCriteria criteria = substitution.getCriteria();
            if (substitution instanceof TerminatorSubstitution) {
                if (criteria == null || criteria.isSatisfied(executionContext, task, assignedActor, substitutorActor)) {
                    log.debug(task + " is ignored due to acceptable terminator rule");
                    return false;
                }
                continue;
            }
            boolean canISubstitute = false;
            boolean substitutionApplies = false;
            for (Actor actor : substitutionRule.getValue()) {
                if (actor.isActive() && (criteria == null || criteria.isSatisfied(executionContext, task, assignedActor, actor))) {
                    log.debug("To " + task + " is applied " + substitutionRule.getKey());
                    substitutionApplies = true;
                }
                if (Objects.equal(actor, substitutorActor)) {
                    canISubstitute = true;
                }
            }
            if (!substitutionApplies) {
                continue;
            }
            return canISubstitute;
        }
        log.debug(task + " is ignored due to no subsitution rule applies: " + mapOfSubstitionRule);
        return false;
    }

    private boolean hasActiveActorInGroup(Group group) {
        for (Actor actor : executorDAO.getGroupActors(group)) {
            if (actor.isActive()) {
                return true;
            }
        }
        return false;
    }

}
