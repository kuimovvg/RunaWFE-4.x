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

import java.util.List;

import ru.runa.wfe.commons.ftl.FreemarkerTag;
import ru.runa.wfe.var.ISelectable;
import freemarker.template.TemplateModelException;

public class ViewListTag extends FreemarkerTag {
    private static final long serialVersionUID = 1L;

    @SuppressWarnings("unchecked")
    @Override
    protected Object executeTag() throws TemplateModelException {
        String optionsVarName = getParameterAs(String.class, 0);
        List<Object> options = getVariableAs(List.class, optionsVarName, false);
        String mode = getParameterAs(String.class, 1);
        if (mode == null) {
            mode = "ul";
        }
        StringBuffer html = new StringBuffer();
        if ("ul".equals(mode) || "ol".equals(mode)) {
            html.append("<").append(mode).append(">");
        }
        for (Object option : options) {
            String value;
            if (option instanceof ISelectable) {
                value = ((ISelectable) option).getDisplayName();
            } else {
                value = option.toString();
            }
            if ("ul".equals(mode) || "ol".equals(mode)) {
                html.append("<li>").append(value);
            } else if ("raw".equals(mode)) {
                html.append(value).append("&nbsp;");
            }
        }
        if ("ul".equals(mode) || "ol".equals(mode)) {
            html.append("</").append(mode).append(">");
        }
        return html;
    }

}
