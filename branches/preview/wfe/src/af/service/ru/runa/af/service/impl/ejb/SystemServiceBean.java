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

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.interceptor.Interceptors;
import javax.security.auth.Subject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;

import ru.runa.af.ArgumentsCommons;
import ru.runa.af.AuthenticationException;
import ru.runa.af.AuthorizationException;
import ru.runa.af.logic.SystemLogic;
import ru.runa.af.service.SystemServiceLocal;
import ru.runa.af.service.SystemServiceRemote;

/**
 * Represent system ru.runa.commons.test operations login/logout. Created on 16.08.2004
 */
@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@Interceptors({SpringBeanAutowiringInterceptor.class, LoggerInterceptor.class})
public class SystemServiceBean implements SystemServiceLocal, SystemServiceRemote {
    @Autowired
    private SystemLogic systemLogic;

    @Override
    public void login(Subject subject, ru.runa.af.ASystem system) throws AuthorizationException, AuthenticationException {
        ArgumentsCommons.checkNotNull(subject);
        ArgumentsCommons.checkNotNull(system);
        systemLogic.login(subject, system);
    }

    @Override
    public void logout(Subject subject, ru.runa.af.ASystem system) {
        ArgumentsCommons.checkNotNull(subject);
        ArgumentsCommons.checkNotNull(system);
        systemLogic.logout(subject, system);
    }

}
