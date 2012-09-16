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

import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.ejb.MessageDrivenContext;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.interceptor.Interceptors;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;

import ru.runa.af.service.impl.ejb.LoggerInterceptor;
import ru.runa.bpm.context.def.VariableMapping;
import ru.runa.bpm.db.ProcessExecutionDAO;
import ru.runa.bpm.graph.exe.ExecutionContext;
import ru.runa.bpm.graph.exe.Token;
import ru.runa.bpm.graph.log.MessageNodeLog;
import ru.runa.bpm.graph.node.ReceiveMessage;
import ru.runa.commons.JBPMLazyLoaderHelper;
import ru.runa.commons.JMSUtil;
import ru.runa.commons.cache.CachingLogic;
import ru.runa.wf.logic.JbpmCommonLogic;

/**
 * Created on 14.01.2012
 */
@MessageDriven(activationConfig = { @ActivationConfigProperty(propertyName = "destination", propertyValue = "queue/jbpmQueue"),
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue") })
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@Interceptors({ SpringBeanAutowiringInterceptor.class, LoggerInterceptor.class })
public class JbpmMessageListenerBean implements MessageListener {
    private static Log log = LogFactory.getLog(JbpmMessageListenerBean.class);
    @Resource
    private MessageDrivenContext messageDrivenContext;
    @Autowired
    private ProcessExecutionDAO processExecutionDAO;
    @Autowired
    private JbpmCommonLogic commonLogic;

    @SuppressWarnings("unchecked")
    @Override
    public void onMessage(Message message) {
        log.info("Got message: " + message);
        ObjectMessage objectMessage = (ObjectMessage) message;
        try {
            boolean handled = false;
            List<Token> tokens = processExecutionDAO.findAllActiveReceiveMessageTokens();
            for (Token token : tokens) {
                ReceiveMessage node = (ReceiveMessage) JBPMLazyLoaderHelper.getImplementation(token.getNode());
                boolean suitable = true;
                for (VariableMapping variableMapping : node.getVariableMappings()) {
                    if (variableMapping.isPropertySelector()) {
                        String selectorValue = objectMessage.getStringProperty(variableMapping.getName());
                        String expectedValue = variableMapping.getMappedName();
                        if ("${currentInstanceId}".equals(expectedValue)) {
                            expectedValue = String.valueOf(token.getProcessInstance().getId());
                        }
                        if ("${currentDefinitionName}".equals(expectedValue)) {
                            expectedValue = token.getProcessInstance().getProcessDefinition().getName();
                        }
                        if ("${currentNodeName}".equals(expectedValue)) {
                            expectedValue = token.getNode().getName();
                        }
                        if (!expectedValue.equals(selectorValue)) {
                            suitable = false;
                            break;
                        }
                    }
                }
                if (suitable) {
                    HashMap<String, Object> map = (HashMap<String, Object>) objectMessage.getObject();
                    ExecutionContext executionContext = new ExecutionContext(commonLogic.getDefinition(token.getProcessInstance()), token);
                    for (VariableMapping variableMapping : node.getVariableMappings()) {
                        if (!variableMapping.isPropertySelector()) {
                            Object value = map.get(variableMapping.getMappedName());
                            executionContext.setVariable(variableMapping.getName(), value);
                        }
                    }
                    node.leave(executionContext);
                    String log = JMSUtil.toString(objectMessage);
                    token.addLog(new MessageNodeLog(node, new Date(), log));
                    // FIXME jbpmContext.save(token);
                    handled = true;
                }
            }
            if (handled) {
                CachingLogic.onTaskChange(null, null, null, null, null);
            } else {
                messageDrivenContext.setRollbackOnly();
            }
        } catch (JMSException e) {
            log.error("", e);
            throw new RuntimeException(e);
        } finally {
            CachingLogic.onTransactionComplete();
        }
    }
}
