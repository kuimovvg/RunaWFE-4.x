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

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.commons.cache.BaseCacheImpl;
import ru.runa.wfe.commons.cache.Cache;
import ru.runa.wfe.commons.cache.Change;
import ru.runa.wfe.execution.logic.SwimlaneInitializerHelper;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.ss.Substitution;
import ru.runa.wfe.ss.TerminatorSubstitution;
import ru.runa.wfe.ss.dao.SubstitutionDAO;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.Group;
import ru.runa.wfe.user.dao.ExecutorDAO;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

class AltSubstitutionCacheImpl extends BaseCacheImpl implements SubstitutionCache {
    public static final String substitutorsName = "ru.runa.wfe.ss.cache.substitutors";
    public static final String substitutedName = "ru.runa.wfe.ss.cache.substituted";
    private final Cache<Actor, TreeMap<Substitution, HashSet<Actor>>> actorToSubstitutorsCache;
    private final Cache<Actor, HashSet<Actor>> actorToSubstitutedCache;
    private ExecutorDAO executorDAO = ApplicationContextFactory.getExecutorDAO();
    private SubstitutionDAO substitutionDAO = ApplicationContextFactory.getSubstitutionDAO();

    public AltSubstitutionCacheImpl() {
        actorToSubstitutorsCache = createCache(substitutorsName, true);
        actorToSubstitutedCache = createCache(substitutedName, true);
        for (Actor actor : executorDAO.getAllActors(BatchPresentationFactory.ACTORS.createNonPaged())) {
            if (!actor.isActive()) {
                loadCacheFor(actor);
            }
        }
    }

    private HashSet<Actor> loadCacheFor(Actor actor, Substitution substitution) {
        HashSet<Actor> substitutors = Sets.newHashSet();
        if (!substitution.isEnabled()) {
            return substitutors;
        }
        if (substitution instanceof TerminatorSubstitution) {
            return substitutors;
        }
        try {
            List<? extends Executor> executors = SwimlaneInitializerHelper.evaluate(substitution.getOrgFunction(), null);
            for (Executor executor : executors) {
                if (executor instanceof Actor) {
                    substitutors.add((Actor) executor);
                } else {
                    for (Actor groupActor : executorDAO.getGroupActors((Group) executor)) {
                        substitutors.add(groupActor);
                    }
                }
            }
            for (Actor substitutor : substitutors) {
                HashSet<Actor> substituted = actorToSubstitutedCache.get(substitutor);
                if (substituted == null) {
                    substituted = Sets.newHashSet();
                    actorToSubstitutedCache.put(substitutor, substituted);
                }
                substituted.add(actor);
            }
        } catch (Exception e) {
            log.error("Error in " + substitution, e);
        }
        return substitutors;
    }

    private HashSet<Actor> clearCacheFor(Actor actor, Substitution substitution) {
        HashSet<Actor> substitutors = Sets.newHashSet();
        if (!substitution.isEnabled()) {
            return substitutors;
        }
        if (substitution instanceof TerminatorSubstitution) {
            return substitutors;
        }
        try {
            List<? extends Executor> executors = SwimlaneInitializerHelper.evaluate(substitution.getOrgFunction(), null);
            for (Executor executor : executors) {
                if (executor instanceof Actor) {
                    substitutors.add((Actor) executor);
                } else {
                    for (Actor groupActor : executorDAO.getGroupActors((Group) executor)) {
                        substitutors.add(groupActor);
                    }
                }
            }
            for (Actor substitutor : substitutors) {
                HashSet<Actor> substituted = actorToSubstitutedCache.get(substitutor);
                if (substituted == null) {
                    substituted = Sets.newHashSet();
                    actorToSubstitutedCache.put(substitutor, substituted);
                }
                substituted.add(actor);
            }
        } catch (Exception e) {
            log.error("Error in " + substitution, e);
        }
        return substitutors;
    }

    private void loadCacheFor(Actor actor) {
        TreeMap<Substitution, HashSet<Actor>> result = Maps.newTreeMap();
        for (Substitution substitution : substitutionDAO.getByActorId(actor.getId(), true)) {
            HashSet<Actor> substitutors = loadCacheFor(actor, substitution);
            result.put(substitution, substitutors);
        }
        actorToSubstitutorsCache.put(actor, result);
    }

    @Override
    public TreeMap<Substitution, Set<Actor>> getSubstitutors(Actor actor) {
        TreeMap<Substitution, HashSet<Actor>> result = actorToSubstitutorsCache.get(actor);
        return result != null ? new TreeMap<Substitution, Set<Actor>>(result) : new TreeMap<Substitution, Set<Actor>>();
    }

    @Override
    public HashSet<Actor> getSubstituted(Actor actor) {
        HashSet<Actor> result = actorToSubstitutedCache.get(actor);
        return result != null ? result : new HashSet<Actor>();
    }

    public void onSubstitutionChange(Actor actor, Substitution substitution, Change change) {
        if (actor != null && !actor.isActive()) {
            if (change == Change.CREATE) {
                loadCacheFor(actor, substitution);
            }
            if (change == Change.UPDATE) {
                // TODO
            }
            if (change == Change.DELETE) {
                // TODO
            }
        }
    }

    public void onActorNameChange(Actor actor, Change change) {
    }

    public void onActorStatusChange(Actor actor, Change change) {
        TreeMap<Substitution, HashSet<Actor>> substitutors = actorToSubstitutorsCache.get(actor);
        if (substitutors == null) {
            // status change: active -> inactive OR new actor has been created
            if (!actor.isActive()) {
                loadCacheFor(actor);
            }
        } else {
            // status change: inactive -> active OR actor deletion
            if (actor.isActive() || change == Change.DELETE) {
                for (Map.Entry<Substitution, HashSet<Actor>> entry : substitutors.entrySet()) {
                    if (entry.getKey() instanceof TerminatorSubstitution) {
                        continue;
                    }
                    for (Actor substitutor : entry.getValue()) {
                        HashSet<Actor> substituted = actorToSubstitutedCache.get(substitutor);
                        substituted.remove(actor);
                    }
                }
                actorToSubstitutorsCache.remove(actor);
            }
        }
    }

}
