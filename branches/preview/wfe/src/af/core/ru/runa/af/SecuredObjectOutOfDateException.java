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

import ru.runa.InternalApplicationException;

/**
 * Signals that {@link SecuredObject}does not exist.
 * 
 */
public class SecuredObjectOutOfDateException extends InternalApplicationException {

    private static final long serialVersionUID = 5177439264016603273L;

    private final Long extId;

    private final int type;

    public SecuredObjectOutOfDateException(Long extId, int type) {
        super("Secured object with extId " + extId + " and type " + type + " does not exists");
        this.extId = extId;
        this.type = type;
    }

    public Long getExtId() {
        return extId;
    }

    public int getType() {
        return type;
    }
}
