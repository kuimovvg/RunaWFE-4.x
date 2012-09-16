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
package ru.runa.wf.web.forms.format;

import java.util.HashMap;

import javax.security.auth.Subject;
import javax.servlet.jsp.PageContext;

import org.apache.ecs.html.A;

import ru.runa.common.web.Commons;
import ru.runa.common.web.Commons.PortletUrl;
import ru.runa.common.web.IdentityType;
import ru.runa.commons.format.VariableDisplaySupport;
import ru.runa.commons.format.WebFormat;
import ru.runa.wf.FileVariable;

/**
 * This class is marker class for validation.
 */
public class FileFormat implements WebFormat, VariableDisplaySupport {

    @Override
    public String format(Object object) {
        return ((FileVariable) object).getName();
    }

    @Override
    public Object parse(String[] strings) {
        throw new UnsupportedOperationException("file variable cannot be representable as string");
    }

    @Override
    public String getHtml(Subject subject, PageContext pageContext, Long instanceId, String name, Object value) {
        FileVariable fileVariable = (FileVariable) value;
        A ahref = new A();
        ahref.addElement(fileVariable.getName());
        HashMap<String, String> parametersMap = new HashMap<String, String>();
        parametersMap.put("id", String.valueOf(instanceId));
        parametersMap.put("variableName", name);
        parametersMap.put("identityType", IdentityType.PROCESS_INSTANCE.toString());
        ahref.setHref(Commons.getActionUrl("/variableDownloader", parametersMap, pageContext, PortletUrl.Render));
        return ahref.toString();
    }
}
