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
     * Graph element name. Can be null if not set.
     */
    private final String name;

    /**
     * Graph element position constraints.
     */
    private final int[] graphConstraints;

    /**
     * Some additional data, assigned to graph element.
     * This data is differs in graph elements, returned by different kinds of requests.
     * May be null if not set.  
     */
    private final Object data;

    /**
     * @param name Graph element name. Can be null if not set.
     * @param graphConstraints Graph element position constraints.
     * @param data Some additional data, assigned to graph element.
     */
    protected BaseGraphElementPresentation(String name, int[] graphConstraints, Object data) {
        super();
        this.name = name;
        this.graphConstraints = graphConstraints;
        this.data = data;
    }

    @Override
    public abstract void visit(GraphElementPresentationVisitor visitor);

    /**
     * Graph element name. Can be null if not set.
     */
    public String getName() {
        return name;
    }

    /**
     * Graph element position constraints.
     * For rectangles represents upper-left and bottom-right corners. 
     */
    public int[] getGraphConstraints() {
        return graphConstraints;
    }

    /**
     * Some additional data, assigned to graph element.
     */
    public Object getData() {
        return data;
    }
}
