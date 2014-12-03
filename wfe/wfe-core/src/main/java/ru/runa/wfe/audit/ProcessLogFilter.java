package ru.runa.wfe.audit;

import java.io.Serializable;
import java.util.List;

import com.google.common.collect.Lists;

public class ProcessLogFilter implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long processId;
    private Long tokenId;
    private String nodeId;
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

    public Long getTokenId() {
        return tokenId;
    }

    public void setTokenId(Long tokenId) {
        this.tokenId = tokenId;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
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

    public void setSeverities(List<Severity> severities) {
        this.severities = severities;
    }
}
