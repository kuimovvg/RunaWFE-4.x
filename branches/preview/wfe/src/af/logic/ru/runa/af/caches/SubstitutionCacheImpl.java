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
package ru.runa.af.caches;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.af.Actor;
import ru.runa.af.Executor;
import ru.runa.af.ExecutorOutOfDateException;
import ru.runa.af.Group;
import ru.runa.af.Substitution;
import ru.runa.af.TerminatorSubstitution;
import ru.runa.af.TmpApplicationContextFactory;
import ru.runa.af.dao.ExecutorDAO;
import ru.runa.af.dao.SubstitutionDAO;
import ru.runa.af.organizationfunction.FunctionParserException;
import ru.runa.af.organizationfunction.OrgFunctionHelper;
import ru.runa.af.organizationfunction.OrganizationFunctionException;
import ru.runa.commons.cache.BaseCacheImpl;
import ru.runa.commons.cache.Cache;

class SubstitutionCacheImpl extends BaseCacheImpl implements SubstitutionCache {
    private static final Log log = LogFactory.getLog(SubstitutionCacheImpl.class);
    public static final String substitutorsName = "ru.runa.af.caches.substitutors";
    public static final String substitutedName = "ru.runa.af.caches.substituted";
    private final Cache<Long, TreeMap<Substitution, HashSet<Long>>> actorToSubstitutors;
    private final Cache<Long, HashSet<Long>> actorToSubstituted;
    private ExecutorDAO executorDAO = TmpApplicationContextFactory.getExecutorDAO();
    private SubstitutionDAO substitutionDAO = TmpApplicationContextFactory.getSubstitutionDAO();

    public SubstitutionCacheImpl() {
        actorToSubstitutors = createCache(substitutorsName, true);
        actorToSubstituted = createCache(substitutedName, true);
        Map<Long, TreeMap<Substitution, HashSet<Long>>> actToSubstitutors = getMapActorToSubstitutors(getAllSubstitution());
        Map<Long, HashSet<Long>> actToSubstituted = getMapActorToSubstituted(actToSubstitutors);
        for (Actor actor : executorDAO.getAllActors(ru.runa.af.presentation.AFProfileStrategy.EXECUTOR_DEAFAULT_BATCH_PRESENTATOIN_FACTORY
                .getDefaultBatchPresentation())) {
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

    public TreeMap<Substitution, Set<Long>> getSubstitutors(Actor actor) {
        if (actor.isActive()) {
            return new TreeMap<Substitution, Set<Long>>();
        }
        TreeMap<Substitution, HashSet<Long>> result = actorToSubstitutors.get(actor.getId());
        if (result != null) {
            return new TreeMap<Substitution, Set<Long>>(result);
        }
        Map<Long, TreeMap<Substitution, HashSet<Long>>> actToSubstitutors = getMapActorToSubstitutors(getAllSubstitution());
        result = actToSubstitutors.get(actor.getId());
        return result != null ? new TreeMap<Substitution, Set<Long>>(result) : new TreeMap<Substitution, Set<Long>>();
    }

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
         * if(actorToSubstitutors.containsKey(actor.getId())) return; actorToSubstitutors.put(actor.getId(), new TreeMap<Substitution,HashSet<Long>>());
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

    private void logError(Substitution substitution) {
        try {
            log.error("Error in substitution for actor with name " + executorDAO.getActor(substitution.getActorId()).getName() + " and function "
                    + substitution.getSubstitutionOrgFunction());
        } catch (ExecutorOutOfDateException e) {
            log.error("Error in substitution for actor with id " + substitution.getActorId() + " and function "
                    + substitution.getSubstitutionOrgFunction());
        }
    }

    private Map<Long, TreeMap<Substitution, HashSet<Long>>> getMapActorToSubstitutors(List<Substitution> substitutions) {
        Map<Long, TreeMap<Substitution, HashSet<Long>>> result = new HashMap<Long, TreeMap<Substitution, HashSet<Long>>>();
        for (Substitution substitution : substitutions) {
            try {
                Actor actor = null;
                try {
                    actor = executorDAO.getActor(substitution.getActorId());
                } catch (ExecutorOutOfDateException e) {
                    substitutionDAO.deleteSubstitution(substitution.getId());
                    continue;
                }
                if (!substitution.isEnabled()) {
                    continue;
                }
                /*if (actor.isActive()) {
                    continue;
                }*/
                TreeMap<Substitution, HashSet<Long>> subDescr = result.get(actor.getId());
                if (subDescr == null) {
                    subDescr = new TreeMap<Substitution, HashSet<Long>>(new Comparator<Substitution>() {
                        public int compare(Substitution obj1, Substitution obj2) {
                            return obj1.getPosition() < obj2.getPosition() ? -1 : obj1.getPosition() == obj2.getPosition() ? 0 : 1;
                        }
                    });
                    result.put(actor.getId(), subDescr);
                }
                if (substitution instanceof TerminatorSubstitution) {
                    subDescr.put(substitution, null);
                    continue;
                }
                List<Executor> substitutorsArray = executorDAO.getExecutors(OrgFunctionHelper.evaluateOrgFunction(new HashMap<String, Object>(),
                        substitution.getSubstitutionOrgFunction(), actor.getCode()));
                HashSet<Long> substitutors = new HashSet<Long>();
                for (Executor sub : substitutorsArray) {
                    if (sub instanceof Actor) {
                        substitutors.add(sub.getId());
                    } else {
                        for (Executor ex : executorDAO.getGroupActors((Group) sub)) {
                            substitutors.add(ex.getId());
                        }
                    }
                }
                subDescr.put(substitution, substitutors);
            } catch (ExecutorOutOfDateException e) {
                logError(substitution);
            } catch (FunctionParserException e) {
                logError(substitution);
            } catch (OrganizationFunctionException e) {
                logError(substitution);
            }
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
            } catch (ExecutorOutOfDateException e) {
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
            } catch (ExecutorOutOfDateException e) {
            }
        }
        return result;
    }

    private List<Substitution> getAllSubstitution() {
        return substitutionDAO.getAllSubstitutions();
    }
}
