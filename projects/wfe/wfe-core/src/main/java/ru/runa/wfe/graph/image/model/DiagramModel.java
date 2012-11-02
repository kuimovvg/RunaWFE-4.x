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
package ru.runa.wfe.graph.image.model;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.Element;

import ru.runa.wfe.commons.xml.XmlUtils;

import com.google.common.collect.Maps;

public class DiagramModel {
    private static final String NODE_ELEMENT = "node";
    private static final String TRANSITION_ELEMENT = "transition";
    private static final String BENDPOINT_ELEMENT = "bendpoint";

    private int height;
    private int width;
    private boolean showActions;
    private String notation;
    private final Map<String, NodeModel> nodes = Maps.newHashMap();

    private DiagramModel() {
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

    public int getWidth() {
        return width;
    }

    public boolean isShowActions() {
        return showActions;
    }

    public boolean isUmlNotation() {
        return "uml".equals(notation);
    }

    public static DiagramModel load(byte[] gpdBytes) throws Exception {
        DiagramModel diagramModel = new DiagramModel();
        Document document = XmlUtils.parseWithoutValidation(gpdBytes);
        Element root = document.getRootElement();
        diagramModel.width = Integer.parseInt(root.attributeValue("width"));
        diagramModel.height = Integer.parseInt(root.attributeValue("height"));
        diagramModel.notation = root.attributeValue("notation", "uml");
        diagramModel.showActions = Boolean.parseBoolean(root.attributeValue("showActions", "true"));
        List<Element> nodeElements = root.elements(NODE_ELEMENT);
        for (Element nodeElement : nodeElements) {
            NodeModel nodeModel = new NodeModel();
            nodeModel.setName(nodeElement.attributeValue("name"));
            nodeModel.setX(Integer.parseInt(nodeElement.attributeValue("x")));
            nodeModel.setY(Integer.parseInt(nodeElement.attributeValue("y")));
            nodeModel.setMinimizedView(Boolean.parseBoolean(nodeElement.attributeValue("minimizedView", "false")));
            nodeModel.setWidth(Integer.parseInt(nodeElement.attributeValue("width")));
            nodeModel.setHeight(Integer.parseInt(nodeElement.attributeValue("height")));
            diagramModel.nodes.put(nodeModel.getName(), nodeModel);
            List<Element> transitionElements = nodeElement.elements(TRANSITION_ELEMENT);
            for (Element transitionElement : transitionElements) {
                TransitionModel transitionModel = new TransitionModel();
                transitionModel.setName(transitionElement.attributeValue("name"));
                nodeModel.addTransition(transitionModel);
                List<Element> bendpointElements = transitionElement.elements(BENDPOINT_ELEMENT);
                for (Element bendpointElement : bendpointElements) {
                    BendpointModel bendpointModel = new BendpointModel();
                    bendpointModel.setX(Integer.parseInt(bendpointElement.attributeValue("x")));
                    bendpointModel.setY(Integer.parseInt(bendpointElement.attributeValue("y")));
                    transitionModel.addBendpoint(bendpointModel);
                }
            }
        }
        return diagramModel;
    }
}
