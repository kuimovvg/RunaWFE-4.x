package ru.runa.wfe.execution;

import java.io.Serializable;
import java.util.Date;

/**
 * Filter for process search.
 * 
 * @author Dofs
 * @since 4.0
 */
public class ProcessFilter implements Serializable {
    private static final long serialVersionUID = 1L;
    private String definitionName;
    private Long definitionVersion;
    private Long idFrom;
    private Long idTill;
    private Date startDateFrom;
    private Date startDateTill;
    private Boolean finishedOnly;
    private Date endDateFrom;
    private Date endDateTill;

    public String getDefinitionName() {
        return definitionName;
    }

    public void setDefinitionName(String definitionName) {
        this.definitionName = definitionName;
    }

    public Long getDefinitionVersion() {
        return definitionVersion;
    }

    public void setDefinitionVersion(Long version) {
        this.definitionVersion = version;
    }

    public Long getIdFrom() {
        return idFrom;
    }

    public void setIdFrom(Long idFrom) {
        this.idFrom = idFrom;
    }

    public Long getIdTill() {
        return idTill;
    }

    public void setIdTill(Long idTill) {
        this.idTill = idTill;
    }

    public Date getStartDateFrom() {
        return startDateFrom;
    }

    public void setStartDateFrom(Date startDateFrom) {
        this.startDateFrom = startDateFrom;
    }

    public Date getStartDateTill() {
        return startDateTill;
    }

    public void setStartDateTill(Date startDateTill) {
        this.startDateTill = startDateTill;
    }

    public Date getEndDateFrom() {
        return endDateFrom;
    }

    public void setEndDateFrom(Date endDateFrom) {
        this.endDateFrom = endDateFrom;
    }

    public Date getEndDateTill() {
        return endDateTill;
    }

    public void setEndDateTill(Date endDateTill) {
        this.endDateTill = endDateTill;
    }

    public Boolean getFinishedOnly() {
        return finishedOnly;
    }

    public void setFinishedOnly(Boolean finishedOnly) {
        this.finishedOnly = finishedOnly;
    }
}
