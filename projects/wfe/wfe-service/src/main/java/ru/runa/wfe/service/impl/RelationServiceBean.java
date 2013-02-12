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
import ru.runa.wfe.relation.Relation;
import ru.runa.wfe.relation.RelationAlreadyExistException;
import ru.runa.wfe.relation.RelationPair;
import ru.runa.wfe.relation.logic.RelationLogic;
import ru.runa.wfe.service.decl.RelationServiceLocal;
import ru.runa.wfe.service.decl.RelationServiceRemote;
import ru.runa.wfe.service.interceptors.EjbExceptionSupport;
import ru.runa.wfe.service.interceptors.EjbTransactionSupport;
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
@Interceptors({ EjbExceptionSupport.class, EjbTransactionSupport.class, SpringBeanAutowiringInterceptor.class })
@WebService
@SOAPBinding
public class RelationServiceBean implements RelationServiceLocal, RelationServiceRemote {
    @Autowired
    private RelationLogic relationLogic;

    @Override
    public RelationPair addRelationPair(User user, String relationGroupName, Executor from, Executor to) {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(from);
        Preconditions.checkNotNull(to);
        return relationLogic.addRelationPair(user, relationGroupName, from, to);
    }

    @Override
    public Relation createRelation(User user, String name, String description) throws RelationAlreadyExistException {
        Preconditions.checkNotNull(user);
        return relationLogic.createRelation(user, name, description);
    }

    @Override
    public List<Relation> getRelations(User user, BatchPresentation batchPresentation) {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(batchPresentation);
        return relationLogic.getRelations(user, batchPresentation);
    }

    @Override
    public Relation getRelationByName(User user, String name) {
        Preconditions.checkNotNull(user);
        return relationLogic.getRelation(user, name);
    }

    @Override
    public Relation getRelation(User user, Long relationId) {
        Preconditions.checkNotNull(user);
        return relationLogic.getRelation(user, relationId);
    }

    @Override
    public List<RelationPair> getExecutorsRelationPairsRight(User user, String relationName, List<Executor> right) {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(right);
        return relationLogic.getExecutorRelationPairsRight(user, relationName, right);
    }

    @Override
    public List<RelationPair> getExecutorsRelationPairsLeft(User user, String relationName, List<Executor> left) {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(left);
        return relationLogic.getExecutorRelationPairsLeft(user, relationName, left);
    }

    @Override
    public List<RelationPair> getRelationPairs(User user, String relationName, BatchPresentation batchPresentation) {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(batchPresentation);
        return relationLogic.getRelations(user, relationName, batchPresentation);
    }

    @Override
    public void removeRelationPair(User user, Long relationId) {
        Preconditions.checkNotNull(user);
        relationLogic.removeRelationPair(user, relationId);
    }

    @Override
    public void removeRelation(User user, Long relationGroupId) {
        Preconditions.checkNotNull(user);
        relationLogic.removeRelation(user, relationGroupId);
    }

}
