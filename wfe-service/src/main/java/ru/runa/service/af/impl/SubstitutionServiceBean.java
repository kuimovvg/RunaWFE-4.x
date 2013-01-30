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
package ru.runa.service.af.impl;

import java.util.List;

import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.interceptor.Interceptors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;

import ru.runa.service.af.SubstitutionServiceLocal;
import ru.runa.service.af.SubstitutionServiceRemote;
import ru.runa.service.interceptors.EjbExceptionSupport;
import ru.runa.service.interceptors.EjbTransactionSupport;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.ss.Substitution;
import ru.runa.wfe.ss.SubstitutionCriteria;
import ru.runa.wfe.ss.SubstitutionDoesNotExistException;
import ru.runa.wfe.ss.logic.SubstitutionLogic;
import ru.runa.wfe.user.ExecutorDoesNotExistException;
import ru.runa.wfe.user.User;

import com.google.common.base.Preconditions;

/**
 * Created on 30.01.2006
 */
@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
@Interceptors({ EjbExceptionSupport.class, EjbTransactionSupport.class, SpringBeanAutowiringInterceptor.class })
public class SubstitutionServiceBean implements SubstitutionServiceLocal, SubstitutionServiceRemote {
    @Autowired
    private SubstitutionLogic substitutionLogic;

    @Override
    public Substitution createSubstitution(User user, Long actorId, Substitution substitution) throws AuthorizationException,
            AuthenticationException, ExecutorDoesNotExistException {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(substitution);
        substitutionLogic.createSubstitution(user, actorId, substitution);
        return substitution;
    }

    @Override
    public List<Substitution> get(User user, Long actorId) throws AuthorizationException, AuthenticationException, ExecutorDoesNotExistException {
        return substitutionLogic.getSubstitutions(user, actorId);
    }

    @Override
    public void delete(User user, List<Long> substitutionIds) throws AuthorizationException, AuthenticationException, ExecutorDoesNotExistException,
            SubstitutionDoesNotExistException {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(substitutionIds);
        substitutionLogic.delete(user, substitutionIds);
    }

    @Override
    public void switchSubstitutionsPositions(User user, Long substitutionId1, Long substitutionId2) throws SubstitutionDoesNotExistException,
            AuthorizationException, AuthenticationException, ExecutorDoesNotExistException {
        Preconditions.checkNotNull(user);
        substitutionLogic.switchSubstitutionsPositions(user, substitutionId1, substitutionId2);
    }

    @Override
    public Substitution getSubstitution(User user, Long substitutionId) throws AuthorizationException, AuthenticationException,
            ExecutorDoesNotExistException {
        Preconditions.checkNotNull(user);
        return substitutionLogic.getSubstitution(user, substitutionId);
    }

    @Override
    public void store(User user, Substitution substitution) throws AuthorizationException, AuthenticationException, ExecutorDoesNotExistException {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(substitution);
        substitutionLogic.create(user, substitution);
    }

    @Override
    public void createSubstitutionCriteria(User user, SubstitutionCriteria substitutionCriteria) throws AuthorizationException,
            AuthenticationException, ExecutorDoesNotExistException {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(substitutionCriteria);
        substitutionLogic.createSubstitutionCriteria(user, substitutionCriteria);
    }

    @Override
    public SubstitutionCriteria getSubstitutionCriteria(User user, Long substitutionCriteriaId) throws AuthorizationException,
            AuthenticationException, ExecutorDoesNotExistException {
        Preconditions.checkNotNull(user);
        return substitutionLogic.getSubstitutionCriteria(user, substitutionCriteriaId);
    }

    @Override
    public List<SubstitutionCriteria> getSubstitutionCriteriaAll(User user) throws AuthorizationException, AuthenticationException,
            ExecutorDoesNotExistException {
        Preconditions.checkNotNull(user);
        return substitutionLogic.getSubstitutionCriteriaAll(user);
    }

    @Override
    public void store(User user, SubstitutionCriteria substitutionCriteria) throws AuthorizationException, AuthenticationException,
            ExecutorDoesNotExistException {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(substitutionCriteria);
        substitutionLogic.store(user, substitutionCriteria);
    }

    @Override
    public void deleteSubstitutionCriteria(User user, Long substitutionCriteriaId) throws AuthorizationException, AuthenticationException,
            ExecutorDoesNotExistException {
        Preconditions.checkNotNull(user);
        substitutionLogic.deleteSubstitutionCriteria(user, substitutionCriteriaId);
    }

    @Override
    public void deleteSubstitutionCriteria(User user, SubstitutionCriteria substitutionCriteria) throws AuthorizationException,
            AuthenticationException, ExecutorDoesNotExistException {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(substitutionCriteria);
        substitutionLogic.deleteSubstitutionCriteria(user, substitutionCriteria);
    }

    @Override
    public List<Substitution> getBySubstitutionCriteria(User user, SubstitutionCriteria substitutionCriteria) throws AuthorizationException,
            AuthenticationException, ExecutorDoesNotExistException {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(substitutionCriteria);
        return substitutionLogic.getBySubstitutionCriteria(user, substitutionCriteria);
    }

}
