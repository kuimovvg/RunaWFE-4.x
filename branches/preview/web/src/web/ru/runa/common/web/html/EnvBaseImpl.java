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
package ru.runa.common.web.html;

import java.util.HashMap;
import java.util.Map;

import ru.runa.af.AuthenticationException;
import ru.runa.af.AuthorizationException;
import ru.runa.af.Identifiable;
import ru.runa.af.Permission;
import ru.runa.af.service.AuthorizationService;
import ru.runa.common.web.html.TDBuilder.Env;
import ru.runa.delegate.DelegateFactory;
import ru.runa.wf.ProcessDefinitionDoesNotExistException;
import ru.runa.wf.ProcessDefinition;
import ru.runa.wf.service.DefinitionService;

public abstract class EnvBaseImpl implements Env {

    protected class DefaultIdentifiableExtractor implements Env.IdentifiableExtractor {
        public Identifiable getIdentifiable(Object o, Env env) {
            return (Identifiable) o;
        }
    }

    @Override
    public boolean hasProcessDefinitionPermission(Permission permission, Long processDefinitionId) throws ProcessDefinitionDoesNotExistException {
        try {
            Boolean result = processDefPermissionCache.get(processDefinitionId);
            if (result != null) {
                return result;
            }
            DefinitionService definitionService = DelegateFactory.getInstance().getDefinitionService();
            ProcessDefinition processDef = definitionService.getProcessDefinitionStub(getSubject(), processDefinitionId);
            AuthorizationService authorizationService = ru.runa.delegate.DelegateFactory.getInstance()
                    .getAuthorizationService();
            result = authorizationService.isAllowed(getSubject(), permission, processDef);
            processDefPermissionCache.put(processDefinitionId, result);
            return result;
        } catch (AuthenticationException e) {
        } catch (AuthorizationException e) {
        }
        return false;
    }

    private final Map<Long, Boolean> processDefPermissionCache = new HashMap<Long, Boolean>();
}
