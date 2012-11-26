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

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.wfe.commons.BackCompatibilityClassNames;
import ru.runa.wfe.commons.CalendarUtil;
import ru.runa.wfe.commons.ftl.FreemarkerTag;
import ru.runa.wfe.var.dto.WfVariable;
import ru.runa.wfe.var.format.DateFormat;
import ru.runa.wfe.var.format.DateTimeFormat;
import ru.runa.wfe.var.format.TimeFormat;
import freemarker.template.TemplateModelException;

public class DateTimeInputTag extends FreemarkerTag {
    private static final long serialVersionUID = 1L;
    private static final Log log = LogFactory.getLog(DateTimeInputTag.class);

    @Override
    protected Object executeTag() throws TemplateModelException {
        String variableName = getParameterAs(String.class, 0);
        WfVariable variable = variableProvider.getVariableNotNull(variableName);
        String format = BackCompatibilityClassNames.getClassName(variable.getDefinition().getFormat());
        Date date = (Date) variable.getValue();
        String html = "";
        if (DateFormat.class.getName().equals(format) || DateTimeFormat.class.getName().equals(format)) {
            html += "<input type=\"text\" class=\"inputDate\" length=\"10\" name=\"" + variableName + "\" style=\"width: 100px;\" ";
            if (date != null) {
                html += "value=\"" + CalendarUtil.formatDate(date) + "\" ";
            }
            html += "/>";
        }
        if (TimeFormat.class.getName().equals(format) || DateTimeFormat.class.getName().equals(format)) {
            html += "<input type=\"text\" class=\"inputTime\" length=\"5\" name=\"" + variableName + "\" style=\"width: 50px;\" ";
            if (date != null) {
                html += "value=\"" + CalendarUtil.formatTime(date) + "\" ";
            }
            html += "/>";
        }
        if (html.length() == 0) {
            log.warn("No HTML built (" + variableName + ") for format " + variable.getDefinition().getFormat());
        }
        return html;
    }

}
