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
package ru.runa.service.delegate;

import javax.security.auth.Subject;

import ru.runa.service.af.AuthenticationService;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.user.Actor;

public class AuthenticationServiceDelegate extends EJB3Delegate implements AuthenticationService {

    public AuthenticationServiceDelegate() {
        super(AuthenticationService.class);
    }

    private AuthenticationService getAuthenticationService() {
        return getService();
    }

    @Override
    public Actor getActor(Subject subject) throws AuthenticationException {
        return getAuthenticationService().getActor(subject);
    }

    @Override
    public Subject authenticate() throws AuthenticationException {
        return getAuthenticationService().authenticate();
    }

    @Override
    public Subject authenticate(String name, String password) throws AuthenticationException {
        return getAuthenticationService().authenticate(name, password);
    }

    @Override
    public Subject authenticate(byte[] token) throws AuthenticationException {
        return getAuthenticationService().authenticate(token);
    }
}
