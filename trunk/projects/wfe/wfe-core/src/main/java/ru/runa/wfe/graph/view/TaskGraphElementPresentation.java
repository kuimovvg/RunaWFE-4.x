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
     * Swimlane name of this task element.
     */
    private final String swimlaneName;

    /**
     * @param swimlaneName
     *            Swimlane name of this task element.
     * @param isMinimized
     *            Flag, equals true, if state is minimized; false otherwise.
     */
    public TaskGraphElementPresentation(String swimlaneName, boolean isMinimized) {
        this.swimlaneName = swimlaneName;
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
     * Swimlane name of this task element.
     */
    public String getSwimlaneName() {
        return swimlaneName;
    }

}
