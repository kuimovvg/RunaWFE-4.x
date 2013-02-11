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

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.commons.cache.BaseCacheImpl;
import ru.runa.wfe.commons.cache.Cache;
import ru.runa.wfe.extension.orgfunction.OrgFunctionHelper;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.ss.Substitution;
import ru.runa.wfe.ss.TerminatorSubstitution;
import ru.runa.wfe.ss.dao.SubstitutionDAO;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.ExecutorDoesNotExistException;
import ru.runa.wfe.user.Group;
import ru.runa.wfe.user.dao.ExecutorDAO;

import com.google.common.base.Objects;

class SubstitutionCacheImpl extends BaseCacheImpl implements SubstitutionCache {
    private static final Log log = LogFactory.getLog(SubstitutionCacheImpl.class);
    public static final String substitutorsName = "ru.runa.wfe.wfe.af.caches.substitutors";
    public static final String substitutedName = "ru.runa.wfe.wfe.af.caches.substituted";
    private final Cache<Long, TreeMap<Substitution, HashSet<Long>>> actorToSubstitutors;
    private final Cache<Long, HashSet<Long>> actorToSubstituted;
    private ExecutorDAO executorDAO = ApplicationContextFactory.getExecutorDAO();
    private SubstitutionDAO substitutionDAO = ApplicationContextFactory.getSubstitutionDAO();

    public SubstitutionCacheImpl() {
        actorToSubstitutors = createCache(substitutorsName, true);
        actorToSubstituted = createCache(substitutedName, true);
        Map<Long, TreeMap<Substitution, HashSet<Long>>> actToSubstitutors = getMapActorToSubstitutors();
        Map<Long, HashSet<Long>> actToSubstituted = getMapActorToSubstituted(actToSubstitutors);
        for (Actor actor : executorDAO.getAllActors(BatchPresentationFactory.ACTORS.createNonPaged())) {
            if (actToSubstituted.get(actor.getId()) == null) {
                actToSubstituted.put(actor.getId(), new HashSet<Long>());
            }
            if (actToSubstitutors.get(actor.getId()) == null) {
                actToSubstitutors.put(actor.getId(), new TreeMap<Substitution, HashSet<Long>>());
            }
        }
        actorToSubstitutors.putAll(actToSubstitutors);
        actorToSubstituted.putAll(actToSubstituted);
    }

    @Override
    public TreeMap<Substitution, Set<Long>> getSubstitutors(Actor actor) {
        if (actor.isActive()) {
            return new TreeMap<Substitution, Set<Long>>();
        }
        TreeMap<Substitution, HashSet<Long>> result = actorToSubstitutors.get(actor.getId());
        if (result != null) {
            return new TreeMap<Substitution, Set<Long>>(result);
        }
        Map<Long, TreeMap<Substitution, HashSet<Long>>> actToSubstitutors = getMapActorToSubstitutors();
        result = actToSubstitutors.get(actor.getId());
        return result != null ? new TreeMap<Substitution, Set<Long>>(result) : new TreeMap<Substitution, Set<Long>>();
    }

    @Override
    public HashSet<Long> getSubstituted(Actor actor) {
        HashSet<Long> result = actorToSubstituted.get(actor.getId());
        if (result != null) {
            return result;
        }
        Map<Long, HashSet<Long>> actToSubstituted = getMapActorToSubstituted(actorToSubstitutors);
        result = actToSubstituted.get(actor.getId());
        return result != null ? result : new HashSet<Long>();
    }

    public void onActorChange(Actor actor) {
        /*
         * if(actorToSubstitutors.containsKey(actor.getId())) return;
         * actorToSubstitutors.put(actor.getId(), new
         * TreeMap<Substitution,HashSet<Long>>());
         */
    }

    public void onActorStatusChange(Actor actor) {
        TreeMap<Substitution, HashSet<Long>> substitutions = actorToSubstitutors.get(actor.getId());
        if (substitutions == null) {
            return;
        }
        for (HashSet<Long> substitutors : substitutions.values()) {
            if (substitutors != null) {
                for (Long substitutor : substitutors) {
                    actorToSubstituted.remove(substitutor);
                }
            }
        }
    }

    public void reinitialize() {
        actorToSubstituted.clear();
        actorToSubstituted.putAll(getMapActorToSubstituted(actorToSubstitutors));
    }

    private Map<Long, TreeMap<Substitution, HashSet<Long>>> getMapActorToSubstitutors() {
        Map<Long, TreeMap<Substitution, HashSet<Long>>> result = new HashMap<Long, TreeMap<Substitution, HashSet<Long>>>();
        Substitution currentSubstitution = null;
        try {
            for (Substitution substitution : substitutionDAO.getAll()) {
                currentSubstitution = substitution;
                Actor actor = null;
                try {
                    actor = executorDAO.getActor(substitution.getActorId());
                } catch (ExecutorDoesNotExistException e) {
                    log.warn("ERROR in " + substitution);
                    continue;
                }
                if (!substitution.isEnabled()) {
                    continue;
                }
                /*
                 * if (actor.isActive()) { continue; }
                 */
                TreeMap<Substitution, HashSet<Long>> subDescr = result.get(actor.getId());
                if (subDescr == null) {
                    subDescr = new TreeMap<Substitution, HashSet<Long>>(new Comparator<Substitution>() {
                        @Override
                        public int compare(Substitution obj1, Substitution obj2) {
                            return obj1.getPosition() < obj2.getPosition() ? -1 : Objects.equal(obj1.getPosition(), obj2.getPosition()) ? 0 : 1;
                        }
                    });
                    result.put(actor.getId(), subDescr);
                }
                if (substitution instanceof TerminatorSubstitution) {
                    subDescr.put(substitution, null);
                    continue;
                }
                List<? extends Executor> substitutors = OrgFunctionHelper.evaluateOrgFunction(substitution.getOrgFunction());
                HashSet<Long> substitutorIds = new HashSet<Long>();
                for (Executor sub : substitutors) {
                    if (sub instanceof Actor) {
                        substitutorIds.add(sub.getId());
                    } else {
                        for (Executor ex : executorDAO.getGroupActors((Group) sub)) {
                            substitutorIds.add(ex.getId());
                        }
                    }
                }
                subDescr.put(substitution, substitutorIds);
            }
        } catch (Exception e) {
            log.error("Error in " + currentSubstitution, e);
        }
        return result;
    }

    private Map<Long, HashSet<Long>> getMapActorToSubstituted(Cache<Long, TreeMap<Substitution, HashSet<Long>>> mapActorToSubstitutors) {
        Map<Long, HashSet<Long>> result = new HashMap<Long, HashSet<Long>>();
        for (Long substituted : mapActorToSubstitutors.keySet()) {
            try {
                if (executorDAO.getActor(substituted).isActive()) {
                    continue;
                }
                for (HashSet<Long> substitutors : mapActorToSubstitutors.get(substituted).values()) {
                    if (substitutors == null) {
                        continue;
                    }
                    for (Long substitutor : substitutors) {
                        HashSet<Long> set = result.get(substitutor);
                        if (set == null) {
                            set = new HashSet<Long>();
                            result.put(substitutor, set);
                        }
                        set.add(substituted);
                    }
                }
            } catch (ExecutorDoesNotExistException e) {
            }
        }
        return result;
    }

    private Map<Long, HashSet<Long>> getMapActorToSubstituted(Map<Long, TreeMap<Substitution, HashSet<Long>>> mapActorToSubstitutors) {
        Map<Long, HashSet<Long>> result = new HashMap<Long, HashSet<Long>>();
        for (Long substituted : mapActorToSubstitutors.keySet()) {
            try {
                if (executorDAO.getActor(substituted).isActive()) {
                    continue;
                }
                for (HashSet<Long> substitutors : mapActorToSubstitutors.get(substituted).values()) {
                    if (substitutors == null) {
                        continue;
                    }
                    for (Long substitutor : substitutors) {
                        HashSet<Long> set = result.get(substitutor);
                        if (set == null) {
                            set = new HashSet<Long>();
                            result.put(substitutor, set);
                        }
                        set.add(substituted);
                    }
                }
            } catch (ExecutorDoesNotExistException e) {
            }
        }
        return result;
    }

}
