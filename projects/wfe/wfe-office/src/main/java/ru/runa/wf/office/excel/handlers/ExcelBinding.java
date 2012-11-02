package ru.runa.wf.office.excel.handlers;

import ru.runa.wf.office.excel.IExcelConstraints;

public class ExcelBinding {
    private IExcelConstraints constraints;
    private String variableName;

    public IExcelConstraints getConstraints() {
        return constraints;
    }

    public void setConstraints(IExcelConstraints constraints) {
        this.constraints = constraints;
    }

    public String getVariableName() {
        return variableName;
    }

    public void setVariableName(String variableName) {
        this.variableName = variableName;
    }

}
