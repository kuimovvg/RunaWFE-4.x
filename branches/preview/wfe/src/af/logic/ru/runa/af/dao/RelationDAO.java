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
package ru.runa.af.dao;

import java.util.Collection;
import java.util.List;

import ru.runa.af.Executor;
import ru.runa.af.Relation;
import ru.runa.af.RelationDoesNotExistsException;
import ru.runa.af.RelationExistException;
import ru.runa.af.RelationPair;
import ru.runa.af.RelationPairDoesNotExistException;
import ru.runa.af.presentation.BatchPresentation;

/**
  * Relations dao level interface.
  * @author Konstantinov Aleksey 12.02.2012
  */
public interface RelationDAO {
    /**
     * Create {@link Relation} with specified name and description or throws {@link RelationExistException} if relation with such name is already
     * exists.
     * 
     * @param name
     *            Relation name
     * @param description
     *            Relation description
     * @return Created relation.
     * @throws RelationExistException
     *             Relation already exists.
     */
    public Relation createRelation(String name, String description) throws RelationExistException;

    /**
     * Return {@link Relation} with specified name or throws {@link RelationDoesNotExistsException} if relation with such name does not exists.
     * 
     * @param name
     *            Relation name
     * @return Relation with specified name.
     * @throws RelationDoesNotExistsException
     *             Relation with specified name is not exists.
     */
    public Relation getRelation(String name) throws RelationDoesNotExistsException;

    /**
     * Return {@link Relation} with specified identity or throws {@link RelationDoesNotExistsException} if relation with such identity does not exists.
     * 
     * @param id
     *            Relation identity.
     * @return Relation with specified name.
     * @throws RelationDoesNotExistsException
     *             Relation with specified name is not exists.
     */
    public Relation getRelation(Long id) throws RelationDoesNotExistsException;

    /**
     * Return list of {@link Relation}, according to specified {@link BatchPresentation}.
     * 
     * @param batchPresentation
     *            Restrictions to get relations.
     * @return List of {@link Relation}.
     */
    public List<Relation> getRelations(BatchPresentation batchPresentation);

    /**
     * Remove {@link Relation} with specified identity.
     * 
     * @param id
     *            Relation identity.
     * @throws RelationDoesNotExistsException
     *             Relation with specified identity does not exists.
     */
    public void removeRelation(Long id) throws RelationDoesNotExistsException;

    public void removeAllRelationPairs(Executor executor);

    /**
     * Add {@link RelationPair} to {@link Relation} with specified name.
     * 
     * @param relationName
     *            Relation name.
     * @param left
     *            Left part of relation pair.
     * @param right
     *            Right part of relation pair.
     * @return Created relation pair.
     * @throws RelationDoesNotExistsException
     *             Relation with specified name does not exists.
     */
    public RelationPair addRelationPair(String relationName, Executor left, Executor right) throws RelationDoesNotExistsException;

    /**
     * Removes {@link RelationPair} with specified identity.
     * 
     * @param id
     *            {@link RelationPair} identity.
     * @throws RelationPairDoesnotExistException
     *             {@link RelationPair} does not exists.
     */
    public void removeRelationPair(Long id) throws RelationPairDoesNotExistException;

    /**
     * Return {@link RelationPair} for specified {@link Relation}, according to specified {@link BatchPresentation}.
     * 
     * @param relationName
     *            Relation name
     * @param batchPresentation
     *            Restrictions to get {@link RelationPair}.
     * @return
     * @throws RelationDoesNotExistsException
     */
    public List<RelationPair> getRelationPairs(String relationName, BatchPresentation batchPresentation) throws RelationDoesNotExistsException;

    /**
     * Return {@link RelationPair} for specified {@link Relation}, according to specified {@link BatchPresentation}.
     * 
     * @param id
     *            Relation identity.
     * @param batchPresentation
     *            Restrictions to get {@link RelationPair}.
     * @return
     * @throws RelationDoesNotExistsException
     */
    public List<RelationPair> getRelationPairs(Long id, BatchPresentation batchPresentation) throws RelationDoesNotExistsException;

    /**
     * Return {@link RelationPair} with specified identity.
     * 
     * @param id
     *            {@link RelationPair} identity.
     * @return {@link RelationPair} with specified identity.
     * @throws RelationPairDoesnotExistException
     *             {@link RelationPair} does not exists.
     */
    public RelationPair getRelationPair(Long id) throws RelationPairDoesNotExistException;

    /**
     * Return {@link RelationPair} for specified {@link Relation}, which right part contains executor from 'right' parameter.
     * 
     * @param relationName
     *            {@link Relation} name. If null, when {@link RelationPair} for all {@link Relation} returned.
     * @param right
     *            Collection of {@link Executor}, which contains in right part of {@link RelationPair}.
     * @return List of {@link RelationPair}.
     * @throws RelationDoesNotExistsException
     *             {@link Relation} with specified name does not exists.
     */
    public List<RelationPair> getExecutorsRelationPairsRight(String relationName, Collection<Executor> right) throws RelationDoesNotExistsException;

    /**
     * Return {@link RelationPair} for specified {@link Relation}, which left part contains executor from 'left' parameter.
     * 
     * @param relationName
     *            {@link Relation} name. If null, when {@link RelationPair} for all {@link Relation} returned.
     * @param right
     *            Collection of {@link Executor}, which contains in left part of {@link RelationPair}.
     * @return List of {@link RelationPair}.
     * @throws RelationDoesNotExistsException
     *             {@link Relation} with specified name does not exists.
     */
    public List<RelationPair> getExecutorsRelationPairsLeft(String relationName, Collection<Executor> left) throws RelationDoesNotExistsException;
}
