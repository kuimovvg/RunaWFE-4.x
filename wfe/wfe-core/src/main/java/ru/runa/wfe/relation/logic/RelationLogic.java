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
package ru.runa.wfe.relation.logic;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.security.auth.Subject;

import org.springframework.beans.factory.annotation.Autowired;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.logic.CommonLogic;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.relation.Relation;
import ru.runa.wfe.relation.RelationAlreadyExistException;
import ru.runa.wfe.relation.RelationDoesNotExistException;
import ru.runa.wfe.relation.RelationPair;
import ru.runa.wfe.relation.RelationPairDoesNotExistException;
import ru.runa.wfe.relation.RelationPermission;
import ru.runa.wfe.relation.RelationsGroupSecure;
import ru.runa.wfe.relation.dao.RelationDAO;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredObjectType;
import ru.runa.wfe.security.auth.SubjectPrincipalsHelper;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.ExecutorDoesNotExistException;

/**
 * Relation logic.
 * 
 * @author Konstantinov Aleksey 12.02.2012
 */
public class RelationLogic extends CommonLogic {
    @Autowired
    private RelationDAO relationDAO;

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
            AuthorizationException, AuthenticationException {
        checkPermissionAllowed(subject, relationDAO.getRelationNotNull(relationName), RelationPermission.UPDATE_RELATION);
        return relationDAO.addRelationPair(relationName, left, right);
    }

    /**
     * Create {@link Relation} with specified name and description or throws {@link RelationAlreadyExistException} if relation with such name is already exists.
     * 
     * @param subject
     *            Subject, which perform operation.
     * @param name
     *            Relation name
     * @param description
     *            Relation description
     * @return Created relation.
     * @throws RelationAlreadyExistException
     *             Relation already exists.
     * @throws AuthorizationException
     *             Insufficient permission to perform operation.
     * @throws AuthenticationException
     *             Subject is incorrect.
     */
    public Relation createRelation(Subject subject, String name, String description) throws AuthorizationException, AuthenticationException,
            RelationAlreadyExistException {
        try {
            checkPermissionAllowed(subject, RelationsGroupSecure.INSTANCE, RelationPermission.UPDATE_RELATION);
            return relationDAO.createRelation(name, description);
        } catch (ExecutorDoesNotExistException e) {
            throw new InternalApplicationException(e);
        }
    }

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
    @SuppressWarnings("unchecked")
    public List<Relation> getRelations(Subject subject, BatchPresentation batchPresentation) throws AuthorizationException, AuthenticationException {
        checkPermissionAllowed(subject, RelationsGroupSecure.INSTANCE, Permission.READ);
        Actor actor = SubjectPrincipalsHelper.getActor(subject);
        List<Long> actorAndGroupsIds = executorDAO.getActorAndGroupsIds(actor);
        return (List<Relation>) permissionDAO.getPersistentObjects(actorAndGroupsIds, batchPresentation, Permission.READ,
                new SecuredObjectType[] { SecuredObjectType.RELATION }, false);
    }

    /**
     * Return {@link RelationPair} for specified {@link Relation}, which right part contains executor from 'right' parameter.
     * 
     * @param subject
     *            Subject, which perform operation.
     * @param relationName
     *            {@link Relation} name. If null, when {@link RelationPair} for all {@link Relation} returned.
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
    public List<RelationPair> getExecutorRelationPairsRight(Subject subject, String relationName, List<Executor> right)
            throws RelationDoesNotExistException, AuthorizationException, AuthenticationException {
        List<RelationPair> result = new ArrayList<RelationPair>();
        List<RelationPair> loadedPairs = relationDAO.getExecutorsRelationPairsRight(relationName, right);
        Set<Relation> allowedRelations = getRelationsWithReadPermission(subject, loadedPairs);
        for (RelationPair pair : loadedPairs) {
            if (allowedRelations.contains(pair.getRelation())) {
                result.add(pair);
            }
        }
        return result;
    }

    /**
     * Return {@link RelationPair} for specified {@link Relation}, which left part contains executor from 'left' parameter.
     * 
     * @param subject
     *            Subject, which perform operation.
     * @param relationName
     *            {@link Relation} name. If null, when {@link RelationPair} for all {@link Relation} returned.
     * @param left
     *            Collection of {@link Executor}, which contains in left part of {@link RelationPair}.
     * @return List of {@link RelationPair}.
     * @throws RelationDoesNotExistException
     *             {@link Relation} with specified name does not exists.
     * @throws AuthorizationException
     *             Insufficient permission to perform operation.
     * @throws AuthenticationException
     *             Subject is incorrect.
     */
    public List<RelationPair> getExecutorRelationPairsLeft(Subject subject, String relationName, List<Executor> left)
            throws RelationDoesNotExistException, AuthorizationException, AuthenticationException {
        List<RelationPair> result = new ArrayList<RelationPair>();
        List<RelationPair> loadedPairs = relationDAO.getExecutorsRelationPairsLeft(relationName, left);
        Set<Relation> allowedRelations = getRelationsWithReadPermission(subject, loadedPairs);
        for (RelationPair pair : loadedPairs) {
            if (allowedRelations.contains(pair.getRelation())) {
                result.add(pair);
            }
        }
        return result;
    }

    /**
     * Return {@link Relation} with specified name or throws {@link RelationDoesNotExistException} if relation with such name does not exists.
     * 
     * @param subject
     *            Subject, which perform operation.
     * @param relationName
     *            Relation name
     * @return Relation with specified name.
     * @throws RelationDoesNotExistException
     *             Relation with specified name is not exists.
     * @throws AuthorizationException
     *             Insufficient permission to perform operation.
     * @throws AuthenticationException
     *             Subject is incorrect.
     */
    public Relation getRelation(Subject subject, String relationName) throws RelationDoesNotExistException, AuthorizationException,
            AuthenticationException {
        checkPermissionAllowed(subject, relationDAO.getRelationNotNull(relationName), Permission.READ);
        return relationDAO.getRelationNotNull(relationName);
    }

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
            AuthenticationException {
        checkPermissionAllowed(subject, relationDAO.getRelationNotNull(relationId), Permission.READ);
        return relationDAO.getRelationNotNull(relationId);
    }

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
    public List<RelationPair> getRelations(Subject subject, String relationName, BatchPresentation batchPresentation)
            throws RelationDoesNotExistException, AuthorizationException, AuthenticationException {
        checkPermissionAllowed(subject, relationDAO.getRelationNotNull(relationName), Permission.READ);
        return relationDAO.getRelationPairs(relationName, batchPresentation);
    }

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
    public List<RelationPair> getRelations(Subject subject, Long relationId, BatchPresentation batchPresentation)
            throws RelationDoesNotExistException, AuthorizationException, AuthenticationException {
        checkPermissionAllowed(subject, relationDAO.getRelationNotNull(relationId), Permission.READ);
        return relationDAO.getRelationPairs(relationId, batchPresentation);
    }

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
            AuthenticationException {
        RelationPair relationPair = relationDAO.getRelationPairNotNull(relationPairId);
        checkPermissionAllowed(subject, relationPair.getRelation(), RelationPermission.UPDATE_RELATION);
        relationDAO.removeRelationPair(relationPairId);
    }

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
            AuthenticationException {
        checkPermissionAllowed(subject, RelationsGroupSecure.INSTANCE, RelationPermission.UPDATE_RELATION);
        relationDAO.removeRelation(relationId);
    }

    /**
     * Returns set of {@link Relation} from {@link relationPairs} parameter with Read permission for current Subject.
     * 
     * @param daoHolder
     *            Object to get different DAO.
     * @param subject
     *            Subject, which perform operation.
     * @param relationPairs
     *            Relation pairs, from which {@link Relation} extracted.
     * @return {@link Relation}'s with READ permission.
     */
    private Set<Relation> getRelationsWithReadPermission(Subject subject, List<RelationPair> relationPairs) throws AuthenticationException {
        Set<Relation> result = new HashSet<Relation>();
        for (RelationPair relationPair : relationPairs) {
            Relation relation = relationPair.getRelation();
            if (isPermissionAllowed(subject, relation, Permission.READ)) {
                result.add(relation);
            }
        }
        return result;
    }
}
