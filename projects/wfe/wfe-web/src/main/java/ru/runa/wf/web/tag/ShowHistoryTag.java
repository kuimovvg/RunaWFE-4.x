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

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.ecs.html.A;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.TH;
import org.apache.ecs.html.TR;

import ru.runa.af.web.ExecutorNameConverter;
import ru.runa.common.WebResources;
import ru.runa.common.web.Commons;
import ru.runa.common.web.Messages;
import ru.runa.common.web.Resources;
import ru.runa.common.web.StrutsWebHelper;
import ru.runa.common.web.form.IdForm;
import ru.runa.common.web.html.HeaderBuilder;
import ru.runa.common.web.html.RowBuilder;
import ru.runa.common.web.html.TRRowBuilder;
import ru.runa.common.web.html.TableBuilder;
import ru.runa.wf.web.action.CancelProcessAction;
import ru.runa.wf.web.action.ShowGraphModeHelper;
import ru.runa.wfe.audit.ProcessLog;
import ru.runa.wfe.audit.ProcessLogFilter;
import ru.runa.wfe.audit.ProcessLogs;
import ru.runa.wfe.audit.Severity;
import ru.runa.wfe.audit.presentation.ExecutorIdsValue;
import ru.runa.wfe.audit.presentation.ExecutorNameValue;
import ru.runa.wfe.audit.presentation.FileValue;
import ru.runa.wfe.audit.presentation.ProcessIdValue;
import ru.runa.wfe.commons.CalendarUtil;
import ru.runa.wfe.commons.web.PortletUrlType;
import ru.runa.wfe.execution.ProcessPermission;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.var.format.FileFormat;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * @jsp.tag name = "showHistory" body-content = "JSP"
 */
public class ShowHistoryTag extends ProcessBaseFormTag {
    private static final long serialVersionUID = 1L;

    @Override
    protected void fillFormData(TD tdFormElement) {
        String withSubprocesses = Objects.firstNonNull(pageContext.getRequest().getParameter("withSubprocesses"), "false");
        String[] severityNames = pageContext.getRequest().getParameterValues("severities");
        ProcessLogFilter filter = new ProcessLogFilter(getIdentifiableId());
        filter.setIncludeSubprocessLogs(Boolean.valueOf(withSubprocesses));
        if (severityNames != null) {
            for (String severityName : severityNames) {
                filter.addSeverity(Severity.valueOf(severityName));
            }
        }
        // filter
        String filterHtml = "\n";
        filterHtml += "<form action=\"" + Commons.getActionUrl("/show_history", pageContext, PortletUrlType.Action) + "\" method=\"get\">\n";
        filterHtml += "<input type=\"hidden\" name=\"id\" value=\"" + filter.getProcessId() + "\">\n";
        filterHtml += "<table class=\"box\"><tr><th class=\"box\">" + Commons.getMessage("label.filter_criteria", pageContext) + "</th></tr>\n";
        filterHtml += "<tr><td>\n";
        filterHtml += "<input type=\"checkbox\" name=\"withSubprocesses\" value=\"true\"";
        if (filter.isIncludeSubprocessLogs()) {
            filterHtml += " checked=\"true\"";
        }
        filterHtml += ">" + Commons.getMessage("title.process_subprocess_list", pageContext) + "\n";
        for (Severity severity : Severity.values()) {
            filterHtml += "<input type=\"checkbox\" name=\"severities\" value=\"" + severity.name() + "\"";
            if (filter.getSeverities().contains(severity)) {
                filterHtml += " checked=\"true\"";
            }
            filterHtml += "> " + severity.name() + "\n";
        }
        filterHtml += "<button type=\"submit\">" + Commons.getMessage("button.form", pageContext) + "</button>\n";
        filterHtml += "</td></tr></table>\n";
        tdFormElement.addElement(filterHtml);
        // content
        ProcessLogs logs = Delegates.getExecutionService().getProcessLogs(getUser(), filter);
        int maxLevel = logs.getMaxSubprocessLevel();
        List<TR> rows = Lists.newArrayList();
        TD mergedEventDateTD = null;
        String mergedEventDateString = null;
        int mergedRowsCount = 0;
        for (ProcessLog log : logs.getLogs()) {
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
            List<Long> processIds = logs.getSubprocessIds(log);
            for (Long processId : processIds) {
                Map<String, Object> params = Maps.newHashMap();
                params.put(IdForm.ID_INPUT_NAME, processId);
                String url = Commons.getActionUrl(ShowGraphModeHelper.getManageProcessAction(), params, pageContext, PortletUrlType.Render);
                tr.addElement(new TD().addElement(new A(url, processId.toString())).setClass(Resources.CLASS_EMPTY20_TABLE_TD));
            }
            for (int i = processIds.size(); i < maxLevel; i++) {
                tr.addElement(new TD().addElement("").setClass(Resources.CLASS_EMPTY20_TABLE_TD));
            }
            String eventDateString = CalendarUtil.format(log.getDate(), CalendarUtil.DATE_WITH_HOUR_MINUTES_SECONDS_FORMAT);
            if (!Objects.equal(mergedEventDateString, eventDateString)) {
                if (mergedEventDateTD != null) {
                    mergedEventDateTD.setRowSpan(mergedRowsCount + 1);
                }
                mergedRowsCount = 0;
                mergedEventDateTD = (TD) new TD().addElement(eventDateString).setClass(Resources.CLASS_LIST_TABLE_TD);
                mergedEventDateString = eventDateString;
                tr.addElement(mergedEventDateTD);
            } else {
                mergedRowsCount++;
            }
            tr.addElement(new TD().addElement(description).setClass(Resources.CLASS_LIST_TABLE_TD));
            rows.add(tr);
        }
        if (mergedEventDateTD != null) {
            mergedEventDateTD.setRowSpan(mergedRowsCount + 1);
        }
        HeaderBuilder tasksHistoryHeaderBuilder = new HistoryHeaderBuilder(maxLevel);
        RowBuilder rowBuilder = new TRRowBuilder(rows);
        TableBuilder tableBuilder = new TableBuilder();
        tdFormElement.addElement(tableBuilder.build(tasksHistoryHeaderBuilder, rowBuilder));
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
                    Executor executor = Delegates.getExecutorService().getExecutorByName(getUser(), name);
                    result[i] = createExecutorLink(executor);
                } catch (Exception e) {
                    log.error("could not get executor '" + name + "': " + e.getMessage());
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
                        Executor executor = Delegates.getExecutorService().getExecutor(getUser(), id);
                        executors += createExecutorLink(executor);
                        executors += "&nbsp;";
                    } catch (Exception e) {
                        log.error("could not get executor by " + id + ": " + e.getMessage());
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
                result[i] = FileFormat.getHtml(fileValue.getFileName(), new StrutsWebHelper(pageContext), fileValue.getLogId());
            } else if (arguments[i] instanceof String) {
                result[i] = StringEscapeUtils.escapeHtml((String) arguments[i]);
            } else {
                result[i] = arguments[i];
            }
        }
        return result;
    }

    private String createExecutorLink(Executor executor) {
        Map<String, Object> params = Maps.newHashMap();
        params.put(IdForm.ID_INPUT_NAME, executor.getId());
        String url = Commons.getActionUrl(WebResources.ACTION_MAPPING_UPDATE_EXECUTOR, params, pageContext, PortletUrlType.Render);
        return new A(url, ExecutorNameConverter.getName(executor, pageContext)).setClass(Resources.CLASS_LINK).toString();
    }

    @Override
    protected Permission getPermission() {
        return ProcessPermission.READ;
    }

    @Override
    protected String getTitle() {
        return Messages.getMessage(Messages.TITLE_HISTORY, pageContext);
    }

    @Override
    public String getAction() {
        return CancelProcessAction.ACTION_PATH;
    }

    @Override
    protected boolean isFormButtonVisible() {
        return false;
    }

    private class HistoryHeaderBuilder implements HeaderBuilder {
        private final int subprocessLevel;

        public HistoryHeaderBuilder(int subprocessLevel) {
            this.subprocessLevel = subprocessLevel;
        }

        @Override
        public TR build() {
            TR tr = new TR();
            for (int i = 0; i < subprocessLevel; i++) {
                tr.addElement(new TH("").setClass(Resources.CLASS_EMPTY20_TABLE_TD));
            }
            tr.addElement(new TH("Date").setClass(Resources.CLASS_LIST_TABLE_TH));
            tr.addElement(new TH("Event").setClass(Resources.CLASS_LIST_TABLE_TH));
            // tr.addElement(new
            // TH(Messages.getMessage(Messages.LABEL_TASK_HISTORY_TABLE_TASK_NAME,
            // pageContext))
            // .setClass(Resources.CLASS_LIST_TABLE_TH));
            // tr.addElement(new
            // TH(Messages.getMessage(Messages.LABEL_TASK_HISTORY_TABLE_EXECUTOR,
            // pageContext))
            // .setClass(Resources.CLASS_LIST_TABLE_TH));
            // tr.addElement(new
            // TH(Messages.getMessage(Messages.LABEL_TASK_HISTORY_TABLE_START_DATE,
            // pageContext))
            // .setClass(Resources.CLASS_LIST_TABLE_TH));
            // tr.addElement(new
            // TH(Messages.getMessage(Messages.LABEL_TASK_HISTORY_TABLE_END_DATE,
            // pageContext))
            // .setClass(Resources.CLASS_LIST_TABLE_TH));
            // tr.addElement(new
            // TH(Messages.getMessage(Messages.LABEL_TASK_HISTORY_TABLE_DURATION,
            // pageContext))
            // .setClass(Resources.CLASS_LIST_TABLE_TH));
            return tr;
        }
    }

}
