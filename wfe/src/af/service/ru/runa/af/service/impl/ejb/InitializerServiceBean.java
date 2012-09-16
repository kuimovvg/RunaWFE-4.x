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
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.interceptor.Interceptors;
import javax.transaction.UserTransaction;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;

import ru.runa.af.logic.InitializerLogic;
import ru.runa.af.service.InitializerService;

/**
 * Created on 02.08.2004
 */
@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@Interceptors({SpringBeanAutowiringInterceptor.class, LoggerInterceptor.class})
public class InitializerServiceBean implements InitializerService {
    @Autowired
    private InitializerLogic initializerLogic; // TODO different impl
    @Resource 
    private SessionContext sessionContext;  

    @Override
    public void init(boolean force, boolean isArchiveDBinit) {
        UserTransaction transaction = sessionContext.getUserTransaction();  
        initializerLogic.init(transaction, force, isArchiveDBinit);
    }

}
