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
package ru.runa.wfe.commons;

import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.wfe.commons.ftl.ExpressionEvaluator;
import ru.runa.wfe.var.IVariableProvider;
import ru.runa.wfe.var.VariableMapping;

public class JMSUtil {
    private static Log log = LogFactory.getLog(JMSUtil.class);

    private static QueueConnection connection;
    private static QueueSession session;
    private static Queue queue;

    private static synchronized void init() throws JMSException, NamingException {
        if (session == null) {
            InitialContext iniCtx = new InitialContext();
            Object tmp = iniCtx.lookup("ConnectionFactory");
            QueueConnectionFactory qcf = (QueueConnectionFactory) tmp;
            connection = qcf.createQueueConnection();
            queue = (Queue) iniCtx.lookup("queue/jbpmQueue");
            session = connection.createQueueSession(false, QueueSession.AUTO_ACKNOWLEDGE);
            connection.start();
        }
    }

    public static ObjectMessage sendMessage(List<VariableMapping> data, IVariableProvider variableProvider) throws JMSException, NamingException {
        init();
        QueueSender sender = session.createSender(queue);
        HashMap<String, Object> map = new HashMap<String, Object>();
        for (VariableMapping variableMapping : data) {
            if (!variableMapping.isPropertySelector()) {
                map.put(variableMapping.getMappedName(), variableProvider.getValue(variableMapping.getName()));
            }
        }
        ObjectMessage message = session.createObjectMessage(map);
        for (VariableMapping variableMapping : data) {
            if (variableMapping.isPropertySelector()) {
                String value = variableMapping.getMappedName();
                Object v = ExpressionEvaluator.evaluateVariable(variableProvider, value);
                message.setObjectProperty(variableMapping.getName(), v);
            }
        }
        long ttl = 24 * 60 * 60 * 1000; // TODO get from process
        sender.send(message, Message.DEFAULT_DELIVERY_MODE, Message.DEFAULT_PRIORITY, ttl);
        sender.close();
        log.info("message sent: " + message);
        return message;
    }

    @SuppressWarnings("unchecked")
    public static String toString(ObjectMessage message) throws JMSException {
        StringBuffer buffer = new StringBuffer();
        if (message.getJMSExpiration() != 0) {
            buffer.append("{JMSExpiration=").append(new Date(message.getJMSExpiration())).append("}; ");
        }
        Enumeration<String> propertyNames = message.getPropertyNames();
        Map<String, String> propertyMap = new HashMap<String, String>();
        while (propertyNames.hasMoreElements()) {
            String propertyName = propertyNames.nextElement();
            Object propertyValue = message.getObjectProperty(propertyName);
            if (propertyValue == null) {
                propertyValue = "(null)";
            }
            propertyMap.put(propertyName, propertyValue.toString());
        }
        buffer.append(propertyMap);
        buffer.append("; ");
        buffer.append(message.getObject());
        return buffer.toString();
    }

    public static void stop() throws JMSException {
        connection.stop();
        session.close();
        connection.close();
    }

}
