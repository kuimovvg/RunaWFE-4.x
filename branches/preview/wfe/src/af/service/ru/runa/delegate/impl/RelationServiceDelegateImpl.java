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
import ru.runa.af.Executor;
import ru.runa.af.Relation;
import ru.runa.af.RelationDoesNotExistsException;
import ru.runa.af.RelationExistException;
import ru.runa.af.RelationPair;
import ru.runa.af.RelationPairDoesNotExistException;
import ru.runa.af.presentation.BatchPresentation;
import ru.runa.af.service.RelationService;

/**
  * Local implementation for {@link RelationServiceDelegate}.
  *
  * @author Konstantinov Aleksey 12.02.2012
  */
public class RelationServiceDelegateImpl extends EJB3Delegate implements RelationService {
    @Override
    protected String getBeanName() {
        return "RelationServiceBean";
    }

    private RelationService getRelationService() {
        return getService();
    }

    @Override
    public RelationPair addRelationPair(Subject subject, String relationGroupName, Executor from, Executor to) throws RelationDoesNotExistsException,
            AuthorizationException, AuthenticationException {
        return getRelationService().addRelationPair(subject, relationGroupName, from, to);
    }

    @Override
    public List<Relation> getRelations(Subject subject, BatchPresentation batchPresentation) throws AuthorizationException, AuthenticationException {
        return getRelationService().getRelations(subject, batchPresentation);
    }

    @Override
    public Relation getRelation(Subject subject, String relationGroupName) throws RelationDoesNotExistsException, AuthorizationException,
            AuthenticationException {
        return getRelationService().getRelation(subject, relationGroupName);
    }

    @Override
    public Relation getRelation(Subject subject, Long relationId) throws RelationDoesNotExistsException, AuthorizationException,
            AuthenticationException {
        return getRelationService().getRelation(subject, relationId);
    }

    @Override
    public Relation createRelation(Subject subject, String name, String description) throws RelationExistException, AuthorizationException,
            AuthenticationException {
        return getRelationService().createRelation(subject, name, description);
    }

    @Override
    public List<RelationPair> getExecutorsRelationPairsRight(Subject subject, String relationName, List<Executor> right)
            throws RelationDoesNotExistsException, AuthorizationException, AuthenticationException {
        return getRelationService().getExecutorsRelationPairsRight(subject, relationName, right);
    }

    @Override
    public List<RelationPair> getExecutorsRelationPairsLeft(Subject subject, String relationName, List<Executor> left)
            throws RelationDoesNotExistsException, AuthorizationException, AuthenticationException {
        return getRelationService().getExecutorsRelationPairsLeft(subject, relationName, left);
    }

    @Override
    public List<RelationPair> getRelationPairs(Subject subject, String relationsGroupName, BatchPresentation batchPresentation)
            throws RelationDoesNotExistsException, AuthorizationException, AuthenticationException {
        return getRelationService().getRelationPairs(subject, relationsGroupName, batchPresentation);
    }

    @Override
    public List<RelationPair> getRelationPairs(Subject subject, Long relationId, BatchPresentation batchPresentation)
            throws RelationDoesNotExistsException, AuthorizationException, AuthenticationException {
        return getRelationService().getRelationPairs(subject, relationId, batchPresentation);
    }

    @Override
    public void removeRelationPair(Subject subject, Long relationId) throws RelationPairDoesNotExistException, AuthorizationException,
            AuthenticationException {
        getRelationService().removeRelationPair(subject, relationId);
    }

    @Override
    public void removeRelation(Subject subject, Long relationGroupName) throws RelationDoesNotExistsException, AuthorizationException,
            AuthenticationException {
        getRelationService().removeRelation(subject, relationGroupName);
    }
}
