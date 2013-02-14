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
package ru.runa.wfe.service.impl;

import javax.annotation.Resource;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.interceptor.Interceptors;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;

import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.auth.KerberosLoginModuleResources;
import ru.runa.wfe.security.logic.AuthenticationLogic;
import ru.runa.wfe.service.decl.AuthenticationServiceLocal;
import ru.runa.wfe.service.decl.AuthenticationServiceRemote;
import ru.runa.wfe.service.interceptors.EjbExceptionSupport;
import ru.runa.wfe.service.interceptors.EjbTransactionSupport;
import ru.runa.wfe.user.User;

import com.google.common.base.Preconditions;

/**
 * Implements AuthenticationService as bean. Created on 20.07.2004
 */
@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
@Interceptors({ EjbExceptionSupport.class, EjbTransactionSupport.class, SpringBeanAutowiringInterceptor.class })
@WebService(name = "AuthenticationAPI", serviceName = "AuthenticationWebService")
@SOAPBinding
public class AuthenticationServiceBean implements AuthenticationServiceLocal, AuthenticationServiceRemote {
    private static final Log log = LogFactory.getLog(AuthenticationServiceBean.class);
    @Autowired
    private AuthenticationLogic authenticationLogic;
    @Resource
    private SessionContext context;

    @Override
    public User authenticateByCallerPrincipal() throws AuthenticationException {
        log.debug("Authenticating (principal)");
        User user = authenticationLogic.authenticate(context.getCallerPrincipal());
        log.debug("Authenticated (principal): " + user);
        return user;
    }

    @Override
    public User authenticateByKerberos(byte[] token) throws AuthenticationException {
        Preconditions.checkNotNull(token, "Kerberos authentication information");
        log.debug("Authenticating (kerberos)");
        User user = authenticationLogic.authenticate(token, KerberosLoginModuleResources.rtnKerberosResources);
        log.debug("Authenticated (kerberos): " + user);
        return user;
    }

    @Override
    public User authenticateByLoginPassword(String name, String password) throws AuthenticationException {
        log.debug("Authenticating (login) " + name);
        User user = authenticationLogic.authenticate(name, password);
        log.debug("Authenticated (login): " + user);
        return user;
    }

}
