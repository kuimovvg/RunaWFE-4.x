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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ru.runa.common.web.Commons;
import ru.runa.common.web.ProfileHttpSessionHelper;
import ru.runa.common.web.TabHttpSessionHelper;
import ru.runa.common.web.portlet.PortletAuthenticator;
import ru.runa.service.ProfileService;
import ru.runa.service.SystemService;
import ru.runa.service.delegate.Delegates;
import ru.runa.wfe.security.ASystem;
import ru.runa.wfe.user.Profile;
import ru.runa.wfe.user.User;

public class AuthenticateMandatory implements PortletAuthenticator {
    protected boolean silent = false;

    @Override
    public boolean authenticate(HttpServletRequest request, HttpServletResponse response, PortletSession session) {
        try {
            User user = Commons.getUser(request.getSession());
            if (session.getAttribute(ProfileHttpSessionHelper.PROFILE_ATTRIBUTE_NAME) == null) {
                ProfileService profileService = Delegates.getProfileService();
                Profile profile = profileService.getProfile(user);
                ProfileHttpSessionHelper.setProfile(profile, session);
                TabHttpSessionHelper.setTabForwardName(request.getRequestURL().toString(), session);
            }
        } catch (Exception e) {
            try {
                SystemService systemService = Delegates.getSystemService();
                User user = Delegates.getAuthenticationService().authenticateByCallerPrincipal();
                systemService.login(user, ASystem.INSTANCE);
                ProfileService profileService = Delegates.getProfileService();
                Profile profile = profileService.getProfile(user);
                ProfileHttpSessionHelper.setProfile(profile, session);
                ProfileHttpSessionHelper.setProfile(profile, request.getSession());
                Commons.setUser(user, session);
                Commons.setUser(user, request.getSession());
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
