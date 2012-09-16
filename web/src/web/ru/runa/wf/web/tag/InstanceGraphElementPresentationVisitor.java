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

import org.apache.ecs.html.TD;

import ru.runa.wf.graph.MultiinstanceGraphElementPresentation;
import ru.runa.wf.graph.SubprocessGraphElementPresentation;
import ru.runa.wf.graph.SubprocessesGraphElementAdapter;
import ru.runa.wf.graph.TaskGraphElementPresentation;
import ru.runa.wf.web.action.ShowGraphModeHelper;

/**
 * Operation to create links to subprocesses and tool tips to minimized elements.
 */
public class InstanceGraphElementPresentationVisitor extends SubprocessesGraphElementAdapter {

    /**
     * Created map of elements, represents links and tool tips areas. 
     */
    private final org.apache.ecs.html.Map map = new org.apache.ecs.html.Map();

    /**
     * Helper to create links to subprocesses.
     */
    private final SubprocessGraphElementPresentationHelper helperSubprocess;

    /**
     * Helper to create tool tips for task graph elements.
     */
    private final TaskGraphElementHelper helperTasks;

    /**
     * Creates operation to create links to subprocesses and tool tips to minimized elements.
     * @param taskId Current task identity.
     * @param pageContext Rendered page context.
     * @param formDataTD Root form element.
     */
    public InstanceGraphElementPresentationVisitor(Long taskId, PageContext pageContext, TD formDataTD) {
        super();
        map.setName("processInstanceMap");
        helperSubprocess = new SubprocessGraphElementPresentationHelper(taskId, pageContext, formDataTD, map, ShowGraphModeHelper
                .getManageProcessInstanceAction());
        helperTasks = new TaskGraphElementHelper(map);
    }

    @Override
    public void onMultiinstance(MultiinstanceGraphElementPresentation element) {
        helperSubprocess.createMultiinstanceLinks(element);
    }

    @Override
    public void onSubprocess(SubprocessGraphElementPresentation element) {
        helperSubprocess.createSubprocessLink(element);
    }

    @Override
    public void onTaskState(TaskGraphElementPresentation element) {
        helperTasks.createTaskTooltip(element);
    }

    /**
     * Operation result.
     * @return Map of elements, represents links and tool tips areas. 
     */
    public org.apache.ecs.html.Map getResultMap() {
        return map;
    }
}
