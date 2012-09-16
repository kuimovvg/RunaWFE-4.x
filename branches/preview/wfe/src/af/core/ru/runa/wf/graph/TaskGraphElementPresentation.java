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
package ru.runa.wf.graph;

import ru.runa.bpm.taskmgmt.def.Swimlane;

/**
 * Represents an task state graph element. 
 */
public class TaskGraphElementPresentation extends BaseGraphElementPresentation {

    private static final long serialVersionUID = 1L;

    /**
     * Flag, equals true, if task state is minimized; false otherwise.
     */
    private final boolean isMinimized;

    /**
     * Swimlane of this task element.
     */
    private final Swimlane swimlane;

    /**
     * @param name Graph element name. Can be null if not set.
     * @param graphConstraints Graph element position constraints.
     * @param swimlane Swimlane of this task element.
     * @param isMinimized Flag, equals true, if state is minimized; false otherwise.
     */
    public TaskGraphElementPresentation(String name, int[] graphConstraints, Swimlane swimlane, boolean isMinimized) {
        this(name, graphConstraints, swimlane, isMinimized, null);
    }

    /**
     * @param name Graph element name. Can be null if not set.
     * @param graphConstraints Graph element position constraints.
     * @param swimlane Swimlane of this task element.
     * @param isMinimized Flag, equals true, if state is minimized; false otherwise.
     * @param data Some additional data, assigned to graph element.
     */
    public TaskGraphElementPresentation(String name, int[] graphConstraints, Swimlane swimlane, boolean isMinimized, Object data) {
        super(name, graphConstraints, data);
        this.swimlane = swimlane;
        this.isMinimized = isMinimized;
    }

    @Override
    public void visit(GraphElementPresentationVisitor visitor) {
        visitor.onTaskState(this);
    }

    /**
     * Flag, equals true, if state is collapsed; false otherwise.
     */
    public boolean isMinimized() {
        return isMinimized;
    }

    /**
     * Swimlane of this task element.
     */
    public Swimlane getSwimlane() {
        return swimlane;
    }
}
