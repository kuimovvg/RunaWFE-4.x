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

import ru.runa.af.web.SubjectHttpSessionHelper;
import ru.runa.service.delegate.DelegateFactory;
import ru.runa.service.wf.ExecutionService;
import ru.runa.wf.web.form.TaskIdForm;

/**
 * @struts:action path="/processGraphImage" name="taskIdForm" validate="true" input = "/WEB-INF/wf/manage_process.jsp"
 */
public class ProcessGraphImageAction extends Action {

    protected static final Log log = LogFactory.getLog(ProcessGraphImageAction.class);

    public static final String ACTION_PATH = "/processGraphImage";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        TaskIdForm idForm = (TaskIdForm) form;
        try {
            Subject subject = SubjectHttpSessionHelper.getActorSubject(request.getSession());
            ExecutionService executionService = DelegateFactory.getExecutionService();
            byte[] diagramBytes = executionService.getProcessDiagram(subject, idForm.getId(), idForm.getTaskId(), idForm.getChildProcessId());
            response.setContentType("image/png");
            OutputStream os = response.getOutputStream();
            os.write(diagramBytes);
            os.flush();
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        }
        return null;
    }

}
