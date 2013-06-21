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

import groovy.lang.GroovyShell;

import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.interceptor.Interceptors;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.script.AdminScriptRunner;
import ru.runa.wfe.service.ScriptingService;
import ru.runa.wfe.service.interceptors.CacheReloader;
import ru.runa.wfe.service.interceptors.EjbExceptionSupport;
import ru.runa.wfe.service.interceptors.EjbTransactionSupport;
import ru.runa.wfe.service.interceptors.PerformanceObserver;
import ru.runa.wfe.user.User;

@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
@Interceptors({ EjbExceptionSupport.class, CacheReloader.class, PerformanceObserver.class, EjbTransactionSupport.class,
        SpringBeanAutowiringInterceptor.class })
@WebService(name = "ScriptingAPI", serviceName = "ScriptingWebService")
@SOAPBinding
public class ScriptingServiceBean implements ScriptingService {
    @Autowired
    private AdminScriptRunner runner;

    @Override
    public void executeAdminScript(User user, byte[] configData, byte[][] processDefinitionsBytes) {
        runner.setUser(user);
        runner.setProcessDefinitionsBytes(processDefinitionsBytes);
        runner.runScript(configData);
    }

    @Override
    public void executeGroovyScript(User user, String script) {
        boolean enabled = SystemProperties.getResources().getBooleanProperty("scripting.groovy.enabled", false);
        if (!enabled) {
            throw new InternalApplicationException(
                    "In order to enable script execution set property 'scripting.groovy.enabled' to 'true' in system.properties or wfe.custom.system.properties");
        }
        GroovyShell shell = new GroovyShell();
        shell.evaluate(script);
    }
}
