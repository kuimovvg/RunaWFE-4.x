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
package ru.runa.wfe.relation;

import ru.runa.wfe.WfException;

/**
 * Thrown if {@link Relation} not found.
 */
public class RelationDoesNotExistException extends WfException {

    private static final long serialVersionUID = 1L;

    /**
     * If {@link Relation} identity is unspecified, when {@link #relationId} was set to {@link #UNSET_RELATION_ID}
     */
    public final long UNSET_RELATION_ID = -1;

    /**
     * Name of {@link Relation}, which can't be found. May be null, if {@link Relation} searching by id.
     */
    final String relationName;

    /**
     * Identity of {@link Relation}, which can't be found. May be {@link #UNSET_RELATION_ID}, if {@link Relation} searching by name.
     */
    final Long relationId;

    /**
     * Return name of relation, which can't be found. May be null, if relation searching by identity.
     * 
     * @return Name of relation.
     */
    public String getName() {
        return relationName;
    }

    /**
     * Create instance with specified relation name and inner exception.
     * 
     * @param name
     *            Name of relation, which can't be found.
     * @param exception
     *            Exception, occurs when searching.
     */
    public RelationDoesNotExistException(String name, Throwable exception) {
        super(exception);
        relationName = name;
        relationId = UNSET_RELATION_ID;
    }

    /**
     * Create instance with specified relation name.
     * 
     * @param name
     *            Name of relation, which can't be found.
     */
    public RelationDoesNotExistException(String name) {
        relationName = name;
        relationId = UNSET_RELATION_ID;
    }

    /**
     * Create instance with specified relation identity.
     * 
     * @param relationId
     *            Identity of relation, which can't e found.
     */
    public RelationDoesNotExistException(Long relationId) {
        relationName = null;
        this.relationId = relationId;
    }
}
