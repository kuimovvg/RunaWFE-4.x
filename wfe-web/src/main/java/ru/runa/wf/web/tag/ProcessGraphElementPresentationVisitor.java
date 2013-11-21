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
import java.util.Map;

import javax.servlet.jsp.PageContext;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.ecs.html.A;
import org.apache.ecs.html.Area;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.TR;
import org.apache.ecs.html.Table;

import ru.runa.common.web.Commons;
import ru.runa.common.web.HTMLUtils;
import ru.runa.common.web.Messages;
import ru.runa.common.web.Resources;
import ru.runa.common.web.StrutsWebHelper;
import ru.runa.common.web.form.IdForm;
import ru.runa.wf.web.action.ShowGraphModeHelper;
import ru.runa.wf.web.ftl.method.ViewUtil;
import ru.runa.wf.web.html.GraphElementPresentationHelper;
import ru.runa.wfe.audit.ProcessLog;
import ru.runa.wfe.audit.presentation.ExecutorIdsValue;
import ru.runa.wfe.audit.presentation.ExecutorNameValue;
import ru.runa.wfe.audit.presentation.FileValue;
import ru.runa.wfe.audit.presentation.ProcessIdValue;
import ru.runa.wfe.commons.CalendarUtil;
import ru.runa.wfe.commons.web.PortletUrlType;
import ru.runa.wfe.graph.view.GraphElementPresentation;
import ru.runa.wfe.graph.view.GraphElementPresentationVisitor;
import ru.runa.wfe.graph.view.MultiinstanceGraphElementPresentation;
import ru.runa.wfe.graph.view.SubprocessGraphElementPresentation;
import ru.runa.wfe.graph.view.TaskGraphElementPresentation;
import ru.runa.wfe.lang.NodeType;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.User;

import com.google.common.collect.Maps;

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
            area = presentationHelper.createSubprocessLink((SubprocessGraphElementPresentation) element, ShowGraphModeHelper.getManageProcessAction());
        }
        if (element.getNodeType() == NodeType.MULTI_SUBPROCESS) {
            td.addElement(presentationHelper.createMultiSubprocessLinks((MultiinstanceGraphElementPresentation) element, ShowGraphModeHelper.getManageProcessAction()));
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
                    Object[] substitutedArguments = substituteArguments(arguments);
                    description = log.toString(format, substitutedArguments);
                } catch (Exception e) {
                    description = log.toString();
                }
                TR tr = new TR();
                String eventDateString = CalendarUtil.format(log.getDate(), CalendarUtil.DATE_WITH_HOUR_MINUTES_SECONDS_FORMAT);
                tr.addElement(new TD().addElement(eventDateString).setClass(Resources.CLASS_LIST_TABLE_TD));
                tr.addElement(new TD().addElement(description).setClass(Resources.CLASS_LIST_TABLE_TD));
                table.addElement(tr);
            }
            presentationHelper.addTooltip(element, area, table.toString());
        }
    }

    private Object[] substituteArguments(Object[] arguments) {
        Object[] result = new Object[arguments.length];
        for (int i = 0; i < result.length; i++) {
            if (arguments[i] instanceof ExecutorNameValue) {
                String name = ((ExecutorNameValue) arguments[i]).getName();
                if (name == null) {
                    result[i] = "null";
                    continue;
                }
                try {
                    Executor executor = Delegates.getExecutorService().getExecutorByName(user, name);
                    result[i] = HTMLUtils.createExecutorElement(pageContext, executor);
                } catch (Exception e) {
                    result[i] = name;
                }
            } else if (arguments[i] instanceof ExecutorIdsValue) {
                List<Long> ids = ((ExecutorIdsValue) arguments[i]).getIds();
                if (ids == null || ids.isEmpty()) {
                    result[i] = "null";
                    continue;
                }
                String executors = "{ ";
                for (Long id : ids) {
                    try {
                        Executor executor = Delegates.getExecutorService().getExecutor(user, id);
                        executors += HTMLUtils.createExecutorElement(pageContext, executor);
                        executors += "&nbsp;";
                    } catch (Exception e) {
                        executors += id + "&nbsp;";
                    }
                }
                executors += "}";
                result[i] = executors;
            } else if (arguments[i] instanceof ProcessIdValue) {
                Long processId = ((ProcessIdValue) arguments[i]).getId();
                if (processId == null) {
                    result[i] = "null";
                    continue;
                }
                Map<String, Object> params = Maps.newHashMap();
                params.put(IdForm.ID_INPUT_NAME, processId);
                String url = Commons.getActionUrl(ShowGraphModeHelper.getManageProcessAction(), params, pageContext, PortletUrlType.Render);
                result[i] = new A(url, processId.toString()).setClass(Resources.CLASS_LINK).toString();
            } else if (arguments[i] instanceof FileValue) {
                FileValue fileValue = (FileValue) arguments[i];
                result[i] = ViewUtil.getFileLogOutput(new StrutsWebHelper(pageContext), fileValue.getLogId(), fileValue.getFileName());
            } else if (arguments[i] instanceof String) {
                result[i] = StringEscapeUtils.escapeHtml((String) arguments[i]);
            } else {
                result[i] = arguments[i];
            }
        }
        return result;
    }

    public GraphElementPresentationHelper getPresentationHelper() {
        return presentationHelper;
    }

}
