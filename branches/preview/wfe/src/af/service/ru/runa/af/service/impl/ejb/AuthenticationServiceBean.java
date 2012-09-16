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
package ru.runa.af.service.impl.ejb;

import javax.annotation.Resource;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.interceptor.Interceptors;
import javax.security.auth.Subject;

import jcifs.smb.NtlmPasswordAuthentication;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;

import ru.runa.af.Actor;
import ru.runa.af.ArgumentsCommons;
import ru.runa.af.AuthenticationException;
import ru.runa.af.authenticaion.KerberosLoginModuleResources;
import ru.runa.af.authenticaion.SubjectPrincipalsHelper;
import ru.runa.af.logic.AuthenticationLogic;
import ru.runa.af.service.AuthenticationServiceLocal;
import ru.runa.af.service.AuthenticationServiceRemote;

/**
 * Implements AuthenticationService as bean. Created on 20.07.2004
 */
@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@Interceptors({SpringBeanAutowiringInterceptor.class, LoggerInterceptor.class})
public class AuthenticationServiceBean implements AuthenticationServiceLocal, AuthenticationServiceRemote {
    private static final Log log = LogFactory.getLog(AuthenticationServiceBean.class);
    @Autowired
    private AuthenticationLogic authenticationLogic;

    @Resource
    private SessionContext context;

    @Override
    public Subject authenticate() throws AuthenticationException {
        log.debug("Authenticating (principal)");
        Subject subject = authenticationLogic.authenticate(context.getCallerPrincipal());
        log.debug("Authenticated (principal): " + SubjectPrincipalsHelper.getActor(subject));
        return subject;
    }

    @Override
    public Subject authenticate(NtlmPasswordAuthentication authentication) throws AuthenticationException {
        ArgumentsCommons.checkNotNull(authentication, "NtlmPasswordAuthentication");
        log.debug("Authenticating (ntlm)");
        Subject subject = authenticationLogic.authenticate(authentication);
        log.debug("Authenticated (ntlm): " + SubjectPrincipalsHelper.getActor(subject));
        return subject;
    }

    @Override
    public Subject authenticate(byte[] token) throws AuthenticationException {
        ArgumentsCommons.checkNotNull(token, "Kerberos authentication information");
        log.debug("Authenticating (kerberos)");
        Subject subject = authenticationLogic.authenticate(token, KerberosLoginModuleResources.rtnKerberosResources);
        log.debug("Authenticated (kerberos): " + SubjectPrincipalsHelper.getActor(subject));
        return subject;
    }

    @Override
    public Subject authenticate(String name, String password) throws AuthenticationException {
        ArgumentsCommons.checkNotEmpty(name, "User login");
        log.debug("Authenticating (login) " + name);
        Subject subject = authenticationLogic.authenticate(name, password);
        log.debug("Authenticated (login): " + name);
        return subject;
    }

    @Override
    public Actor getActor(Subject subject) throws AuthenticationException {
        ArgumentsCommons.checkNotNull(subject);
        return authenticationLogic.getActor(subject);
    }

}
