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
package ru.runa.wf.graph.model;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.digester.Digester;
import org.xml.sax.SAXException;

public class DiagramModel {
    private static final String PROCESS_DIAGRAM_NODE_TRANSITION_BENDPOINT_PATTERN = "process-diagram/node/transition/bendpoint";
    private static final String PROCESS_DIAGRAM_NODE_TRANSITION_PATTERN = "process-diagram/node/transition";
    private static final String PROCESS_DIAGRAM_NODE_PATTERN = "process-diagram/node";
    private static final String PROCESS_DIAGRAM_PATTERN = "process-diagram";

    private final Map<String, NodeModel> nodes = new HashMap<String, NodeModel>();
    private int height;
    private int width;
    private boolean showActions;
    private String notation = "uml";

    private DiagramModel() {
    }

    public void addNode(NodeModel nodeModel) {
        nodes.put(nodeModel.getName(), nodeModel);
    }

    public Collection<NodeModel> getNodes() {
        return nodes.values();
    }

    public NodeModel getNode(String name) {
        return nodes.get(name);
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public boolean isShowActions() {
        return showActions;
    }

    public void setShowActions(boolean showActions) {
        this.showActions = showActions;
    }

    public void setNotation(String notation) {
        this.notation = notation;
    }

    public boolean isUmlNotation() {
        return "uml".equals(notation);
    }

    public static DiagramModel load(byte[] gpdBytes) throws IOException, SAXException {
        Digester digester = new Digester();

        digester.push(new DiagramModel());
        digester.addSetProperties(PROCESS_DIAGRAM_PATTERN);

        digester.addObjectCreate(PROCESS_DIAGRAM_NODE_PATTERN, NodeModel.class);
        digester.addSetProperties(PROCESS_DIAGRAM_NODE_PATTERN);
        digester.addSetNext(PROCESS_DIAGRAM_NODE_PATTERN, "addNode");

        digester.addObjectCreate(PROCESS_DIAGRAM_NODE_TRANSITION_PATTERN, TransitionModel.class);
        digester.addSetProperties(PROCESS_DIAGRAM_NODE_TRANSITION_PATTERN);
        digester.addSetNext(PROCESS_DIAGRAM_NODE_TRANSITION_PATTERN, "addTransition");

        digester.addObjectCreate(PROCESS_DIAGRAM_NODE_TRANSITION_BENDPOINT_PATTERN, BendpointModel.class);
        digester.addSetProperties(PROCESS_DIAGRAM_NODE_TRANSITION_BENDPOINT_PATTERN);
        digester.addSetNext(PROCESS_DIAGRAM_NODE_TRANSITION_BENDPOINT_PATTERN, "addBendpoint");

        return (DiagramModel) digester.parse(new ByteArrayInputStream(gpdBytes));
    }
}
