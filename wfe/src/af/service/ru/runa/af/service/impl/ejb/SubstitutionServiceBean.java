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
package ru.runa.af.service.impl.ejb;

import java.util.List;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.interceptor.Interceptors;
import javax.security.auth.Subject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;

import ru.runa.af.ArgumentsCommons;
import ru.runa.af.AuthenticationException;
import ru.runa.af.AuthorizationException;
import ru.runa.af.ExecutorOutOfDateException;
import ru.runa.af.Substitution;
import ru.runa.af.SubstitutionCriteria;
import ru.runa.af.SubstitutionOutOfDateException;
import ru.runa.af.logic.SubstitutionLogic;
import ru.runa.af.service.SubstitutionServiceLocal;
import ru.runa.af.service.SubstitutionServiceRemote;

/**
 * Created on 30.01.2006
 */
@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@Interceptors({SpringBeanAutowiringInterceptor.class, LoggerInterceptor.class})
public class SubstitutionServiceBean implements SubstitutionServiceLocal, SubstitutionServiceRemote {
    @Autowired
    private SubstitutionLogic substitutionLogic;

    @Override
    public Substitution createSubstitution(Subject subject, Long actorId, Substitution substitution) throws AuthorizationException, AuthenticationException,
            ExecutorOutOfDateException {
        ArgumentsCommons.checkNotNull(subject);
        ArgumentsCommons.checkNotNull(substitution);
        substitutionLogic.createSubstitution(subject, actorId, substitution);
        return substitution;
    }

    @Override
    public List<Substitution> get(Subject subject, Long actorId) throws AuthorizationException, AuthenticationException, ExecutorOutOfDateException {
        return substitutionLogic.getSubstitutions(subject, actorId);
    }

    @Override
    public void delete(Subject subject, List<Long> substitutionIds) throws AuthorizationException, AuthenticationException, ExecutorOutOfDateException,
            SubstitutionOutOfDateException {
        ArgumentsCommons.checkNotNull(subject);
        ArgumentsCommons.checkNotNull(substitutionIds);
        substitutionLogic.delete(subject, substitutionIds);
    }

    @Override
    public void switchSubstitutionsPositions(Subject subject, Long substitutionId1, Long substitutionId2) throws SubstitutionOutOfDateException,
            AuthorizationException, AuthenticationException, ExecutorOutOfDateException {
        ArgumentsCommons.checkNotNull(subject);
        substitutionLogic.switchSubstitutionsPositions(subject, substitutionId1, substitutionId2);
    }

    @Override
    public Substitution getSubstitution(Subject subject, Long substitutionId) throws AuthorizationException, AuthenticationException,
            ExecutorOutOfDateException {
        ArgumentsCommons.checkNotNull(subject);
        return substitutionLogic.getSubstitution(subject, substitutionId);
    }

    @Override
    public void store(Subject subject, Substitution substitution) throws AuthorizationException, AuthenticationException, ExecutorOutOfDateException {
        ArgumentsCommons.checkNotNull(subject);
        ArgumentsCommons.checkNotNull(substitution);
        substitutionLogic.store(subject, substitution);
    }

    @Override
    public void createSubstitutionCriteria(Subject subject, SubstitutionCriteria substitutionCriteria) throws AuthorizationException,
            AuthenticationException, ExecutorOutOfDateException {
        ArgumentsCommons.checkNotNull(subject);
        ArgumentsCommons.checkNotNull(substitutionCriteria);
        substitutionLogic.createSubstitutionCriteria(subject, substitutionCriteria);
    }

    @Override
    public SubstitutionCriteria getSubstitutionCriteria(Subject subject, Long substitutionCriteriaId) throws AuthorizationException,
            AuthenticationException, ExecutorOutOfDateException {
        ArgumentsCommons.checkNotNull(subject);
        return substitutionLogic.getSubstitutionCriteria(subject, substitutionCriteriaId);
    }

    @Override
    public List<SubstitutionCriteria> getSubstitutionCriteriaAll(Subject subject) throws AuthorizationException, AuthenticationException,
            ExecutorOutOfDateException {
        ArgumentsCommons.checkNotNull(subject);
        return substitutionLogic.getSubstitutionCriteriaAll(subject);
    }

    @Override
    public void store(Subject subject, SubstitutionCriteria substitutionCriteria) throws AuthorizationException, AuthenticationException,
            ExecutorOutOfDateException {
        ArgumentsCommons.checkNotNull(subject);
        ArgumentsCommons.checkNotNull(substitutionCriteria);
        substitutionLogic.store(subject, substitutionCriteria);
    }

    @Override
    public void deleteSubstitutionCriteria(Subject subject, Long substitutionCriteriaId) throws AuthorizationException, AuthenticationException,
            ExecutorOutOfDateException {
        ArgumentsCommons.checkNotNull(subject);
        substitutionLogic.deleteSubstitutionCriteria(subject, substitutionCriteriaId);
    }

    @Override
    public void deleteSubstitutionCriteria(Subject subject, SubstitutionCriteria substitutionCriteria) throws AuthorizationException,
            AuthenticationException, ExecutorOutOfDateException {
        ArgumentsCommons.checkNotNull(subject);
        ArgumentsCommons.checkNotNull(substitutionCriteria);
        substitutionLogic.deleteSubstitutionCriteria(subject, substitutionCriteria);
    }

    @Override
    public List<Substitution> getBySubstitutionCriteria(Subject subject, SubstitutionCriteria substitutionCriteria) throws AuthorizationException,
            AuthenticationException, ExecutorOutOfDateException {
        ArgumentsCommons.checkNotNull(subject);
        ArgumentsCommons.checkNotNull(substitutionCriteria);
        return substitutionLogic.getBySubstitutionCriteria(subject, substitutionCriteria);
    }

}
