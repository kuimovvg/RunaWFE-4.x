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
package ru.runa.wfe.service.impl;

import java.util.List;

import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.interceptor.Interceptors;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;

import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.relation.Relation;
import ru.runa.wfe.relation.RelationDoesNotExistException;
import ru.runa.wfe.relation.RelationPair;
import ru.runa.wfe.relation.RelationPairDoesNotExistException;
import ru.runa.wfe.relation.logic.RelationLogic;
import ru.runa.wfe.service.decl.RelationServiceLocal;
import ru.runa.wfe.service.decl.RelationServiceRemote;
import ru.runa.wfe.service.interceptors.EjbExceptionSupport;
import ru.runa.wfe.service.interceptors.EjbTransactionSupport;
import ru.runa.wfe.service.interceptors.PerformanceObserver;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.User;

import com.google.common.base.Preconditions;

/**
 * Implements RelationService as bean.
 * 
 * @author Konstantinov Aleksey 12.02.2012
 */
@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
@Interceptors({ EjbExceptionSupport.class, PerformanceObserver.class, EjbTransactionSupport.class, SpringBeanAutowiringInterceptor.class })
@WebService(name = "RelationAPI", serviceName = "RelationWebService")
@SOAPBinding
public class RelationServiceBean implements RelationServiceLocal, RelationServiceRemote {
    @Autowired
    private RelationLogic relationLogic;

    @Override
    public RelationPair addRelationPair(User user, Long relationId, Executor from, Executor to) {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(from);
        Preconditions.checkNotNull(to);
        return relationLogic.addRelationPair(user, relationId, from, to);
    }

    @Override
    public Relation createRelation(User user, Relation relation) {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(relation);
        return relationLogic.createRelation(user, relation);
    }

    @Override
    public Relation updateRelation(User user, Relation relation) throws RelationDoesNotExistException {
        Preconditions.checkNotNull(user);
        return relationLogic.updateRelation(user, relation);
    }

    @Override
    public List<Relation> getRelations(User user, BatchPresentation batchPresentation) {
        Preconditions.checkNotNull(user);
        if (batchPresentation == null) {
            batchPresentation = BatchPresentationFactory.RELATIONS.createDefault();
        }
        return relationLogic.getRelations(user, batchPresentation);
    }

    @Override
    public Relation getRelationByName(User user, String name) {
        Preconditions.checkNotNull(user);
        return relationLogic.getRelation(user, name);
    }

    @Override
    public Relation getRelation(User user, Long id) {
        Preconditions.checkNotNull(user);
        return relationLogic.getRelation(user, id);
    }

    @Override
    public List<RelationPair> getExecutorsRelationPairsRight(User user, String name, List<? extends Executor> right) {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(right);
        return relationLogic.getExecutorRelationPairsRight(user, name, right);
    }

    @Override
    public List<RelationPair> getExecutorsRelationPairsLeft(User user, String name, List<? extends Executor> left) {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(left);
        return relationLogic.getExecutorRelationPairsLeft(user, name, left);
    }

    @Override
    public List<RelationPair> getRelationPairs(User user, String name, BatchPresentation batchPresentation) {
        Preconditions.checkNotNull(user);
        if (batchPresentation == null) {
            batchPresentation = BatchPresentationFactory.RELATION_PAIRS.createDefault();
        }
        return relationLogic.getRelations(user, name, batchPresentation);
    }

    @Override
    public void removeRelationPair(User user, Long id) throws RelationPairDoesNotExistException {
        Preconditions.checkNotNull(user);
        relationLogic.removeRelationPair(user, id);
    }

    @Override
    public void removeRelationPairs(User user, List<Long> ids) {
        Preconditions.checkNotNull(user);
        for (Long id : ids) {
            relationLogic.removeRelationPair(user, id);
        }
    }

    @Override
    public void removeRelation(User user, Long id) {
        Preconditions.checkNotNull(user);
        relationLogic.removeRelation(user, id);
    }

}
