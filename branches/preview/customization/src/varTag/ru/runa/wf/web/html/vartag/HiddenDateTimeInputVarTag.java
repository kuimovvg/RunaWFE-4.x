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

import ru.runa.af.AuthenticationException;
import ru.runa.wf.web.html.VarTag;
import ru.runa.wf.web.html.WorkflowFormProcessingException;

/**
 * Created on 01.08.2005
 */
public class HiddenDateTimeInputVarTag implements VarTag {

    public String getHtml(Subject subject, String varName, Object varValue, PageContext pageContext) throws WorkflowFormProcessingException,
            AuthenticationException {
        Input input = new Input();
        input.setType(Input.HIDDEN);
        input.setName(varName);
        input.setValue(getFormat().format(new Date(System.currentTimeMillis())));
        return input.toString();
    }

    private static final SimpleDateFormat FORMAT = new SimpleDateFormat(ru.runa.wf.web.forms.format.DateTimeFormat.PATTERN);

    protected SimpleDateFormat getFormat() {
        return FORMAT;
    }
}
