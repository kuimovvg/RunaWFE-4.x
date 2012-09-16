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

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.security.auth.Subject;
import javax.servlet.jsp.PageContext;

import org.apache.ecs.html.Input;

import ru.runa.wf.web.html.VarTag;
import ru.runa.wf.web.html.WorkflowFormProcessingException;

public class TimeWithSecondsInputVarTag implements VarTag {
    private static final SimpleDateFormat FORMAT = new SimpleDateFormat(ru.runa.wf.web.forms.format.TimeWithSecondsFormat.PATTERN);

    public String getHtml(Subject subject, String varName, Object var, PageContext pageContext) throws WorkflowFormProcessingException {
        Input input = new Input(Input.TEXT, varName);
        if (var == null) {
            return input.toString();
        }
        if (var instanceof Date) {
            input.setValue(FORMAT.format((Date) var));
            return input.toString();
        } else {
            throw VarTagUtils.createTypeMismatchException(varName, var, this.getClass(), Date.class);
        }
    }
}
