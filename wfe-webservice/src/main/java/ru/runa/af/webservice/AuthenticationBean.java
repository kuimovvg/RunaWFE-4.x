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
package ru.runa.af.webservice;

import javax.annotation.Resource;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebParam.Mode;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;

import ru.runa.WSLoggerInterceptor;
import ru.runa.service.AuthenticationService;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.auth.KerberosLoginModuleResources;
import ru.runa.wfe.security.logic.AuthenticationLogic;
import ru.runa.wfe.user.User;

@Stateless
@WebService(name = "Authentication", targetNamespace = "http://runa.ru/workflow/webservices", serviceName = "AuthenticationWebService")
@SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.WRAPPED)
@Interceptors({ SpringBeanAutowiringInterceptor.class, WSLoggerInterceptor.class })
public class AuthenticationBean implements AuthenticationService {
    @Autowired
    private AuthenticationLogic authenticationLogic;
    @Resource
    private SessionContext context;

    @Override
    @WebMethod
    public User authenticateByCallerPrincipal() throws AuthenticationException {
        return authenticationLogic.authenticate(context.getCallerPrincipal());
    }

    @Override
    @WebMethod(operationName = "authenticateByKerberos")
    public User authenticateByKerberos(@WebParam(name = "token") byte[] token) throws AuthenticationException {
        return authenticationLogic.authenticate(token, KerberosLoginModuleResources.rtnKerberosResources);
    }

    @WebMethod(operationName = "authenticateByKerberos2")
    public User authenticate2(@WebParam byte[] token) throws AuthenticationException {
        return authenticationLogic.authenticate(token, KerberosLoginModuleResources.rtnKerberosResources);
    }

    @Override
    @WebMethod(operationName = "authenticateByLoginPassword")
    public User authenticateByLoginPassword(@WebParam(mode = Mode.IN, name = "name", targetNamespace = "http://runa.ru/workflow/webservices") String name,
            @WebParam(mode = Mode.IN, name = "password", targetNamespace = "http://runa.ru/workflow/webservices") String password)
            throws AuthenticationException {
        return authenticationLogic.authenticate(name, password);
    }

}
