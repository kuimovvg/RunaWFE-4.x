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
package ru.runa.wf.web.action;

import java.io.OutputStream;

import javax.security.auth.Subject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.google.common.base.Charsets;

import ru.runa.af.web.SubjectHttpSessionHelper;
import ru.runa.common.web.Commons;
import ru.runa.commons.IOCommons;
import ru.runa.delegate.DelegateFactory;
import ru.runa.wf.service.ExecutionService;
import ru.runa.wf.web.html.vartag.AbstractActorVarTag;

/**
 * @struts:action path="/executeGetActorVarTag" name="executeGetActorVarTag" validate="false"
 */
public class ExecuteGetActorVarTag extends Action {
    protected static final Log log = LogFactory.getLog(ExecuteGetActorVarTag.class);

    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        String result;
        try {
            Subject subject = SubjectHttpSessionHelper.getActorSubject(request.getSession());
            String actorVarTagClassName = request.getParameter("vartagclass");
            String varName = request.getParameter("varname");

            Long taskId = (Long) Commons.getSessionAttribute(request.getSession(), "xsnTaskId");
            AbstractActorVarTag actorVarTag = (AbstractActorVarTag) Class.forName(actorVarTagClassName).newInstance();
            ExecutionService executionService = DelegateFactory.getInstance().getExecutionService();
            String actorCode = executionService.getVariable(subject, taskId, varName).toString();
            result = actorVarTag.getHtml(subject, varName, actorCode, null);
        } catch (Throwable e) {
            result = "Error: " + e.getMessage();
            log.error(e.getMessage(), e);
        }
        response.setContentType("text/xml");
        OutputStream os = response.getOutputStream();
        os.write(result.getBytes(Charsets.UTF_8));
        os.flush();
        return null;
    }
}
