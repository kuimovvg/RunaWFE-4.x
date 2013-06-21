package ru.runa.wfe.audit;

import java.io.Serializable;
import java.util.List;

import com.google.common.collect.Lists;

public class ProcessLogFilter implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long processId;
    private boolean includeSubprocessLogs;
    private List<Severity> severities = Lists.newArrayList();

    public ProcessLogFilter() {
    }

    public ProcessLogFilter(Long processId) {
        this.processId = processId;
    }

    public Long getProcessId() {
        return processId;
    }

    public void setProcessId(Long processId) {
        this.processId = processId;
    }

    public boolean isIncludeSubprocessLogs() {
        return includeSubprocessLogs;
    }

    public void setIncludeSubprocessLogs(boolean includeSubprocessLogs) {
        this.includeSubprocessLogs = includeSubprocessLogs;
    }

    public List<Severity> getSeverities() {
        return severities;
    }

    public void addSeverity(Severity severity) {
        this.severities.add(severity);
    }

}
