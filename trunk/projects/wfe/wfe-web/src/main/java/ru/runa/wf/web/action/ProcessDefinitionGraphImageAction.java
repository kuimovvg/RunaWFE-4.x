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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ru.runa.common.web.action.ActionBase;
import ru.runa.common.web.form.IdForm;
import ru.runa.wfe.definition.IFileDataProvider;
import ru.runa.wfe.service.DefinitionService;
import ru.runa.wfe.service.delegate.Delegates;

/**
 * Created on 27.09.2005
 * 
 * @struts:action path="/processDefinitionGraphImage" name="idForm"
 *                validate="true" input =
 *                "/WEB-INF/wf/manage_process_definitions.jsp"
 */
public class ProcessDefinitionGraphImageAction extends ActionBase {
    public static final String ACTION_PATH = "/processDefinitionGraphImage";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        IdForm idForm = (IdForm) form;
        try {
            DefinitionService definitionService = Delegates.getDefinitionService();
            byte[] bytes = definitionService.getFile(getLoggedUser(request), idForm.getId(), IFileDataProvider.GRAPH_IMAGE_NEW_FILE_NAME);
            if (bytes == null) {
                bytes = definitionService.getFile(getLoggedUser(request), idForm.getId(), IFileDataProvider.GRAPH_IMAGE_OLD_FILE_NAME);
            }
            if (bytes == null) {
                throw new NullPointerException("No graph stream found.");
            }
            response.setContentType("image/jpg");
            OutputStream os = response.getOutputStream();
            os.write(bytes);
            os.flush();
        } catch (Exception e) {
            log.error("Unable to fetch process diagram", e);
        }
        return null;
    }

}
