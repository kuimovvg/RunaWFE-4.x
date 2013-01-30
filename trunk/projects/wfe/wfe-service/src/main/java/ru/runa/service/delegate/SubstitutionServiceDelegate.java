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
package ru.runa.service.delegate;

import java.util.List;

import ru.runa.service.af.SubstitutionService;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.ss.Substitution;
import ru.runa.wfe.ss.SubstitutionCriteria;
import ru.runa.wfe.ss.SubstitutionDoesNotExistException;
import ru.runa.wfe.user.ExecutorDoesNotExistException;
import ru.runa.wfe.user.User;

/**
 * Created on 30.01.2006
 * 
 */
public class SubstitutionServiceDelegate extends EJB3Delegate implements SubstitutionService {

    public SubstitutionServiceDelegate() {
        super(SubstitutionService.class);
    }

    private SubstitutionService getSubstitutionService() {
        return getService();
    }

    @Override
    public Substitution createSubstitution(User user, Long actorId, Substitution substitution) throws AuthorizationException,
            AuthenticationException, ExecutorDoesNotExistException {
        return getSubstitutionService().createSubstitution(user, actorId, substitution);
    }

    @Override
    public List<Substitution> get(User user, Long actorId) throws AuthorizationException, AuthenticationException, ExecutorDoesNotExistException {
        return getSubstitutionService().get(user, actorId);
    }

    @Override
    public void delete(User user, List<Long> substitutionIds) throws AuthorizationException, AuthenticationException, ExecutorDoesNotExistException,
            SubstitutionDoesNotExistException {
        getSubstitutionService().delete(user, substitutionIds);
    }

    @Override
    public void switchSubstitutionsPositions(User user, Long substitutionId1, Long substitutionId2) throws SubstitutionDoesNotExistException,
            AuthorizationException, AuthenticationException, ExecutorDoesNotExistException {
        getSubstitutionService().switchSubstitutionsPositions(user, substitutionId1, substitutionId2);
    }

    @Override
    public Substitution getSubstitution(User user, Long substitutionId) throws AuthorizationException, AuthenticationException,
            ExecutorDoesNotExistException {
        return getSubstitutionService().getSubstitution(user, substitutionId);
    }

    @Override
    public void store(User user, Substitution substitution) throws AuthorizationException, AuthenticationException, ExecutorDoesNotExistException {
        getSubstitutionService().store(user, substitution);
    }

    @Override
    public <T extends SubstitutionCriteria> void createSubstitutionCriteria(User user, T substitutionCriteria) throws AuthorizationException,
            AuthenticationException, ExecutorDoesNotExistException {
        getSubstitutionService().createSubstitutionCriteria(user, substitutionCriteria);
    }

    @Override
    public SubstitutionCriteria getSubstitutionCriteria(User user, Long substitutionCriteriaId) throws AuthorizationException,
            AuthenticationException, ExecutorDoesNotExistException {
        return getSubstitutionService().getSubstitutionCriteria(user, substitutionCriteriaId);
    }

    @Override
    public List<SubstitutionCriteria> getSubstitutionCriteriaAll(User user) throws AuthorizationException, AuthenticationException,
            ExecutorDoesNotExistException {
        return getSubstitutionService().getSubstitutionCriteriaAll(user);
    }

    @Override
    public void store(User user, SubstitutionCriteria substitutionCriteria) throws AuthorizationException, AuthenticationException,
            ExecutorDoesNotExistException {
        getSubstitutionService().store(user, substitutionCriteria);
    }

    @Override
    public void deleteSubstitutionCriteria(User user, Long substitutionCriteriaId) throws AuthorizationException, AuthenticationException,
            ExecutorDoesNotExistException {
        getSubstitutionService().deleteSubstitutionCriteria(user, substitutionCriteriaId);
    }

    @Override
    public void deleteSubstitutionCriteria(User user, SubstitutionCriteria substitutionCriteria) throws AuthorizationException,
            AuthenticationException, ExecutorDoesNotExistException {
        getSubstitutionService().deleteSubstitutionCriteria(user, substitutionCriteria);
    }

    @Override
    public List<Substitution> getBySubstitutionCriteria(User user, SubstitutionCriteria substitutionCriteria) throws AuthorizationException,
            AuthenticationException, ExecutorDoesNotExistException {
        return getSubstitutionService().getBySubstitutionCriteria(user, substitutionCriteria);
    }
}
