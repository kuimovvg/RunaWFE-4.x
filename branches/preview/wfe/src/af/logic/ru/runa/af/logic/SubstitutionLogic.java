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
package ru.runa.af.logic;

import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import javax.security.auth.Subject;

import org.springframework.beans.factory.annotation.Autowired;

import ru.runa.af.Actor;
import ru.runa.af.AuthenticationException;
import ru.runa.af.AuthorizationException;
import ru.runa.af.ExecutorOutOfDateException;
import ru.runa.af.ExecutorPermission;
import ru.runa.af.Permission;
import ru.runa.af.Substitution;
import ru.runa.af.SubstitutionCriteria;
import ru.runa.af.SubstitutionOutOfDateException;
import ru.runa.af.TerminatorSubstitution;
import ru.runa.af.caches.SubstitutionCache;
import ru.runa.af.caches.SubstitutionCacheCtrl;
import ru.runa.af.dao.SubstitutionDAO;
import ru.runa.af.presentation.AFProfileStrategy;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * Created on 27.01.2006
 * 
 * @author Semochkin_v
 * @author Gordienko_m
 */
public class SubstitutionLogic extends CommonLogic {
    SubstitutionCache substitutionCache = null;
    @Autowired
    private SubstitutionDAO substitutionDAO;

    public SubstitutionLogic() {
        substitutionCache = SubstitutionCacheCtrl.getInstance();
    }

    public void createSubstitution(Subject subject, Long actorId, Substitution substitution) throws AuthorizationException, AuthenticationException,
            ExecutorOutOfDateException {
        create(subject, actorId, substitution);
    }

    public void createSubstitution(Subject subject, Long actorId) throws AuthorizationException, AuthenticationException, ExecutorOutOfDateException {
        create(subject, actorId, fillDefaultSubstitution(new Substitution()));
    }

    public void createTerminator(Subject subject, Long actorId, TerminatorSubstitution terminator) throws AuthorizationException,
            AuthenticationException, ExecutorOutOfDateException {
        create(subject, actorId, terminator);
    }

    public void createTerminator(Subject subject, Long actorId) throws AuthorizationException, AuthenticationException, ExecutorOutOfDateException {
        create(subject, actorId, fillDefaultSubstitution(new TerminatorSubstitution()));
    }

    private Substitution fillDefaultSubstitution(Substitution substitution) {
        substitution.setEnabled(false);
        substitution.setSubstitutionOrgFunction("");
        return substitution;
    }

    private void create(Subject subject, Long actorId, Substitution substitution) throws AuthorizationException, AuthenticationException,
            ExecutorOutOfDateException {
        Actor actor = executorDAO.getActor(actorId);
        checkPermissionsOnExecutor(subject, actor, ExecutorPermission.UPDATE);
        substitution.setActorId(actorId);
        List<Substitution> substitutions = substitutionDAO.getActorSubstitutions(actorId);
        int position = substitutions.size() == 0 ? 0 : substitutions.get(substitutions.size() - 1).getPosition() + 1;
        substitution.setPosition(position);
        substitutionDAO.storeSubstitution(substitution);
    }

    public void insertSubstitution(Subject subject, Long actorId, Substitution substitution, int position) throws AuthorizationException,
            AuthenticationException, ExecutorOutOfDateException {
        Actor actor = executorDAO.getActor(actorId);
        checkPermissionsOnExecutor(subject, actor, ExecutorPermission.UPDATE);
        substitution.setActorId(actorId);
        substitution.setPosition(position);
        List<Substitution> substitutions = substitutionDAO.getActorSubstitutions(actorId);
        for (Substitution existing : substitutions) {
            if (existing.getPosition() >= position) {
                existing.setPosition(existing.getPosition() + 1);
                substitutionDAO.storeSubstitution(existing);
            }
        }
        substitutionDAO.storeSubstitution(substitution);
    }

    public List<Substitution> getSubstitutions(Subject subject, Long actorId) throws AuthorizationException, AuthenticationException, ExecutorOutOfDateException {
        Actor actor = executorDAO.getActor(actorId);
        // TODO add permissions on read substitutors
        checkPermissionsOnExecutor(subject, actor, Permission.READ);
        return substitutionDAO.getActorSubstitutions(actorId);
    }

    public List<Substitution> getAllSubstitutions(Subject subject) throws AuthorizationException, AuthenticationException, ExecutorOutOfDateException {
        List<Actor> actors = executorDAO.getAllActors(AFProfileStrategy.EXECUTOR_DEAFAULT_BATCH_PRESENTATOIN_FACTORY.getDefaultBatchPresentation());
        // TODO add permissions on read substitutors
        checkPermissionsOnExecutors(subject, actors, Permission.READ);
        return substitutionDAO.getAllSubstitutions();
    }

    public void delete(Subject subject, List<Long> substitutionIds) throws AuthorizationException, AuthenticationException, ExecutorOutOfDateException,
            SubstitutionOutOfDateException {
        List<Substitution> substitutions = substitutionDAO.getSubstitutions(substitutionIds);
        if (substitutions.size() != substitutionIds.size()) {
            throw new SubstitutionOutOfDateException();
        }
        List<Actor> actors = getSubstitutionActors(substitutions);
        // TODO add permissions on update substitutors
        checkPermissionsOnExecutors(subject, actors, ExecutorPermission.UPDATE);
        substitutionDAO.deleteSubstitutions(substitutionIds);
        /* TODO uncomment after moving to new sync alg
        for (Actor actor : actors) {
            fixPositionsForDeletedSubstitution(daoHolder.getSubstitutionDAO(), actor.getId());
        }
        */
    }

    private void fixPositionsForDeletedSubstitution(Long actorId) {
        List<Substitution> actorSubstitutions = substitutionDAO.getActorSubstitutions(actorId);
        for (int i = 0; i < actorSubstitutions.size(); i++) {
            Substitution substitution = actorSubstitutions.get(i);
            if (substitution.getPosition() != i) {
                substitution.setPosition(i);
                substitutionDAO.storeSubstitution(substitution);
            }
        }
    }

    public void delete(Subject subject, Substitution substitution) throws AuthorizationException, AuthenticationException,
            ExecutorOutOfDateException, SubstitutionOutOfDateException {
        Actor actor = executorDAO.getActor(substitution.getActorId());
        checkPermissionsOnExecutors(subject, Lists.newArrayList(actor), ExecutorPermission.UPDATE);
        substitutionDAO.deleteSubstitution(substitution.getId());
        fixPositionsForDeletedSubstitution(substitution.getActorId());
    }

    public void switchSubstitutionsPositions(Subject subject, Long substitutionId1, Long substitutionId2) throws AuthorizationException,
            AuthenticationException, ExecutorOutOfDateException, SubstitutionOutOfDateException {
        List<Long> ids = Lists.newArrayList(substitutionId1, substitutionId2);
        List<Substitution> substitutions = substitutionDAO.getSubstitutions(ids);
        if (substitutions.size() != 2) {
            throw new SubstitutionOutOfDateException();
        }
        List<Actor> actors = getSubstitutionActors(substitutions);
        checkPermissionsOnExecutors(subject, actors, ExecutorPermission.UPDATE);
        substitutionDAO.deleteSubstitutions(ids);
        int pos0 = substitutions.get(0).getPosition();
        substitutions.get(0).setId(null);
        substitutions.get(0).setPosition(substitutions.get(1).getPosition());
        substitutions.get(1).setId(null);
        substitutions.get(1).setPosition(pos0);
        store(subject, substitutions.get(0));
        store(subject, substitutions.get(1));
    }

    public void store(Subject subject, Substitution substitution) throws AuthorizationException, AuthenticationException, ExecutorOutOfDateException {
        substitutionDAO.storeSubstitution(substitution);
    }

    public Substitution getSubstitution(Subject subject, Long id) throws AuthorizationException, AuthenticationException,
            ExecutorOutOfDateException {
        // TODO add permissions on read substitutors
        return substitutionDAO.getSubstitution(id);
    }

    private List<Actor> getSubstitutionActors(List<Substitution> substitutions) throws ExecutorOutOfDateException {
        Set<Long> actorIdSet = Sets.newHashSetWithExpectedSize(substitutions.size());
        for (Substitution substitution : substitutions) {
            actorIdSet.add(substitution.getActorId());
        }
        return executorDAO.getActors(Lists.newArrayList(actorIdSet));
    }

    public TreeMap<Substitution, Set<Long>> getSubstitutors(Actor actor) {
        return substitutionCache.getSubstitutors(actor);
    }

    public Set<Long> getSubstituted(Actor actor) {
        return substitutionCache.getSubstituted(actor);
    }

    public void createSubstitutionCriteria(Subject subject, SubstitutionCriteria substitutionCriteria) throws AuthorizationException,
            AuthenticationException {
        substitutionDAO.createCriteria(substitutionCriteria);
    }

    public SubstitutionCriteria getSubstitutionCriteria(Subject subject, Long id) throws AuthorizationException,
            AuthenticationException, ExecutorOutOfDateException {
        return substitutionDAO.getCriteria(id);
    }

    public List<SubstitutionCriteria> getSubstitutionCriteriaAll(Subject subject) throws AuthorizationException, AuthenticationException,
            ExecutorOutOfDateException {
        return substitutionDAO.getAllCriterias();
    }

    public void store(Subject subject, SubstitutionCriteria substitutionsCriteria) throws AuthorizationException, AuthenticationException,
            ExecutorOutOfDateException {
        substitutionDAO.storeCriteria(substitutionsCriteria);
    }

    public void deleteSubstitutionCriteria(Subject subject, Long substitutionCriteriaId) throws AuthorizationException, AuthenticationException,
            ExecutorOutOfDateException {
        substitutionDAO.deleteCriteria(substitutionCriteriaId);
    }

    public void deleteSubstitutionCriteria(Subject subject, SubstitutionCriteria substitutionCriteria) throws AuthorizationException,
            AuthenticationException, ExecutorOutOfDateException {
        substitutionDAO.deleteCriteria(substitutionCriteria);
    }

    public List<Substitution> getBySubstitutionCriteria(Subject subject, SubstitutionCriteria substitutionCriteria) throws AuthorizationException,
            AuthenticationException, ExecutorOutOfDateException {
        return substitutionDAO.getSubstitutionsByCriteria(substitutionCriteria);
    }
}
