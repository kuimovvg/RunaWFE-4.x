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

import javax.servlet.jsp.PageContext;

import org.apache.ecs.html.Area;
import org.apache.ecs.html.Map;
import org.apache.ecs.html.TD;

import ru.runa.common.WebResources;
import ru.runa.wfe.graph.view.GraphElementPresentation;
import ru.runa.wfe.graph.view.MultiinstanceGraphElementPresentation;
import ru.runa.wfe.graph.view.SubprocessGraphElementPresentation;
import ru.runa.wfe.graph.view.SubprocessesGraphElementAdapter;
import ru.runa.wfe.graph.view.TaskGraphElementPresentation;

/**
 * Operation to create tool tips on and links on process history graph.
 */
public class GraphHistoryElementPresentationVisitor extends SubprocessesGraphElementAdapter {

    /**
     * Created map of elements, represents links and tool tips areas.
     */
    private final Map map = new Map();

    /**
     * Helper to create links to subprocesses.
     */
    private final SubprocessGraphElementPresentationHelper helperSubprocess;

    /**
     * Helper to create tool tips for task graph elements.
     */
    private final TaskGraphElementHelper helperTasks;

    /**
     * Creates operation to add tool tips and links on process history graph.
     * 
     * @param taskId
     *            Current task identity.
     * @param pageContext
     *            Rendered page context.
     * @param tdFormElement
     *            Main TD element of form, containing graph history.
     */
    public GraphHistoryElementPresentationVisitor(Long taskId, PageContext pageContext, TD formDataTD) {
        map.setName("processMap");
        helperSubprocess = new SubprocessGraphElementPresentationHelper(taskId, pageContext, formDataTD, map, WebResources.ACTION_SHOW_GRAPH_HISTORY);
        helperTasks = new TaskGraphElementHelper(map);
    }

    @Override
    public void onMultiSubprocess(MultiinstanceGraphElementPresentation element) {
        helperSubprocess.createMultiinstanceLinks(element);
        addTooltip(element, null);
    }

    @Override
    public void onSubprocess(SubprocessGraphElementPresentation element) {
        Area area = helperSubprocess.createSubprocessLink(element);
        addTooltip(element, area);
    }

    @Override
    public void onTaskState(TaskGraphElementPresentation element) {
        Area area = helperTasks.createTaskTooltip(element);
        addTooltip(element, area);
    }

    /**
     * Operation result.
     * 
     * @return Map of elements, represents links and tool tips areas.
     */
    public Map getResultMap() {
        return map;
    }

    /**
     * Add tool tip for graph process element.
     * 
     * @param element
     *            Process element to add tool tip.
     * @param area
     *            {@link Area} element, to add tool tips, or null, if
     *            {@link Area} element must be created.
     */
    private Area addTooltip(GraphElementPresentation element, Area area) {
        if (area == null) {
            area = new Area("RECT", element.getGraphConstraints());
            map.addElement(area);
        }
        area.setTitle(String.valueOf(element.getData()));
        return area;
    }
}
