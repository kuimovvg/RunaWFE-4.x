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

import ru.runa.common.web.html.TDBuilder.Env;
import ru.runa.service.af.AuthorizationService;
import ru.runa.service.delegate.Delegates;
import ru.runa.service.wf.DefinitionService;
import ru.runa.wfe.definition.DefinitionDoesNotExistException;
import ru.runa.wfe.definition.dto.WfDefinition;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.security.Identifiable;
import ru.runa.wfe.security.Permission;

public abstract class EnvBaseImpl implements Env {

    protected class DefaultIdentifiableExtractor implements Env.IdentifiableExtractor {
        private static final long serialVersionUID = 1L;

        @Override
        public Identifiable getIdentifiable(Object o, Env env) {
            return (Identifiable) o;
        }
    }

    @Override
    public boolean hasProcessDefinitionPermission(Permission permission, Long processDefinitionId) throws DefinitionDoesNotExistException {
        try {
            Boolean result = processDefPermissionCache.get(processDefinitionId);
            if (result != null) {
                return result;
            }
            DefinitionService definitionService = Delegates.getDefinitionService();
            WfDefinition processDef = definitionService.getProcessDefinition(getSubject(), processDefinitionId);
            AuthorizationService authorizationService = ru.runa.service.delegate.Delegates.getAuthorizationService();
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
