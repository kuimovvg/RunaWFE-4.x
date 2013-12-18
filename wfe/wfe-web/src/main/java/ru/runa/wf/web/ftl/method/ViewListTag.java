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
package ru.runa.wf.web.ftl.method;

import java.util.ArrayList;
import java.util.List;

import ru.runa.wfe.commons.ftl.FreemarkerTag;
import ru.runa.wfe.var.dto.WfVariable;
import freemarker.template.TemplateModelException;


/**
 * shared code with {@link DisplayVariableTag}.
 * 
 * @author dofs
 * @since 4.0
 */
public class ViewListTag extends FreemarkerTag {
    private static final long serialVersionUID = 1L;

    @Override
    protected Object executeTag() throws TemplateModelException {
        String variableName = getParameterAs(String.class, 0);
        WfVariable variable = variableProvider.getVariableNotNull(variableName);
        String scriptingVariableName = variable.getDefinition().getScriptingName();
        String elementFormatClassName = ViewUtil.getElementFormatClassName(variable, 0);
        StringBuffer html = new StringBuffer();
        List<Object> list = variableProvider.getValue(List.class, variableName);
        if (list == null) {
            list = new ArrayList<Object>();
        }
        html.append("<span class=\"viewList\" id=\"").append(scriptingVariableName).append("\">");
        for (int row = 0; row < list.size(); row++) {
            Object value = list.get(row);
            html.append("<div row=\"").append(row).append("\">");
            html.append(ViewUtil.getComponentOutput(user, webHelper, variableProvider.getProcessId(), 
                    variableName + "[" + row + "]", elementFormatClassName, value));
            html.append("</div>");
        }
        html.append("</span>");
        return html.toString();
    }
}
