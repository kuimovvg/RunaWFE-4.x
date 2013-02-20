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

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.type.Type;

import ru.runa.wfe.commons.cache.BaseCacheCtrl;
import ru.runa.wfe.commons.cache.CachingLogic;
import ru.runa.wfe.commons.cache.TaskChangeListener;
import ru.runa.wfe.execution.Swimlane;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.ss.Substitution;
import ru.runa.wfe.ss.cache.SubstitutionCacheCtrl;
import ru.runa.wfe.task.Task;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Group;
import ru.runa.wfe.user.cache.ExecutorCacheCtrl;
import ru.runa.wfe.user.cache.ExecutorCacheImpl;

public class TaskCacheCtrl extends BaseCacheCtrl<TaskCacheImpl> implements TaskChangeListener, TaskCache {
    private static final TaskCacheCtrl instance = new TaskCacheCtrl();

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
    public void doOnChange(Object object, Object[] currentState, Object[] previousState, String[] propertyNames, Type[] types) {
        if (!isSmartCache()) {
            uninitialize(object);
            return;
        }
        if (object instanceof Task) {
            int idx = 0; // TODO check this (actorId) -> executor
            while (!propertyNames[idx].equals("actorId")) {
                ++idx;
            }
            if (idx == propertyNames.length) {
                uninitialize(object);
                return;
            }
            clearCacheForActors((String) currentState[idx]);
            if (previousState != null) {
                clearCacheForActors((String) previousState[idx]);
            }
        } else if (object instanceof Swimlane) {
            int idx = 0;
            while (!propertyNames[idx].equals("actorId")) {
                ++idx;
            }
            if (idx == propertyNames.length) {
                uninitialize(object);
                return;
            }
            clearCacheForActors((String) currentState[idx]);
            if (previousState != null) {
                clearCacheForActors((String) previousState[idx]);
            }
        } else {
            uninitialize(object);
        }
    }

    @Override
    public List<WfTask> getTasks(Long actorId, BatchPresentation batchPresentation) {
        synchronized (CachingLogic.class) { // Do not wait until cache is
                                            // released
            if (isLocked()) {
                return null;
            }
        }
        return CachingLogic.getCacheImpl(this).getTasks(actorId, batchPresentation);
    }

    @Override
    public void setTasks(int cacheVersion, Long actorId, BatchPresentation batchPresentation, List<WfTask> tasks) {
        synchronized (CachingLogic.class) { // Do not wait until cache is
                                            // released
            if (isLocked()) {
                return;
            }
        }
        CachingLogic.getCacheImpl(this).setTasks(cacheVersion, actorId, batchPresentation, tasks);
    }

    @Override
    public int getCacheVersion() {
        return CachingLogic.getCacheImpl(this).getCacheVersion();
    }

    private void clearCacheForActors(String executorStr) {
        TaskCacheImpl cache = getCache();
        if (cache == null) {
            return;
        }
        if (executorStr == null) {
            return;
        }
        Set<Actor> ex = new HashSet<Actor>();
        try {
            ExecutorCacheImpl executorCache = ExecutorCacheCtrl.getInstance().getCache();
            if (executorCache == null) {
                uninitialize(executorStr);
                return;
            }
            if (executorStr.charAt(0) == 'G') {
                ex = executorCache.getGroupActorsAll((Group) executorCache.getExecutor(Long.parseLong(executorStr.substring(1))));
            } else {
                ex.add(executorCache.getActor(Long.parseLong(executorStr)));
            }
        } catch (Throwable e) {
            uninitialize(executorStr);
            return;
        }
        for (Actor actor : ex) {
            clearActorCache(actor);
        }
    }

    private void clearActorCache(Actor actor) {
        TaskCacheImpl cache = getCache();
        if (cache == null) {
            return;
        }
        cache.clearActorTasks(actor.getId());
        Map<Substitution, Set<Long>> sub = SubstitutionCacheCtrl.getInstance().tryToGetSubstitutors(actor);
        if (sub == null) {
            uninitialize(this);
            return;
        }
        for (Set<Long> substitutors : sub.values()) {
            if (substitutors != null) {
                for (Long substitutorActor : substitutors) {
                    cache.clearActorTasks(substitutorActor);
                }
            }
        }
    }
}
