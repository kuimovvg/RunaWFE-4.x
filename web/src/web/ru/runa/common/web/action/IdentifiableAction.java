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
package ru.runa.common.web.action;

import java.util.List;

import javax.security.auth.Subject;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionMessages;

import ru.runa.af.AuthenticationException;
import ru.runa.af.AuthorizationException;
import ru.runa.af.Identifiable;
import ru.runa.af.Permission;

/**
 */
public abstract class IdentifiableAction extends Action {

    protected abstract List<Permission> getIdentifiablePermissions();

    /**
     * ugly fuzzy method ;-P (today is friday)
     * 
     * @param subject
     * @param identifiableName
     * @param errors
     * @param errorForwardName
     * @return return specific identifiable (WARNING Might return null if errors
     *         occured)
     * @throws AuthorizationFailedException
     */
    protected abstract Identifiable getIdentifiable(Subject subject, Long identifiableId, ActionMessages errors) throws AuthorizationException,
            AuthenticationException;

}
