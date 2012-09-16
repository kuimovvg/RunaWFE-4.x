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
package ru.runa.wf.caches;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import ru.runa.af.presentation.BatchPresentation;
import ru.runa.commons.cache.BaseCacheImpl;
import ru.runa.commons.cache.Cache;
import ru.runa.wf.TaskStub;

class TaskCacheImpl extends BaseCacheImpl implements TaskCache {

    public static final String taskCacheName = "ru.runa.wf.caches.taskLists";
    private final Cache<Long, ConcurrentHashMap<TaskCacheImpl.BatchPresentationStrongComparison, List<TaskStub>>> actorToTasksCache;
    private volatile Date lastTaskListChangeDate = new Date();

    public TaskCacheImpl() {
        actorToTasksCache = createCache(taskCacheName);
    }

    @Override
    public List<TaskStub> getTasks(Long actorId, BatchPresentation batchPresentation) {
        Map<TaskCacheImpl.BatchPresentationStrongComparison, List<TaskStub>> lists = actorToTasksCache.get(actorId);
        if (lists == null) {
            return null;
        }
        return lists.get(new BatchPresentationStrongComparison(batchPresentation));
    }

    @Override
    public void setTasks(int cacheVersion, Long actorId, BatchPresentation batchPresentation, List<TaskStub> tasks) {
        if (cacheVersion != currentCacheVersion) {
            return;
        }
        ConcurrentHashMap<TaskCacheImpl.BatchPresentationStrongComparison, List<TaskStub>> lists = actorToTasksCache.get(actorId);
        if (lists == null) {
            lists = new ConcurrentHashMap<TaskCacheImpl.BatchPresentationStrongComparison, List<TaskStub>>();
            actorToTasksCache.put(actorId, lists);
        }
        lists.put(new BatchPresentationStrongComparison(batchPresentation), tasks);
    }

    public void clearActorTasks(long actorId) {
        actorToTasksCache.remove(actorId);
        lastTaskListChangeDate = new Date();
    }

    @Override
    public Date getLastChangeDate() {
        return lastTaskListChangeDate;
    }

    public void resetChangeDate() {
        lastTaskListChangeDate = new Date();
    }

    /**
     * Class need to compare BatchPresentation with strongEquals, instead of equals criteria.
     * It necessary in task list cache which using BatchPresentation as key. 
     */
    private static class BatchPresentationStrongComparison implements Serializable {
        private static final long serialVersionUID = 1L;
        BatchPresentation batchPresentation;

        BatchPresentationStrongComparison(BatchPresentation batchPresentation) {
            this.batchPresentation = batchPresentation.clone();
        }

        public boolean equals(Object obj) {
            if (obj instanceof TaskCacheImpl.BatchPresentationStrongComparison) {
                return batchPresentation.strongEquals(((TaskCacheImpl.BatchPresentationStrongComparison) obj).batchPresentation);
            } else if (obj instanceof BatchPresentation) {
                return batchPresentation.strongEquals(obj);
            }
            return false;
        }

        public int hashCode() {
            return batchPresentation.hashCode();
        }
    }
}
