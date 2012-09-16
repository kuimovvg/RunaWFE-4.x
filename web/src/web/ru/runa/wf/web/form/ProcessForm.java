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
package ru.runa.wf.web.form;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;

import ru.runa.common.web.Messages;

/**
 * @struts:form name = "processForm"
 */
public class ProcessForm extends CommonProcessForm {
    private static final long serialVersionUID = -4345644809568995336L;

    public static final String ACTOR_ID_INPUT_NAME = "actorId";

    public static final String TASK_INPUT_NAME = "taskName";

    private Long actorId;

    private String taskName;

    public Long getActorId() {
        return actorId;
    }

    public void setActorId(Long actorId) {
        this.actorId = actorId;
    }

    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors errors = super.validate(mapping, request);

        if ((actorId == 0) || (taskName == null)) {
            //TODO probably we must inform user in other form, this error may appear only when user tries to play with URL
            errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(Messages.ERROR_WEB_CLIENT_NULL_VALUE));
        }
        return errors;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }
}
