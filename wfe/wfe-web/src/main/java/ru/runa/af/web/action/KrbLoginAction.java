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

import java.security.Principal;
import java.util.HashSet;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessages;

import ru.runa.af.web.SubjectHttpSessionHelper;
import ru.runa.common.WebResources;
import ru.runa.common.web.ActionExceptionHelper;
import ru.runa.common.web.ProfileHttpSessionHelper;
import ru.runa.common.web.Resources;
import ru.runa.common.web.TabHttpSessionHelper;
import ru.runa.service.af.ProfileService;
import ru.runa.service.af.SystemService;
import ru.runa.service.delegate.DelegateFactory;
import ru.runa.wfe.security.ASystem;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.auth.SubjectPrincipalsHelper;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.ActorPrincipal;
import ru.runa.wfe.user.Profile;

/**
 * This class provides Kerberos auth for IE
 * 
 * @struts:action path="/krblogin"
 * @struts.action-forward name="success" path="/manage_tasks.do" redirect = "true"
 * @struts.action-forward name="failure" path="/start.do" redirect = "true"
 */
public class KrbLoginAction extends Action {

    /* this must be changed if "success" forward changed! */
    private final static String DEFAULT_TAB_FORWARD_NAME = "manage_tasks";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {
        ActionMessages errors = new ActionMessages();
        try {
            if (!WebResources.isKrbSupported()) {
                throw new AuthenticationException("Kerberos support disabled");
            }
            if (request.getUserPrincipal() == null) {
                throw new LoginException("No client name was provided.");
            }
            String domainActorName = request.getUserPrincipal().getName();
            int atIndex = domainActorName.indexOf("@");
            if (atIndex == -1) {
                throw new LoginException("Invalid domain name '" + domainActorName + "'");
            }
            String actorName = domainActorName.substring(0, atIndex);
            Actor actor = DelegateFactory.getExecutorService().getActorCaseInsensitive(actorName);
            ActorPrincipal ap = new ActorPrincipal(actor, SubjectPrincipalsHelper.encodeActor(actor));

            HashSet<Principal> princ = new HashSet<Principal>();
            princ.add(ap);
            Subject subject = new Subject(false, princ, new HashSet<Object>(), new HashSet<Object>());

            SystemService systemService = DelegateFactory.getSystemService();
            systemService.login(subject, ASystem.INSTANCE);
            HttpSession session = request.getSession();
            ProfileService profileService = DelegateFactory.getProfileService();
            Profile profile = profileService.getProfile(subject);
            ProfileHttpSessionHelper.setProfile(profile, session);
            SubjectHttpSessionHelper.addActorSubject(subject, session);
            TabHttpSessionHelper.setTabForwardName(DEFAULT_TAB_FORWARD_NAME, session);
            saveToken(request);
        } catch (Exception e) {
            ActionExceptionHelper.addException(errors, e);
        }
        if (!errors.isEmpty()) {
            saveErrors(request.getSession(), errors);
            return mapping.findForward(Resources.FORWARD_FAILURE);
        }
        return mapping.findForward(Resources.FORWARD_SUCCESS);
    }
}
