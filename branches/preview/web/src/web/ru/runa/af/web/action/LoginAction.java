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
package ru.runa.af.web.action;

import javax.security.auth.Subject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessages;

import ru.runa.af.ASystem;
import ru.runa.af.AuthenticationException;
import ru.runa.af.presentation.Profile;
import ru.runa.af.service.ProfileService;
import ru.runa.af.service.SystemService;
import ru.runa.af.web.SubjectHttpSessionHelper;
import ru.runa.af.web.form.LoginForm;
import ru.runa.common.web.ActionExceptionHelper;
import ru.runa.common.web.ProfileHttpSessionHelper;
import ru.runa.common.web.Resources;
import ru.runa.common.web.TabHttpSessionHelper;
import ru.runa.delegate.DelegateFactory;

/**
 * Created on 09.08.2004
 * 
 * @struts:action path="/login" name="loginForm" validate="true" input =
 *                "/start.do"
 * @struts.action-forward name="success" path="/manage_tasks.do" redirect =
 *                        "true"
 * @struts.action-forward name="failure" path="/start.do" redirect = "true"
 */
public class LoginAction extends Action {

    /* this must be changed if "success" forward changed! */
    private final static String DEFAULT_TAB_FORWARD_NAME = "manage_tasks";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {
        ActionMessages errors = new ActionMessages();
        ActionForward forward = mapping.findForward(Resources.FORWARD_SUCCESS);
        try {
            LoginForm loginForm = (LoginForm) form;
            String login = loginForm.getLogin();
            String password = loginForm.getPassword();

            SystemService systemService = DelegateFactory.getInstance().getSystemService();
            Subject subject = DelegateFactory.getInstance().getAuthenticationService().authenticate(login, password);
            systemService.login(subject, ASystem.SYSTEM);

            HttpSession session = request.getSession();

            ProfileService profileService = DelegateFactory.getInstance().getProfileService();
            Profile profile = profileService.getProfile(subject);
            ProfileHttpSessionHelper.setProfile(profile, session);
            SubjectHttpSessionHelper.addActorSubject(subject, session);
            if (request.getParameter("forwardUrl") != null) {
                forward = new ActionForward(request.getParameter("forwardUrl"));
            }
            TabHttpSessionHelper.setTabForwardName(DEFAULT_TAB_FORWARD_NAME, session);
            saveToken(request);
        } catch (Exception e) {
            e.printStackTrace();
            ActionExceptionHelper.addException(errors, e);
        }
        if (!errors.isEmpty()) {
            saveErrors(request.getSession(), errors);
            return mapping.findForward(Resources.FORWARD_FAILURE);
        }

        return forward;
    }

}
