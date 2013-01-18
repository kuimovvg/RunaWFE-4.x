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

import ru.runa.af.web.SubjectHttpSessionHelper;
import ru.runa.af.web.form.LoginForm;
import ru.runa.common.web.ProfileHttpSessionHelper;
import ru.runa.common.web.Resources;
import ru.runa.common.web.TabHttpSessionHelper;
import ru.runa.service.af.ProfileService;
import ru.runa.service.af.SystemService;
import ru.runa.service.delegate.Delegates;
import ru.runa.wfe.security.ASystem;
import ru.runa.wfe.user.Profile;

/**
 * Created on 09.08.2004
 * 
 * @struts:action path="/login" name="loginForm" validate="true" input = "/start.do"
 * @struts.action-forward name="success" path="/manage_tasks.do" redirect = "true"
 * @struts.action-forward name="failure" path="/start.do" redirect = "true"
 */
public class LoginAction extends Action {

    /* this must be changed if "success" forward changed! */
    private final static String DEFAULT_TAB_FORWARD_NAME = "manage_tasks";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
        ActionForward forward = mapping.findForward(Resources.FORWARD_SUCCESS);
        LoginForm loginForm = (LoginForm) form;
        String login = loginForm.getLogin();
        String password = loginForm.getPassword();

        SystemService systemService = Delegates.getSystemService();
        Subject subject = Delegates.getAuthenticationService().authenticate(login, password);
        systemService.login(subject, ASystem.INSTANCE);

        HttpSession session = request.getSession();

        ProfileService profileService = Delegates.getProfileService();
        Profile profile = profileService.getProfile(subject);
        ProfileHttpSessionHelper.setProfile(profile, session);
        SubjectHttpSessionHelper.addActorSubject(subject, session);
        if (request.getParameter("forwardUrl") != null) {
            forward = new ActionForward(request.getParameter("forwardUrl"));
        }
        TabHttpSessionHelper.setTabForwardName(DEFAULT_TAB_FORWARD_NAME, session);
        saveToken(request);
        return forward;
    }

}
