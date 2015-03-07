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
package ru.runa.wfe.user.cache;

import java.util.List;
import java.util.Set;

import org.hibernate.type.Type;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.cache.BaseCacheCtrl;
import ru.runa.wfe.commons.cache.CachingLogic;
import ru.runa.wfe.commons.cache.Change;
import ru.runa.wfe.commons.cache.ChangedObjectParameter;
import ru.runa.wfe.commons.cache.ExecutorChangeListener;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.ExecutorGroupMembership;
import ru.runa.wfe.user.Group;

public class ExecutorCacheCtrl extends BaseCacheCtrl<ExecutorCacheImpl> implements ExecutorChangeListener, ExecutorCache {

    private static final ExecutorCacheCtrl instance = new ExecutorCacheCtrl();

    private ExecutorCacheCtrl() {
        CachingLogic.registerChangeListener(this);
    }

    public static ExecutorCacheCtrl getInstance() {
        return instance;
    }

    @Override
    public ExecutorCacheImpl buildCache() {
        return new ExecutorCacheImpl();
    }

    @Override
    public void doOnChange(ChangedObjectParameter changedObject) {
        ExecutorCacheImpl cache = getCache();
        if (cache == null) {
            return;
        }
        if (changedObject.object instanceof Executor) {
            boolean cleared = false;
            int idx = changedObject.getPropertyIndex("name");
            boolean createOrDelete = changedObject.changeType == Change.CREATE || changedObject.changeType == Change.DELETE;
            if (changedObject.object instanceof Actor) {
                cleared = cache.onExecutorChange((String) changedObject.currentState[idx], Actor.class, createOrDelete);
                if (changedObject.previousState != null) {
                    cleared = cleared && cache.onExecutorChange((String) changedObject.previousState[idx], Actor.class, createOrDelete);
                }
            } else {
                cleared = cache.onExecutorChange((String) changedObject.currentState[idx], Executor.class, createOrDelete);
                if (changedObject.previousState != null) {
                    cleared = cleared && cache.onExecutorChange((String) changedObject.previousState[idx], Executor.class, createOrDelete);
                }
            }
            if (!cleared) {
                uninitialize(changedObject);
            }
            return;
        }
        if (changedObject.object instanceof ExecutorGroupMembership) {
            boolean cleared = true;
            ExecutorGroupMembership membership = (ExecutorGroupMembership) changedObject.object;
            cleared = cleared && cache.onExecutorInGroupChange(membership.getExecutor());
            cleared = cleared && cache.onGroupMembersChange(membership.getGroup());
            if (!cleared) {
                uninitialize(changedObject);
            }
            return;
        }
        throw new InternalApplicationException("Unexpected object " + changedObject.object);
    }

    @Override
    protected void doMarkTransactionComplete() {
        if (!isLocked()) {
            uninitialize(this, Change.REFRESH);
        }
    }

    @Override
    public Actor getActor(Long code) {
        return CachingLogic.getCacheImpl(this).getActor(code);
    }

    @Override
    public Executor getExecutor(String name) {
        return CachingLogic.getCacheImpl(this).getExecutor(name);
    }

    @Override
    public Executor getExecutor(Long id) {
        return CachingLogic.getCacheImpl(this).getExecutor(id);
    }

    @Override
    public Set<Executor> getGroupMembers(Group group) {
        return CachingLogic.getCacheImpl(this).getGroupMembers(group);
    }

    @Override
    public Set<Actor> getGroupActorsAll(Group group) {
        return CachingLogic.getCacheImpl(this).getGroupActorsAll(group);
    }

    @Override
    public Set<Group> getExecutorParents(Executor executor) {
        return CachingLogic.getCacheImpl(this).getExecutorParents(executor);
    }

    @Override
    public Set<Group> getExecutorParentsAll(Executor executor) {
        return CachingLogic.getCacheImpl(this).getExecutorParentsAll(executor);
    }

    @Override
    public <T extends Executor> List<T> getAllExecutor(Class<T> clazz, BatchPresentation batch) {
        return CachingLogic.getCacheImpl(this).getAllExecutor(clazz, batch);
    }

    @Override
    public void addAllExecutor(int cacheVersion, Class<?> clazz, BatchPresentation batch, List<? extends Executor> executors) {
        CachingLogic.getCacheImpl(this).addAllExecutor(cacheVersion, clazz, batch, executors);
    }

    @Override
    public int getCacheVersion() {
        return CachingLogic.getCacheImpl(this).getCacheVersion();
    }
}
