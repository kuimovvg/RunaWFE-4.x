package ru.runa.wfe.audit;

import java.io.Serializable;

public class ProcessLogFilter implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long processId;
    private boolean includeSubprocessLogs;
    private Severity[] severities;

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

    public Severity[] getSeverities() {
        return severities;
    }

    public void setSeverities(Severity[] severities) {
        this.severities = severities;
    }

}
