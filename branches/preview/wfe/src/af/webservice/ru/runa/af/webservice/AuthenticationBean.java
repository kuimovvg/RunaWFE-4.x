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

import java.util.Set;

import javax.ejb.Stateless;
import javax.interceptor.Interceptors;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebParam.Mode;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.security.auth.Subject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;

import ru.runa.af.ActorPrincipal;
import ru.runa.af.AuthenticationException;
import ru.runa.af.logic.AuthenticationLogic;
import ru.runa.af.service.impl.ejb.LoggerInterceptor;

@Stateless
@WebService(name = "Authentication", targetNamespace = "http://runa.ru/workflow/webservices", serviceName = "AuthenticationWebService")
@SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.WRAPPED)
@Interceptors({SpringBeanAutowiringInterceptor.class, LoggerInterceptor.class})
public class AuthenticationBean {
    @Autowired
    private AuthenticationLogic authenticationLogic;

    @WebMethod(operationName = "authenticateDB")
    public ActorPrincipal authenticate(@WebParam(mode = Mode.IN, name = "name", targetNamespace = "http://runa.ru/workflow/webservices") String name,
            @WebParam(mode = Mode.IN, name = "password", targetNamespace = "http://runa.ru/workflow/webservices") String password)
            throws AuthenticationException {
        return getActorPrincipal(authenticationLogic.authenticate(name, password));
    }

    private ActorPrincipal getActorPrincipal(Subject subject) {
        if (subject == null) {
            return null;
        }
        Set<ActorPrincipal> princs = subject.getPrincipals(ActorPrincipal.class);
        if (princs == null || princs.isEmpty()) {
            return null;
        }
        return princs.iterator().next();
    }
}
