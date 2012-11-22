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
package ru.runa.service.wf.impl;

import java.util.HashMap;
import java.util.List;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.interceptor.Interceptors;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;

import ru.runa.service.interceptors.EjbExceptionSupport;
import ru.runa.service.interceptors.EjbTransactionSupport;
import ru.runa.wfe.audit.ReceiveMessageLog;
import ru.runa.wfe.commons.JMSUtil;
import ru.runa.wfe.commons.cache.CachingLogic;
import ru.runa.wfe.definition.dao.IProcessDefinitionLoader;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.execution.Token;
import ru.runa.wfe.execution.dao.TokenDAO;
import ru.runa.wfe.lang.NodeType;
import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.lang.ReceiveMessage;
import ru.runa.wfe.var.VariableMapping;

/**
 * Created on 14.01.2012
 */
@MessageDriven(activationConfig = { @ActivationConfigProperty(propertyName = "destination", propertyValue = "queue/jbpmQueue"),
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue") })
@TransactionManagement(TransactionManagementType.BEAN)
@Interceptors({ EjbExceptionSupport.class, EjbTransactionSupport.class, SpringBeanAutowiringInterceptor.class })
public class ReceiveMessageBean implements MessageListener {
    private static Log log = LogFactory.getLog(ReceiveMessageBean.class);
    @Autowired
    private TokenDAO tokenDAO;
    @Autowired
    private IProcessDefinitionLoader processDefinitionLoader;

    @SuppressWarnings("unchecked")
    @Override
    public void onMessage(Message message) {
        log.info("Got message: " + message);
        ObjectMessage objectMessage = (ObjectMessage) message;
        try {
            String log = JMSUtil.toString(objectMessage);
            boolean handled = false;
            List<Token> tokens = tokenDAO.findActiveTokens(NodeType.ReceiveMessage);
            for (Token token : tokens) {
                ProcessDefinition processDefinition = processDefinitionLoader.getDefinition(token.getProcess().getDefinition().getId());
                ReceiveMessage receiveMessage = (ReceiveMessage) token.getNode(processDefinition);
                ExecutionContext executionContext = new ExecutionContext(processDefinition, token);
                boolean suitable = true;
                for (VariableMapping variableMapping : receiveMessage.getVariableMappings()) {
                    if (variableMapping.isPropertySelector()) {
                        String selectorValue = objectMessage.getStringProperty(variableMapping.getName());
                        String expectedValue = variableMapping.getMappedName();
                        if ("${currentProcessId}".equals(expectedValue) || "${currentInstanceId}".equals(expectedValue)) {
                            expectedValue = String.valueOf(token.getProcess().getId());
                        }
                        if ("${currentDefinitionName}".equals(expectedValue)) {
                            expectedValue = token.getProcess().getDefinition().getName();
                        }
                        if ("${currentNodeName}".equals(expectedValue)) {
                            expectedValue = receiveMessage.getName();
                        }
                        if ("${currentNodeId}".equals(expectedValue)) {
                            expectedValue = receiveMessage.getNodeId();
                        }
                        if (!expectedValue.equals(selectorValue)) {
                            suitable = false;
                            break;
                        }
                    }
                }
                if (suitable) {
                    HashMap<String, Object> map = (HashMap<String, Object>) objectMessage.getObject();
                    for (VariableMapping variableMapping : receiveMessage.getVariableMappings()) {
                        if (!variableMapping.isPropertySelector()) {
                            Object value = map.get(variableMapping.getMappedName());
                            executionContext.setVariable(variableMapping.getName(), value);
                        }
                    }
                    executionContext.addLog(new ReceiveMessageLog(receiveMessage, log));
                    receiveMessage.leave(executionContext);
                    handled = true;
                }
            }
            if (!handled) {
                throw new MessagePostponedException(log);
            }
            CachingLogic.onTaskChange(null, null, null, null, null);
        } catch (JMSException e) {
            log.error("", e);
            throw new RuntimeException(e);
        }
    }
}
