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
package ru.runa.wfe.graph.view;

import java.io.Serializable;

/**
 * Base class for {@link GraphElementPresentation} implementations.
 */
public abstract class BaseGraphElementPresentation implements GraphElementPresentation, Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Graph element ID.
     */
    private String nodeId;

    /**
     * Graph element name.
     */
    private String name;

    /**
     * Graph element position constraints.
     */
    private int[] graphConstraints;

    /**
     * Some additional data, assigned to graph element. This data is differs in
     * graph elements, returned by different kinds of requests. May be null if
     * not set.
     */
    private Object data;

    @Override
    public abstract void visit(GraphElementPresentationVisitor visitor);

    /**
     * Graph element ID.
     */
    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    /**
     * Graph element name.
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Graph element position constraints. For rectangles represents upper-left
     * and bottom-right corners.
     */
    public int[] getGraphConstraints() {
        return graphConstraints;
    }

    public void setGraphConstraints(int[] graphConstraints) {
        this.graphConstraints = graphConstraints;
    }

    /**
     * Some additional data, assigned to graph element.
     */
    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
