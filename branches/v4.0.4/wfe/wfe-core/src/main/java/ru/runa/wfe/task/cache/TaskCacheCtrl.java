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
package ru.runa.wfe.task.cache;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.type.Type;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.cache.BaseCacheCtrl;
import ru.runa.wfe.commons.cache.CachingLogic;
import ru.runa.wfe.commons.cache.Change;
import ru.runa.wfe.commons.cache.TaskChangeListener;
import ru.runa.wfe.execution.Swimlane;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.ss.Substitution;
import ru.runa.wfe.ss.SubstitutionCriteria;
import ru.runa.wfe.ss.TerminatorSubstitution;
import ru.runa.wfe.ss.cache.SubstitutionCacheCtrl;
import ru.runa.wfe.task.Task;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.ExecutorGroupMembership;
import ru.runa.wfe.user.Group;
import ru.runa.wfe.user.TemporaryGroup;
import ru.runa.wfe.user.cache.ExecutorCacheCtrl;
import ru.runa.wfe.user.cache.ExecutorCacheImpl;

import com.google.common.base.Objects;
import com.google.common.collect.Sets;

public class TaskCacheCtrl extends BaseCacheCtrl<TaskCacheImpl> implements TaskChangeListener, TaskCache {
    private static final TaskCacheCtrl instance = new TaskCacheCtrl();
    private static final String EXECUTOR_PROPERTY_NAME = "executor";

    private TaskCacheCtrl() {
        CachingLogic.registerChangeListener(this);
    }

    public static TaskCacheCtrl getInstance() {
        return instance;
    }

    @Override
    public TaskCacheImpl buildCache() {
        return new TaskCacheImpl();
    }

    @Override
    public void doOnChange(Object object, Change change, Object[] currentState, Object[] previousState, String[] propertyNames, Type[] types) {
        if (!isSmartCache()) {
            uninitialize(object, change);
            return;
        }
        if (object instanceof Task) {
            int idx = 0;
            while (!propertyNames[idx].equals(EXECUTOR_PROPERTY_NAME)) {
                ++idx;
            }
            if (idx == propertyNames.length) {
                throw new InternalApplicationException("No '" + EXECUTOR_PROPERTY_NAME + "' found in " + object);
            }
            clearCacheForActors((Executor) currentState[idx], change);
            if (previousState != null) {
                clearCacheForActors((Executor) previousState[idx], change);
            }
            return;
        }
        if (object instanceof Swimlane) {
            int idx = 0;
            while (!propertyNames[idx].equals(EXECUTOR_PROPERTY_NAME)) {
                ++idx;
            }
            if (idx == propertyNames.length) {
                throw new InternalApplicationException("No '" + EXECUTOR_PROPERTY_NAME + "' found in " + object);
            }
            clearCacheForActors((Executor) currentState[idx], change);
            if (previousState != null) {
                clearCacheForActors((Executor) previousState[idx], change);
            }
            return;
        }
        if (object instanceof ExecutorGroupMembership) {
            ExecutorGroupMembership membership = (ExecutorGroupMembership) object;
            clearCacheForActors(membership.getExecutor(), change);
            return;
        }
        if (object instanceof Actor) {
            if (change == Change.UPDATE) {
                int activePropertyIndex = 0;
                for (int i = 0; i < propertyNames.length; i++) {
                    if (propertyNames[i].equals("active")) {
                        activePropertyIndex = i;
                        break;
                    }
                }
                if (previousState != null && !Objects.equal(previousState[activePropertyIndex], currentState[activePropertyIndex])) {
                    // TODO clear cache for affected actors only
                    uninitialize(object, change);
                }
            }
            return;
        }
        if (object instanceof Substitution) {
            Substitution substitution = (Substitution) object;
            ExecutorCacheImpl executorCache = ExecutorCacheCtrl.getInstance().getCache();
            if (executorCache == null) {
                uninitialize(object, change);
                return;
            }
            Actor actor = (Actor) executorCache.getExecutor(substitution.getActorId());
            // actor can be null if created in same transaction
            if (actor != null && !actor.isActive()) {
                clearCacheForActors(actor, change);
            }
            return;
        }
        if (object instanceof SubstitutionCriteria) {
            // TODO clear cache for affected actors only
            uninitialize(object, change);
            return;
        }
        throw new InternalApplicationException("Unexpected object " + object);
    }

    /**
     * Do not wait until cache is released
     * 
     * @return cache or <code>null</code> in case of locked
     */
    private TaskCacheImpl getCacheNoWait() {
        TaskCacheImpl cache = getCache();
        if (cache == null && !isLocked()) {
            cache = CachingLogic.getCacheImpl(this);
        }
        return cache;
    }

    @Override
    public List<WfTask> getTasks(Long actorId, BatchPresentation batchPresentation) {
        TaskCacheImpl cache = getCacheNoWait();
        if (cache == null) {
            return null;
        }
        return cache.getTasks(actorId, batchPresentation);
    }

    @Override
    public void setTasks(int cacheVersion, Long actorId, BatchPresentation batchPresentation, List<WfTask> tasks) {
        TaskCacheImpl cache = getCacheNoWait();
        if (cache == null) {
            return;
        }
        cache.setTasks(cacheVersion, actorId, batchPresentation, tasks);
    }

    @Override
    public int getCacheVersion() {
        TaskCacheImpl cache = getCacheNoWait();
        if (cache == null) {
            return 0;
        }
        return cache.getCacheVersion();
    }

    private void clearCacheForActors(Executor executor, Change change) {
        TaskCacheImpl cache = getCache();
        if (cache == null) {
            return;
        }
        if (executor == null) {
            return;
        }
        Set<Actor> actors;
        if (executor instanceof Group) {
            // TODO make caches retrieval not blocking and remove
            // uninitialize(...)
            // call for this cache
            ExecutorCacheImpl executorCache = ExecutorCacheCtrl.getInstance().getCache();
            if (executorCache == null) {
                if (executor instanceof TemporaryGroup) {
                    log.debug("Ignored cache recalc [executorCache == null] on " + change + " with " + executor);
                    return;
                }
                uninitialize(executor, change);
                return;
            }
            actors = executorCache.getGroupActorsAll((Group) executor);
            if (actors == null) {
                if (executor instanceof TemporaryGroup) {
                    log.debug("Ignored cache recalc [actors == null] on " + change + " with " + executor);
                    return;
                }
                log.error("No group actors found in cache for " + executor);
                uninitialize(executor, change);
                return;
            }
        } else {
            actors = Sets.newHashSet((Actor) executor);
        }
        for (Actor actor : actors) {
            cache.clearActorTasks(actor.getId());
            Map<Substitution, Set<Actor>> substitutors = SubstitutionCacheCtrl.getInstance().tryToGetSubstitutors(actor);
            if (substitutors == null) {
                uninitialize(this, change);
                return;
            }
            for (Map.Entry<Substitution, Set<Actor>> entry : substitutors.entrySet()) {
                if (entry.getKey() instanceof TerminatorSubstitution) {
                    continue;
                }
                for (Actor substitutorActor : entry.getValue()) {
                    cache.clearActorTasks(substitutorActor.getId());
                }
            }
        }
    }

}
