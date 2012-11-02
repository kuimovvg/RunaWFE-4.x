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
package ru.runa.wfe.var.format;

import java.util.HashMap;

import javax.security.auth.Subject;
import javax.servlet.jsp.PageContext;

import ru.runa.wfe.commons.web.PortletUrlType;
import ru.runa.wfe.commons.web.WebHelper;
import ru.runa.wfe.var.FileVariable;

import com.google.common.collect.Maps;

/**
 * This class is marker class for validation.
 */
public class FileFormat implements VariableFormat, VariableDisplaySupport {

    @Override
    public String format(Object object) {
        return ((FileVariable) object).getName();
    }

    @Override
    public Object parse(String[] strings) {
        throw new UnsupportedOperationException("file variable cannot be representable as string");
    }

    @Override
    public String getHtml(Subject subject, PageContext pageContext, WebHelper webHelper, Long processId, String name, Object value) {
        FileVariable fileVariable = (FileVariable) value;
        HashMap<String, Object> params = Maps.newHashMap();
        params.put("id", processId);
        params.put("variableName", name);
        String href = webHelper.getActionUrl("/variableDownloader", params, pageContext, PortletUrlType.Render);
        return "<a href=\"" + href + "\">" + fileVariable.getName() + "</>";
    }
}
