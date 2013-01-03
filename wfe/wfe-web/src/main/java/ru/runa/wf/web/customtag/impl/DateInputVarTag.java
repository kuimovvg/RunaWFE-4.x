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
package ru.runa.wf.web.customtag.impl;

import java.util.Date;

import javax.security.auth.Subject;
import javax.servlet.jsp.PageContext;

import ru.runa.wf.web.customtag.VarTag;
import ru.runa.wfe.commons.CalendarUtil;

/**
 * Created 12.05.2005
 * 
 */
public class DateInputVarTag implements VarTag {

    @Override
    public String getHtml(Subject subject, String varName, Object var, PageContext pageContext) throws Exception {
        String html = "<input type=\"text\" class=\"inputDate\" name=\"" + varName + "\" style=\"width: 100px;\" ";
        if (var instanceof Date) {
            html += "value=\"" + CalendarUtil.formatTime((Date) var) + "\" ";
        }
        html += "/>";
        return html;
    }
}
