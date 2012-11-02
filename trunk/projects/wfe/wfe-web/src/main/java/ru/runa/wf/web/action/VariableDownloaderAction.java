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
import ru.runa.common.web.HTMLUtils;
import ru.runa.service.delegate.DelegateFactory;
import ru.runa.service.wf.ExecutionService;
import ru.runa.wf.web.form.VariableForm;
import ru.runa.wfe.var.FileVariable;

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
            VariableForm form = (VariableForm) actionForm;
            Subject subject = SubjectHttpSessionHelper.getActorSubject(request.getSession());
            ExecutionService executionService = DelegateFactory.getExecutionService();
            FileVariable fileVariable = (FileVariable) executionService.getVariable(subject, form.getId(), form.getVariableName()).getValue();

            response.setContentType(fileVariable.getContentType());
            // http://forum.java.sun.com/thread.jspa?forumID=45&threadID=233446
            response.setHeader("Pragma", "public");
            response.setHeader("Cache-Control", "max-age=0");
            // non-ascii filenames (Opera does not support it)
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

}
