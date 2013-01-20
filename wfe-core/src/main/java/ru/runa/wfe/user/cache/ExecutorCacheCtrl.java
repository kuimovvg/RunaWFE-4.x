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

import ru.runa.wfe.commons.cache.BaseCacheCtrl;
import ru.runa.wfe.commons.cache.CachingLogic;
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

    public ExecutorCacheImpl buildCache() {
        return new ExecutorCacheImpl();
    }

    @Override
    public void doOnChange(Object object, Object[] currentState, Object[] previousState, String[] propertyNames, Type[] types) {
        if (!isSmartCache()) {
            uninitialize(object);
            return;
        }
        ExecutorCacheImpl cache = getCache();
        if (cache == null) {
            return;
        }
        if (object instanceof Executor) {
            boolean cleared = false;
            int i = 0;
            for (; i < propertyNames.length; ++i) {
                if (propertyNames[i].equals("name")) {
                    break;
                }
            }
            if (object instanceof Actor) {
                cleared = cache.onExecutorChange((String) currentState[i], Actor.class, previousState == null);
                if (previousState != null) {
                    cleared = cleared && cache.onExecutorChange((String) previousState[i], Actor.class, previousState == null);
                }
            } else {
                cleared = cache.onExecutorChange((String) currentState[i], Executor.class, previousState == null);
                if (previousState != null) {
                    cleared = cleared && cache.onExecutorChange((String) previousState[i], Executor.class, previousState == null);
                }
            }
            if (!cleared) {
                uninitialize(object);
            }
        } else if (object instanceof ExecutorGroupMembership) {
            boolean cleared = true;
            ExecutorGroupMembership relation = (ExecutorGroupMembership) object;
            cleared = cleared && cache.onExecutorInGroupChange(relation.getExecutor());
            cleared = cleared && cache.onGroupMembersChange(relation.getGroup());
            if (!cleared) {
                uninitialize(object);
            }
        } else {
            uninitialize(object);
        }
    }

    @Override
    protected void doMarkTransactionComplete() {
        if (!isLocked()) {
            uninitialize(this);
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
