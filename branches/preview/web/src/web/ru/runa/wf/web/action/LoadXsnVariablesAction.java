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

import javax.security.auth.Subject;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.ecs.xml.XML;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ru.runa.af.web.SubjectHttpSessionHelper;
import ru.runa.delegate.DelegateFactory;
import ru.runa.wf.FileVariable;
import ru.runa.wf.service.ExecutionService;
import sun.misc.BASE64Encoder;

import com.google.common.base.Charsets;

/**
 * 
 * @struts:action path="/getXsnVariables" name="getVariables" validate="true"
 */

public class LoadXsnVariablesAction extends Action {

    @Override
    public ActionForward execute(ActionMapping actionMapping, ActionForm actionForm, HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        // logRequest(request);
        ExecutionService executionService = DelegateFactory.getInstance().getExecutionService();

        Subject subject = SubjectHttpSessionHelper.getActorSubject(request.getSession());
        Long taskId = Long.parseLong(request.getParameter("task_id"));
        //String taskName = request.getParameter("task_name");

        request.getSession().setAttribute("xsnTaskId", taskId);

        // "v" stands for shortcut for "variables"
        String[] variablesNames = request.getParameterValues("v");
        if (variablesNames == null) {
            // Sometimes v is null
            variablesNames = new String[0];
        }

        XML variablesTag = new XML("vars");
        for (String variableName : variablesNames) {
            XML variableTag = new XML("v");
            variableTag.addAttribute("name", variableName);
            Object value = executionService.getVariable(subject, taskId, variableName);
            String variableValue;
            if (value instanceof FileVariable) {
                FileVariable fileVariable = (FileVariable) value;
                String fileName = fileVariable.getName();
                byte[] fileData = fileVariable.getData();
                byte[] fileNameBytes = fileName.getBytes(Charsets.UTF_8);
                byte[] tmp = new byte[fileData.length + fileNameBytes.length + 1];
                System.arraycopy(fileNameBytes, 0, tmp, 0, fileNameBytes.length);
                tmp[fileNameBytes.length] = 0;
                System.arraycopy(fileData, 0, tmp, fileNameBytes.length + 1, fileData.length);
                variableValue = new BASE64Encoder().encode(tmp);
            } else {
                variableValue = (value == null) ? "" : value.toString();
            }

            variableTag.addAttribute("value", variableValue);
            variablesTag.addElement(variableTag);
        }

        response.setContentType("text/xml");
        ServletOutputStream o = response.getOutputStream();
        variablesTag.output(o);
        o.flush();
        return null;
    }
    /*
     * private void logRequest(HttpServletRequest request) { // TODO eliminate
     * function or make it common String msg =
     * LoadProcessDefinitionFileAction.class.toString(); msg += "\nURL: " +
     * request.getRequestURL(); msg += "\nQueryString: " +
     * request.getQueryString(); msg += "\nPathInfo: " + request.getPathInfo();
     * msg += "\nHEADERS: "; Enumeration headerNames = request.getHeaderNames();
     * while(headerNames.hasMoreElements()) { String nextElement =
     * (String)headerNames.nextElement(); msg += "\n" + nextElement + ": " +
     * request.getHeader(nextElement); }
     * 
     * msg += "\nATTRIBUTES: "; Enumeration attributeNames =
     * request.getAttributeNames(); while(attributeNames.hasMoreElements()) {
     * String nextElement = (String)(attributeNames.nextElement()); msg += "\n"
     * + nextElement + ": " + request.getAttribute(nextElement);; }
     * 
     * LogFactory.getLog(LoadXsnVariablesAction.class).error(msg); }
     */
}
