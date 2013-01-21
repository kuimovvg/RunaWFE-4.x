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
package ru.runa.wfe.ss.logic;

import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import javax.security.auth.Subject;

import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import ru.runa.wfe.commons.logic.CommonLogic;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.ss.Substitution;
import ru.runa.wfe.ss.SubstitutionCriteria;
import ru.runa.wfe.ss.SubstitutionDoesNotExistException;
import ru.runa.wfe.ss.TerminatorSubstitution;
import ru.runa.wfe.ss.cache.SubstitutionCache;
import ru.runa.wfe.ss.cache.SubstitutionCacheCtrl;
import ru.runa.wfe.ss.dao.SubstitutionCriteriaDAO;
import ru.runa.wfe.ss.dao.SubstitutionDAO;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.ExecutorDoesNotExistException;
import ru.runa.wfe.user.ExecutorPermission;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * Created on 27.01.2006
 * 
 * @author Semochkin_v
 * @author Gordienko_m
 */
public class SubstitutionLogic extends CommonLogic {
    private final SubstitutionCache substitutionCache = SubstitutionCacheCtrl.getInstance();
    @Autowired
    private SubstitutionDAO substitutionDAO;
    @Autowired
    private SubstitutionCriteriaDAO substitutionCriteriaDAO;

    public void createSubstitution(Subject subject, Long actorId, Substitution substitution) throws AuthorizationException, AuthenticationException,
            ExecutorDoesNotExistException {
        create(subject, actorId, substitution);
    }

    public void createSubstitution(Subject subject, Long actorId) throws AuthorizationException, AuthenticationException,
            ExecutorDoesNotExistException {
        create(subject, actorId, fillDefaultSubstitution(new Substitution()));
    }

    public Substitution getSubstitution(Subject subject, Long id) {
        return substitutionDAO.getNotNull(id);
    }

    public void createTerminator(Subject subject, Long actorId, TerminatorSubstitution terminator) throws AuthorizationException,
            AuthenticationException, ExecutorDoesNotExistException {
        create(subject, actorId, terminator);
    }

    public void createTerminator(Subject subject, Long actorId) throws AuthorizationException, AuthenticationException, ExecutorDoesNotExistException {
        create(subject, actorId, fillDefaultSubstitution(new TerminatorSubstitution()));
    }

    private Substitution fillDefaultSubstitution(Substitution substitution) {
        substitution.setEnabled(false);
        substitution.setSubstitutionOrgFunction("");
        return substitution;
    }

    private void create(Subject subject, Long actorId, Substitution substitution) throws AuthorizationException, ExecutorDoesNotExistException {
        Actor actor = executorDAO.getActor(actorId);
        checkPermissionsOnExecutor(subject, actor, ExecutorPermission.UPDATE);
        substitution.setActorId(actorId);
        List<Substitution> substitutions = substitutionDAO.getByActorId(actorId);
        int position = substitutions.size() == 0 ? 0 : substitutions.get(substitutions.size() - 1).getPosition() + 1;
        substitution.setPosition(position);
        substitutionDAO.create(substitution);
    }

    public void insertSubstitution(Subject subject, Long actorId, Substitution substitution, int position) throws AuthorizationException,
            ExecutorDoesNotExistException {
        Actor actor = executorDAO.getActor(actorId);
        checkPermissionsOnExecutor(subject, actor, ExecutorPermission.UPDATE);
        substitution.setActorId(actorId);
        substitution.setPosition(position);
        List<Substitution> substitutions = substitutionDAO.getByActorId(actorId);
        for (Substitution existing : substitutions) {
            if (existing.getPosition() >= position) {
                existing.setPosition(existing.getPosition() + 1);
                // TODO merge substitutionDAO.update(existing);
            }
        }
        substitutionDAO.create(substitution);
    }

    public List<Substitution> getSubstitutions(Subject subject, Long actorId) {
        Actor actor = executorDAO.getActor(actorId);
        checkPermissionsOnExecutor(subject, actor, Permission.READ);
        return substitutionDAO.getByActorId(actorId);
    }

    public List<Substitution> getAllSubstitutions(Subject subject) {
        List<Actor> actors = executorDAO.getAllActors(BatchPresentationFactory.ACTORS.createNonPaged());
        // this is workaround for n*1000 records
        try {
            checkPermissionsOnExecutors(subject, actors, Permission.READ);
        } catch (AuthorizationException e) {
            throw e;
        } catch (Exception e) {
            LogFactory.getLog(getClass()).error("", e);
        }
        return substitutionDAO.getAll();
    }

    public void delete(Subject subject, List<Long> substitutionIds) throws SubstitutionDoesNotExistException {
        List<Substitution> substitutions = substitutionDAO.get(substitutionIds);
        if (substitutions.size() != substitutionIds.size()) {
            throw new SubstitutionDoesNotExistException();
        }
        List<Actor> actors = getSubstitutionActors(substitutions);
        checkPermissionsOnExecutors(subject, actors, ExecutorPermission.UPDATE);
        substitutionDAO.delete(substitutionIds);
        for (Actor actor : actors) {
            fixPositionsForDeletedSubstitution(actor.getId());
        }
    }

    private void fixPositionsForDeletedSubstitution(Long actorId) {
        List<Substitution> actorSubstitutions = substitutionDAO.getByActorId(actorId);
        for (int i = 0; i < actorSubstitutions.size(); i++) {
            Substitution substitution = actorSubstitutions.get(i);
            if (substitution.getPosition() != i) {
                substitution.setPosition(i);
                // TODO merge substitutionDAO.update(substitution);
            }
        }
    }

    public void delete(Subject subject, Substitution substitution) {
        Actor actor = executorDAO.getActor(substitution.getActorId());
        checkPermissionsOnExecutors(subject, Lists.newArrayList(actor), ExecutorPermission.UPDATE);
        substitutionDAO.delete(substitution);
        fixPositionsForDeletedSubstitution(substitution.getActorId());
    }

    public void switchSubstitutionsPositions(Subject subject, Long substitutionId1, Long substitutionId2) throws SubstitutionDoesNotExistException {
        List<Long> ids = Lists.newArrayList(substitutionId1, substitutionId2);
        List<Substitution> substitutions = substitutionDAO.get(ids);
        if (substitutions.size() != 2) {
            throw new SubstitutionDoesNotExistException();
        }
        List<Actor> actors = getSubstitutionActors(substitutions);
        checkPermissionsOnExecutors(subject, actors, ExecutorPermission.UPDATE);
        substitutionDAO.delete(ids);
        int pos0 = substitutions.get(0).getPosition();
        substitutions.get(0).setId(null);
        substitutions.get(0).setPosition(substitutions.get(1).getPosition());
        substitutions.get(1).setId(null);
        substitutions.get(1).setPosition(pos0);
        // TODO merge substitutionDAO.update(substitutions.get(0));
        // TODO merge substitutionDAO.update(substitutions.get(1));
    }

    public void create(Subject subject, Substitution substitution) {
        substitutionDAO.create(substitution);
    }

    private List<Actor> getSubstitutionActors(List<Substitution> substitutions) {
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

    public void createSubstitutionCriteria(Subject subject, SubstitutionCriteria substitutionCriteria) {
        substitutionCriteriaDAO.create(substitutionCriteria);
    }

    public SubstitutionCriteria getSubstitutionCriteria(Subject subject, Long id) {
        return substitutionCriteriaDAO.getNotNull(id);
    }

    public List<SubstitutionCriteria> getSubstitutionCriteriaAll(Subject subject) {
        return substitutionCriteriaDAO.getAll();
    }

    public void store(Subject subject, SubstitutionCriteria substitutionsCriteria) {
        substitutionCriteriaDAO.store(substitutionsCriteria);
    }

    public void deleteSubstitutionCriteria(Subject subject, Long substitutionCriteriaId) {
        substitutionCriteriaDAO.delete(substitutionCriteriaId);
    }

    public void deleteSubstitutionCriteria(Subject subject, SubstitutionCriteria substitutionCriteria) {
        substitutionCriteriaDAO.delete(substitutionCriteria);
    }

    public List<Substitution> getBySubstitutionCriteria(Subject subject, SubstitutionCriteria substitutionCriteria) {
        return substitutionCriteriaDAO.getSubstitutionsByCriteria(substitutionCriteria);
    }
}
