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
package ru.runa.bpm.graph.node;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.jms.ObjectMessage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;

import ru.runa.bpm.context.def.VariableMapping;
import ru.runa.bpm.graph.def.Event;
import ru.runa.bpm.graph.def.Node;
import ru.runa.bpm.graph.def.ExecutableProcessDefinition;
import ru.runa.bpm.graph.exe.ExecutionContext;
import ru.runa.bpm.graph.log.MessageNodeLog;
import ru.runa.bpm.jpdl.xml.JpdlXmlReader;
import ru.runa.bpm.jpdl.xml.Parsable;
import ru.runa.commons.JMSUtil;

import com.google.common.collect.Lists;

public class SendMessage extends Node implements Parsable {
    private static final long serialVersionUID = 1L;
    private static Log log = LogFactory.getLog(SendMessage.class);

    @Override
    public NodeType getNodeType() {
        return NodeType.SendMessage;
    }

    protected List<VariableMapping> variableMappings = Lists.newArrayList();

    // event types
    // //////////////////////////////////////////////////////////////

    public static final String[] supportedEventTypes = new String[] { Event.EVENTTYPE_NODE_ENTER, Event.EVENTTYPE_NODE_LEAVE };

    @Override
    public String[] getSupportedEventTypes() {
        return supportedEventTypes;
    }

    // xml
    // //////////////////////////////////////////////////////////////////////

    @Override
    public void read(ExecutableProcessDefinition processDefinition, Element element, JpdlXmlReader jpdlReader) {
        this.variableMappings.addAll(jpdlReader.readVariableMappings(element));
    }

    @Override
    public void execute(ExecutionContext executionContext) {
        try {
            Map<String, Object> variables = executionContext.getContextInstance().getVariables();
            variables.put("currentInstanceId", executionContext.getProcessInstance().getId());
            variables.put("currentDefinitionName", executionContext.getProcessDefinition().getName());
            variables.put("currentNodeName", executionContext.getNode().getName());
            ObjectMessage message = JMSUtil.sendMessage(variableMappings, variables);
            String log = JMSUtil.toString(message);
            executionContext.getToken().addLog(new MessageNodeLog(this, new Date(), log));
            super.execute(executionContext);
        } catch (Exception e) {
            log.error("", e);
            throw new RuntimeException(e);
        }
    }

}
