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
import ru.runa.af.Executor;
import ru.runa.af.Relation;
import ru.runa.af.RelationDoesNotExistsException;
import ru.runa.af.RelationExistException;
import ru.runa.af.RelationPair;
import ru.runa.af.RelationPairDoesNotExistException;
import ru.runa.af.logic.RelationLogic;
import ru.runa.af.presentation.BatchPresentation;
import ru.runa.af.service.RelationServiceLocal;
import ru.runa.af.service.RelationServiceRemote;

/**
 * Implements RelationService as bean.
 * 
 * @author Konstantinov Aleksey 12.02.2012
 */
@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@Interceptors({SpringBeanAutowiringInterceptor.class, LoggerInterceptor.class})
public class RelationServiceBean implements RelationServiceLocal, RelationServiceRemote {
    @Autowired
    private RelationLogic relationLogic;

    @Override
    public RelationPair addRelationPair(Subject subject, String relationGroupName, Executor from, Executor to)
            throws RelationDoesNotExistsException, AuthorizationException, AuthenticationException {
        ArgumentsCommons.checkNotNull(subject);
        ArgumentsCommons.checkNotEmpty(relationGroupName, "Relation name");
        ArgumentsCommons.checkNotNull(from);
        ArgumentsCommons.checkNotNull(to);
        return relationLogic.addRelationPair(subject, relationGroupName, from, to);
    }

    @Override
    public Relation createRelation(Subject subject, String name, String description) throws RelationExistException, AuthorizationException,
            AuthenticationException {
        ArgumentsCommons.checkNotNull(subject);
        ArgumentsCommons.checkNotEmpty(name, "Relation name");
        return relationLogic.createRelation(subject, name, description);
    }

    @Override
    public List<Relation> getRelations(Subject subject, BatchPresentation batchPresentation) throws AuthorizationException,
            AuthenticationException {
        ArgumentsCommons.checkNotNull(subject);
        ArgumentsCommons.checkNotNull(batchPresentation);
        return relationLogic.getRelations(subject, batchPresentation);
    }

    @Override
    public Relation getRelation(Subject subject, String relationsGroupName) throws RelationDoesNotExistsException, AuthorizationException,
            AuthenticationException {
        ArgumentsCommons.checkNotNull(subject);
        ArgumentsCommons.checkNotEmpty(relationsGroupName, "Relation name");
        return relationLogic.getRelation(subject, relationsGroupName);
    }

    @Override
    public Relation getRelation(Subject subject, Long relationId) throws RelationDoesNotExistsException, AuthorizationException,
            AuthenticationException {
        ArgumentsCommons.checkNotNull(subject);
        return relationLogic.getRelation(subject, relationId);
    }

    @Override
    public List<RelationPair> getExecutorsRelationPairsRight(Subject subject, String relationName, List<Executor> right)
            throws RelationDoesNotExistsException, AuthorizationException, AuthenticationException {
        ArgumentsCommons.checkNotNull(subject);
        ArgumentsCommons.checkNotNull(right);
        return relationLogic.getExecutorRelationPairsRight(subject, relationName, right);
    }

    @Override
    public List<RelationPair> getExecutorsRelationPairsLeft(Subject subject, String relationName, List<Executor> left)
            throws RelationDoesNotExistsException, AuthorizationException, AuthenticationException {
        ArgumentsCommons.checkNotNull(subject);
        ArgumentsCommons.checkNotNull(left);
        return relationLogic.getExecutorRelationPairsLeft(subject, relationName, left);
    }

    @Override
    public List<RelationPair> getRelationPairs(Subject subject, String relationsGroupName, BatchPresentation batchPresentation)
            throws RelationDoesNotExistsException, AuthorizationException, AuthenticationException {
        ArgumentsCommons.checkNotNull(subject);
        ArgumentsCommons.checkNotEmpty(relationsGroupName, "Relation name");
        ArgumentsCommons.checkNotNull(batchPresentation);
        return relationLogic.getRelations(subject, relationsGroupName, batchPresentation);
    }

    @Override
    public List<RelationPair> getRelationPairs(Subject subject, Long relationId, BatchPresentation batchPresentation)
            throws RelationDoesNotExistsException, AuthorizationException, AuthenticationException {
        ArgumentsCommons.checkNotNull(subject);
        ArgumentsCommons.checkNotNull(batchPresentation);
        return relationLogic.getRelations(subject, relationId, batchPresentation);
    }

    @Override
    public void removeRelationPair(Subject subject, Long relationId) throws RelationPairDoesNotExistException, AuthorizationException,
            AuthenticationException {
        ArgumentsCommons.checkNotNull(subject);
        relationLogic.removeRelationPair(subject, relationId);
    }

    @Override
    public void removeRelation(Subject subject, Long relationGroupId) throws RelationDoesNotExistsException, AuthorizationException,
            AuthenticationException {
        ArgumentsCommons.checkNotNull(subject);
        relationLogic.removeRelation(subject, relationGroupId);
    }

}
