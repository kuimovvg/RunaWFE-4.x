package ru.runa.wfe.task;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Autowired;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.definition.dao.ProcessDefinitionLoader;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.form.Interaction;
import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.hibernate.BatchPresentationHibernateCompiler;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.ss.Substitution;
import ru.runa.wfe.ss.SubstitutionCriteria;
import ru.runa.wfe.ss.TerminatorSubstitution;
import ru.runa.wfe.ss.logic.SubstitutionLogic;
import ru.runa.wfe.task.cache.TaskCache;
import ru.runa.wfe.task.cache.TaskCacheCtrl;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.task.dto.WfTaskFactory;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.ExecutorDoesNotExistException;
import ru.runa.wfe.user.Group;
import ru.runa.wfe.user.dao.ExecutorDAO;

import com.google.common.base.Objects;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;

/**
 * Task list builder component.
 * 
 * @author Dofs
 * @since 4.0
 */
public class TasklistBuilder {
    private TaskCache taskCache = TaskCacheCtrl.getInstance();
    @Autowired
    private WfTaskFactory taskObjectFactory;
    @Autowired
    private ExecutorDAO executorDAO;
    @Autowired
    private SubstitutionLogic substitutionLogic;
    @Autowired
    private ProcessDefinitionLoader processDefinitionLoader;

    public List<WfTask> getTasks(Actor actor, BatchPresentation batchPresentation) throws AuthenticationException {
        try {
            List<WfTask> result = taskCache.getTasks(actor.getId(), batchPresentation);
            if (result != null) {
                return result;
            }
            int cacheVersion = taskCache.getCacheVersion();
            result = Lists.newArrayList();
            Set<Executor> executorsToGetTasks = getActorsToGetTasks(actor, false);
            Set<Executor> executorsToGetTasksSub = getSubstitutableExecutors(actor);
            List<Task> tasks = new BatchPresentationHibernateCompiler(batchPresentation).getBatch(executorsToGetTasksSub, "executor", false);
            for (Task task : tasks) {
                Executor taskExecutor = task.getExecutor();
                ProcessDefinition processDefinition = processDefinitionLoader.getDefinition(task);
                Interaction interaction = processDefinition.getInteractionNotNull(task.getNodeId());
                String formType = (interaction == null ? null : interaction.getType());
                if (executorsToGetTasks.contains(taskExecutor)) {
                    // Our task - get.
                    result.add(taskObjectFactory.create(task, actor, formType));
                    continue;
                }
                if (processDefinition.isSubsitutionIgnoredFor(task)) {
                    continue;
                }
                ExecutionContext executionContext = new ExecutionContext(processDefinition, task);
                // Task to substitute (may be)
                if (taskExecutor instanceof Actor) {
                    if (isCanSubToken(executionContext, task, (Actor) taskExecutor, actor)) {
                        result.add(taskObjectFactory.create(task, (Actor) taskExecutor, formType));
                    }
                } else {
                    for (Actor actInGroup : executorDAO.getGroupActors((Group) taskExecutor)) {
                        if (isCanSubToken(executionContext, task, actInGroup, actor)) {
                            result.add(taskObjectFactory.create(task, actInGroup, formType));
                            break;
                        }
                    }
                }
            }
            taskCache.setTasks(cacheVersion, actor.getId(), batchPresentation, result);
            return result;
        } catch (Exception e) {
            Throwables.propagateIfInstanceOf(e, InternalApplicationException.class);
            Throwables.propagateIfInstanceOf(e, AuthenticationException.class);
            throw Throwables.propagate(e);
        }
    }

    private Set<Executor> getActorsToGetTasks(Actor actor, boolean inactiveGroup) {
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

    private Set<Executor> getSubstitutableExecutors(Actor actor) throws ExecutorDoesNotExistException {
        Set<Executor> executorsToGetTasks = getActorsToGetTasks(actor, false);
        Set<Executor> executorsToGetTasksSub = new HashSet<Executor>();
        executorsToGetTasksSub.addAll(executorsToGetTasks);
        for (Long substitutedActor : substitutionLogic.getSubstituted(actor)) {
            executorsToGetTasksSub.addAll(getActorsToGetTasks(executorDAO.getActor(substitutedActor), true));
        }
        return executorsToGetTasksSub;
    }

    private boolean isCanSubToken(ExecutionContext executionContext, Task task, Actor asActor, Actor substitutorActor) {
        TreeMap<Substitution, Set<Long>> mapOfSubstitionRule = substitutionLogic.getSubstitutors(asActor);
        for (Map.Entry<Substitution, Set<Long>> substitutionRule : mapOfSubstitionRule.entrySet()) {
            Substitution substitution = substitutionRule.getKey();
            SubstitutionCriteria criteria = substitution.getCriteria();
            Set<Long> substitutors = substitutionRule.getValue();
            if (substitution instanceof TerminatorSubstitution) {
                if (criteria == null || criteria.isSatisfied(executionContext, task, asActor, substitutorActor)) {
                    return false;
                }
                continue;
            }
            boolean canISubstitute = false;
            boolean substitutionApplies = false;
            for (Long actorId : substitutors) {
                Actor actor = executorDAO.getActor(actorId);
                if (actor.isActive() && (criteria == null || criteria.isSatisfied(executionContext, task, asActor, actor))) {
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
