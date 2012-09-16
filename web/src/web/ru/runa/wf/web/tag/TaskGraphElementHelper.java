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
package ru.runa.wf.web.tag;

import org.apache.ecs.html.Area;
import org.apache.ecs.html.Map;

import ru.runa.wf.graph.TaskGraphElementPresentation;

/**
 * Helper class to create task graph element tool tip.
 */
public class TaskGraphElementHelper {

    /**
     * Created map of elements, represents links and tool tips areas.
     */
    private final org.apache.ecs.html.Map map;

    /**
     * Creates instance of helper class to create task graph element tool tip.
     * 
     * @param map
     *            Created map of elements, represents links and tool tips areas.
     */
    public TaskGraphElementHelper(Map map) {
        super();
        this.map = map;
    }

    /**
     * Creates tool tip for given task graph element.
     * 
     * @param element
     *            Graph element, to create tool tip.
     * @return {@link Area} instance with tool tip or null, if {@link Area} not created.
     */
    public Area createTaskTooltip(TaskGraphElementPresentation element) {
        if (!element.isMinimized()) {
            return null;
        }
        Area area = new Area("RECT", element.getGraphConstraints());
        String name = "";
        if (element.getSwimlane() != null) {
            name += "(" + element.getSwimlane().getName() + ")\n";
        }
        name += element.getName();
        area.setAlt(name);
        area.setTitle(name);
        map.addElement(area);
        return area;
    }
}
