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
package ru.runa.af.logic;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.security.auth.Subject;

import org.springframework.beans.factory.annotation.Autowired;

import ru.runa.InternalApplicationException;
import ru.runa.af.Actor;
import ru.runa.af.AuthenticationException;
import ru.runa.af.AuthorizationException;
import ru.runa.af.Executor;
import ru.runa.af.ExecutorOutOfDateException;
import ru.runa.af.Permission;
import ru.runa.af.Relation;
import ru.runa.af.RelationDoesNotExistsException;
import ru.runa.af.RelationExistException;
import ru.runa.af.RelationPair;
import ru.runa.af.RelationPairDoesNotExistException;
import ru.runa.af.RelationPermission;
import ru.runa.af.RelationsGroupSecure;
import ru.runa.af.UnapplicablePermissionException;
import ru.runa.af.authenticaion.SubjectPrincipalsHelper;
import ru.runa.af.dao.RelationDAO;
import ru.runa.af.presentation.BatchPresentation;

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
     * @throws RelationDoesNotExistsException
     *             Relation with specified name does not exists.
     * @throws AuthorizationException
     *             Insufficient permission to perform operation.
     * @throws AuthenticationException
     *             Subject is incorrect.
     */
    public RelationPair addRelationPair(Subject subject, String relationName, Executor left, Executor right) throws RelationDoesNotExistsException,
            AuthorizationException, AuthenticationException {
        checkPermissionAllowed(subject, relationDAO.getRelation(relationName), RelationPermission.UPDATE_RELATION);
        return relationDAO.addRelationPair(relationName, left, right);
    }

    /**
     * Create {@link Relation} with specified name and description or throws {@link RelationExistException} if relation with such name is already
     * exists.
     * 
     * @param subject
     *            Subject, which perform operation.
     * @param name
     *            Relation name
     * @param description
     *            Relation description
     * @return Created relation.
     * @throws RelationExistException
     *             Relation already exists.
     * @throws AuthorizationException
     *             Insufficient permission to perform operation.
     * @throws AuthenticationException
     *             Subject is incorrect.
     */
    public Relation createRelation(Subject subject, String name, String description) throws AuthorizationException, AuthenticationException,
            RelationExistException {
        try {
            checkPermissionAllowed(subject, RelationsGroupSecure.INSTANCE, RelationPermission.UPDATE_RELATION);
            Relation result = relationDAO.createRelation(name, description);
            securedObjectDAO.create(result);
            setPrivelegedExecutorsPermissionsOnIdentifiable(result);
            return result;
        } catch (UnapplicablePermissionException e) {
            throw new InternalApplicationException(e); // Unexpected
        } catch (ExecutorOutOfDateException e) {
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
        return (List<Relation>) securedObjectDAO.getPersistentObjects(actorAndGroupsIds, batchPresentation, Permission.READ,
                new int[] { securedObjectDAO.getType(Relation.class) }, false);
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
     * @throws RelationDoesNotExistsException
     *             {@link Relation} with specified name does not exists.
     * @throws AuthorizationException
     *             Insufficient permission to perform operation.
     * @throws AuthenticationException
     *             Subject is incorrect.
     */
    public List<RelationPair> getExecutorRelationPairsRight(Subject subject, String relationName, List<Executor> right)
            throws RelationDoesNotExistsException, AuthorizationException, AuthenticationException {
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
     * @throws RelationDoesNotExistsException
     *             {@link Relation} with specified name does not exists.
     * @throws AuthorizationException
     *             Insufficient permission to perform operation.
     * @throws AuthenticationException
     *             Subject is incorrect.
     */
    public List<RelationPair> getExecutorRelationPairsLeft(Subject subject, String relationName, List<Executor> left)
            throws RelationDoesNotExistsException, AuthorizationException, AuthenticationException {
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
     * Return {@link Relation} with specified name or throws {@link RelationDoesNotExistsException} if relation with such name does not exists.
     * 
     * @param subject
     *            Subject, which perform operation.
     * @param relationName
     *            Relation name
     * @return Relation with specified name.
     * @throws RelationDoesNotExistsException
     *             Relation with specified name is not exists.
     * @throws AuthorizationException
     *             Insufficient permission to perform operation.
     * @throws AuthenticationException
     *             Subject is incorrect.
     */
    public Relation getRelation(Subject subject, String relationName) throws RelationDoesNotExistsException, AuthorizationException,
            AuthenticationException {
        checkPermissionAllowed(subject, relationDAO.getRelation(relationName), Permission.READ);
        return relationDAO.getRelation(relationName);
    }

    /**
     * Return {@link Relation} with specified identity or throws {@link RelationDoesNotExistsException} if relation with such identity does not
     * exists.
     * 
     * @param subject
     *            Subject, which perform operation.
     * @param relationId
     *            Relation identity.
     * @return Relation with specified name.
     * @throws RelationDoesNotExistsException
     *             Relation with specified name is not exists.
     * @throws AuthorizationException
     *             Insufficient permission to perform operation.
     * @throws AuthenticationException
     *             Subject is incorrect.
     */
    public Relation getRelation(Subject subject, Long relationId) throws RelationDoesNotExistsException, AuthorizationException,
            AuthenticationException {
        checkPermissionAllowed(subject, relationDAO.getRelation(relationId), Permission.READ);
        return relationDAO.getRelation(relationId);
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
     * @throws RelationDoesNotExistsException
     * @throws AuthorizationException
     *             Insufficient permission to perform operation.
     * @throws AuthenticationException
     *             Subject is incorrect.
     */
    public List<RelationPair> getRelations(Subject subject, String relationName, BatchPresentation batchPresentation)
            throws RelationDoesNotExistsException, AuthorizationException, AuthenticationException {
        checkPermissionAllowed(subject, relationDAO.getRelation(relationName), Permission.READ);
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
     * @throws RelationDoesNotExistsException
     * @throws AuthorizationException
     *             Insufficient permission to perform operation.
     * @throws AuthenticationException
     *             Subject is incorrect.
     */
    public List<RelationPair> getRelations(Subject subject, Long relationId, BatchPresentation batchPresentation)
            throws RelationDoesNotExistsException, AuthorizationException, AuthenticationException {
        checkPermissionAllowed(subject, relationDAO.getRelation(relationId), Permission.READ);
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
        RelationPair relationPair = relationDAO.getRelationPair(relationPairId);
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
     * @throws RelationDoesNotExistsException
     *             Relation with specified identity does not exists.
     * @throws AuthorizationException
     *             Insufficient permission to perform operation.
     * @throws AuthenticationException
     *             Subject is incorrect.
     */
    public void removeRelation(Subject subject, Long relationId) throws RelationDoesNotExistsException, AuthorizationException,
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
