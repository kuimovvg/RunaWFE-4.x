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
import javax.security.auth.Subject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;

import ru.runa.service.af.RelationServiceLocal;
import ru.runa.service.af.RelationServiceRemote;
import ru.runa.service.interceptors.EjbExceptionSupport;
import ru.runa.service.interceptors.EjbTransactionSupport;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.relation.Relation;
import ru.runa.wfe.relation.RelationAlreadyExistException;
import ru.runa.wfe.relation.RelationDoesNotExistException;
import ru.runa.wfe.relation.RelationPair;
import ru.runa.wfe.relation.RelationPairDoesNotExistException;
import ru.runa.wfe.relation.logic.RelationLogic;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.user.Executor;

import com.google.common.base.Preconditions;

/**
 * Implements RelationService as bean.
 * 
 * @author Konstantinov Aleksey 12.02.2012
 */
@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
@Interceptors({ EjbExceptionSupport.class, EjbTransactionSupport.class, SpringBeanAutowiringInterceptor.class })
public class RelationServiceBean implements RelationServiceLocal, RelationServiceRemote {
    @Autowired
    private RelationLogic relationLogic;

    @Override
    public RelationPair addRelationPair(Subject subject, String relationGroupName, Executor from, Executor to) throws RelationDoesNotExistException,
            AuthorizationException, AuthenticationException {
        Preconditions.checkNotNull(subject);
        Preconditions.checkNotNull(from);
        Preconditions.checkNotNull(to);
        return relationLogic.addRelationPair(subject, relationGroupName, from, to);
    }

    @Override
    public Relation createRelation(Subject subject, String name, String description) throws RelationAlreadyExistException, AuthorizationException,
            AuthenticationException {
        Preconditions.checkNotNull(subject);
        return relationLogic.createRelation(subject, name, description);
    }

    @Override
    public List<Relation> getRelations(Subject subject, BatchPresentation batchPresentation) throws AuthorizationException, AuthenticationException {
        Preconditions.checkNotNull(subject);
        Preconditions.checkNotNull(batchPresentation);
        return relationLogic.getRelations(subject, batchPresentation);
    }

    @Override
    public Relation getRelation(Subject subject, String relationsGroupName) throws RelationDoesNotExistException, AuthorizationException,
            AuthenticationException {
        Preconditions.checkNotNull(subject);
        return relationLogic.getRelation(subject, relationsGroupName);
    }

    @Override
    public Relation getRelation(Subject subject, Long relationId) throws RelationDoesNotExistException, AuthorizationException,
            AuthenticationException {
        Preconditions.checkNotNull(subject);
        return relationLogic.getRelation(subject, relationId);
    }

    @Override
    public List<RelationPair> getExecutorsRelationPairsRight(Subject subject, String relationName, List<Executor> right)
            throws RelationDoesNotExistException, AuthorizationException, AuthenticationException {
        Preconditions.checkNotNull(subject);
        Preconditions.checkNotNull(right);
        return relationLogic.getExecutorRelationPairsRight(subject, relationName, right);
    }

    @Override
    public List<RelationPair> getExecutorsRelationPairsLeft(Subject subject, String relationName, List<Executor> left)
            throws RelationDoesNotExistException, AuthorizationException, AuthenticationException {
        Preconditions.checkNotNull(subject);
        Preconditions.checkNotNull(left);
        return relationLogic.getExecutorRelationPairsLeft(subject, relationName, left);
    }

    @Override
    public List<RelationPair> getRelationPairs(Subject subject, String relationsGroupName, BatchPresentation batchPresentation)
            throws RelationDoesNotExistException, AuthorizationException, AuthenticationException {
        Preconditions.checkNotNull(subject);
        Preconditions.checkNotNull(batchPresentation);
        return relationLogic.getRelations(subject, relationsGroupName, batchPresentation);
    }

    @Override
    public List<RelationPair> getRelationPairs(Subject subject, Long relationId, BatchPresentation batchPresentation)
            throws RelationDoesNotExistException, AuthorizationException, AuthenticationException {
        Preconditions.checkNotNull(subject);
        Preconditions.checkNotNull(batchPresentation);
        return relationLogic.getRelations(subject, relationId, batchPresentation);
    }

    @Override
    public void removeRelationPair(Subject subject, Long relationId) throws RelationPairDoesNotExistException, AuthorizationException,
            AuthenticationException {
        Preconditions.checkNotNull(subject);
        relationLogic.removeRelationPair(subject, relationId);
    }

    @Override
    public void removeRelation(Subject subject, Long relationGroupId) throws RelationDoesNotExistException, AuthorizationException,
            AuthenticationException {
        Preconditions.checkNotNull(subject);
        relationLogic.removeRelation(subject, relationGroupId);
    }

}
