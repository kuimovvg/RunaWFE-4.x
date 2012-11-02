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

import javax.security.auth.Subject;

import ru.runa.service.af.SubstitutionService;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.ss.Substitution;
import ru.runa.wfe.ss.SubstitutionCriteria;
import ru.runa.wfe.ss.SubstitutionDoesNotExistException;
import ru.runa.wfe.user.ExecutorDoesNotExistException;

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
    public Substitution createSubstitution(Subject subject, Long actorId, Substitution substitution) throws AuthorizationException,
            AuthenticationException, ExecutorDoesNotExistException {
        return getSubstitutionService().createSubstitution(subject, actorId, substitution);
    }

    @Override
    public List<Substitution> get(Subject subject, Long actorId) throws AuthorizationException, AuthenticationException,
            ExecutorDoesNotExistException {
        return getSubstitutionService().get(subject, actorId);
    }

    @Override
    public void delete(Subject subject, List<Long> substitutionIds) throws AuthorizationException, AuthenticationException,
            ExecutorDoesNotExistException, SubstitutionDoesNotExistException {
        getSubstitutionService().delete(subject, substitutionIds);
    }

    @Override
    public void switchSubstitutionsPositions(Subject subject, Long substitutionId1, Long substitutionId2) throws SubstitutionDoesNotExistException,
            AuthorizationException, AuthenticationException, ExecutorDoesNotExistException {
        getSubstitutionService().switchSubstitutionsPositions(subject, substitutionId1, substitutionId2);
    }

    @Override
    public Substitution getSubstitution(Subject subject, Long substitutionId) throws AuthorizationException, AuthenticationException,
            ExecutorDoesNotExistException {
        return getSubstitutionService().getSubstitution(subject, substitutionId);
    }

    @Override
    public void store(Subject subject, Substitution substitution) throws AuthorizationException, AuthenticationException,
            ExecutorDoesNotExistException {
        getSubstitutionService().store(subject, substitution);
    }

    @Override
    public <T extends SubstitutionCriteria> void createSubstitutionCriteria(Subject subject, T substitutionCriteria) throws AuthorizationException,
            AuthenticationException, ExecutorDoesNotExistException {
        getSubstitutionService().createSubstitutionCriteria(subject, substitutionCriteria);
    }

    @Override
    public SubstitutionCriteria getSubstitutionCriteria(Subject subject, Long substitutionCriteriaId) throws AuthorizationException,
            AuthenticationException, ExecutorDoesNotExistException {
        return getSubstitutionService().getSubstitutionCriteria(subject, substitutionCriteriaId);
    }

    @Override
    public List<SubstitutionCriteria> getSubstitutionCriteriaAll(Subject subject) throws AuthorizationException, AuthenticationException,
            ExecutorDoesNotExistException {
        return getSubstitutionService().getSubstitutionCriteriaAll(subject);
    }

    @Override
    public void store(Subject subject, SubstitutionCriteria substitutionCriteria) throws AuthorizationException, AuthenticationException,
            ExecutorDoesNotExistException {
        getSubstitutionService().store(subject, substitutionCriteria);
    }

    @Override
    public void deleteSubstitutionCriteria(Subject subject, Long substitutionCriteriaId) throws AuthorizationException, AuthenticationException,
            ExecutorDoesNotExistException {
        getSubstitutionService().deleteSubstitutionCriteria(subject, substitutionCriteriaId);
    }

    @Override
    public void deleteSubstitutionCriteria(Subject subject, SubstitutionCriteria substitutionCriteria) throws AuthorizationException,
            AuthenticationException, ExecutorDoesNotExistException {
        getSubstitutionService().deleteSubstitutionCriteria(subject, substitutionCriteria);
    }

    @Override
    public List<Substitution> getBySubstitutionCriteria(Subject subject, SubstitutionCriteria substitutionCriteria) throws AuthorizationException,
            AuthenticationException, ExecutorDoesNotExistException {
        return getSubstitutionService().getBySubstitutionCriteria(subject, substitutionCriteria);
    }
}
