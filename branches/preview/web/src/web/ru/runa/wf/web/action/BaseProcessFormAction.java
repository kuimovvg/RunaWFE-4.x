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

import java.util.Map;

import javax.security.auth.Subject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.Globals;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;

import ru.runa.af.AuthenticationException;
import ru.runa.af.presentation.Profile;
import ru.runa.af.web.SubjectHttpSessionHelper;
import ru.runa.common.web.ActionExceptionHelper;
import ru.runa.common.web.Messages;
import ru.runa.common.web.ProfileHttpSessionHelper;
import ru.runa.common.web.Resources;
import ru.runa.wf.TaskDoesNotExistException;
import ru.runa.wf.form.Interaction;
import ru.runa.wf.form.VariablesValidationException;
import ru.runa.wf.web.VariablesFormatException;

/**
 * Created on 15.12.2005
 * 
 */
abstract class BaseProcessFormAction extends Action {
    protected static final Log log = LogFactory.getLog(BaseProcessFormAction.class);

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {
        ActionMessages errors = new ActionMessages();
        Map<String, String> userInputErrors = null;
        ActionForward successForward = null;
        try {
            Subject subject = SubjectHttpSessionHelper.getActorSubject(request.getSession());
            Profile profile = ProfileHttpSessionHelper.getProfile(request.getSession());
            if (request.getSession().getAttribute(Globals.TRANSACTION_TOKEN_KEY) == null || isTokenValid(request, true)) {
                saveToken(request);
                successForward = executeProcessFromAction(request, form, mapping, subject, profile);
            } else {
                return new ActionForward("/manage_tasks.do", true);
            }
        } catch (TaskDoesNotExistException e) {
            // In this case we must go to success forwarding, because of this task is absent and form can't be displayed
            ActionExceptionHelper.addException(errors, e);
            saveErrors(request.getSession(), errors);
            // save in request user input
            request.setAttribute("UserDefinedVariables", VariableExtractionHelper.extractAllAvailableVariables(form));
            // save in request user errors
            request.setAttribute("UserErrors", userInputErrors);
            return mapping.findForward(Resources.FORWARD_SUCCESS);
        } catch (VariablesValidationException e) {
            userInputErrors = e.getConcatenatedFieldErrors();
            for (String msg : e.getGlobalErrors()) {
                // we already have localized string
                errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(msg, false));
            }
            if (errors.size() == 0) {
                // we add at least 1 error message in order to prevent successful forward
                errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(Messages.MESSAGE_WEB_CLIENT_VALIDATION_ERROR));
            }
        } catch (VariablesFormatException e) {
            errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(Messages.MESSAGE_WEB_CLIENT_VARIABLE_FORMAT_ERROR, e.getErrorFields()));
        } catch (Exception e) {
            ActionExceptionHelper.addException(errors, e);
        }
        if (!errors.isEmpty()) {
            saveErrors(request.getSession(), errors);
            // save in request user input
            request.setAttribute("UserDefinedVariables", VariableExtractionHelper.extractAllAvailableVariables(form));
            // save in request user errors
            request.setAttribute("UserErrors", userInputErrors);
            return getErrorForward(mapping, form);
        }

        ActionMessage userMessage = getMessage();
        request.getSession().setAttribute(Resources.USER_MESSAGE_KEY, userMessage);
        return successForward;
    }

    protected Map<String, Object> getFormVariables(HttpServletRequest request, ActionForm actionForm, Interaction interaction) throws VariablesFormatException {
        return VariableExtractionHelper.extractVariables(request.getSession(), actionForm, interaction);
    }

    protected abstract ActionMessage getMessage();

    protected abstract ActionForward executeProcessFromAction(HttpServletRequest request, ActionForm form, ActionMapping mapping,
            Subject subject, Profile profile) throws Exception;

    protected abstract ActionForward getErrorForward(ActionMapping mapping, ActionForm actionForm);
}
