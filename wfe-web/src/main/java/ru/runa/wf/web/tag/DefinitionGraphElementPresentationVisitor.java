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

import java.util.Map;

import javax.servlet.jsp.PageContext;

import org.apache.ecs.html.Area;

import ru.runa.common.WebResources;
import ru.runa.common.web.Commons;
import ru.runa.common.web.form.IdForm;
import ru.runa.wfe.commons.web.PortletUrlType;
import ru.runa.wfe.graph.view.MultiinstanceGraphElementPresentation;
import ru.runa.wfe.graph.view.SubprocessGraphElementPresentation;
import ru.runa.wfe.graph.view.SubprocessesGraphElementAdapter;
import ru.runa.wfe.graph.view.TaskGraphElementPresentation;

import com.google.common.collect.Maps;

/**
 * Operation to create links to subprocess definitions and tool tips for
 * minimized states.
 */
public class DefinitionGraphElementPresentationVisitor extends SubprocessesGraphElementAdapter {

    /**
     * Rendered page context.
     */
    private final PageContext pageContext;

    /**
     * Created map of elements, represents links and tool tips areas.
     */
    private final org.apache.ecs.html.Map map = new org.apache.ecs.html.Map();

    /**
     * Helper to create tool tips for task graph elements.
     */
    private final TaskGraphElementHelper helperTasks;

    /**
     * Instantiates operation to create links to subprocess definitions and tool
     * tips for minimized states.
     * 
     * @param pageContext
     *            Rendered page context.
     */
    public DefinitionGraphElementPresentationVisitor(PageContext pageContext) {
        super();
        this.pageContext = pageContext;
        map.setName("processMap");
        helperTasks = new TaskGraphElementHelper(map);
    }

    @Override
    public void onSubprocess(SubprocessGraphElementPresentation element) {
        if (!element.isReadPermission()) {
            return;
        }
        Map<String, Object> params = Maps.newHashMap();
        params.put(IdForm.ID_INPUT_NAME, element.getSubprocessId());
        String url = Commons.getActionUrl(WebResources.ACTION_MAPPING_MANAGE_DEFINITION, params, pageContext, PortletUrlType.Render);
        Area area = new Area("RECT", element.getGraphConstraints());
        area.setHref(url);
        area.setTitle(element.getName());
        map.addElement(area);
    }

    @Override
    public void onMultiinstance(MultiinstanceGraphElementPresentation element) {
        if (!element.isReadPermission()) {
            return;
        }
        Map<String, Object> params = Maps.newHashMap();
        params.put(IdForm.ID_INPUT_NAME, element.getIds().get(0));
        String url = Commons.getActionUrl(WebResources.ACTION_MAPPING_MANAGE_DEFINITION, params, pageContext, PortletUrlType.Render);
        Area area = new Area("RECT", element.getGraphConstraints());
        area.setHref(url);
        area.setTitle(element.getName());
        map.addElement(area);
    }

    @Override
    public void onTaskState(TaskGraphElementPresentation element) {
        helperTasks.createTaskTooltip(element);
    }

    /**
     * Operation result.
     * 
     * @return Map of elements, represents links and tool tips areas.
     */
    public org.apache.ecs.html.Map getResultMap() {
        return map;
    }
}
