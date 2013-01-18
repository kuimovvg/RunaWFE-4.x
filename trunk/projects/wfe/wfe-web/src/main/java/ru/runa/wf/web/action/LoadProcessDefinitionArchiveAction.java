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

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ru.runa.af.web.SubjectHttpSessionHelper;
import ru.runa.common.web.HTMLUtils;
import ru.runa.common.web.form.IdForm;
import ru.runa.service.delegate.Delegates;
import ru.runa.service.wf.DefinitionService;
import ru.runa.wfe.definition.IFileDataProvider;

/**
 * @struts:action path="/processDefinitionArchive" name="idForm" validate="false"
 */
public class LoadProcessDefinitionArchiveAction extends Action {

    public static final String ACTION_PATH = "/processDefinitionArchive";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        IdForm idForm = (IdForm) form;
        Subject subject = SubjectHttpSessionHelper.getActorSubject(request.getSession());
        DefinitionService definitionService = Delegates.getDefinitionService();
        String parFileName = definitionService.getProcessDefinition(subject, idForm.getId()).getName() + ".par";
        byte[] bytes = definitionService.getFile(subject, idForm.getId(), IFileDataProvider.PAR_FILE);
        response.setContentType("application/zip");
        String encodedFileName = HTMLUtils.encodeFileName(parFileName, request.getHeader("User-Agent"));
        response.setHeader("Content-disposition", "attachment; filename=\"" + encodedFileName + "\"");
        OutputStream os = response.getOutputStream();
        os.write(bytes);
        os.flush();
        return null;
    }

}
