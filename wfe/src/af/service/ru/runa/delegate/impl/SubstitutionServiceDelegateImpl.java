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
package ru.runa.delegate.impl;

import java.util.List;

import javax.security.auth.Subject;

import ru.runa.af.AuthenticationException;
import ru.runa.af.AuthorizationException;
import ru.runa.af.ExecutorOutOfDateException;
import ru.runa.af.Substitution;
import ru.runa.af.SubstitutionCriteria;
import ru.runa.af.SubstitutionOutOfDateException;
import ru.runa.af.service.SubstitutionService;

/**
 * Created on 30.01.2006
 * 
 */
public class SubstitutionServiceDelegateImpl extends EJB3Delegate implements SubstitutionService {
    @Override
    protected String getBeanName() {
        return "SubstitutionServiceBean";
    }

    private SubstitutionService getSubstitutionService() {
        return getService();
    }

    @Override
    public Substitution createSubstitution(Subject subject, Long actorId, Substitution substitution) throws AuthorizationException, AuthenticationException,
            ExecutorOutOfDateException {
        return getSubstitutionService().createSubstitution(subject, actorId, substitution);
    }

    @Override
    public List<Substitution> get(Subject subject, Long actorId) throws AuthorizationException, AuthenticationException, ExecutorOutOfDateException {
        return getSubstitutionService().get(subject, actorId);
    }

    @Override
    public void delete(Subject subject, List<Long> substitutionIds) throws AuthorizationException, AuthenticationException, ExecutorOutOfDateException,
            SubstitutionOutOfDateException {
        getSubstitutionService().delete(subject, substitutionIds);
    }

    @Override
    public void switchSubstitutionsPositions(Subject subject, Long substitutionId1, Long substitutionId2) throws SubstitutionOutOfDateException,
            AuthorizationException, AuthenticationException, ExecutorOutOfDateException {
        getSubstitutionService().switchSubstitutionsPositions(subject, substitutionId1, substitutionId2);
    }

    @Override
    public Substitution getSubstitution(Subject subject, Long substitutionId) throws AuthorizationException, AuthenticationException,
            ExecutorOutOfDateException {
        return getSubstitutionService().getSubstitution(subject, substitutionId);
    }

    @Override
    public void store(Subject subject, Substitution substitution) throws AuthorizationException, AuthenticationException, ExecutorOutOfDateException {
        getSubstitutionService().store(subject, substitution);
    }

    @Override
    public <T extends SubstitutionCriteria> void createSubstitutionCriteria(Subject subject, T substitutionCriteria) throws AuthorizationException,
            AuthenticationException, ExecutorOutOfDateException {
        getSubstitutionService().createSubstitutionCriteria(subject, substitutionCriteria);
    }

    @Override
    public SubstitutionCriteria getSubstitutionCriteria(Subject subject, Long substitutionCriteriaId) throws AuthorizationException,
            AuthenticationException, ExecutorOutOfDateException {
        return getSubstitutionService().getSubstitutionCriteria(subject, substitutionCriteriaId);
    }

    @Override
    public List<SubstitutionCriteria> getSubstitutionCriteriaAll(Subject subject) throws AuthorizationException, AuthenticationException,
            ExecutorOutOfDateException {
        return getSubstitutionService().getSubstitutionCriteriaAll(subject);
    }

    @Override
    public void store(Subject subject, SubstitutionCriteria substitutionCriteria) throws AuthorizationException, AuthenticationException,
            ExecutorOutOfDateException {
        getSubstitutionService().store(subject, substitutionCriteria);
    }

    @Override
    public void deleteSubstitutionCriteria(Subject subject, Long substitutionCriteriaId) throws AuthorizationException, AuthenticationException,
            ExecutorOutOfDateException {
        getSubstitutionService().deleteSubstitutionCriteria(subject, substitutionCriteriaId);
    }

    @Override
    public void deleteSubstitutionCriteria(Subject subject, SubstitutionCriteria substitutionCriteria) throws AuthorizationException,
            AuthenticationException, ExecutorOutOfDateException {
        getSubstitutionService().deleteSubstitutionCriteria(subject, substitutionCriteria);
    }

    @Override
    public List<Substitution> getBySubstitutionCriteria(Subject subject, SubstitutionCriteria substitutionCriteria) throws AuthorizationException,
            AuthenticationException, ExecutorOutOfDateException {
        return getSubstitutionService().getBySubstitutionCriteria(subject, substitutionCriteria);
    }
}
