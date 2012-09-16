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
import org.apache.ecs.html.Div;
import org.apache.ecs.html.Map;
import org.apache.ecs.html.Span;
import org.apache.ecs.html.TD;

import ru.runa.wf.graph.BaseGraphElementPresentation;
import ru.runa.wf.graph.MultiinstanceGraphElementPresentation;
import ru.runa.wf.graph.SubprocessGraphElementPresentation;
import ru.runa.wf.graph.SubprocessesGraphElementAdapter;
import ru.runa.wf.graph.TaskGraphElementPresentation;
import ru.runa.wf.web.Resources;

/**
 * Operation to create tool tips on and links on process history graph.
 */
public class GraphHistoryElementPresentationVisitor extends SubprocessesGraphElementAdapter {

    /**
     * Processed graph element index. Incremented to ensure unique id's. 
     */
    private int elementIdx = 0;

    /**
     * Created map of elements, represents links and tool tips areas. 
     */
    private final Map map = new Map();

    /**
     * Root form element.
     */
    private final TD formDataTD;

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
     * @param taskId Current task instance identity.
     * @param pageContext Rendered page context.
     * @param tdFormElement Main TD element of form, containing graph history.
     */
    public GraphHistoryElementPresentationVisitor(Long taskId, PageContext pageContext, TD formDataTD) {
        this.formDataTD = formDataTD;
        map.setName("processInstanceMap");
        helperSubprocess = new SubprocessGraphElementPresentationHelper(taskId, pageContext, formDataTD, map, Resources.ACTION_SHOW_GRAPH_HISTORY);
        helperTasks = new TaskGraphElementHelper(map);
    }

    @Override
    public void onMultiinstance(MultiinstanceGraphElementPresentation element) {
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
     * @return Map of elements, represents links and tool tips areas. 
     */
    public Map getResultMap() {
        return map;
    }

    /**
     * Add tool tip for graph process element.
     * @param element Process element to add tool tip. 
     * @param area {@link Area} element, to add tool tips, or null, if {@link Area} element must be created.  
     */
    private Area addTooltip(BaseGraphElementPresentation element, Area area) {
        ++elementIdx;
        String toolTipId = "log_" + elementIdx + "_tt";
        if (area == null) {
            area = new Area("RECT", element.getGraphConstraints());
            map.addElement(area);
        }
        area.setOnMouseOver("showTip(event, '" + toolTipId + "')");
        area.setOnMouseOut("hideTip('" + toolTipId + "')");
        Span span = new Span();
        span.setID(toolTipId);
        span.setClass("field-hint");
        span.setStyle("display: none;");
        addTooltipToSpan(span, element.getData());
        formDataTD.addElement(span);
        return area;
    }

    /**
     * Add tool tip to span element. 
     * @param span Span element, to add tool tip.
     * @param elementData Tool tip data. Must be String or String[].
     */
    private void addTooltipToSpan(Span span, Object elementData) {
        if (elementData instanceof String) {
            span.addElement(new Div(elementData.toString()));
        } else if (elementData instanceof String[]) {
            for (String element : (String[]) elementData) {
                span.addElement(new Div(element));
            }
        } else {
            span.addElement(new Div("Unexpected data type: " + elementData == null ? "null" : elementData.getClass().getName()));
        }
    }
}
