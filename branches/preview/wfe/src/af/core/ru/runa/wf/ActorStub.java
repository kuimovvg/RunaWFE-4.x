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
package ru.runa.wf;

import java.io.InvalidObjectException;
import java.io.ObjectStreamException;
import java.util.Set;

import ru.runa.af.Actor;

/**
 * This stub is used to replace real actor in case performer does not have permission to read {@link ru.runa.af.Actor}but an actor must be returned insted of {@link ru.runa.af.AuthorizationException}All methods throws {@link java.lang.IllegalStateException}Created on 13.04.2005
 * 
 */
public class ActorStub extends Actor {
    private static final long serialVersionUID = -7900616626362375711L;

    public static final ActorStub UNAUTHORIZED_ACTOR_STUB = new ActorStub();

    public static final ActorStub NOT_EXISTING_ACTOR_STUB = new ActorStub();

    private final static UnsupportedOperationException UNSUPPORTED_OPERATION_EXCEPTION = new UnsupportedOperationException(
            "Trying to access method in class " + ActorStub.class.getName());

    private final int hashCode;

    private static int currentCode = 0;

    private ActorStub() {
        super();
        hashCode = currentCode++;
    }

    public Long getCode() {
        throw UNSUPPORTED_OPERATION_EXCEPTION;
    }

    public String getFullName() {
        throw UNSUPPORTED_OPERATION_EXCEPTION;
    }

    public void setFullName(String fullName) {
        throw UNSUPPORTED_OPERATION_EXCEPTION;
    }

    public String getDescription() {
        throw UNSUPPORTED_OPERATION_EXCEPTION;
    }

    public Set getGroups() {
        throw UNSUPPORTED_OPERATION_EXCEPTION;
    }

    public Long getId() {
        throw UNSUPPORTED_OPERATION_EXCEPTION;
    }

    public String getName() {
        throw UNSUPPORTED_OPERATION_EXCEPTION;
    }

    public Set getPermissionMappings() {
        throw UNSUPPORTED_OPERATION_EXCEPTION;
    }

    public void setDescription(String description) {
        throw UNSUPPORTED_OPERATION_EXCEPTION;
    }

    public void setGroups(Set executorGroups) {
        throw UNSUPPORTED_OPERATION_EXCEPTION;
    }

    public void setName(String name) {
        throw UNSUPPORTED_OPERATION_EXCEPTION;
    }

    public void setPermissionMappings(Set permissionMappings) {
        throw UNSUPPORTED_OPERATION_EXCEPTION;
    }

    public String toString() {
        return getClass().getName();
    }

    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ActorStub)) {
            return false;
        }
        ActorStub stub = (ActorStub) obj;

        if (hashCode == stub.hashCode) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        int result = 17;
        result = 37 * result + hashCode;
        return result;
    }

    private Object readResolve() throws ObjectStreamException {
        if (hashCode == UNAUTHORIZED_ACTOR_STUB.hashCode) {
            return UNAUTHORIZED_ACTOR_STUB;
        } else if (hashCode == NOT_EXISTING_ACTOR_STUB.hashCode) {
            return NOT_EXISTING_ACTOR_STUB;
        } else {
            throw new InvalidObjectException("Unknown object with id " + hashCode + " of class " + getClass().getName());
        }
    }
}
