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

import java.util.Map;

import javax.security.auth.Subject;

import ru.runa.service.af.SystemService;
import ru.runa.wfe.security.ASystem;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;

/**
 * Base interface which implementaions provide operations on ASystem they are
 * represent. For example {@link SystemServiceDelegete}represents delegete to
 * AASystem. Created on 17.08.2004
 * 
 * 
 */
public class SystemServiceDelegate extends EJB3Delegate implements SystemService {

    public SystemServiceDelegate() {
        super(SystemService.class);
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

    @Override
    public Map<String, String> getLocalizations(Subject subject) {
        return getSystemService().getLocalizations(subject);
    }

    @Override
    public void saveLocalizations(Subject subject, Map<String, String> localizations) {
        getSystemService().saveLocalizations(subject, localizations);
    }

}
