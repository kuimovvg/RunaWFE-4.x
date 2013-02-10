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

import javax.activation.MimetypesFileTypeMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessages;

import ru.runa.common.web.ActionExceptionHelper;
import ru.runa.common.web.HTMLUtils;
import ru.runa.common.web.Resources;
import ru.runa.common.web.action.ActionBase;
import ru.runa.service.DefinitionService;
import ru.runa.service.delegate.Delegates;
import ru.runa.wf.web.form.DefinitionFileForm;

/**
 * Powered by Dofs
 * 
 * @struts:action path="/getHtmlFile" name="idUrlForm" validate="true" input =
 *                "/WEB-INF/wf/manage_process_definitions.jsp"
 * @struts.action-forward name="success"
 *                        path="/WEB-INF/wf/process_definition_description.jsp"
 * @struts.action-forward name="failure" path="/error.do" redirect = "true"
 */
public class LoadProcessDefinitionHtmlFileAction extends ActionBase {

    public static final String ACTION_PATH = "/getHtmlFile";
    private static MimetypesFileTypeMap fileTypeMap = new MimetypesFileTypeMap();

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        ActionMessages errors = new ActionMessages();
        DefinitionFileForm form = (DefinitionFileForm) actionForm;
        Long id = form.getId();
        String fileName = form.getFileName();
        ActionForward successForward = null;
        try {
            DefinitionService definitionService = Delegates.getDefinitionService();
            byte[] bytes = definitionService.getFile(getLoggedUser(request), id, fileName);

            if (fileName.endsWith(".html")) {
                request.setAttribute("htmlBytes", bytes);
                request.setAttribute("processDefinitionId", id);
                request.setAttribute("pageHref", fileName);
                successForward = mapping.findForward(Resources.FORWARD_SUCCESS);
            } else {
                String encodedFileName = HTMLUtils.encodeFileName(fileName, request.getHeader("User-Agent"));
                response.setHeader("Content-disposition", "inline; filename=\"" + encodedFileName + "\"");
                response.setContentType(fileTypeMap.getContentType(fileName));
                OutputStream os = response.getOutputStream();
                os.write(bytes);
                os.flush();
            }
        } catch (Exception e) {
            ActionExceptionHelper.addException(errors, e);
        }

        if (!errors.isEmpty()) {
            saveErrors(request.getSession(), errors);
            return mapping.findForward(Resources.FORWARD_FAILURE);
        }
        return successForward;
    }
}
