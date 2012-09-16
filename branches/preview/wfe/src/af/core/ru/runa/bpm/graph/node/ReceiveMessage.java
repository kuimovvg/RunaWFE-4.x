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

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;

import ru.runa.bpm.context.def.VariableMapping;
import ru.runa.bpm.graph.def.Event;
import ru.runa.bpm.graph.def.Node;
import ru.runa.bpm.graph.def.ExecutableProcessDefinition;
import ru.runa.bpm.graph.exe.ExecutionContext;
import ru.runa.bpm.graph.exe.Token;
import ru.runa.bpm.jpdl.xml.JpdlXmlReader;
import ru.runa.bpm.jpdl.xml.Parsable;

import com.google.common.collect.Lists;

public class ReceiveMessage extends Node implements Parsable {
    private static final long serialVersionUID = 1L;
    private static Log log = LogFactory.getLog(ReceiveMessage.class);

    @Override
    public NodeType getNodeType() {
        return NodeType.ReceiveMessage;
    }

    protected List<VariableMapping> variableMappings = Lists.newArrayList();

    public List<VariableMapping> getVariableMappings() {
        return variableMappings;
    }

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
    public void read(ExecutableProcessDefinition processDefinition, Element processStateElement, JpdlXmlReader jpdlReader) {
        this.variableMappings.addAll(jpdlReader.readVariableMappings(processStateElement));
    }

    @Override
    public void execute(ExecutionContext executionContext) {
        log.info("Waiting for message");
    }

    @Override
    protected void addNodeLog(Token token) {
        super.addNodeLog(token);
    }

}
