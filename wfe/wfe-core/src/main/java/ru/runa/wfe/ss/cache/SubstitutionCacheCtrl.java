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

import ru.runa.wfe.commons.cache.BaseCacheCtrl;
import ru.runa.wfe.commons.cache.CachingLogic;
import ru.runa.wfe.commons.cache.SubstitutionChangeListener;
import ru.runa.wfe.ss.Substitution;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Group;

public class SubstitutionCacheCtrl extends BaseCacheCtrl<SubstitutionCacheImpl> implements SubstitutionChangeListener, SubstitutionCache {

    private static final SubstitutionCacheCtrl instance = new SubstitutionCacheCtrl();

    private SubstitutionCacheCtrl() {
        CachingLogic.registerChangeListener(this);
    }

    public static SubstitutionCacheCtrl getInstance() {
        return instance;
    }

    public SubstitutionCacheImpl buildCache() {
        return new SubstitutionCacheImpl();
    }

    public TreeMap<Substitution, Set<Long>> getSubstitutors(Actor actor) {
        SubstitutionCacheImpl cache = CachingLogic.getCacheImpl(this);
        return cache.getSubstitutors(actor);
    }

    public Set<Long> getSubstituted(Actor actor) {
        SubstitutionCacheImpl cache = CachingLogic.getCacheImpl(this);
        return cache.getSubstituted(actor);
    }

    public TreeMap<Substitution, Set<Long>> tryToGetSubstitutors(Actor actor) {
        SubstitutionCacheImpl cache = getCache();
        if (cache == null) {
            return null;
        }
        return cache.getSubstitutors(actor);
    }

    @Override
    public void doOnChange(Object object, Object[] currentState, Object[] previousState, String[] propertyNames, Type[] types) {
        if (!isSmartCache()) {
            uninitialize(object);
            return;
        }
        SubstitutionCacheImpl cache = getCache();
        if (cache == null) {
            return;
        }
        if (object instanceof Group) {
            return;
        }
        if (object instanceof Actor) {
            int i = 0;
            for (; i < propertyNames.length; ++i) {
                if (propertyNames[i].equals("active")) {
                    break;
                }
            }
            if (previousState != null && !previousState[i].equals(currentState[i])) {
                cache.onActorStatusChange((Actor) object);
            } else {
                cache.onActorChange((Actor) object);
            }
        } else {
            uninitialize(object);
        }
    }

    public void onTransactionComplete() {
        super.onTransactionComplete();
        SubstitutionCacheImpl cache = getCache();
        if (cache != null && !isLocked()) {
            cache.reinitialize();
        }
    }
}
