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
package ru.runa.delegate.impl;

import javax.security.auth.Subject;

import ru.runa.af.ASystem;
import ru.runa.af.AuthenticationException;
import ru.runa.af.AuthorizationException;
import ru.runa.af.service.SystemService;

/**
 * Base interface which implementaions provide operations on ASystem they are represent. For example {@link SystemServiceDelegete}represents delegete
 * to AASystem.
 * Created on 17.08.2004
 */
public class SystemServiceDelegateImpl extends EJB3Delegate implements SystemService {
    @Override
    protected String getBeanName() {
        return "SystemServiceBean";
    }

    private SystemService getSystemService() {
        return getService();
    }

    @Override
    public void login(Subject subject, ASystem system) throws AuthorizationException, AuthenticationException {
        getSystemService().login(subject, system);
    }

    @Override
    public void logout(Subject subject, ASystem system) {
        getSystemService().logout(subject, system);
    }
}
