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
package ru.runa.wf.web.html.vartag;

import java.util.HashMap;

import javax.security.auth.Subject;
import javax.servlet.jsp.PageContext;

import org.apache.ecs.StringElement;
import org.apache.ecs.html.A;

import ru.runa.InternalApplicationException;
import ru.runa.af.AuthenticationException;
import ru.runa.common.web.Commons;
import ru.runa.common.web.Commons.PortletUrl;
import ru.runa.common.web.IdentityType;
import ru.runa.wf.FileVariable;
import ru.runa.wf.web.html.VarTag;
import ru.runa.wf.web.html.WorkflowFormProcessingException;

/**
 * Created on 14.06.2005
 * 
 */
public class FileVariableValueDownloadVarTag implements VarTag {

    /**
     * Object identity type to load file variable.
     */
    final IdentityType identityType;

    public FileVariableValueDownloadVarTag() {
        super();
        identityType = IdentityType.TASK;
    }

    public FileVariableValueDownloadVarTag(IdentityType identityType) {
        super();
        this.identityType = identityType;
    }

    public String getHtml(Subject subject, String varName, Object var, PageContext pageContext) throws WorkflowFormProcessingException,
            AuthenticationException {
        if (pageContext == null || var == null) {
            return "";
        }

        String pricessInstanceIdParam = pageContext.getRequest().getParameter("id");
        if (pricessInstanceIdParam == null) {
            throw new InternalApplicationException("taskIdParam was not passed correctly to FileVariableValueDownloadVarTag");
        }

        if (var instanceof FileVariable) {
            A ahref = new A();
            ahref.addElement(new StringElement(((FileVariable) var).getName()));

            HashMap<String, String> parametersMap = new HashMap<String, String>();
            parametersMap.put("id", pricessInstanceIdParam);
            parametersMap.put("variableName", varName);
            parametersMap.put("identityType", identityType.toString());
            ahref.setHref(Commons.getActionUrl("/variableDownloader", parametersMap, pageContext, PortletUrl.Render));
            return ahref.toString();
        }
        throw VarTagUtils.createTypeMismatchException(varName, var, this.getClass(), FileVariable.class);
    }
}
