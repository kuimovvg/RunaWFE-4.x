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

import javax.security.auth.Subject;
import javax.servlet.jsp.PageContext;

import ru.runa.af.Actor;
import ru.runa.af.AuthenticationException;
import ru.runa.af.AuthorizationException;
import ru.runa.af.Executor;
import ru.runa.af.ExecutorOutOfDateException;
import ru.runa.af.logic.ExecutorLogic;
import ru.runa.wf.web.html.VarTag;
import ru.runa.wf.web.html.WorkflowFormProcessingException;

public class SendEmailAction_ActorFullName extends SendEmailAction_Common implements VarTag {

    public String getHtml(Subject subject, String varName, Object varValue, PageContext pageContext) throws WorkflowFormProcessingException,
            AuthenticationException {
        try {
            String executorCode = getExecutorCode(varValue);
            Executor ex = new ExecutorLogic().getActorByCode(subject, Long.parseLong(executorCode));
            if (ex == null) {
                return "Undefined";
            }
            String retVal = ex.getName();;
            if (ex instanceof Actor) {
                retVal = ((Actor) ex).getFullName();
                if (retVal == null || retVal.equals("")) {
                    retVal = ((Actor) ex).getName();
                }
            }
            if (retVal == null) {
                retVal = "";
            }
            return retVal;
        } catch (ExecutorOutOfDateException e) {
            throw new WorkflowFormProcessingException(e);
        } catch (AuthorizationException e) {
            throw new WorkflowFormProcessingException(e);
        }
    }
}
