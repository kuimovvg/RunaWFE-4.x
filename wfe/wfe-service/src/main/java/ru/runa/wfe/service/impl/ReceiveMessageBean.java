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

import ru.runa.wfe.audit.ReceiveMessageLog;
import ru.runa.wfe.commons.JMSUtil;
import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.commons.ftl.ExpressionEvaluator;
import ru.runa.wfe.definition.dao.IProcessDefinitionLoader;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.execution.Token;
import ru.runa.wfe.execution.dao.TokenDAO;
import ru.runa.wfe.lang.NodeType;
import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.lang.ReceiveMessage;
import ru.runa.wfe.service.interceptors.EjbExceptionSupport;
import ru.runa.wfe.service.interceptors.EjbTransactionSupport;
import ru.runa.wfe.service.interceptors.PerformanceObserver;
import ru.runa.wfe.var.VariableMapping;

import com.google.common.base.Objects;

@MessageDriven(activationConfig = { @ActivationConfigProperty(propertyName = "destination", propertyValue = "queue/jbpmQueue"),
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
        @ActivationConfigProperty(propertyName = "useDLQ", propertyValue = "false") })
@TransactionManagement(TransactionManagementType.BEAN)
@Interceptors({ EjbExceptionSupport.class, PerformanceObserver.class, EjbTransactionSupport.class, SpringBeanAutowiringInterceptor.class })
public class ReceiveMessageBean implements MessageListener {
    private static Log log = LogFactory.getLog(ReceiveMessageBean.class);
    @Autowired
    private TokenDAO tokenDAO;
    @Autowired
    private IProcessDefinitionLoader processDefinitionLoader;

    @SuppressWarnings("unchecked")
    @Override
    public void onMessage(Message message) {
        ObjectMessage objectMessage = (ObjectMessage) message;
        try {
            log.info(message);
            boolean handled = false;
            List<Token> tokens = tokenDAO.findActiveTokens(NodeType.RECEIVE_MESSAGE);
            for (Token token : tokens) {
                ProcessDefinition processDefinition = processDefinitionLoader.getDefinition(token.getProcess().getDeployment().getId());
                ReceiveMessage receiveMessage = (ReceiveMessage) token.getNode(processDefinition);
                ExecutionContext executionContext = new ExecutionContext(processDefinition, token);
                boolean suitable = true;
                for (VariableMapping variableMapping : receiveMessage.getVariableMappings()) {
                    if (variableMapping.isPropertySelector()) {
                        String selectorValue = objectMessage.getStringProperty(variableMapping.getName());
                        String testValue = variableMapping.getMappedName();
                        String expectedValue;
                        if ("${currentProcessId}".equals(testValue) || "${currentInstanceId}".equals(testValue)) {
                            expectedValue = String.valueOf(token.getProcess().getId());
                        } else if ("${currentDefinitionName}".equals(testValue)) {
                            expectedValue = token.getProcess().getDeployment().getName();
                        } else if ("${currentNodeName}".equals(testValue)) {
                            expectedValue = receiveMessage.getName();
                        } else if ("${currentNodeId}".equals(testValue)) {
                            expectedValue = receiveMessage.getNodeId();
                        } else {
                            Object value = ExpressionEvaluator.evaluateVariable(executionContext.getVariableProvider(), testValue);
                            expectedValue = TypeConversionUtil.convertTo(String.class, value);
                        }
                        if (!Objects.equal(expectedValue, selectorValue)) {
                            log.debug("Rejected in " + token + " due to diff in " + variableMapping.getName() + "(" + expectedValue + "!="
                                    + selectorValue + ")");
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
                    executionContext.addLog(new ReceiveMessageLog(receiveMessage, JMSUtil.toString(objectMessage, true)));
                    receiveMessage.leave(executionContext);
                    handled = true;
                }
            }
            if (!handled) {
                throw new MessagePostponedException(JMSUtil.toString(objectMessage, false));
            }
        } catch (JMSException e) {
            log.error("", e);
            throw new RuntimeException(e);
        }
    }
}
