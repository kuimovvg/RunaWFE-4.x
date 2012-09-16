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
package ru.runa.common.web.portlet.impl;

import javax.portlet.PortletSession;
import javax.security.auth.Subject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ru.runa.af.ASystem;
import ru.runa.af.presentation.Profile;
import ru.runa.af.service.ProfileService;
import ru.runa.af.service.SystemService;
import ru.runa.af.web.SubjectHttpSessionHelper;
import ru.runa.common.web.ProfileHttpSessionHelper;
import ru.runa.common.web.TabHttpSessionHelper;
import ru.runa.common.web.portlet.PortletAuthenticator;
import ru.runa.delegate.DelegateFactory;

public class AuthenticateMandatory implements PortletAuthenticator {
    protected boolean silent = false;

    public boolean authenticate(HttpServletRequest request, HttpServletResponse response, PortletSession session) {
        try {
            Subject subject = SubjectHttpSessionHelper.getActorSubject(request.getSession());
            if (session.getAttribute(ProfileHttpSessionHelper.PROFILE_ATTRIBUTE_NAME) == null) {
                ProfileService profileService = DelegateFactory.getInstance().getProfileService();
                Profile profile = profileService.getProfile(subject);
                ProfileHttpSessionHelper.setProfile(profile, session);
                SubjectHttpSessionHelper.addActorSubject(subject, session);
                TabHttpSessionHelper.setTabForwardName(request.getRequestURL().toString(), session);
            }
        } catch (Exception e) {
            try {
                SystemService systemService = DelegateFactory.getInstance().getSystemService();
                Subject subject = DelegateFactory.getInstance().getAuthenticationService().authenticate();
                systemService.login(subject, ASystem.SYSTEM);
                ProfileService profileService = DelegateFactory.getInstance().getProfileService();
                Profile profile = profileService.getProfile(subject);
                ProfileHttpSessionHelper.setProfile(profile, session);
                ProfileHttpSessionHelper.setProfile(profile, request.getSession());
                SubjectHttpSessionHelper.addActorSubject(subject, session);
                SubjectHttpSessionHelper.addActorSubject(subject, request.getSession());
                TabHttpSessionHelper.setTabForwardName(request.getRequestURL().toString(), session);
                TabHttpSessionHelper.setTabForwardName(request.getRequestURL().toString(), request.getSession());
            } catch (Exception e2) {
                try {
                    if (!silent) {
                        response.getWriter().println("Auth is required");
                    }
                } catch (Exception e4) {
                }
                return false;
            }
        }
        return true;
    }
}
