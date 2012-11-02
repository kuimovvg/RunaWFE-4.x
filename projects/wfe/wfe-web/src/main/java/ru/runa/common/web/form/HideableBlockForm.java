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
package ru.runa.common.web.form;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;

import ru.runa.common.web.Messages;

/**
 * Created on 26.08.2004
 * 
 * @struts:form name = "hideableBlockForm"
 */
public class HideableBlockForm extends ReturnActionForm {

    private static final long serialVersionUID = 919851474057172392L;

    public static final String HIDEABLE_BLOCK_ID = "hideableBlockId";

    private String hideableBlockId;

    public String getHideableBlockId() {
        return hideableBlockId;
    }

    public void setHideableBlockId(String hideableBlockId) {
        this.hideableBlockId = hideableBlockId;
    }

    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors errors = super.validate(mapping, request);

        if (hideableBlockId == null || hideableBlockId.length() < 1) {
            errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(Messages.ERROR_WEB_CLIENT_NULL_VALUE));
        }
        return errors;
    }
}
