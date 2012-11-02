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
package ru.runa.wf.logic.bot;

import java.util.Set;

import javax.security.auth.Subject;

import ru.runa.wfe.user.ActorPrincipal;

/**
 * Created on 28.04.2006
 * 
 */
abstract class AbstractOrgFunctionTaskHandler implements TaskHandler {

    protected Long getActorToSubstituteCode(Subject subject) {
        Set<ActorPrincipal> principals = subject.getPrincipals(ActorPrincipal.class);
        if (principals.size() > 0) {
            ActorPrincipal principal = principals.iterator().next();
            return new Long(principal.getActor().getCode());
        }
        return null;
    }
}
