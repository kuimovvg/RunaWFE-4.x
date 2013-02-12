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

import java.util.HashMap;

import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.interceptor.Interceptors;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;

import ru.runa.wfe.audit.logic.AuditLogic;
import ru.runa.wfe.security.ASystem;
import ru.runa.wfe.service.decl.SystemServiceLocal;
import ru.runa.wfe.service.decl.SystemServiceRemote;
import ru.runa.wfe.service.interceptors.EjbExceptionSupport;
import ru.runa.wfe.service.interceptors.EjbTransactionSupport;
import ru.runa.wfe.user.User;

import com.google.common.base.Preconditions;

/**
 * Represent system ru.runa.commons.test operations login/logout. Created on
 * 16.08.2004
 */
@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
@Interceptors({ EjbExceptionSupport.class, EjbTransactionSupport.class, SpringBeanAutowiringInterceptor.class })
@WebService
@SOAPBinding
public class SystemServiceBean implements SystemServiceLocal, SystemServiceRemote {
    @Autowired
    private AuditLogic auditLogic;

    @Override
    public void login(User user) {
        Preconditions.checkNotNull(user);
        auditLogic.login(user, ASystem.INSTANCE);
    }

    @Override
    public void logout(User user) {
        Preconditions.checkNotNull(user);
        auditLogic.logout(user, ASystem.INSTANCE);
    }

    @Override
    public HashMap<String, String> getLocalizations(User user) {
        Preconditions.checkNotNull(user);
        return auditLogic.getLocalizations(user);
    }

    @Override
    public String getLocalized(User user, String name) {
        return auditLogic.getLocalized(user, name);
    }

    @Override
    public void saveLocalizations(User user, HashMap<String, String> localizations) {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(localizations);
        auditLogic.saveLocalizations(user, localizations);
    }

}
