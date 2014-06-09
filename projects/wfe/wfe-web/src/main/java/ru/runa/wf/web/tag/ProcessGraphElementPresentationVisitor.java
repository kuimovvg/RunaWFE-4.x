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

import java.util.List;

import javax.servlet.jsp.PageContext;

import org.apache.ecs.html.Area;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.TR;
import org.apache.ecs.html.Table;

import ru.runa.common.web.HTMLUtils;
import ru.runa.common.web.Messages;
import ru.runa.common.web.Resources;
import ru.runa.wf.web.action.ShowGraphModeHelper;
import ru.runa.wf.web.html.GraphElementPresentationHelper;
import ru.runa.wfe.audit.ProcessLog;
import ru.runa.wfe.commons.CalendarUtil;
import ru.runa.wfe.graph.view.GraphElementPresentation;
import ru.runa.wfe.graph.view.GraphElementPresentationVisitor;
import ru.runa.wfe.graph.view.MultiinstanceGraphElementPresentation;
import ru.runa.wfe.graph.view.SubprocessGraphElementPresentation;
import ru.runa.wfe.graph.view.TaskGraphElementPresentation;
import ru.runa.wfe.lang.NodeType;
import ru.runa.wfe.user.User;

/**
 * Operation to create links to subprocesses and tool tips to minimized elements.
 */
public class ProcessGraphElementPresentationVisitor extends GraphElementPresentationVisitor {
    /**
     * Helper to create links to subprocesses.
     */
    private final GraphElementPresentationHelper presentationHelper;
    private final User user;
    private final PageContext pageContext;
    /**
     * Helper to create tool tips for task graph elements.
     */
    private final TD td;

    /**
     * Creates operation to create links to subprocesses and tool tips to minimized elements.
     * 
     * @param taskId
     *            Current task identity.
     * @param pageContext
     *            Rendered page context.
     * @param td
     *            Root form element.
     */
    public ProcessGraphElementPresentationVisitor(User user, PageContext pageContext, TD td, String subprocessId) {
        this.user = user;
        this.pageContext = pageContext;
        this.td = td;
        presentationHelper = new GraphElementPresentationHelper(pageContext, subprocessId);
    }

    @Override
    protected void visit(GraphElementPresentation element) {
        Area area = null;
        if (element.getNodeType() == NodeType.SUBPROCESS) {
            area = presentationHelper.createSubprocessLink((SubprocessGraphElementPresentation) element,
                    ShowGraphModeHelper.getManageProcessAction(), "javascript:showEmbeddedSubprocess");
        }
        if (element.getNodeType() == NodeType.MULTI_SUBPROCESS) {
            td.addElement(presentationHelper.createMultiSubprocessLinks((MultiinstanceGraphElementPresentation) element,
                    ShowGraphModeHelper.getManageProcessAction()));
        }
        if (element.getNodeType() == NodeType.TASK_STATE) {
            area = presentationHelper.createTaskTooltip((TaskGraphElementPresentation) element);
        }
        if (element.getData() != null) {
            Table table = new Table();
            table.setClass(Resources.CLASS_LIST_TABLE);
            List<ProcessLog> logs = (List<ProcessLog>) element.getData();
            for (ProcessLog log : logs) {
                String description;
                try {
                    String format = Messages.getMessage("history.log." + log.getClass().getSimpleName(), pageContext);
                    Object[] arguments = log.getPatternArguments();
                    Object[] substitutedArguments = HTMLUtils.substituteArguments(user, pageContext, arguments);
                    description = log.toString(format, substitutedArguments);
                } catch (Exception e) {
                    description = log.toString();
                }
                TR tr = new TR();
                String eventDateString = CalendarUtil.format(log.getCreateDate(), CalendarUtil.DATE_WITH_HOUR_MINUTES_SECONDS_FORMAT);
                tr.addElement(new TD().addElement(eventDateString).setClass(Resources.CLASS_LIST_TABLE_TD));
                tr.addElement(new TD().addElement(description).setClass(Resources.CLASS_LIST_TABLE_TD));
                table.addElement(tr);
            }
            presentationHelper.addTooltip(element, area, table.toString());
        }
    }

    public GraphElementPresentationHelper getPresentationHelper() {
        return presentationHelper;
    }

}
