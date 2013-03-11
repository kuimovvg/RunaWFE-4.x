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
package ru.runa.wfe.ss.cache;

import java.util.Set;
import java.util.TreeMap;

import org.hibernate.type.Type;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.cache.BaseCacheCtrl;
import ru.runa.wfe.commons.cache.CachingLogic;
import ru.runa.wfe.commons.cache.Change;
import ru.runa.wfe.commons.cache.SubstitutionChangeListener;
import ru.runa.wfe.ss.Substitution;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.cache.ExecutorCacheCtrl;
import ru.runa.wfe.user.cache.ExecutorCacheImpl;

import com.google.common.base.Objects;

public class SubstitutionCacheCtrl extends BaseCacheCtrl<SubstitutionCacheImpl> implements SubstitutionChangeListener, SubstitutionCache {

    private static final SubstitutionCacheCtrl instance = new SubstitutionCacheCtrl();

    private SubstitutionCacheCtrl() {
        CachingLogic.registerChangeListener(this);
    }

    public static SubstitutionCacheCtrl getInstance() {
        return instance;
    }

    @Override
    public SubstitutionCacheImpl buildCache() {
        return new SubstitutionCacheImpl();
    }

    @Override
    public TreeMap<Substitution, Set<Actor>> getSubstitutors(Actor actor) {
        SubstitutionCacheImpl cache = CachingLogic.getCacheImpl(this);
        return cache.getSubstitutors(actor);
    }

    @Override
    public Set<Actor> getSubstituted(Actor actor) {
        SubstitutionCacheImpl cache = CachingLogic.getCacheImpl(this);
        return cache.getSubstituted(actor);
    }

    public TreeMap<Substitution, Set<Actor>> tryToGetSubstitutors(Actor actor) {
        SubstitutionCacheImpl cache = getCache();
        if (cache == null) {
            return null;
        }
        return cache.getSubstitutors(actor);
    }

    @Override
    public void doOnChange(Object object, Change change, Object[] currentState, Object[] previousState, String[] propertyNames, Type[] types) {
        if (!isSmartCache()) {
            uninitialize(object, change);
            return;
        }
        SubstitutionCacheImpl cache = getCache();
        if (cache == null) {
            return;
        }
        if (object instanceof Substitution) {
            ExecutorCacheImpl executorCache = ExecutorCacheCtrl.getInstance().getCache();
            if (executorCache == null) {
                uninitialize(object, change);
                return;
            }
            Substitution substitution = (Substitution) object;
            Actor actor = (Actor) executorCache.getExecutor(substitution.getActorId());
            // cache.onSubstitutionChange(actor, substitution, change);
            return;
        }
        if (object instanceof Actor) {
            int activePropertyIndex = 0;
            int namePropertyIndex = 0;
            for (int i = 0; i < propertyNames.length; i++) {
                if (propertyNames[i].equals("active")) {
                    activePropertyIndex = i;
                }
                if (propertyNames[i].equals("name")) {
                    namePropertyIndex = i;
                }
            }
            if (previousState != null && !Objects.equal(previousState[activePropertyIndex], currentState[activePropertyIndex])) {
                cache.onActorStatusChange((Actor) object, change);
            }
            if (previousState != null && !Objects.equal(previousState[namePropertyIndex], currentState[namePropertyIndex])) {
                // this event interested due to Actor.hashCode() implementation
                // cache.onActorNameChange((Actor) object, change);
            }
            return;
        }
        throw new InternalApplicationException("Unexpected object " + object);
    }

}
