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

import java.util.Date;

import javax.security.auth.Subject;
import javax.servlet.jsp.PageContext;

import ru.runa.wf.web.forms.format.TimeFormat;
import ru.runa.wf.web.html.VarTag;
import ru.runa.wf.web.html.WorkflowFormProcessingException;

public class TimeValueDisplayVarTag implements VarTag {

    public String getHtml(Subject subject, String varName, Object var, PageContext pageContext) throws WorkflowFormProcessingException {
        if (var == null) {
            return "<p class='error'>null</p>";
        }
        if (var instanceof Date) {
            return new TimeFormat().format(var);
        }
        throw VarTagUtils.createTypeMismatchException(varName, var, this.getClass(), Date.class);
    }
}
