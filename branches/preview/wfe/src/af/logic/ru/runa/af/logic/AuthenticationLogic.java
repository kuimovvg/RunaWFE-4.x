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

package ru.runa.af.logic;

import java.security.Principal;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import jcifs.smb.NtlmPasswordAuthentication;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.af.Actor;
import ru.runa.af.AuthenticationException;
import ru.runa.af.authenticaion.KerberosCallbackHandler;
import ru.runa.af.authenticaion.KerberosLoginModuleConfiguration;
import ru.runa.af.authenticaion.KerberosLoginModuleResources;
import ru.runa.af.authenticaion.LoginModuleConfiguration;
import ru.runa.af.authenticaion.NTLMLoginModuleCallbackHandler;
import ru.runa.af.authenticaion.PasswordLoginModuleCallbackHandler;
import ru.runa.af.authenticaion.PrincipalCallbackHandler;
import ru.runa.af.authenticaion.SubjectPrincipalsHelper;
import ru.runa.commons.Loader;

/**
 * Created on 14.03.2005
 * 
 */
public class AuthenticationLogic extends CommonLogic {
    private static final Log log = LogFactory.getLog(AuthenticationLogic.class);

    public static enum AuthType {
        db_auth, kerberos_auth, ntlm_auth, other
    };

    public static interface LoginHandler {

        public void onUserLogin(Actor actor, AuthType type);
    }

    public Subject authenticate(Principal principal) throws AuthenticationException {
        Subject subject = null;
        try {
            LoginContext loginContext = new LoginContext(LogicResources.LOGIN_MODULE_CONFIGURATION, null, new PrincipalCallbackHandler(principal),
                    new LoginModuleConfiguration());
            loginContext.login();
            subject = loginContext.getSubject();
            callHandlers(SubjectPrincipalsHelper.getActor(subject), AuthType.other);
            log.info(principal.getName() + " succesfully authenticated");
        } catch (LoginException e) {
            log.warn("Failed to authenticate with HttpServletRequest because of: " + e.getMessage());
            throw new AuthenticationException(e);
        }
        return subject;
    }

    public Subject authenticate(NtlmPasswordAuthentication authentication) throws AuthenticationException {
        Subject subject = null;
        try {
            LoginContext loginContext = new LoginContext(LogicResources.LOGIN_MODULE_CONFIGURATION, null, new NTLMLoginModuleCallbackHandler(
                    authentication), new LoginModuleConfiguration());
            loginContext.login();
            subject = loginContext.getSubject();
            callHandlers(SubjectPrincipalsHelper.getActor(subject), AuthType.ntlm_auth);
            String actorName = SubjectPrincipalsHelper.getActor(subject).getName();
            log.info(actorName + " successfully authenticated");
        } catch (LoginException e) {
            log.warn("Failed to authenticate with NTLM credentials because of: " + e.getMessage());
            throw new AuthenticationException(e);
        }
        return subject;
    }

    public Subject authenticate(byte[] kerberosToken, KerberosLoginModuleResources res) throws AuthenticationException {
        Subject subject = null;
        try {
            LoginContext loginContext = new LoginContext(LogicResources.LOGIN_MODULE_CONFIGURATION, null, new KerberosCallbackHandler(kerberosToken,
                    res), new KerberosLoginModuleConfiguration(res));
            loginContext.login();
            subject = loginContext.getSubject();
            callHandlers(SubjectPrincipalsHelper.getActor(subject), AuthType.kerberos_auth);
            String actorName = SubjectPrincipalsHelper.getActor(subject).getName();
            if (log.isDebugEnabled()) {
                log.info(actorName + " successfully authenticated");
            }
        } catch (LoginException e) {
            log.warn("Failed to authenticate with kerberos token because of: " + e.getMessage());
            throw new AuthenticationException(e);
        }
        return subject;
    }

    public Subject authenticate(String name, String password) throws AuthenticationException {
        Subject subject = null;
        try {
            LoginContext loginContext = new LoginContext(LogicResources.LOGIN_MODULE_CONFIGURATION, null, new PasswordLoginModuleCallbackHandler(
                    name, password), new LoginModuleConfiguration());
            loginContext.login();
            subject = loginContext.getSubject();
            callHandlers(SubjectPrincipalsHelper.getActor(subject), AuthType.db_auth);
            log.info(name + " succesfully authenticated");
        } catch (Exception e) {
            log.warn("Failed to authenticate with login:" + name + " and password because of: " + e.getMessage());
            throw new AuthenticationException(e);
        }
        return subject;
    }

    public Actor getActor(Subject subject) throws AuthenticationException {
        return SubjectPrincipalsHelper.getActor(subject);
    }

    private void callHandlers(Actor actor, AuthType type) {
        for (String handlerClass : LogicResources.getOnLoginHandlers()) {
            try {
                LoginHandler handler = (LoginHandler) (Loader.loadObject(handlerClass, null));
                handler.onUserLogin(actor, type);
            } catch (Throwable e) {
                log.warn("Exception while calling loginHandler " + handlerClass, e);
            }
        }
    }

}
