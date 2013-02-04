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
import ru.runa.wfe.ss.Substitution;
import ru.runa.wfe.ss.SubstitutionCriteria;
import ru.runa.wfe.ss.logic.SubstitutionLogic;
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
    public Substitution createSubstitution(User user, Substitution substitution) {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(substitution);
        substitutionLogic.create(user, substitution);
        return substitution;
    }

    @Override
    public List<Substitution> getSubstitutions(User user, Long actorId) {
        return substitutionLogic.getSubstitutions(user, actorId);
    }

    @Override
    public void deleteSubstitutions(User user, List<Long> substitutionIds) {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(substitutionIds);
        substitutionLogic.delete(user, substitutionIds);
    }

    @Override
    public Substitution getSubstitution(User user, Long substitutionId) {
        Preconditions.checkNotNull(user);
        return substitutionLogic.getSubstitution(user, substitutionId);
    }

    @Override
    public void updateSubstitution(User user, Substitution substitution) {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(substitution);
        substitutionLogic.create(user, substitution);
    }

    @Override
    public void createCriteria(User user, SubstitutionCriteria substitutionCriteria) {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(substitutionCriteria);
        substitutionLogic.create(user, substitutionCriteria);
    }

    @Override
    public SubstitutionCriteria getCriteria(User user, Long substitutionCriteriaId) {
        Preconditions.checkNotNull(user);
        return substitutionLogic.getCriteria(user, substitutionCriteriaId);
    }

    @Override
    public List<SubstitutionCriteria> getAllCriterias(User user) {
        Preconditions.checkNotNull(user);
        return substitutionLogic.getAllCriterias(user);
    }

    @Override
    public void updateCriteria(User user, SubstitutionCriteria substitutionCriteria) {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(substitutionCriteria);
        substitutionLogic.update(user, substitutionCriteria);
    }

    @Override
    public void deleteCriterias(User user, List<SubstitutionCriteria> criterias) {
        Preconditions.checkNotNull(user);
        substitutionLogic.deleteCriterias(user, criterias);
    }

    @Override
    public void deleteCriteria(User user, SubstitutionCriteria substitutionCriteria) {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(substitutionCriteria);
        substitutionLogic.delete(user, substitutionCriteria);
    }

    @Override
    public List<Substitution> getSubstitutionsByCriteria(User user, SubstitutionCriteria substitutionCriteria) {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(substitutionCriteria);
        return substitutionLogic.getSubstitutionsByCriteria(user, substitutionCriteria);
    }

}
