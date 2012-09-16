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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.security.auth.Subject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessages;

import ru.runa.af.AuthenticationException;
import ru.runa.af.AuthorizationException;
import ru.runa.af.web.SubjectHttpSessionHelper;
import ru.runa.common.web.ActionExceptionHelper;
import ru.runa.common.web.form.FileForm;
import ru.runa.wf.ProcessDefinitionArchiveException;
import ru.runa.wf.web.ProcessTypesIterator;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

/**
 * Created on 14.10.2004
 * 
 */
abstract class BaseDeployProcessDefinitionAction extends Action {

    abstract protected void doAction(Subject subject, FileForm fileForm, List<String> processType, ActionMessages errors) throws AuthorizationException,
            AuthenticationException, FileNotFoundException, IOException, ProcessDefinitionArchiveException;

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, final HttpServletRequest request, HttpServletResponse responce)
            throws AuthenticationException {
        ActionMessages errors = getErrors(request);
        String paramType = request.getParameter("type");
        String paramTypeSelected = request.getParameter("typeSel");

        Map<String, String> typeParamsHolder = new HashMap<String, String>();
        typeParamsHolder.put("type", paramType);
        typeParamsHolder.put("typeSel", paramTypeSelected);
        request.setAttribute("TypeAttributes", typeParamsHolder);

        List<String> fullType;

        FileForm fileForm = (FileForm) form;
        prepare(fileForm);
        Subject subject = SubjectHttpSessionHelper.getActorSubject(request.getSession());
        try {
            ProcessTypesIterator iter = new ProcessTypesIterator(subject);
            if (paramTypeSelected == null || paramTypeSelected.equals("_default_type_")) {
                if (paramType == null || paramType.equals("")) {
                    throw new ru.runa.wf.ProcessDefinitionTypeNotPresentException();
                }
                fullType = Lists.newArrayList(paramType);
            } else {
                String[] selectedType = iter.getItem(Integer.parseInt(paramTypeSelected));
                fullType = Lists.newArrayList(selectedType);
                if (!Strings.isNullOrEmpty(paramType)) {
                    fullType.add(paramType);
                }
            }
            doAction(subject, fileForm, fullType, errors);
        } catch (Exception e) {
            ActionExceptionHelper.addException(errors, e);
        }

        if (!errors.isEmpty()) {
            saveErrors(request.getSession(), errors);
            return getErrorForward(mapping);
        }
        return getSuccessAction(mapping);
    }

    protected abstract ActionForward getSuccessAction(ActionMapping mapping);

    protected abstract ActionForward getErrorForward(ActionMapping mapping);

    protected abstract void prepare(FileForm fileForm);
}
