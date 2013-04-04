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

import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.interceptor.Interceptors;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;

import ru.runa.wfe.script.AdminScriptRunner;
import ru.runa.wfe.service.AdminScriptService;
import ru.runa.wfe.service.interceptors.CacheReloader;
import ru.runa.wfe.service.interceptors.EjbExceptionSupport;
import ru.runa.wfe.service.interceptors.EjbTransactionSupport;
import ru.runa.wfe.user.User;

@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
@Interceptors({ EjbExceptionSupport.class, CacheReloader.class, EjbTransactionSupport.class, SpringBeanAutowiringInterceptor.class })
@WebService(name = "AdminScriptAPI", serviceName = "AdminScriptWebService")
@SOAPBinding
public class AdminScriptServiceBean implements AdminScriptService {
    @Autowired
    private AdminScriptRunner runner;

    @Override
    public void run(User user, byte[] configData, byte[][] processDefinitionsBytes) {
        runner.setUser(user);
        runner.setProcessDefinitionsBytes(processDefinitionsBytes);
        runner.runScript(configData);
    }
}
