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
import ru.runa.wfe.commons.cache.Change;
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

import com.google.common.collect.Maps;

class SubstitutionCacheImpl extends BaseCacheImpl implements SubstitutionCache {
    private static final Log log = LogFactory.getLog(SubstitutionCacheImpl.class);
    public static final String substitutorsName = "ru.runa.wfe.ss.cache.substitutors";
    public static final String substitutedName = "ru.runa.wfe.ss.cache.substituted";
    private final Cache<Long, TreeMap<Substitution, HashSet<Actor>>> actorToSubstitutorsCache;
    private final Cache<Long, HashSet<Actor>> actorToSubstitutedCache;
    private ExecutorDAO executorDAO = ApplicationContextFactory.getExecutorDAO();
    private SubstitutionDAO substitutionDAO = ApplicationContextFactory.getSubstitutionDAO();

    public SubstitutionCacheImpl() {
        actorToSubstitutorsCache = createCache(substitutorsName, true);
        actorToSubstitutedCache = createCache(substitutedName, true);
        Map<Long, TreeMap<Substitution, HashSet<Actor>>> actorToSubstitutors = getMapActorToSubstitutors();
        Map<Long, HashSet<Actor>> actorToSubstituted = getMapActorToSubstituted(actorToSubstitutors);
        for (Actor actor : executorDAO.getAllActors(BatchPresentationFactory.ACTORS.createNonPaged())) {
            if (actorToSubstituted.get(actor.getId()) == null) {
                actorToSubstituted.put(actor.getId(), new HashSet<Actor>());
            }
            if (actorToSubstitutors.get(actor.getId()) == null) {
                actorToSubstitutors.put(actor.getId(), new TreeMap<Substitution, HashSet<Actor>>());
            }
        }
        actorToSubstitutorsCache.putAll(actorToSubstitutors);
        actorToSubstitutedCache.putAll(actorToSubstituted);
    }

    @Override
    public TreeMap<Substitution, Set<Actor>> getSubstitutors(Actor actor) {
        if (actor.isActive()) {
            return new TreeMap<Substitution, Set<Actor>>();
        }
        TreeMap<Substitution, HashSet<Actor>> result = actorToSubstitutorsCache.get(actor.getId());
        if (result != null) {
            return new TreeMap<Substitution, Set<Actor>>(result);
        }
        Map<Long, TreeMap<Substitution, HashSet<Actor>>> actorToSubstitutors = getMapActorToSubstitutors();
        result = actorToSubstitutors.get(actor.getId());
        return result != null ? new TreeMap<Substitution, Set<Actor>>(result) : new TreeMap<Substitution, Set<Actor>>();
    }

    @Override
    public HashSet<Actor> getSubstituted(Actor actor) {
        HashSet<Actor> result = actorToSubstitutedCache.get(actor.getId());
        if (result != null) {
            return result;
        }
        Map<Long, HashSet<Actor>> actToSubstituted = getMapActorToSubstituted(actorToSubstitutorsCache);
        result = actToSubstituted.get(actor.getId());
        return result != null ? result : new HashSet<Actor>();
    }

    public void onActorStatusChange(Actor actor, Change change) {
        log.debug("onActorStatusChange: " + actor);
        TreeMap<Substitution, HashSet<Actor>> substitutions = actorToSubstitutorsCache.get(actor.getId());
        if (substitutions == null) {
            return;
        }
        for (HashSet<Actor> substitutors : substitutions.values()) {
            if (substitutors != null) {
                for (Actor substitutor : substitutors) {
                    actorToSubstitutedCache.remove(substitutor.getId());
                }
            }
        }
    }

    private Map<Long, TreeMap<Substitution, HashSet<Actor>>> getMapActorToSubstitutors() {
        Map<Long, TreeMap<Substitution, HashSet<Actor>>> result = Maps.newHashMap();
        for (Substitution substitution : substitutionDAO.getAll()) {
            try {
                Actor actor = null;
                try {
                    actor = executorDAO.getActor(substitution.getActorId());
                } catch (ExecutorDoesNotExistException e) {
                    log.error("in " + substitution + ": " + e);
                    continue;
                }
                if (!substitution.isEnabled()) {
                    continue;
                }
                TreeMap<Substitution, HashSet<Actor>> subDescr = result.get(actor.getId());
                if (subDescr == null) {
                    subDescr = new TreeMap<Substitution, HashSet<Actor>>();
                    result.put(actor.getId(), subDescr);
                }
                if (substitution instanceof TerminatorSubstitution) {
                    subDescr.put(substitution, null);
                    continue;
                }
                List<? extends Executor> executors = OrgFunctionHelper.evaluateOrgFunction(substitution.getOrgFunction());
                HashSet<Actor> substitutors = new HashSet<Actor>();
                for (Executor sub : executors) {
                    if (sub instanceof Actor) {
                        substitutors.add((Actor) sub);
                    } else {
                        for (Actor groupActor : executorDAO.getGroupActors((Group) sub)) {
                            substitutors.add(groupActor);
                        }
                    }
                }
                subDescr.put(substitution, substitutors);
            } catch (Exception e) {
                log.error("Error in " + substitution, e);
            }
        }
        return result;
    }

    private Map<Long, HashSet<Actor>> getMapActorToSubstituted(Cache<Long, TreeMap<Substitution, HashSet<Actor>>> mapActorToSubstitutors) {
        Map<Long, HashSet<Actor>> result = new HashMap<Long, HashSet<Actor>>();
        for (Long substituted : mapActorToSubstitutors.keySet()) {
            try {
                Actor substitutedActor = executorDAO.getActor(substituted);
                if (substitutedActor.isActive()) {
                    continue;
                }
                for (HashSet<Actor> substitutors : mapActorToSubstitutors.get(substituted).values()) {
                    if (substitutors == null) {
                        continue;
                    }
                    for (Actor substitutor : substitutors) {
                        HashSet<Actor> set = result.get(substitutor.getId());
                        if (set == null) {
                            set = new HashSet<Actor>();
                            result.put(substitutor.getId(), set);
                        }
                        set.add(substitutedActor);
                    }
                }
            } catch (ExecutorDoesNotExistException e) {
            }
        }
        return result;
    }

    private Map<Long, HashSet<Actor>> getMapActorToSubstituted(Map<Long, TreeMap<Substitution, HashSet<Actor>>> mapActorToSubstitutors) {
        Map<Long, HashSet<Actor>> result = new HashMap<Long, HashSet<Actor>>();
        for (Long substitutedId : mapActorToSubstitutors.keySet()) {
            try {
                Actor substitutedActor = executorDAO.getActor(substitutedId);
                if (substitutedActor.isActive()) {
                    continue;
                }
                for (HashSet<Actor> substitutors : mapActorToSubstitutors.get(substitutedId).values()) {
                    if (substitutors == null) {
                        // TODO terminators handling?
                        continue;
                    }
                    for (Actor substitutor : substitutors) {
                        HashSet<Actor> set = result.get(substitutor.getId());
                        if (set == null) {
                            set = new HashSet<Actor>();
                            result.put(substitutor.getId(), set);
                        }
                        set.add(substitutedActor);
                    }
                }
            } catch (ExecutorDoesNotExistException e) {
            }
        }
        return result;
    }

}
