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
package ru.runa.service.af.impl;

import java.util.Map;

import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.interceptor.Interceptors;
import javax.security.auth.Subject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;

import ru.runa.service.af.SystemServiceLocal;
import ru.runa.service.af.SystemServiceRemote;
import ru.runa.service.interceptors.EjbExceptionSupport;
import ru.runa.service.interceptors.EjbTransactionSupport;
import ru.runa.wfe.audit.logic.AuditLogic;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;

import com.google.common.base.Preconditions;

/**
 * Represent system ru.runa.commons.test operations login/logout. Created on
 * 16.08.2004
 */
@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
@Interceptors({ EjbExceptionSupport.class, EjbTransactionSupport.class, SpringBeanAutowiringInterceptor.class })
public class SystemServiceBean implements SystemServiceLocal, SystemServiceRemote {
    @Autowired
    private AuditLogic auditLogic;

    @Override
    public void login(Subject subject, ru.runa.wfe.security.ASystem system) throws AuthorizationException, AuthenticationException {
        Preconditions.checkNotNull(subject);
        Preconditions.checkNotNull(system);
        auditLogic.login(subject, system);
    }

    @Override
    public void logout(Subject subject, ru.runa.wfe.security.ASystem system) {
        Preconditions.checkNotNull(subject);
        Preconditions.checkNotNull(system);
        auditLogic.logout(subject, system);
    }

    @Override
    public Map<String, String> getLocalizations(Subject subject) {
        Preconditions.checkNotNull(subject);
        return auditLogic.getLocalizations(subject);
    }

    @Override
    public void saveLocalizations(Subject subject, Map<String, String> localizations) {
        Preconditions.checkNotNull(subject);
        Preconditions.checkNotNull(localizations);
        auditLogic.saveLocalizations(subject, localizations);
    }

}
