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

import java.util.Map;

import javax.security.auth.Subject;

import org.dom4j.Document;
import org.dom4j.Element;

import ru.runa.service.af.ExecutorService;
import ru.runa.service.delegate.Delegates;
import ru.runa.wfe.ConfigurationException;
import ru.runa.wfe.commons.xml.XmlUtils;
import ru.runa.wfe.handler.bot.TaskHandlerBase;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.var.IVariableProvider;

/**
 * This task handler changes actor status. Configuration looks like <config
 * actorVariableName='actorVar' statusVariableName='statusVar'/> where -
 * actorVar is variable which contains actor code - statusVar is variable of
 * Boolean or Number type and tells whether actor will be active or not
 * 
 * @since 3.0
 */
public class SetActorStatusTaskHandler extends TaskHandlerBase {
    private Config config;

    @Override
    public void setConfiguration(String configuration) {
        config = XmlParser.parse(configuration);
    }

    @Override
    public Map<String, Object> handle(Subject subject, IVariableProvider variableProvider, WfTask task) {
        ExecutorService executorDelegate = Delegates.getExecutorService();
        Long actorCode = variableProvider.getValueNotNull(Long.class, config.actorVariableName);
        Actor actor = executorDelegate.getActorByCode(subject, actorCode);
        boolean isActive = variableProvider.getValueNotNull(Boolean.class, config.statusVariableName);
        executorDelegate.setStatus(subject, actor.getId(), isActive);
        return null;
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

        public static Config parse(String configuration) {
            Document document = XmlUtils.parseWithoutValidation(configuration);
            Element root = document.getRootElement();
            if (!CONFIG_ELEMENT_NAME.equals(root.getName())) {
                throw new ConfigurationException("No <config> element found at root");
            }
            Config config = new Config();
            config.actorVariableName = root.attributeValue(ACTOR_ARRT_NAME);
            config.statusVariableName = root.attributeValue(STATUS_ATTR_NAME);
            return config;
        }
    }
}
