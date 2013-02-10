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

import ru.runa.service.SystemService;
import ru.runa.wfe.security.ASystem;
import ru.runa.wfe.user.User;

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
    public void login(User user, ASystem system) {
        getSystemService().login(user, system);
    }

    @Override
    public void logout(User user, ASystem system) {
        getSystemService().logout(user, system);
    }

    @Override
    public Map<String, String> getLocalizations(User user) {
        return getSystemService().getLocalizations(user);
    }

    @Override
    public String getLocalized(User user, String name) {
        return getSystemService().getLocalized(user, name);
    }

    @Override
    public void saveLocalizations(User user, Map<String, String> localizations) {
        getSystemService().saveLocalizations(user, localizations);
    }

}
