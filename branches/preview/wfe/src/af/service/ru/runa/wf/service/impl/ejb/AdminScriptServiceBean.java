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
package ru.runa.wf.service.impl.ejb;

import java.io.ByteArrayInputStream;

import javax.annotation.Resource;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.interceptor.Interceptors;
import javax.security.auth.Subject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;

import ru.runa.af.ArgumentsCommons;
import ru.runa.af.AuthenticationException;
import ru.runa.af.service.AuthenticationService;
import ru.runa.af.service.impl.ejb.LoggerInterceptor;
import ru.runa.delegate.DelegateFactory;
import ru.runa.wf.WfeScriptException;
import ru.runa.wf.logic.WfeScriptRunner;
import ru.runa.wf.service.AdminScriptService;

@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@Interceptors({SpringBeanAutowiringInterceptor.class, LoggerInterceptor.class})
public class AdminScriptServiceBean implements AdminScriptService {
    private static final Log log = LogFactory.getLog(AdminScriptServiceBean.class);
    @Resource
    private SessionContext context;
    @Autowired
    private WfeScriptRunner runner;

    @Override
    public void run(String login, String password, byte[] configData, byte[][] processDefinitionsBytes) throws WfeScriptException,
            AuthenticationException {
        ArgumentsCommons.checkNotEmpty(login, "User login");
        ArgumentsCommons.checkNotNull(configData);
        ArgumentsCommons.checkNotNull(processDefinitionsBytes);
        try {
            AuthenticationService delegate = DelegateFactory.getInstance().getAuthenticationService();
            Subject subject = delegate.authenticate(login, password);
            runner.setSubject(subject);
            runner.setProcessDefinitionsBytes(processDefinitionsBytes);
            runner.runScript(new ByteArrayInputStream(configData));
        } catch (AuthenticationException e) {
            log.error("", e);
            context.setRollbackOnly();
            throw e;
        } catch (WfeScriptException e) {
            context.setRollbackOnly();
            throw e;
        }
    }
}
