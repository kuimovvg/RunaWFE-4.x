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
package ru.runa.service.af;

import java.util.Map;

import javax.security.auth.Subject;

import ru.runa.wfe.security.ASystem;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;

/**
 * Interface for common operations.
 * 
 * @since 2.0
 */
public interface SystemService {

    public void login(Subject subject, ASystem system) throws AuthenticationException, AuthorizationException;

    public void logout(Subject subject, ASystem system);

    public Map<String, String> getLocalizations(Subject subject);

    public void saveLocalizations(Subject subject, Map<String, String> localizations);
}
