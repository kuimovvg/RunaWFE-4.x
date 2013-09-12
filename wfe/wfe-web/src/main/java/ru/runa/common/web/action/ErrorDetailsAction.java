package ru.runa.common.web.action;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.TR;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import ru.runa.common.web.Messages;
import ru.runa.common.web.Resources;
import ru.runa.common.web.form.IdNameForm;
import ru.runa.common.web.html.HeaderBuilder;
import ru.runa.common.web.html.RowBuilder;
import ru.runa.common.web.html.TRRowBuilder;
import ru.runa.common.web.html.TableBuilder;
import ru.runa.wfe.audit.ProcessLog;
import ru.runa.wfe.audit.ProcessLogFilter;
import ru.runa.wfe.audit.ProcessLogs;
import ru.runa.wfe.audit.presentation.ExecutorIdsValue;
import ru.runa.wfe.audit.presentation.ExecutorNameValue;
import ru.runa.wfe.audit.presentation.FileValue;
import ru.runa.wfe.audit.presentation.ProcessIdValue;
import ru.runa.wfe.commons.CalendarUtil;
import ru.runa.wfe.definition.IFileDataProvider;
import ru.runa.wfe.execution.dto.WfProcess;
import ru.runa.wfe.execution.logic.ProcessExecutionErrors;
import ru.runa.wfe.execution.logic.ProcessExecutionErrors.BotTaskIdentifier;
import ru.runa.wfe.execution.logic.ProcessExecutionErrors.TokenErrorDetail;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.User;

import com.google.common.base.Charsets;
import com.google.common.base.Objects;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

@SuppressWarnings("unchecked")
public class ErrorDetailsAction extends ActionBase {

    private static final String HTML = "html";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request, HttpServletResponse response) {
        JSONObject rootObject = new JSONObject();
        try {
            IdNameForm form = (IdNameForm) actionForm;
            String action = form.getAction();
            if ("getBotTaskConfigurationError".equals(action)) {
                for (Map.Entry<BotTaskIdentifier, Throwable> entry : ProcessExecutionErrors.getBotTaskConfigurationErrors().entrySet()) {
                    if (Objects.equal(entry.getKey().getBot().getId(), form.getId())
                            && Objects.equal(entry.getKey().getBotTaskName(), form.getName())) {
                        String html = "<form id='supportForm'>";
                        html += "<input type='hidden' name='botId' value='" + form.getId() + "' />";
                        html += "<input type='hidden' name='botTaskName' value='" + form.getName() + "' />";
                        html += "</form>";
                        html += Throwables.getStackTraceAsString(entry.getValue());
                        rootObject.put(HTML, html);
                        break;
                    }
                }
            } else if ("getProcessError".equals(action)) {
                List<TokenErrorDetail> errorDetails = ProcessExecutionErrors.getProcessErrors(form.getId());
                if (errorDetails != null) {
                    for (TokenErrorDetail detail : errorDetails) {
                        if (Objects.equal(detail.getNodeId(), form.getName())) {
                            String html = "<form id='supportForm'>";
                            html += "<input type='hidden' name='processId' value='" + form.getId() + "' />";
                            html += "</form>";
                            html += Throwables.getStackTraceAsString(detail.getThrowable());
                            rootObject.put(HTML, html);
                        }
                    }
                }
            } else if ("showSupportFiles".equals(action)) {
                boolean fileIncluded = false;
                User user = getLoggedUser(request);
                JSONArray processesErrorInfo = new JSONArray();
                // TODO privileges are required for successful operation!
                // TODO zip file name encoding
                // TODO disable Support button after click
                // TODO bots
                Map<Long, List<Long>> processHierarchies = Maps.newHashMap();
                if (request.getParameter("processId") != null) {
                    initProcessHierarchy(user, processHierarchies, Long.parseLong(request.getParameter("processId")));
                } else {
                    for (Long processId : ProcessExecutionErrors.getProcessErrors().keySet()) {
                        initProcessHierarchy(user, processHierarchies, processId);
                    }
                }
                Map<String, byte[]> supportFiles = Maps.newHashMap();
                for (Entry<Long, List<Long>> processesEntry : processHierarchies.entrySet()) {
                    JSONObject errorInfo = new JSONObject();
                    errorInfo.put("id", processesEntry.getKey());
                    for (Long processId : processesEntry.getValue()) {
                        List<WfProcess> processes = Delegates.getExecutionService().getSubprocesses(user, processId, true);
                        processes.add(0, Delegates.getExecutionService().getProcess(user, processId));
                        Map<String, byte[]> errorFiles = Maps.newHashMap();
                        JSONArray includedFileNames = new JSONArray();
                        String exceptions = "";
                        List<TokenErrorDetail> errorDetails = ProcessExecutionErrors.getProcessErrors().get(processId);
                        for (TokenErrorDetail detail : errorDetails) {
                            exceptions += "\r\n---------------------------------------------------------------";
                            exceptions += "\r\n" + CalendarUtil.formatDateTime(detail.getOccuredDate()) + " " + detail.getNodeId() + "/"
                                    + detail.getTaskName();
                            if (detail.getBotTask() != null) {
                                String botTaskIdentifier = detail.getBotTask().getId() + "." + detail.getBotTask().getName();
                                exceptions += "\nbot task = " + detail.getBotTask().getTaskHandlerClassName() + "/" + botTaskIdentifier;
                                if (!errorFiles.containsKey(botTaskIdentifier)) {
                                    errorFiles.put(botTaskIdentifier, detail.getBotTask().getConfiguration());
                                    addSupportFileInfo(includedFileNames, MessageFormat.format(
                                            getResources(request).getMessage("support.file.bottask.configuration"), botTaskIdentifier), true);
                                }
                            }
                            exceptions += "\r\n" + Throwables.getStackTraceAsString(detail.getThrowable());
                        }
                        errorFiles.put("exceptions." + processId + ".txt", exceptions.getBytes(Charsets.UTF_8));
                        addSupportFileInfo(includedFileNames, getResources(request).getMessage("support.file.exceptions"), true);
                        for (WfProcess process : processes) {
                            String processDefinitionFileName = process.getName() + ".par";
                            if (!errorFiles.containsKey(processDefinitionFileName)) {
                                try {
                                    errorFiles.put(
                                            processDefinitionFileName,
                                            Delegates.getDefinitionService().getProcessDefinitionFile(user, process.getDefinitionId(),
                                                    IFileDataProvider.PAR_FILE));
                                    fileIncluded = true;
                                } catch (Exception e) {
                                    fileIncluded = false;
                                    log.warn("definition for " + process, e);
                                }
                                addSupportFileInfo(includedFileNames, MessageFormat.format(
                                        getResources(request).getMessage("support.file.process.definition"), processDefinitionFileName), fileIncluded);
                            }
                            try {
                                errorFiles.put(process.getId() + ".graph.png",
                                        Delegates.getExecutionService().getProcessDiagram(user, process.getId(), null, null));
                                fileIncluded = true;
                            } catch (Exception e) {
                                fileIncluded = false;
                                log.warn("process graph for " + process, e);
                            }
                            addSupportFileInfo(includedFileNames,
                                    MessageFormat.format(getResources(request).getMessage("support.file.process.graph"), process.getId()),
                                    fileIncluded);
                            try {
                                byte[] logs = getProcessLogs(request, user, process.getId()).getBytes(Charsets.UTF_8);
                                errorFiles.put(process.getId() + ".log.html", logs);
                                fileIncluded = true;
                            } catch (Exception e) {
                                fileIncluded = false;
                                log.warn("process logs for " + process, e);
                            }
                            addSupportFileInfo(includedFileNames,
                                    MessageFormat.format(getResources(request).getMessage("support.file.process.logs"), process.getId()),
                                    fileIncluded);
                        }
                        supportFiles.put("support." + processId + ".zip", createZip(errorFiles));
                        errorInfo.put("includedFileNames", includedFileNames);
                        processesErrorInfo.add(errorInfo);
                    }
                }
                rootObject.put("processesErrorInfo", processesErrorInfo);
                String supportFileName = null;
                byte[] supportFile = null;
                if (supportFiles.size() == 1) {
                    supportFileName = supportFiles.keySet().iterator().next();
                    supportFile = supportFiles.values().iterator().next();
                } else if (supportFiles.size() > 1) {
                    supportFileName = "support.files.zip";
                    supportFile = createZip(supportFiles);
                }
                if (supportFileName != null && supportFile != null) {
                    request.getSession().setAttribute(supportFileName, supportFile);
                    rootObject.put("downloadUrl", "/wfe/getSessionFile.do?fileName=" + supportFileName);
                    rootObject.put("downloadTitle", getResources(request).getMessage("support.files.download"));
                }
            } else {
                rootObject.put(HTML, "Unknown action: " + action);
                log.error("Unknown action: " + action);
            }
        } catch (Exception e) {
            log.error("", e);
            rootObject.put(HTML, String.format(getResources(request).getMessage("unknown.exception"), e));
        }
        try {
            OutputStream os = response.getOutputStream();
            os.write(rootObject.toJSONString().getBytes(Charsets.UTF_8));
            os.flush();
        } catch (Exception e) {
            log.error("Unable to write ajax output", e);
        }
        return null;
    }

    private void initProcessHierarchy(User user, Map<Long, List<Long>> processHierarchies, Long processId) {
        try {
            WfProcess process = Delegates.getExecutionService().getProcess(user, processId);
            while (process != null) {
                processId = process.getId();
                process = Delegates.getExecutionService().getParentProcess(user, processId);
            }
            List<Long> processIdsWithErrors = processHierarchies.get(processId);
            if (processIdsWithErrors == null) {
                processIdsWithErrors = Lists.newArrayList();
                processHierarchies.put(processId, processIdsWithErrors);
            }
            processIdsWithErrors.add(processId);
        } catch (Exception e) {
            log.warn("for process " + processId, e);
        }
    }

    private void addSupportFileInfo(JSONArray array, String fileInfo, boolean fileIncluded) {
        JSONObject object = new JSONObject();
        object.put("info", fileInfo);
        object.put("included", fileIncluded);
        array.add(object);
    }

    private byte[] createZip(Map<String, byte[]> files) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipOutputStream zipStream = new ZipOutputStream(baos);
        for (Map.Entry<String, byte[]> entry : files.entrySet()) {
            if (entry.getValue() == null || entry.getValue().length == 0) {
                continue;
            }
            zipStream.putNextEntry(new ZipEntry(entry.getKey()));
            zipStream.write(entry.getValue());
        }
        zipStream.close();
        baos.flush();
        return baos.toByteArray();
    }

    private String getProcessLogs(HttpServletRequest request, User user, Long processId) {
        ProcessLogFilter filter = new ProcessLogFilter(processId);
        ProcessLogs logs = Delegates.getExecutionService().getProcessLogs(user, filter);
        int maxLevel = logs.getMaxSubprocessLevel();
        List<TR> rows = Lists.newArrayList();
        TD mergedEventDateTD = null;
        String mergedEventDateString = null;
        int mergedRowsCount = 0;
        for (ProcessLog log : logs.getLogs()) {
            String description;
            try {
                Object[] arguments = log.getPatternArguments();
                String format = getResources(request).getMessage("history.log." + log.getClass().getSimpleName());
                Object[] substitutedArguments = substituteArguments(user, arguments);
                description = log.toString(format, substitutedArguments);
            } catch (Exception e) {
                description = log.toString();
            }
            TR tr = new TR();
            List<Long> processIds = logs.getSubprocessIds(log);
            for (Long subprocessId : processIds) {
                tr.addElement(new TD().addElement(subprocessId.toString()).setClass(Resources.CLASS_EMPTY20_TABLE_TD));
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
        HeaderBuilder tasksHistoryHeaderBuilder = new ru.runa.wf.web.html.HistoryHeaderBuilder(maxLevel, getResources(request).getMessage(
                Messages.LABEL_HISTORY_DATE), getResources(request).getMessage(Messages.LABEL_HISTORY_EVENT));
        RowBuilder rowBuilder = new TRRowBuilder(rows);
        TableBuilder tableBuilder = new TableBuilder();
        return tableBuilder.build(tasksHistoryHeaderBuilder, rowBuilder).toString();
    }

    private Object[] substituteArguments(User user, Object[] arguments) {
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
                    result[i] = executor.toString();
                } catch (Exception e) {
                    log.debug("could not get executor '" + name + "': " + e.getMessage());
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
                        executors += executor.toString();
                        executors += "&nbsp;";
                    } catch (Exception e) {
                        log.debug("could not get executor by " + id + ": " + e.getMessage());
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
                result[i] = processId;
            } else if (arguments[i] instanceof FileValue) {
                FileValue fileValue = (FileValue) arguments[i];
                result[i] = fileValue.getFileName() + " (ID=" + fileValue.getLogId() + ")";
            } else if (arguments[i] instanceof String) {
                result[i] = StringEscapeUtils.escapeHtml((String) arguments[i]);
            } else {
                result[i] = arguments[i];
            }
        }
        return result;
    }

}
