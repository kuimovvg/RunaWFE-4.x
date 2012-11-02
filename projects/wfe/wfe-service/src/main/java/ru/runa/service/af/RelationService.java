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
package ru.runa.service.af;

import java.util.List;

import javax.security.auth.Subject;

import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.relation.Relation;
import ru.runa.wfe.relation.RelationAlreadyExistException;
import ru.runa.wfe.relation.RelationDoesNotExistException;
import ru.runa.wfe.relation.RelationPair;
import ru.runa.wfe.relation.RelationPairDoesNotExistException;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.user.Executor;

/**
  * Relations service interface.
  *
  * @author Konstantinov Aleksey 12.02.2012
  */
public interface RelationService {
    /**
     * Create {@link Relation} with specified name and description or throws {@link RelationAlreadyExistException} if relation with such name is already
     * exists.
     * 
     * @param subject
     *            Subject, which perform operation.
     * @param name
     *            Relation name.
     * @param description
     *            Relation description.
     * @return Created relation.
     * @throws RelationAlreadyExistException
     *             Relation already exists.
     * @throws AuthorizationException
     *             Insufficient permission to perform operation.
     * @throws AuthenticationException
     *             Subject is incorrect.
     */
    public Relation createRelation(Subject subject, String name, String description) throws RelationAlreadyExistException, AuthorizationException,
            AuthenticationException;

    /**
     * Return list of {@link Relation}, according to specified {@link BatchPresentation}.
     * 
     * @param subject
     *            Subject, which perform operation.
     * @param batchPresentation
     *            Restrictions to get relations.
     * @return List of {@link Relation}.
     * @throws AuthorizationException
     *             Insufficient permission to perform operation.
     * @throws AuthenticationException
     *             Subject is incorrect.
     */
    public List<Relation> getRelations(Subject subject, BatchPresentation batchPresentation) throws AuthorizationException, AuthenticationException;

    /**
     * Return {@link Relation} with specified name or throws {@link RelationDoesNotExistException} if relation with such name does not exists.
     * 
     * @param subject
     *            Subject, which perform operation.
     * @param name
     *            Relation name.
     * @return Relation with specified name.
     * @throws RelationDoesNotExistException
     *             Relation with specified name is not exists.
     * @throws AuthorizationException
     *             Insufficient permission to perform operation.
     * @throws AuthenticationException
     *             Subject is incorrect.
     */
    public Relation getRelation(Subject subject, String relationsName) throws RelationDoesNotExistException, AuthorizationException,
            AuthenticationException;

    /**
     * Return {@link Relation} with specified identity or throws {@link RelationDoesNotExistException} if relation with such identity does not exists.
     * 
     * @param subject
     *            Subject, which perform operation.
     * @param relationId
     *            Relation identity.
     * @return Relation with specified name.
     * @throws RelationDoesNotExistException
     *             Relation with specified name is not exists.
     * @throws AuthorizationException
     *             Insufficient permission to perform operation.
     * @throws AuthenticationException
     *             Subject is incorrect.
     */
    public Relation getRelation(Subject subject, Long relationId) throws RelationDoesNotExistException, AuthorizationException,
            AuthenticationException;

    /**
     * Remove {@link Relation} with specified identity.
     * 
     * @param subject
     *            Subject, which perform operation.
     * @param relationId
     *            Relation identity.
     * @throws RelationDoesNotExistException
     *             Relation with specified identity does not exists.
     * @throws AuthorizationException
     *             Insufficient permission to perform operation.
     * @throws AuthenticationException
     *             Subject is incorrect.
     */
    public void removeRelation(Subject subject, Long relationId) throws RelationDoesNotExistException, AuthorizationException,
            AuthenticationException;

    /**
     * Add {@link RelationPair} to {@link Relation} with specified name.
     * 
     * @param subject
     *            Subject, which perform operation.
     * @param relationName
     *            Relation name.
     * @param left
     *            Left part of relation pair.
     * @param right
     *            Right part of relation pair.
     * @return Created relation pair.
     * @throws RelationDoesNotExistException
     *             Relation with specified name does not exists.
     * @throws AuthorizationException
     *             Insufficient permission to perform operation.
     * @throws AuthenticationException
     *             Subject is incorrect.
     */
    public RelationPair addRelationPair(Subject subject, String relationName, Executor left, Executor right) throws RelationDoesNotExistException,
            AuthorizationException, AuthenticationException;

    /**
     * Removes {@link RelationPair} with specified identity.
     * 
     * @param subject
     *            Subject, which perform operation.
     * @param relationPairId
     *            {@link RelationPair} identity.
     * @throws RelationPairDoesnotExistException
     *             {@link RelationPair} does not exists.
     * @throws AuthorizationException
     *             Insufficient permission to perform operation.
     * @throws AuthenticationException
     *             Subject is incorrect.
     */
    public void removeRelationPair(Subject subject, Long relationPairId) throws RelationPairDoesNotExistException, AuthorizationException,
            AuthenticationException;

    /**
     * Return {@link RelationPair} for specified {@link Relation}, according to specified {@link BatchPresentation}.
     * 
     * @param subject
     *            Subject, which perform operation.
     * @param relationName
     *            Relation name.
     * @param batchPresentation
     *            Restrictions to get {@link RelationPair}.
     * @return
     * @throws RelationDoesNotExistException
     * @throws AuthorizationException
     *             Insufficient permission to perform operation.
     * @throws AuthenticationException
     *             Subject is incorrect.
     */
    public List<RelationPair> getRelationPairs(Subject subject, String relationsName, BatchPresentation batchPresentation)
            throws RelationDoesNotExistException, AuthorizationException, AuthenticationException;

    /**
     * Return {@link RelationPair} for specified {@link Relation}, according to specified {@link BatchPresentation}.
     * 
     * @param subject
     *            Subject, which perform operation.
     * @param relationId
     *            Relation identity.
     * @param batchPresentation
     *            Restrictions to get {@link RelationPair}.
     * @return
     * @throws RelationDoesNotExistException
     * @throws AuthorizationException
     *             Insufficient permission to perform operation.
     * @throws AuthenticationException
     *             Subject is incorrect.
     */
    public List<RelationPair> getRelationPairs(Subject subject, Long relationId, BatchPresentation batchPresentation)
            throws RelationDoesNotExistException, AuthorizationException, AuthenticationException;

    /**
     * Return {@link RelationPair} for specified {@link Relation}, which right part contains executor from 'right' parameter.
     * 
     * @param subject
     *            Subject, which perform operation.
     * @param relationName
     *            {@link Relation} name.
     * @param right
     *            Collection of {@link Executor}, which contains in right part of {@link RelationPair}.
     * @return List of {@link RelationPair}.
     * @throws RelationDoesNotExistException
     *             {@link Relation} with specified name does not exists.
     * @throws AuthorizationException
     *             Insufficient permission to perform operation.
     * @throws AuthenticationException
     *             Subject is incorrect.
     */
    public List<RelationPair> getExecutorsRelationPairsRight(Subject subject, String relationName, List<Executor> right)
            throws RelationDoesNotExistException, AuthorizationException, AuthenticationException;

    /**
     * Return {@link RelationPair} for specified {@link Relation}, which left part contains executor from 'left' parameter.
     * 
     * @param subject
     *            Subject, which perform operation.
     * @param relationName
     *            {@link Relation} name.
     * @param right
     *            Collection of {@link Executor}, which contains in left part of {@link RelationPair}.
     * @return List of {@link RelationPair}.
     * @throws RelationDoesNotExistException
     *             {@link Relation} with specified name does not exists.
     * @throws AuthorizationException
     *             Insufficient permission to perform operation.
     * @throws AuthenticationException
     *             Subject is incorrect.
     */
    public List<RelationPair> getExecutorsRelationPairsLeft(Subject subject, String relationName, List<Executor> left)
            throws RelationDoesNotExistException, AuthorizationException, AuthenticationException;
}
