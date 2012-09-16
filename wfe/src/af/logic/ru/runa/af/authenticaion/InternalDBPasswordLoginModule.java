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
package ru.runa.af.authenticaion;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.login.LoginException;

import ru.runa.af.Actor;
import ru.runa.af.ExecutorOutOfDateException;

/**
 * LoginModule for based on actor name and password information provided by
 * ExecutorService.
 * 
 */
public class InternalDBPasswordLoginModule extends LoginModuleBase {

    @Override
    protected Actor login(CallbackHandler callbackHandler) throws Exception {
        Callback[] callbacks = new Callback[2];
        callbacks[0] = new NameCallback("actor name: ");
        callbacks[1] = new PasswordCallback("password: ", false);
        callbackHandler.handle(callbacks);
        String actorName = ((NameCallback) callbacks[0]).getName();
        if (actorName == null) {
            throw new LoginException("No actor name was provided.");
        }
        char[] tmpPasswordChars = ((PasswordCallback) callbacks[1]).getPassword();
        if (tmpPasswordChars == null) {
            throw new LoginException("No password was provided.");
        }
        String password = new String(tmpPasswordChars);
        try {
            Actor actor = executorDAO.getActor(actorName);
            if (executorDAO.isPasswordValid(actor, password)) {
                return actor;
            }
        } catch (ExecutorOutOfDateException e) {
            // do nothing here, we must not let external system know whether
            // actor exist or not
            log.warn(e.getMessage());
        }
        return null;
    }

}
