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
package ru.runa.af;

import java.io.Serializable;

/**
 * Exception, which was thrown, if {@link Relation} already found.  
 */
public class RelationExistException extends Exception implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Name of {@link Relation}, which already exists.
     */
    private final String relationName;

    /**
     * Create instance with specified relation name and inner exception.  
     * @param name Name of relation, which already exists.
     * @param exception Exception, occurs when relation adding.
     */
    public RelationExistException(String name, Throwable exception) {
        super(exception);
        relationName = name;
    }

    /**
     * Create instance with specified relation name.
     * @param name Name of relation, which already exists.
     */
    public RelationExistException(String name) {
        relationName = name;
    }

    /**
     * Return name of relation, which already exists.
     * @return Name of relation.
     */
    public String getRelationName() {
        return relationName;
    }
}
