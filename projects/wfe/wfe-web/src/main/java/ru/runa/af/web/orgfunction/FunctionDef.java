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
package ru.runa.af.web.orgfunction;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.jsp.PageContext;

import ru.runa.common.web.Messages;

public class FunctionDef {
    private final String messageKey;
    private final String message;
    private final String className;
    private final List<ParamDef> params = new ArrayList<ParamDef>();

    public FunctionDef(String messageKey, String message, String className) {
        this.messageKey = messageKey;
        this.message = message;
        this.className = className;
    }

    public void addParam(ParamDef definition) {
        params.add(definition);
    }

    public String getMessage(PageContext pageContext) {
        if (message != null) {
            return message;
        }
        return Messages.getMessage(messageKey, pageContext);
    }

    public String getClassName() {
        return className;
    }

    public List<ParamDef> getParams() {
        return params;
    }
}
