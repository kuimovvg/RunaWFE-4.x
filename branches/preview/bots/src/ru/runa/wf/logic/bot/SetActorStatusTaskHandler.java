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
package ru.runa.wf.logic.bot;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.security.auth.Subject;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import ru.runa.af.Actor;
import ru.runa.af.service.ExecutorService;
import ru.runa.delegate.DelegateFactory;
import ru.runa.wf.TaskStub;
import ru.runa.wf.service.ExecutionService;

/**
 * @created on 13.10.2010
 * This task handler changes actor status.
 * Configuration looks like <config actorVariableName='actorVar' statusVariableName='statusVar'/>
 * where 
 *  - actorVar is variable which contains actor code
 *  - statusVar is variable of Boolean or Number type and tels whether actor will be active or not
 */
public class SetActorStatusTaskHandler implements TaskHandler {
    private static final Log log = LogFactory.getLog(SetActorStatusTaskHandler.class);
    private Config config;

    public void configure(String configurationName) {
        throw new UnsupportedOperationException();
    }

    public void configure(byte[] configuration) throws TaskHandlerException {
        config = XmlParser.parse(configuration);
    }

    public void handle(Subject subject, TaskStub taskStub) throws TaskHandlerException {
        try {
            log.info("Executing task in process " + taskStub.getProcessInstanceId() + " with " + config);
            ExecutionService executionService = DelegateFactory.getInstance().getExecutionService();
            Map<String, Object> variables = executionService.getVariables(subject, taskStub.getId());

            ExecutorService executorDelegate = ru.runa.delegate.DelegateFactory.getInstance().getExecutorService();
            Object actorVariable = variables.get(config.actorVariableName);
            if (actorVariable == null) {
                throw new Exception("No actor variable found " + config.actorVariableName);
            }
            Long code;
            if (actorVariable instanceof Long) {
                code = (Long) actorVariable;
            } else {
                code = new Long(actorVariable.toString());
            }
            Actor actor = executorDelegate.getActorByCode(subject, code);

            Object statusVariable = variables.get(config.statusVariableName);
            if (statusVariable == null) {
                throw new Exception("No status variable found " + config.statusVariableName);
            }
            boolean isActive;
            if (statusVariable instanceof Boolean) {
                isActive = (Boolean) statusVariable;
            } else if (statusVariable instanceof Number) {
                isActive = ((Number) statusVariable).longValue() != 0;
            } else {
                throw new Exception("Status variable has wrong type " + statusVariable.getClass());
            }
            executorDelegate.setStatus(subject, actor.getId(), isActive);

            executionService.completeTask(subject, taskStub.getId(), taskStub.getName(), taskStub.getTargetActor().getId(), new HashMap<String, Object>());
            log.info("task completed: " + taskStub);
        } catch (Exception e) {
            throw new TaskHandlerException(e);
        }
    }

    private static class Config {
        private String actorVariableName;
        private String statusVariableName;

        @Override
        public String toString() {
            return "<config actorVariableName='" + actorVariableName + "' statusVariableName='" + statusVariableName + "'/>";
        }
    }

    private static class XmlParser {
        private static final String CONFIG_ELEMENT_NAME = "config";
        private static final String ACTOR_ARRT_NAME = "actorVariableName";
        private static final String STATUS_ATTR_NAME = "statusVariableName";

        public static Config parse(byte[] bytes) throws TaskHandlerException {
            try {
                InputStream is = new ByteArrayInputStream(bytes);
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                Document document = factory.newDocumentBuilder().parse(is);

                Config config = new Config();
                NodeList nodeList = document.getElementsByTagName(CONFIG_ELEMENT_NAME);
                if (nodeList.getLength() == 0) {
                    throw new Exception("No <config> element found.");
                }
                Element element = (Element) nodeList.item(0);
                config.actorVariableName = element.getAttribute(ACTOR_ARRT_NAME);
                config.statusVariableName = element.getAttribute(STATUS_ATTR_NAME);
                return config;
            } catch (Throwable e) {
                throw new TaskHandlerException(e);
            }
        }
    }
}
