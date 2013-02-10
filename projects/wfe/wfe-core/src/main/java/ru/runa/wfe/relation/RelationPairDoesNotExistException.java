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

import ru.runa.wfe.InternalApplicationException;

/**
 * Exception, which was thrown, if {@link RelationPair} is not exists.
 */
public class RelationPairDoesNotExistException extends InternalApplicationException {
    private static final long serialVersionUID = 1L;

    /**
     * Identity of {@link RelationPair}, which is not exists.
     */
    final Long relationId;

    /**
     * Create instance with specified {@link RelationPair} identity.
     * 
     * @param relationId
     */
    public RelationPairDoesNotExistException(Long relationId) {
        this.relationId = relationId;
    }

    /**
     * Return relation pair identity.
     * 
     * @return Relation pair identity.
     */
    public Long getRelationId() {
        return relationId;
    }
}
