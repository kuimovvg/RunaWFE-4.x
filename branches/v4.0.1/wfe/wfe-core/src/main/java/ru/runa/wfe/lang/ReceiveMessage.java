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
package ru.runa.wfe.lang;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.wfe.execution.ExecutionContext;

public class ReceiveMessage extends VariableContainerNode {
    private static final long serialVersionUID = 1L;
    private static Log log = LogFactory.getLog(ReceiveMessage.class);
    private static final String[] supportedEventTypes = new String[] { Event.EVENTTYPE_NODE_ENTER, Event.EVENTTYPE_NODE_LEAVE };

    @Override
    public NodeType getNodeType() {
        return NodeType.ReceiveMessage;
    }

    @Override
    public String[] getSupportedEventTypes() {
        return supportedEventTypes;
    }

    @Override
    public void execute(ExecutionContext executionContext) {
        log.info("Waiting for message in " + this);
    }

}
