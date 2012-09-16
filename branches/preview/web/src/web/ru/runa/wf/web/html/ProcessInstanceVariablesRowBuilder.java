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
package ru.runa.wf.web.html;

import java.util.List;

import javax.security.auth.Subject;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.TR;

import ru.runa.af.web.SubjectHttpSessionHelper;
import ru.runa.common.web.Resources;
import ru.runa.common.web.html.RowBuilder;
import ru.runa.commons.format.FormatCommons;
import ru.runa.commons.format.VariableDisplaySupport;
import ru.runa.commons.format.WebFormat;
import ru.runa.wf.VariableStub;

public class ProcessInstanceVariablesRowBuilder implements RowBuilder {
    private static final Log log = LogFactory.getLog(ProcessInstanceVariablesRowBuilder.class);

    private int idx = 0;
    private final List<VariableStub> variables;
    private final PageContext pageContext;
    private final Long instanceId;

    public ProcessInstanceVariablesRowBuilder(Long instanceId, List<VariableStub> variables, PageContext pageContext) {
        this.variables = variables;
        this.instanceId = instanceId;
        this.pageContext = pageContext;
    }

    public boolean hasNext() {
        return idx < variables.size();
    }

    public TR buildNext() throws JspException {
        VariableStub variable = variables.get(idx);
        TR tr = new TR();
        tr.addElement(new TD(variable.getDefinition().getName()).setClass(Resources.CLASS_LIST_TABLE_TD));
        tr.addElement(new TD(variable.getClassName()).setClass(Resources.CLASS_LIST_TABLE_TD));
        Object value = variable.getValue();
        String formattedValue;
        if (value == null) {
            formattedValue = "null";
        } else {
            try {
                WebFormat webFormat = FormatCommons.create(variable.getDefinition().getFormat());
                if (webFormat instanceof VariableDisplaySupport) {
                    Subject subject = SubjectHttpSessionHelper.getActorSubject(pageContext.getSession());
                    formattedValue = ((VariableDisplaySupport) webFormat).getHtml(subject, pageContext, instanceId, variable.getDefinition()
                            .getName(), value);
                } else {
                    formattedValue = webFormat.format(value);
                }
            } catch (Exception e) {
                log.warn("Unable to format value " + value + " of decl " + variable.getDefinition() + " in " + instanceId, e);
                formattedValue = value.toString() + " <span class=\"error\">(" + e.getMessage() + ")</span>";
            }
        }
        tr.addElement(new TD(formattedValue).setClass(Resources.CLASS_LIST_TABLE_TD));

        idx++;
        return tr;
    }

}
