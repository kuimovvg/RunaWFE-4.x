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

import java.io.Serializable;

import javax.security.auth.Subject;
import javax.servlet.jsp.PageContext;

import org.apache.ecs.html.TD;

import ru.runa.wfe.definition.DefinitionDoesNotExistException;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.security.Identifiable;
import ru.runa.wfe.security.Permission;

/**
 * @author Gordienko_m
 * @author Vitaliy S aka Yilativs
 */
public interface TDBuilder {

    public interface Env {
        public interface IdentifiableExtractor extends Serializable {
            public Identifiable getIdentifiable(Object o, Env env);
        }

        public Subject getSubject();

        public PageContext getPageContext();

        public BatchPresentation getBatchPresentation();

        public String getURL(Object object);

        public String getConfirmationMessage(Long pid);

        public boolean isAllowed(Permission permission, IdentifiableExtractor extractor) throws AuthorizationException, AuthenticationException;

        public boolean hasProcessDefinitionPermission(Permission permission, Long processDefinitionId) throws AuthorizationException,
                AuthenticationException, DefinitionDoesNotExistException;

        public Object getTaskVariable(Object object, IdentifiableExtractor processIdExtractor, String variableName) throws AuthenticationException;
    }

    public TD build(Object object, Env env);

    public String getValue(Object object, Env env);

    public String[] getSeparatedValues(Object object, Env env);

    public int getSeparatedValuesCount(Object object, Env env);
}
