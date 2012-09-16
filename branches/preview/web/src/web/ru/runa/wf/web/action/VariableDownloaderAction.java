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
import java.util.List;

import javax.security.auth.Subject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ru.runa.af.AuthenticationException;
import ru.runa.af.AuthorizationException;
import ru.runa.af.web.SubjectHttpSessionHelper;
import ru.runa.common.web.IdentityType;
import ru.runa.commons.html.HTMLUtils;
import ru.runa.delegate.DelegateFactory;
import ru.runa.wf.FileVariable;
import ru.runa.wf.TaskDoesNotExistException;
import ru.runa.wf.service.ExecutionService;
import ru.runa.wf.web.form.VariableForm;

import com.google.common.collect.Lists;

/**
 * Created on 27.09.2005
 * 
 * @struts:action path="/variableDownloader" name="variableForm" validate="true"
 */
public class VariableDownloaderAction extends Action {

    private static final Log log = LogFactory.getLog(VariableDownloaderAction.class);

    public static final String ACTION_PATH = "/variableDownloader";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        try {
            FileVariable fileVariable = getVariable(actionForm, request);
            response.setContentType(fileVariable.getContentType());
            // XXX dirty hack to avoid IE glitches (check http://forum.java.sun.com/thread.jspa?forumID=45&threadID=233446 for details)
            response.setHeader("Pragma", "public");
            response.setHeader("Cache-Control", "max-age=0");
            // XXX dirty hack to encode non-ascii filenames (Opera does not support it)
            String encodedFileName = HTMLUtils.encodeFileName(fileVariable.getName(), request.getHeader("User-Agent"));
            response.setHeader("Content-Disposition", "attachment; filename=\"" + encodedFileName + "\"");
            OutputStream os = response.getOutputStream();
            os.write(fileVariable.getData());
            os.flush();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    private FileVariable getVariable(ActionForm actionForm, HttpServletRequest request) throws AuthenticationException, TaskDoesNotExistException,
            AuthorizationException {
        VariableForm form = (VariableForm) actionForm;
        Subject subject = SubjectHttpSessionHelper.getActorSubject(request.getSession());
        ExecutionService executionService = DelegateFactory.getInstance().getExecutionService();
        switch (IdentityType.valueOf(form.getIdentityType())) {
        case PROCESS_INSTANCE:
            List<Object> var = executionService.getVariable(subject, Lists.newArrayList(form.getId()), form.getVariableName());
            return (var != null && var.size() == 1) ? (FileVariable) var.get(0) : null;
        case TASK:
            return (FileVariable) executionService.getVariable(subject, form.getId(), form.getVariableName());
        default:
            throw new IllegalArgumentException("identityType");
        }
    }
}
