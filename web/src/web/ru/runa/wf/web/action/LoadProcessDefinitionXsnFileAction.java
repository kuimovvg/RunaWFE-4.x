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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ru.runa.common.web.form.IdForm;

/**
 * 
 * @struts:action path="/getXsnFormFile" name="getFile" validate="true"
 */
public class LoadProcessDefinitionXsnFileAction extends LoadProcessDefinitionFileAction {

    @Override
    protected String getContentType() {
        return "application/octet-stream";
    }

    @Override
    protected String getFileName(HttpServletRequest request) {
        return request.getParameter("fileName");
    }

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        //		logRequest(request);
        IdForm idForm = new IdForm();
        idForm.setId(Long.parseLong(request.getParameter("def_id")));
        return super.execute(mapping, idForm, request, response);
    }
    /*
    	private void logRequest(HttpServletRequest request) {
    		// TODO eliminate function or make it common
    		String msg = LoadProcessDefinitionFileAction.class.toString();
    		msg += "\nURL: " + request.getRequestURL();
    		msg += "\nQueryString: " + request.getQueryString();
    		msg += "\nPathInfo: " + request.getPathInfo();
    		msg += "\nHEADERS: ";
    		Enumeration headerNames = request.getHeaderNames();
    		while(headerNames.hasMoreElements()) {
    			String nextElement = (String)headerNames.nextElement();
    			msg += "\n" + nextElement + ": " + request.getHeader(nextElement);
    		}
    		
    		msg += "\nATTRIBUTES: ";
    		Enumeration attributeNames = request.getAttributeNames();
    		while(attributeNames.hasMoreElements()) {
    			String nextElement = (String)(attributeNames.nextElement());
    			msg += "\n" + nextElement + ": " + request.getAttribute(nextElement);;
    		}
    		
    		log.error(msg);
    	}
    */
}
